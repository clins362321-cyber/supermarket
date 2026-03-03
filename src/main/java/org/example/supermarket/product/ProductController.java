package org.example.supermarket.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.validation.Valid;
import org.example.supermarket.product.dto.ProductCommentCreateDto;
import org.example.supermarket.product.dto.ProductDetailDto;
import org.example.supermarket.product.dto.ProductSaveDto;
import org.example.supermarket.order.Order;
import org.example.supermarket.order.OrderItem;
import org.example.supermarket.order.mapper.OrderItemMapper;
import org.example.supermarket.order.mapper.OrderMapper;
import org.example.supermarket.product.mapper.ProductCommentMapper;
import org.example.supermarket.product.mapper.ProductMapper;
import org.example.supermarket.product.mapper.ProductMediaMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

    private final ProductMapper productMapper;
    private final ProductMediaMapper mediaMapper;
    private final ProductCommentMapper commentMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Value("${file.upload-dir:E:/supermarket-uploads}")
    private String uploadDir;

    @Value("${file.access-prefix:/files}")
    private String accessPrefix;

    public ProductController(ProductMapper productMapper,
                             ProductMediaMapper mediaMapper,
                             ProductCommentMapper commentMapper,
                             OrderMapper orderMapper,
                             OrderItemMapper orderItemMapper) {
        this.productMapper = productMapper;
        this.mediaMapper = mediaMapper;
        this.commentMapper = commentMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    /**
     * 简单商品列表（用于列表/商城）
     */
    @GetMapping
    public List<Product> list() {
        return productMapper.selectList(null);
    }

    /**
     * 商品详情：基本信息 + 媒体 + 评论
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailDto> detail(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        List<ProductMedia> medias = mediaMapper.selectList(
                new QueryWrapper<ProductMedia>().eq("product_id", id).orderByAsc("sort_order", "id")
        );
        List<ProductComment> comments = commentMapper.selectList(
                new QueryWrapper<ProductComment>().eq("product_id", id).orderByDesc("created_at", "id")
        );
        return ResponseEntity.ok(new ProductDetailDto(product, medias, comments));
    }

    /**
     * 新增商品（包含图片/视频 URL）
     */
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody ProductSaveDto dto) {
        Product p = new Product();
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setStock(dto.getStock());
        productMapper.insert(p);

        saveMedias(p.getId(), dto);
        return ResponseEntity.ok().build();
    }

    /**
     * 修改商品（包含图片/视频 URL）
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody ProductSaveDto dto) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setStock(dto.getStock());
        productMapper.updateById(existing);

        // 先删旧媒体，再保存新媒体
        mediaMapper.delete(new QueryWrapper<ProductMedia>().eq("product_id", id));
        saveMedias(id, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除商品及其媒体、评论
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        mediaMapper.delete(new QueryWrapper<ProductMedia>().eq("product_id", id));
        commentMapper.delete(new QueryWrapper<ProductComment>().eq("product_id", id));
        productMapper.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 用户评价商品
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> comment(@PathVariable Long id,
                                     @Valid @RequestBody ProductCommentCreateDto dto) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body("请先登录后再评价");
        }
        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5
                || dto.getContent() == null || dto.getContent().isEmpty()) {
            return ResponseEntity.badRequest().body("评分或评价内容不合法");
        }

        // 必须下单后才能评价：检查该用户是否至少有一笔包含此商品的订单
        String username = dto.getUsername().trim();
        List<Order> orders = orderMapper.selectList(
                new QueryWrapper<Order>().eq("username", username)
        );
        if (orders.isEmpty()) {
            return ResponseEntity.badRequest().body("下单后才能评价该商品");
        }
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        if (orderIds.isEmpty()) {
            return ResponseEntity.badRequest().body("下单后才能评价该商品");
        }
        Long count = orderItemMapper.selectCount(
                new QueryWrapper<OrderItem>()
                        .in("order_id", orderIds)
                        .eq("product_id", id)
        );
        if (count == null || count == 0) {
            return ResponseEntity.badRequest().body("下单后才能评价该商品");
        }

        ProductComment c = new ProductComment();
        c.setProductId(id);
        c.setUsername(username);
        c.setRating(dto.getRating());
        c.setContent(dto.getContent());
        c.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(c);
        return ResponseEntity.ok().build();
    }

    /**
     * 上传商品图片/视频到本地文件系统，并直接写入数据库
     */
    @PostMapping("/{id}/upload")
    public ResponseEntity<List<String>> upload(@PathVariable Long id,
                                               @RequestParam("files") List<MultipartFile> files) throws IOException {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        Files.createDirectories(Paths.get(uploadDir));
        List<String> urls = new ArrayList<>();
        int order = 0;

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.')) : "";
            String filename = java.util.UUID.randomUUID() + ext;
            Path target = Paths.get(uploadDir, filename);
            file.transferTo(target.toFile());

            String mediaType = (ext.equalsIgnoreCase(".mp4") || ext.equalsIgnoreCase(".mov")) ? "VIDEO" : "IMAGE";

            ProductMedia m = new ProductMedia();
            m.setProductId(id);
            m.setMediaType(mediaType);
            m.setUrl(accessPrefix + "/" + filename);
            m.setSortOrder(order++);
            mediaMapper.insert(m);

            urls.add(m.getUrl());
        }

        return ResponseEntity.ok(urls);
    }

    /**
     * 通用文件上传，仅保存到本地并返回可访问 URL，不直接写入数据库。
     * 前端可以拿到 URL 后，通过原有的 images/videos 字段在保存商品时统一落库。
     */
    @PostMapping("/upload-files")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body("缺少文件参数 files");
            }

            Files.createDirectories(Paths.get(uploadDir));
            List<String> urls = new ArrayList<>();

            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;
                String original = file.getOriginalFilename();
                String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.')) : "";
                String filename = java.util.UUID.randomUUID() + ext;
                Path target = Paths.get(uploadDir, filename);
                file.transferTo(target.toFile());

                String url = accessPrefix + "/" + filename;
                urls.add(url);
            }

            return ResponseEntity.ok(urls);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("文件保存失败：" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("上传异常：" + e.getMessage());
        }
    }

    private void saveMedias(Long productId, ProductSaveDto dto) {
        List<ProductMedia> batch = new ArrayList<>();
        if (dto.getImages() != null) {
            int order = 0;
            for (String url : dto.getImages()) {
                if (url == null || url.isBlank()) continue;
                ProductMedia m = new ProductMedia();
                m.setProductId(productId);
                m.setMediaType("IMAGE");
                m.setUrl(url.trim());
                m.setSortOrder(order++);
                batch.add(m);
            }
        }
        if (dto.getVideos() != null) {
            int order = 0;
            for (String url : dto.getVideos()) {
                if (url == null || url.isBlank()) continue;
                ProductMedia m = new ProductMedia();
                m.setProductId(productId);
                m.setMediaType("VIDEO");
                m.setUrl(url.trim());
                m.setSortOrder(order++);
                batch.add(m);
            }
        }
        for (ProductMedia m : batch) {
            mediaMapper.insert(m);
        }
    }
}

