package vn.huuloc.inventoryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.huuloc.inventoryservice.entity.Inventory;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(Long productId);
}
