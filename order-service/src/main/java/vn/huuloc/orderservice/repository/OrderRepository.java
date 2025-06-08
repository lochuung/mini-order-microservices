package vn.huuloc.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.huuloc.orderservice.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
