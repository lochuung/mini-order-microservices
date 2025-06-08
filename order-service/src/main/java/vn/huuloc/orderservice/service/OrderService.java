package vn.huuloc.orderservice.service;

import vn.huuloc.orderservice.dto.OrderRequest;
import vn.huuloc.orderservice.entity.Order;

public interface OrderService {
    Order getOrder(Long orderId);

    Order createOrder(OrderRequest order);
}
