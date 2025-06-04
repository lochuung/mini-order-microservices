package vn.huuloc.orderservices.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private Long productId;
    private Integer quantity;
}
