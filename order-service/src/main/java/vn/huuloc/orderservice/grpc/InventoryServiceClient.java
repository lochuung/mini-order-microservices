package vn.huuloc.orderservice.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.huuloc.grpc.InventoryServiceGrpc;
import vn.huuloc.grpc.InventoryServiceProto;

@Component
@Slf4j
public class InventoryServiceClient {
    @Value("${inventory.grpc.host:localhost}")
    private String inventoryHost;

    @Value("${inventory.grpc.port:9090}")
    private int inventoryPort;


    private ManagedChannel channel;
    private InventoryServiceGrpc.InventoryServiceBlockingStub stub;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(inventoryHost, inventoryPort)
                .usePlaintext() // Use plaintext for simplicity; consider using TLS in production
                .build();
        stub = InventoryServiceGrpc.newBlockingStub(channel);
        log.info("InventoryServiceClient initialized with host: {}, port: {}", inventoryHost, inventoryPort);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
            log.info("InventoryServiceClient channel closed");
        }
    }

    public InventoryServiceProto.CheckInventoryResponse checkInventory(long productId, int quantity) {
        log.info("Checking inventory for product ID: {}, quantity: {}", productId, quantity);
        InventoryServiceProto.CheckInventoryRequest request = InventoryServiceProto.CheckInventoryRequest.newBuilder()
                .setProductId(productId)
                .setQuantity(quantity)
                .build();
        return stub.checkInventory(request);
    }

    public InventoryServiceProto.UpdateInventoryResponse reserveInventory(long productId, int quantity) {
        String operation = "RESERVE";
        log.info("Updating inventory for product ID: {}, quantity: {}, operation: {}", productId, quantity, operation);
        InventoryServiceProto.UpdateInventoryRequest request = InventoryServiceProto.UpdateInventoryRequest.newBuilder()
                .setProductId(productId)
                .setQuantity(quantity)
                .setOperation(operation)
                .build();
        return stub.updateInventory(request);
    }

    public InventoryServiceProto.UpdateInventoryResponse releaseInventory(long productId, int quantity) {
        String operation = "RELEASE";
        log.info("Updating inventory for product ID: {}, quantity: {}, operation: {}", productId, quantity, operation);
        InventoryServiceProto.UpdateInventoryRequest request = InventoryServiceProto.UpdateInventoryRequest.newBuilder()
                .setProductId(productId)
                .setQuantity(quantity)
                .setOperation(operation)
                .build();
        return stub.updateInventory(request);
    }
}
