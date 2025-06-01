# Event-Driven Microservice

## 🧩 What is a Microservice?
A microservice is a small, independently deployable service that focuses on doing one business function well. In a system, you might have microservices like:
* `OrderService` – handles orders
* `InventoryService` – manages product stock
* `PaymentService` – processes payments

They communicate over a network (usually via `REST`, `gRPC`, or `messaging`).

## ⚡ What is Event-Driven Architecture?
In an **event-driven architecture (EDA)**, services **communicate by sending and reacting to events,** instead of making direct calls to each other (like REST API calls).
An **event** is a message that says **“something happened”**.

For example:
* Event: **"OrderCreated"**
* Meaning: An order was placed by a customer.

Instead of calling the `InventoryService` directly, the `OrderService` will **publish an event** like `OrderCreated`. Then any service (like `InventoryService`, `ShippingService`, etc.) **that subscribed to that event** will react.

## ✅ Benefits of Event-Driven Microservices
| Feature                | Benefit                                                |
| ---------------------- | ------------------------------------------------------ |
| ✅ Loose Coupling       | Services don’t need to know about each other.          |
| ✅ Scalability          | Each service can scale independently.                  |
| ✅ Resilience           | Services don’t crash if one is down (no direct calls). |
| ✅ Real-time Processing | Events can be processed as they happen.                |

## 🛠️ Common Tools/Tech Stack
| Purpose                     | Tools                              |
| --------------------------- | ---------------------------------- |
| Messaging Broker            | Kafka, RabbitMQ, NATS, AWS SNS/SQS |
| Microservices Framework     | Spring Boot, Micronaut, Quarkus    |
| Event Format                | JSON, Avro, Protobuf               |
| Schema Registry (for Kafka) | Confluent Schema Registry          |

## 🧪 Example Scenario
1. User Places Order
- `OrderService` creates a new order
- It publishes an `OrderCreated` event to Kafka
```
{
    "eventType": "OrderCreated",
    "orderId": "123",
    "userId": "456",
    "items": ["itemA", "itemB"]
}
```

2. InventoryService Listens
- It’s subscribed to `OrderCreated` events
- When it receives the event, it checks inventory and reserves items

3. PaymentService Listens
- Also subscribed to `OrderCreated`
- Starts processing payment

## 🔄 Event Flow Example
```
User → OrderService
        ↳ Publishes: OrderCreated
                ↳ InventoryService (Listens)
                ↳ PaymentService (Listens)
                ↳ NotificationService (Listens)
```

## 🚨 Challenges
| Challenge           | How to Handle                                       |
| ------------------- | --------------------------------------------------- |
| Event ordering      | Use partitioning strategies (e.g. Kafka partitions) |
| Guaranteed delivery | Use durable message queues and retries              |
| Schema changes      | Use versioning and schema registries                |
| Debugging           | Use tracing (OpenTelemetry, Jaeger, Zipkin)         |

## 🔚 Summary
* Microservices break apps into small services
* Event-driven microservices **communicate using events** (not direct calls)
* This leads to **better decoupling, scalability, and flexibility**
* You’ll often use a **message broker** like **Kafka** or **RabbitMQ**

# Example using Spring Boot and Kafka
Let's go through a simple event-driven microservice example using Spring Boot and Kafka. We'll create two microservices:

## 📦 Microservices Overview
1. `OrderService`
- Creates an order
- Publishes an `OrderCreated` event to Kafka

2. `InventoryService`
- Listens to `OrderCreated` events
- Deducts stock accordingly

## 🛠️ Tools & Dependencies
In both projects, use Maven or Gradle and include:
```
<!-- spring-boot-starter dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>

<!-- Kafka dependencies -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

## 🧱 Step 1: Kafka Configuration (shared by both services)
`application.yml`
```
spring:
    kafka:
        bootstrap-servers: localhost:9092
        consumer:
            group-id: inventory-service
            auto-offset-reset: earliest
            key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
            value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
        producer:
            key-serializer: org.apache.kafka.common.serialization.StringSerializer
            value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

## 🧾 Step 2: Order Service (Publisher)
`OrderEvent.java`
```
public class OrderEvent {
    private String orderId;
    private String productId;
    private int quantity;

    // Getters and Setters
}
```

`OrderController.java`
```
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderEvent orderEvent) throws JsonProcessingException {
        String event = objectMapper.writeValueAsString(orderEvent);
        kafkaTemplate.send("order-events", event);
        return ResponseEntity.ok("Order published successfully");
    }
}
```

