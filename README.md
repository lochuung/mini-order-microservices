# Mini Order Microservices

A demo microservices architecture project built with Spring Boot that implements a simple order management system. This project demonstrates key microservices patterns including service discovery, API gateway, inter-service communication via gRPC, event-driven architecture with Kafka, and monitoring.

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client/UI     │    │   API Gateway   │    │ Eureka Server   │
│                 │────│    (Port 8080)  │    │   (Port 8761)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                        │
                                │                        │
        ┌───────────────────────┼────────────────────────┼───────────────────────┐
        │                       │                        │                       │
        │                       │                        │                       │
┌───────▼────────┐    ┌─────────▼────────┐    ┌─────────▼────────┐    ┌─────────▼────────┐
│ Order Service  │    │Inventory Service │    │Notification Svc  │    │ Common Library   │
│  (Port 8081)   │    │  (Port 8082)     │    │  (Port 8083)     │    │   (Shared)       │
└────────────────┘    └──────────────────┘    └──────────────────┘    └──────────────────┘
        │                       │                        │
        │       gRPC             │         Kafka          │
        │◄──────────────────────►│◄──────────────────────►│
        │                       │                        │
┌───────▼────────┐    ┌─────────▼────────┐              │
│   MySQL DB     │    │     H2 DB        │              │
│  (Port 3306)   │    │   (In-Memory)    │              │
└────────────────┘    └──────────────────┘              │
                                                         │
                    ┌────────────────────────────────────▼────────────┐
                    │              Apache Kafka                       │
                    │              (Port 9092)                        │
                    └─────────────────────────────────────────────────┘
```

## 🚀 Services

### 1. **Eureka Server** (Port: 8761)
- Service discovery and registration
- Enables dynamic service location
- Health monitoring for all microservices

### 2. **API Gateway** (Port: 8080)
- Single entry point for all client requests
- Route management and load balancing
- Built with Spring Cloud Gateway

### 3. **Order Service** (Port: 8081)
- Core business logic for order management
- RESTful APIs for order operations
- MySQL database integration
- gRPC client for inventory communication
- Kafka producer for order events

### 4. **Inventory Service** (Port: 8082)
- Inventory management and stock validation
- gRPC server for real-time inventory checks
- H2 in-memory database
- Kafka integration for inventory updates

### 5. **Notification Service** (Port: 8083)
- Event-driven notification system
- Kafka consumer for order events
- H2 in-memory database
- Handles order confirmation notifications

### 6. **Common Library**
- Shared utilities and common components
- Exception handling classes
- Response models and DTOs
- Event classes for Kafka communication
- Utility classes for file operations, JSON processing, etc.

## 🛠️ Technology Stack

### Core Framework
- **Spring Boot 3.4.6** - Main application framework
- **Spring Cloud 2024.0.1** - Microservices infrastructure
- **Java 17** - Programming language

### Communication
- **gRPC** - High-performance inter-service communication
- **Apache Kafka** - Event streaming and messaging
- **REST APIs** - HTTP-based communication
- **Protocol Buffers** - Data serialization

### Service Discovery & Gateway
- **Netflix Eureka** - Service discovery
- **Spring Cloud Gateway** - API gateway

### Data Persistence
- **MySQL** - Primary database for Order Service
- **H2 Database** - In-memory database for other services
- **Spring Data JPA** - Data access layer

### Monitoring & Observability
- **Spring Boot Actuator** - Health checks and metrics
- **Micrometer** - Metrics collection
- **Prometheus** - Metrics monitoring

### Development Tools
- **Lombok** - Reduces boilerplate code
- **MapStruct** - Bean mapping
- **Maven** - Build and dependency management

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **Docker & Docker Compose** (for infrastructure)
- **MySQL** (or use Docker)

## 🚦 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/lochuung/mini-order-microservices.git
cd mini-order-microservices
```

### 2. Start Infrastructure Services
```bash
# Start Kafka, Zookeeper, and MySQL
docker-compose up -d
```

### 3. Build All Services
```bash
# Build all modules
mvn clean install
```

### 4. Start Services (in order)

```bash
# 1. Start Eureka Server
cd eureka-server
mvn spring-boot:run

# 2. Start API Gateway (new terminal)
cd api-gateway
mvn spring-boot:run

# 3. Start Inventory Service (new terminal)
cd inventory-service
mvn spring-boot:run

# 4. Start Order Service (new terminal)
cd order-service
mvn spring-boot:run

# 5. Start Notification Service (new terminal)
cd notification-service
mvn spring-boot:run
```

### 5. Verify Setup
- **Eureka Dashboard**: http://localhost:8761
- **API Gateway Health**: http://localhost:8080/actuator/health
- **Order Service**: http://localhost:8081/actuator/health
- **Inventory Service**: http://localhost:8082/actuator/health
- **Notification Service**: http://localhost:8083/actuator/health

## 📚 API Documentation

### Order Management

#### Create Order
```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
    "productId": 1,
    "quantity": 2
}
```

#### Get Order
```http
GET http://localhost:8080/api/orders/{orderId}
```

### Example Response
```json
{
    "status": "success",
    "message": "Order created successfully",
    "data": {
        "id": 1,
        "productId": 1,
        "quantity": 2,
        "createdAt": "2025-06-08T10:00:00",
        "updatedAt": "2025-06-08T10:00:00"
    },
    "timestamp": "2025-06-08T10:00:00Z"
}
```

## 🔄 Service Communication Flow

### Order Creation Flow
1. **Client** → **API Gateway** → **Order Service**
2. **Order Service** → **Inventory Service** (gRPC) - Check stock
3. **Inventory Service** → **Order Service** - Confirm availability
4. **Order Service** → **Database** - Save order
5. **Order Service** → **Kafka** - Publish OrderCreatedEvent
6. **Notification Service** ← **Kafka** - Consume event
7. **Notification Service** - Send confirmation notification

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Tests for Specific Service
```bash
cd order-service
mvn test
```

## 🔧 Configuration

### Environment Variables
```bash
# Database Configuration
MYSQL_URL=jdbc:mysql://localhost:3306/order_db
MYSQL_USERNAME=root
MYSQL_PASSWORD=password

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Service Discovery
EUREKA_URL=http://localhost:8761/eureka/

# gRPC Configuration
INVENTORY_GRPC_HOST=localhost
INVENTORY_GRPC_PORT=9090
```

### Application Profiles
- **default** - Local development
- **docker** - Docker environment
- **prod** - Production environment

## 🐳 Docker Support

### Build Docker Images
```bash
# Build all services
mvn clean package

# Build Docker images for each service
docker build -t order-service ./order-service
docker build -t inventory-service ./inventory-service
docker build -t notification-service ./notification-service
```

### Run with Docker Compose
```bash
docker-compose up --build
```

## 🔒 Security Considerations

- Add authentication/authorization (Spring Security)
- Implement API rate limiting
- Secure inter-service communication
- Add input validation and sanitization
- Implement proper error handling

## 🚀 Future Enhancements

- [ ] Add authentication & authorization
- [ ] Implement distributed tracing (Zipkin/Jaeger)
- [ ] Add circuit breaker pattern (Resilience4j)
- [ ] Implement caching (Redis)
- [ ] Add comprehensive integration tests
- [ ] Implement event sourcing
- [ ] Add configuration management (Spring Cloud Config)
- [ ] Implement blue-green deployment
- [ ] Add API documentation (Swagger/OpenAPI)

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For questions and support, please open an issue in the GitHub repository.

---
**Built with ❤️ using Spring Boot Microservices Architecture**