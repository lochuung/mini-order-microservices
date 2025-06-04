package vn.huuloc.orderservices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.huuloc.orderservices.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