## 🧾 Step 3: Inventory Service (Consumer)
`InventoryListener.java`
```
@Component
public class InventoryListener {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "order-events", groupId = "inventory-service")
    public void handleOrderEvent(String message) throws JsonProcessingException {
        OrderEvent order = objectMapper.readValue(message, OrderEvent.class);
        System.out.println("InventoryService received order: " + order.getOrderId());

        // Logic to deduct inventory...
    }
}
```

## 🧪 Step 4: Run Kafka Locally
Use Docker for Kafka and Zookeeper:
```
# docker-compose.yml
version: '3.8'
services:
    zookeeper:
        image: confluentinc/cp-zookeeper:7.2.1
        ports:
            - "2181:2181"
        environment:
            ZOOKEEPER_CLIENT_PORT: 2181
    
    kafka:
        image: confluentinc/cp-kafka:7.2.1
        ports:
            - "9092:9092"
        environment:
            KAFKA_BROKER_ID: 1
            KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
            KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
            KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

Run with:
```
docker-compose up -d
```

## 🧪 Testing the Flow
1. Start Kafka (using Docker).
2. Start both `OrderService` and `InventoryService`.
3. Use Postman or `curl` to POST an order:
```
curl -X POST http://localhost:8080/orders \
    -H "Content-Type: application/json" \
    -d '{"orderId": "1001", "productId": "P123", "quantity": 2}'
```

4. See the console log from `InventoryService`:
```
InventoryService received order: 1001
```

## ✅ Summary
* We used Kafka as the message broker
* `OrderService` **published** events
* `InventoryService` **consumed** events
* This is a **loose-coupled** architecture

# Error Handling and Retries
Adding **error handling and retries** is crucial in an event-driven microservices system to ensure **resilience** and **message durability**.

## ✅ Goal
When InventoryService fails to process a message:
* It should **log the error**
* **Retry a few times**
* If it still fails, **send the message to a dead-letter topic (DLT)** for later analysis

## 🔧 Kafka Consumer Retry Strategy in Spring Boot
Spring Kafka provides:
* Automatic retries
* Recovery callbacks
* Dead-letter publishing out of the box

## 🧱 Step-by-Step Setup
### 1. Add Dependencies (if not already)
```
<dependency>
   <groupId>org.springframework.kafka</groupId>
   <artifactId>spring-kafka</artifactId>
</dependency>   
```

### 2. Update Kafka Configuration
```
spring:
   kafka:
       consumer:
           group-id: inventory-service
           auto-offset-reset: earliest
       listener:
           ack-mode: record
           concurrency: 1
           missing-topics-fatal: false
       retry:
           topic:
               enabled: true
               attempts: 3
               delay: 2000 # 2 seconds between retries
               dlt:
                    topic-name: order-events.DLT
```

This configuration enables:
* 3 retry attempts
* 2-second delay between retries
* Failing messages go to `order-events.DLT` (Dead Letter Topic)

### 3. Modify the Consumer with Error Handling
```
@Component
public class InventoryListener {

   private final ObjectMapper objectMapper = new ObjectMapper();

   @KafkaListener(topics = "order-events", groupId = "inventory-service")
   public void handleOrderEvent(String message) throws JsonProcessingException {
   OrderEvent order = objectMapper.readValue(message, OrderEvent.class);

        // Simulate an error condition (for example purposes)
        if (order.getProductId().equals("FAIL")) {
            throw new RuntimeException("Failed to process product: " + order.getProductId());
        }

        System.out.println("Inventory updated for order: " + order.getOrderId());
   } 
}
```

### 4. Add a Dead Letter Topic Listener (Optional, for monitoring)
```
@Component
public class DLTListener {

   @KafkaListener(topics = "order-events.DLT", groupId = "inventory-dlt-group")
   public void handleDeadLetter(String message) {
   System.err.println("❌ DLT received failed message: " + message);
   // Optionally: persist to DB or alert monitoring system
   } 
}
```

### 5. Create the DLT Manually (Optional but recommended)
If you're using Kafka without automatic topic creation, you need to manually create `order-events.DLT`.
You can use the Kafka CLI or tools like `kafka-topics.sh`:
```
kafka-topics.sh --create --topic order-events.DLT --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

## 🧪 Testing Error Handling
1. Send a message with productId = FAIL:
```
curl -X POST http://localhost:8080/orders \
    -H "Content-Type: application/json" \
    -d '{"orderId": "1234", "productId": "FAIL", "quantity": 1}'
```

