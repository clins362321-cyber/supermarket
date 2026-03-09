package org.example.supermarket.controller;

import jakarta.validation.Valid;
import org.example.supermarket.dto.ProductCommentCreateDto;
import org.example.supermarket.dto.ProductDetailDto;
import org.example.supermarket.dto.ProductSaveDto;
import org.example.supermarket.entity.Product;
import org.example.supermarket.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> list() {
        return productService.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailDto> detail(@PathVariable Long id) {
        ProductDetailDto dto = productService.getDetail(id);
        return dto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody ProductSaveDto dto) {
        productService.create(dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody ProductSaveDto dto) {
        if (!productService.update(id, dto)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!productService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> comment(@PathVariable Long id,
                                     @Valid @RequestBody ProductCommentCreateDto dto) {
        try {
            productService.addComment(id, dto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/upload")
    public ResponseEntity<List<String>> upload(@PathVariable Long id,
                                               @RequestParam("files") List<MultipartFile> files) {
        try {
            List<String> urls = productService.uploadMedia(id, files);
            return ResponseEntity.ok(urls);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/upload-files")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            List<String> urls = productService.uploadFiles(files);
            return ResponseEntity.ok(urls);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("文件保存失败：" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("上传异常：" + e.getMessage());
        }
    }
}
