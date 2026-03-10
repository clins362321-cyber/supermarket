package org.example.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.supermarket.dao.OrderItemMapper;
import org.example.supermarket.dao.OrderMapper;
import org.example.supermarket.dao.ProductCommentMapper;
import org.example.supermarket.dao.ProductMapper;
import org.example.supermarket.dao.ProductMediaMapper;
import org.example.supermarket.dto.ProductCommentCreateDto;
import org.example.supermarket.dto.ProductDetailDto;
import org.example.supermarket.dto.ProductSaveDto;
import org.example.supermarket.entity.Order;
import org.example.supermarket.entity.OrderItem;
import org.example.supermarket.entity.Product;
import org.example.supermarket.entity.ProductComment;
import org.example.supermarket.entity.ProductMedia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductMediaMapper mediaMapper;
    private final ProductCommentMapper commentMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Value("${file.upload-dir:E:/supermarket-uploads}")
    private String uploadDir;

    @Value("${file.access-prefix:/files}")
    private String accessPrefix;

    public ProductService(ProductMapper productMapper, ProductMediaMapper mediaMapper,
                          ProductCommentMapper commentMapper, OrderMapper orderMapper,
                          OrderItemMapper orderItemMapper) {
        this.productMapper = productMapper;
        this.mediaMapper = mediaMapper;
        this.commentMapper = commentMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    public List<Product> list() {
        return productMapper.selectList(null);
    }

    public List<Long> getCommentedProductIdsByUser(String username) {
        if (username == null || username.isBlank()) return List.of();
        return commentMapper.selectList(
                new QueryWrapper<ProductComment>().eq("username", username.trim())
        ).stream().map(ProductComment::getProductId).distinct().toList();
    }

    public ProductDetailDto getDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return null;
        }
        List<ProductMedia> medias = mediaMapper.selectList(
                new QueryWrapper<ProductMedia>().eq("product_id", id).orderByAsc("sort_order", "id")
        );
        List<ProductComment> comments = commentMapper.selectList(
                new QueryWrapper<ProductComment>().eq("product_id", id).orderByDesc("created_at", "id")
        );
        return new ProductDetailDto(product, medias, comments);
    }

    public void create(ProductSaveDto dto) {
        Product p = new Product();
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setStock(dto.getStock());
        productMapper.insert(p);

        saveMedias(p.getId(), dto);
    }

    public boolean update(Long id, ProductSaveDto dto) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            return false;
        }
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setStock(dto.getStock());
        productMapper.updateById(existing);

        mediaMapper.delete(new QueryWrapper<ProductMedia>().eq("product_id", id));
        saveMedias(id, dto);
        return true;
    }

    public boolean delete(Long id) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            return false;
        }
        mediaMapper.delete(new QueryWrapper<ProductMedia>().eq("product_id", id));
        commentMapper.delete(new QueryWrapper<ProductComment>().eq("product_id", id));
        productMapper.deleteById(id);
        return true;
    }

    public void addComment(Long productId, ProductCommentCreateDto dto) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new IllegalArgumentException("请先登录后再评价");
        }
        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5
                || dto.getContent() == null || dto.getContent().isEmpty()) {
            throw new IllegalArgumentException("评分或评价内容不合法");
        }

        String username = dto.getUsername().trim();
        List<Order> orders = orderMapper.selectList(
                new QueryWrapper<Order>().eq("username", username)
        );
        if (orders.isEmpty()) {
            throw new IllegalArgumentException("下单后才能评价该商品");
        }
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        if (orderIds.isEmpty()) {
            throw new IllegalArgumentException("下单后才能评价该商品");
        }
        Long count = orderItemMapper.selectCount(
                new QueryWrapper<OrderItem>()
                        .in("order_id", orderIds)
                        .eq("product_id", productId)
        );
        if (count == null || count == 0) {
            throw new IllegalArgumentException("下单后才能评价该商品");
        }

        ProductComment c = new ProductComment();
        c.setProductId(productId);
        c.setUsername(username);
        c.setRating(dto.getRating());
        c.setContent(dto.getContent());
        c.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(c);
    }

    public List<String> uploadMedia(Long productId, List<MultipartFile> files) throws IOException {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
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
            m.setProductId(productId);
            m.setMediaType(mediaType);
            m.setUrl(accessPrefix + "/" + filename);
            m.setSortOrder(order++);
            mediaMapper.insert(m);

            urls.add(m.getUrl());
        }

        return urls;
    }

    public List<String> uploadFiles(MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("缺少文件参数 files");
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

        return urls;
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