2. Observe retry logs in `InventoryService`.
3. Final failure message appears in the DLT handler:
```
❌ DLT received failed message: {"orderId":"1234","productId":"FAIL","quantity":1}
```

## ✅ Summary
* ✅ Configured Spring Kafka retry with `spring.kafka.retry.topic.enabled=true`
* ✅ Automatically retried failed messages
* ✅ Redirected unrecoverable messages to a Dead Letter Topic (DLT)
* ✅ Added a listener for the DLT for monitoring/debugging

# Monitoring Event-Driven Microservices
🎯 Monitoring your event-driven microservices is essential for observability, debugging, and proactive issue detection — especially when using Kafka and Spring Boot.
Let’s integrate Prometheus for metrics collection and Grafana for dashboards.

## 📊 Monitoring Kafka-based Microservices with Prometheus & Grafana
### ✅ We will:
* Expose Spring Boot metrics using **Micrometer**
* Collect those metrics using **Prometheus**
* Visualize everything in **Grafana**
* Optionally: Monitor **Kafka consumer lag**, retry attempts, and DLT activity

## 🧱 Step-by-Step Integration

### 1. 🔌 Add Micrometer & Prometheus Dependencies (in each service)
`pom.xml`:
```
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### 2. ⚙️ Enable Metrics in application.yml
```
management:
    endpoints:
        web:
            exposure:
                include: health,info,prometheus
    endpoint:
        prometheus:
            enabled: true
    metrics:
        export:
            prometheus:
                enabled: true
```

This enables `/actuator/prometheus` at:
```
http://localhost:8080/actuator/prometheus
```

### 3. 📦 Docker Compose for Prometheus + Grafana + Kafka
`docker-compose.yml` (extended)
```
version: '3.8'

services:
    zookeeper:
        image: confluentinc/cp-zookeeper:7.2.1
        ports:
            - "2181:2181"
    
    kafka:
        image: confluentinc/cp-kafka:7.2.1
        ports:
            - "9092:9092"
        environment:
            KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
            KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
    
    prometheus:
        image: prom/prometheus
        ports:
            - "9090:9090"
        volumes:
            - ./prometheus.yml:/etc/prometheus/prometheus.yml
    
    grafana:
        image: grafana/grafana
        ports:
            - "3000:3000"
```

### 4. 📝 Prometheus Configuration (prometheus.yml)
```
global:
    scrape_interval: 5s

scrape_configs:
    - job_name: 'order-service'
      static_configs:
        - targets: ['host.docker.internal:8080'] # or use container name if running all in Docker
    
    - job_name: 'inventory-service'
      static_configs:
        - targets: ['host.docker.internal:8081']
```

Replace `host.docker.internal` with actual IPs or Docker container names based on your setup.

### 5. 📈 Grafana Setup
1. Visit http://localhost:3000 (user: `admin`, pass: `admin`)
2. Add Prometheus as a Data Source (http://prometheus:9090)
3. Import Kafka/Consumer dashboards or create custom ones
4. Example metrics to visualize:
   * `kafka_consumer_records_consumed_total`
   * `kafka_consumer_records_lag_max` 
   * `kafka_producer_records_sent_total` 
   * `retry_attempts_total` 
   * `dead_letter_topic_messages_total` (custom)

### 6. 🔐 Optional: Add Custom Kafka Error Metrics
You can define custom counters like this:
```
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class InventoryListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Counter retryCounter;

    public InventoryListener(MeterRegistry registry) {
        this.retryCounter = registry.counter("retry_attempts_total");
    }

    @KafkaListener(topics = "order-events", groupId = "inventory-service")
    public void handle(String message) throws JsonProcessingException {
        try {
            OrderEvent order = objectMapper.readValue(message, OrderEvent.class);

            if (order.getProductId().equals("FAIL")) {
                retryCounter.increment();
                throw new RuntimeException("Simulated error");
            }

            System.out.println("Processed: " + order.getOrderId());

        } catch (Exception ex) {
            retryCounter.increment();
            throw ex;
        }
    }
}
```

## ✅ Result
Once configured:
* **Prometheus** scrapes `/actuator/prometheus` endpoints
* **Grafana** visualizes metrics like:
  * Total Kafka events consumed
  * Retry attempts
  * DLT message count
  * Consumer lag

## 🔍 Example Grafana Dashboards
You can use these:
* [Kafka Exporter Dashboard](https://grafana.com/grafana/dashboards/7589)
* [Spring Boot JVM/Metrics](https://grafana.com/grafana/dashboards/4701)

