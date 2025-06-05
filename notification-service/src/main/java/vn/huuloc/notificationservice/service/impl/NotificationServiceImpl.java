package vn.huuloc.notificationservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.huuloc.commonlibrary.event.OrderCreatedEvent;
import vn.huuloc.notificationservice.service.NotificationService;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    @Override
    public void sendCustomNotification(String message) {
        log.info("📬 Custom notification: {}", message);
    }

    @KafkaListener(topics = "order.created", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("📦 Order created event received: {}", event);
        sendEmailNotification(event);
        log.info("✅ Email notification sent for Order ID: {}", event.getOrderId());
    }

    private void sendEmailNotification(OrderCreatedEvent event) {
        // Simulate email sending process
        log.info("=== EMAIL NOTIFICATION ===");
        log.info("To: customer@example.com");
        log.info("Subject: Order Confirmation - Order #{}", event.getOrderId());
        log.info("Content:");
        log.info("Dear Customer,");
        log.info("Your order has been confirmed!");
        log.info("Order Details:");
        log.info("- Order ID: {}", event.getOrderId());
        log.info("- Product ID: {}", event.getProductId());
        log.info("- Quantity: {}", event.getQuantity());
        log.info("Thank you for your order!");
        log.info("========================");
    }
}
