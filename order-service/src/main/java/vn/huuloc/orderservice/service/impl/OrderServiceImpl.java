package vn.huuloc.orderservice.service.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.huuloc.commonlibrary.event.OrderCreatedEvent;
import vn.huuloc.commonlibrary.exception.BadRequestException;
import vn.huuloc.grpc.InventoryServiceProto;
import vn.huuloc.orderservice.dto.OrderRequest;
import vn.huuloc.orderservice.entity.Order;
import vn.huuloc.orderservice.grpc.InventoryServiceClient;
import vn.huuloc.orderservice.repository.OrderRepository;
import vn.huuloc.orderservice.service.OrderService;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    private static final String TOPIC = "order.created";

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    @Autowired
    private InventoryServiceClient inventoryServiceClient;

    private final Counter orderCounter;

    public OrderServiceImpl(MeterRegistry meterRegistry) {
        this.orderCounter = Counter.builder("orders_created_total")
                .description("Total number of orders created")
                .register(meterRegistry);
    }

    @Override
    public Order getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        log.info("Retrieved order: {}", order);
        return order;
    }

    @Override
    public Order createOrder(OrderRequest order) {
        // Check inventory via gRPC
        InventoryServiceProto.CheckInventoryResponse inventoryResponse =
                inventoryServiceClient.checkInventory(order.getProductId(), order.getQuantity());

        if (!inventoryResponse.getIsAvailable()) {
            throw BadRequestException.message(
                    String.format("Insufficient stock for product ID %d. Available: %d, Requested: %d",
                            order.getProductId(), inventoryResponse.getAvailableQuantity(), order.getQuantity())
            );
        }

        // Reserve inventory
        InventoryServiceProto.UpdateInventoryResponse reserveResponse =
                inventoryServiceClient.reserveInventory(order.getProductId(), order.getQuantity());

        if (!reserveResponse.getSuccess()) {
            throw new RuntimeException("Failed to reserve inventory: " + reserveResponse.getMessage());
        }

        try {
            Order newOrder = Order.builder()
                    .productId(order.getProductId())
                    .quantity(order.getQuantity())
                    .build();

            Order savedOrder = orderRepository.save(newOrder);

            log.info("Created new order: {}", savedOrder);

            OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                    .orderId(savedOrder.getId())
                    .productId(savedOrder.getProductId())
                    .quantity(savedOrder.getQuantity())
                    .build();

            kafkaTemplate.send(TOPIC, orderCreatedEvent);
            log.debug("Sent order created event: {}", orderCreatedEvent);

            orderCounter.increment();
            return savedOrder;
        } catch (Exception e) {
            inventoryServiceClient.releaseInventory(order.getProductId(), order.getQuantity());
            log.error("Error creating order, releasing inventory for product ID {}: {}", order.getProductId(), e.getMessage());
            throw new RuntimeException("Error creating order: " + e.getMessage(), e);
        }
    }
}
