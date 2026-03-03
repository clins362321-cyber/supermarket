package org.example.supermarket.product.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端保存商品时使用的 DTO，包含图片/视频 URL 列表
 */
public class ProductSaveDto {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private List<String> images;
    private List<String> videos;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getVideos() {
        return videos;
    }

    public void setVideos(List<String> videos) {
        this.videos = videos;
    }
}

