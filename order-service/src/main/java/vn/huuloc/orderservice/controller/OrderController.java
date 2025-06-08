package vn.huuloc.orderservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.huuloc.commonlibrary.response.ApiResponse;
import vn.huuloc.orderservice.dto.OrderRequest;
import vn.huuloc.orderservice.entity.Order;
import vn.huuloc.orderservice.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public ApiResponse<Order> getOrder(@PathVariable Long orderId) {
        return ApiResponse.success(orderService.getOrder(orderId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Order> createOrder(@RequestBody OrderRequest order) {
        return ApiResponse.success(orderService.createOrder(order));
    }
}
