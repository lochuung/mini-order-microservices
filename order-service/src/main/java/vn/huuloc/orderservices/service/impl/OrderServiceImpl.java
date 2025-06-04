package vn.huuloc.orderservices.service.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.huuloc.orderservices.dto.OrderRequest;
import vn.huuloc.orderservices.entity.Order;
import vn.huuloc.orderservices.event.OrderCreatedEvent;
import vn.huuloc.orderservices.repository.OrderRepository;
import vn.huuloc.orderservices.service.OrderService;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    private static final String TOPIC = "order.created";

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

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
    }
}
