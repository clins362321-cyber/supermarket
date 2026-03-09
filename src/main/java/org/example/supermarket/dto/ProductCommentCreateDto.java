package org.example.supermarket.dto;

/**
 * 用户提交商品评价时使用的 DTO
 */
public class ProductCommentCreateDto {

    private String username;
    private Integer rating;
    private String content;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
