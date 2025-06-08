package vn.huuloc.inventoryservice.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.huuloc.grpc.InventoryServiceGrpc;
import vn.huuloc.grpc.InventoryServiceProto;
import vn.huuloc.inventoryservice.entity.Inventory;
import vn.huuloc.inventoryservice.repository.InventoryRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl extends InventoryServiceGrpc.InventoryServiceImplBase {
    private final InventoryRepository inventoryRepository;

    @Override
    public void checkInventory(InventoryServiceProto.CheckInventoryRequest request, StreamObserver<InventoryServiceProto.CheckInventoryResponse> responseObserver) {
        log.info("Received check inventory request for product ID: {}", request.getProductId());
        try {
            Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                    .orElse(null);
            InventoryServiceProto.CheckInventoryResponse.Builder responseBuilder = InventoryServiceProto.CheckInventoryResponse.newBuilder();
            if (inventory == null) {
                responseBuilder.setAvailableQuantity(0)
                        .setIsAvailable(false)
                        .setMessage("Product not found in inventory");
            } else {
                int availableQuantity = inventory.getQuantity() - inventory.getReservedQuantity();
                boolean isAvailable = availableQuantity >= request.getQuantity();
                responseBuilder.setAvailableQuantity(availableQuantity)
                        .setIsAvailable(isAvailable)
                        .setMessage(isAvailable ? "Product is available" : "Insufficient stock");
            }
            InventoryServiceProto.CheckInventoryResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("Inventory check completed for product ID: {}", request.getProductId());
        } catch (Exception e) {
            log.error("Error checking inventory for product ID: {}", request.getProductId(), e);
            responseObserver.onError(
                    io.grpc.Status.INTERNAL.withDescription("Internal server error").withCause(e).asRuntimeException()
            );
        }
    }

    @Override
    public void updateInventory(InventoryServiceProto.UpdateInventoryRequest request, StreamObserver<InventoryServiceProto.UpdateInventoryResponse> responseObserver) {
        log.info("Updating inventory for product: {}, quantity: {}, operation: {}",
                request.getProductId(), request.getQuantity(), request.getOperation());

        try {
            Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                    .orElse(Inventory.builder()
                            .productId(request.getProductId())
                            .quantity(0)
                            .reservedQuantity(0)
                            .build());

            InventoryServiceProto.UpdateInventoryResponse.Builder responseBuilder = InventoryServiceProto.UpdateInventoryResponse.newBuilder();

            if ("RESERVE".equals(request.getOperation())) {
                int availableQuantity = inventory.getQuantity() - inventory.getReservedQuantity();
                if (availableQuantity >= request.getQuantity()) {
                    inventory.setReservedQuantity(inventory.getReservedQuantity() + request.getQuantity());
                    inventoryRepository.save(inventory);

                    responseBuilder
                            .setSuccess(true)
                            .setMessage("Inventory reserved successfully")
                            .setNewQuantity(inventory.getQuantity() - inventory.getReservedQuantity());
                } else {
                    responseBuilder
                            .setSuccess(false)
                            .setMessage("Insufficient inventory to reserve")
                            .setNewQuantity(availableQuantity);
                }
            } else if ("RELEASE".equals(request.getOperation())) {
                inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - request.getQuantity()));
                inventoryRepository.save(inventory);

                responseBuilder
                        .setSuccess(true)
                        .setMessage("Inventory released successfully")
                        .setNewQuantity(inventory.getQuantity() - inventory.getReservedQuantity());
            }

            InventoryServiceProto.UpdateInventoryResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("Inventory update completed: {}", response);

        } catch (Exception e) {
            log.error("Error updating inventory", e);
            responseObserver.onError(e);
        }
    }
}
