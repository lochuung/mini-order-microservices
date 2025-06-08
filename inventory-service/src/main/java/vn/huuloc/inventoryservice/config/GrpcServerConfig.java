package vn.huuloc.inventoryservice.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import vn.huuloc.inventoryservice.grpc.InventoryServiceImpl;

import java.io.IOException;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class GrpcServerConfig {
    private final InventoryServiceImpl inventoryServiceImpl;
    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    private Server server;

    @EventListener(ContextRefreshedEvent.class)
    public void startGrpcServer() throws IOException {
        server = ServerBuilder.forPort(grpcPort)
                .addService(inventoryServiceImpl)
                .build().start();
        log.info("Starting gRPC server on port {}", grpcPort);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server");
            if (server != null) {
                server.shutdown();
            }
        }));
    }

}
