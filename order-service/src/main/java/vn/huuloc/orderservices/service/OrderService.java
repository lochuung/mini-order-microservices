package vn.huuloc.orderservices.service;

import vn.huuloc.orderservices.dto.OrderRequest;
import vn.huuloc.orderservices.entity.Order;

public interface OrderService {
    Order getOrder(Long orderId);

    Order createOrder(OrderRequest order);
}
