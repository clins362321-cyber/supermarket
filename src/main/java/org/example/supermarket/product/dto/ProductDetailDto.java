package org.example.supermarket.product.dto;

import org.example.supermarket.product.Product;
import org.example.supermarket.product.ProductComment;
import org.example.supermarket.product.ProductMedia;

import java.util.List;

public class ProductDetailDto {

    private Product product;
    private List<ProductMedia> medias;
    private List<ProductComment> comments;

    public ProductDetailDto() {
    }

    public ProductDetailDto(Product product, List<ProductMedia> medias, List<ProductComment> comments) {
        this.product = product;
        this.medias = medias;
        this.comments = comments;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<ProductMedia> getMedias() {
        return medias;
    }

    public void setMedias(List<ProductMedia> medias) {
        this.medias = medias;
    }

    public List<ProductComment> getComments() {
        return comments;
    }

    public void setComments(List<ProductComment> comments) {
        this.comments = comments;
    }
}

