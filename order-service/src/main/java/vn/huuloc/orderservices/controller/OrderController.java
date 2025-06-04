package vn.huuloc.orderservices.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.huuloc.orderservices.dto.OrderRequest;
import vn.huuloc.orderservices.entity.Order;
import vn.huuloc.orderservices.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(orderService.getOrder(orderId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest order) {
        try {
            return ResponseEntity.ok(orderService.createOrder(order));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
