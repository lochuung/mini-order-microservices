package vn.huuloc.orderservices.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderCreatedEvent {
    private Long orderId;
    private Long productId;
    private Integer quantity;
}
