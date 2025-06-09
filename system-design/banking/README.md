# System Design Banking Industry
System design in the **banking industry** requires careful consideration of **security**, **scalability**, **compliance**, **availability**, and **performance**. 
A typical banking system involves multiple modules and integrations to handle different operations like user authentication, account management, transactions, fraud detection, reporting, etc.

Here’s a breakdown of a typical banking system design:

## 🔧 1. High-Level Requirements
Functional Requirements:
* Customer onboarding (Know Your Customer)
* Account management (create, update, close)
* Transaction management (debit, credit, transfer)
* Loan processing
* Internet/mobile banking
* Statements and notifications
* ATM and POS services
* Fraud detection

Non-Functional Requirements:
* High availability (24/7 uptime)
* Security (encryption, access control)
* Regulatory compliance (e.g., GDPR, PCI DSS)
* Scalability (support millions of users)
* Disaster recovery

## 🏗️ 2. High-Level Architecture
```
              ┌──────────────────────────────────────────────┐
              │              Client Applications             │
              │   (Mobile App, Web Portal, ATM UI, POS)      │
              └──────────────────────────────────────────────┘
                               │
                               ▼
                   ┌────────────────────────────┐
                   │   API Gateway / BFF Layer  │
                   └────────────────────────────┘
                               │
                               ▼
              ┌──────────────────────────────────────────────┐
              │               Authentication Layer           │
              │   (OAuth2, JWT, MFA, Device Binding)         │
              └──────────────────────────────────────────────┘
                               │
                               ▼
        ┌──────────────────────────┬───────────────────────────┐
        │ Account Service          │ Transaction Service       │
        │ (CRUD, balance inquiry)  │ (Fund transfer, payment)  │
        └──────────────────────────┴───────────────────────────┘
                    │                             │
   ┌────────────────┘                             └─────────────────┐
   ▼                                                                ▼
Compliance Engine      Fraud Detection         Notification Service   Loan Service
(Rules engine, AML)    (AI/ML, pattern scan)   (SMS, email, push)    (loan lifecycle)

                            │
                            ▼
                    ┌───────────────────────────────────┐
                    │     Database(s)                   │
                    ├───────────────────────────────────┤
                    │ Relational (PostgreSQL, Oracle)   │
                    │ NoSQL (MongoDB, Redis, Cassandra) │
                    └───────────────────────────────────┘
                            │
                            ▼
                    ┌───────────────────┐
                    │    Message Queue  │
                    │ (Kafka, RabbitMQ) │
                    └───────────────────┘
```

## 🔒 3. Security Considerations
* **Data encryption** (at-rest and in-transit)
* **RBAC/ABAC** (role-based/access-based control)
* **MFA/OTP** for login and transactions
* **Rate limiting** & **API throttling**
* **Audit logging** of all sensitive operations
* **Tokenization of sensitive data** (e.g., PAN)

## 🏦 4. Regulatory Compliance
* PCI DSS for card data
* KYC (Know Your Customer)
* AML (Anti-Money Laundering)
* GDPR (data protection)
* SOC 2 compliance

## ⚙️ 5. Key Components & Technologies
| Layer            | Tech Stack (Example)                    |
| ---------------- | --------------------------------------- |
| API Gateway      | Kong, Apigee, Spring Cloud Gateway      |
| Backend Services | Java (Spring Boot), Go, Node.js         |
| Databases        | PostgreSQL, Oracle DB, Redis, Cassandra |
| Message Queue    | Kafka, RabbitMQ                         |
| Authentication   | OAuth2, Keycloak, Okta                  |
| Monitoring       | Prometheus, Grafana, ELK stack          |
| CI/CD            | Jenkins, GitHub Actions, ArgoCD         |
| Containerization | Docker, Kubernetes, OpenShift           |

## 🔁 6. Core Workflows
a. **Fund Transfer (simplified)**:
1. User authenticates via mobile app.
2. Sends transfer request → API Gateway → Transaction Service.
3. Transaction Service checks:
   * Valid accounts
   * Sufficient balance
   * Fraud check
4. Deducts from sender, credits recipient.
5. Logs transaction.
6. Sends notification.

## 🧠 7. Advanced Features
* **AI/ML for fraud detection** (anomaly detection)
* **Real-time transaction monitoring**
* **Open Banking APIs** (PSD2 support)
* **Blockchain for secure audit trail**
* **Chatbots for customer support**

## 🌐 8. Deployment & Scalability
* Use **microservices** for modularity.
* **Kubernetes** for orchestration.
* **Cloud-native** (AWS, GCP, Azure) or hybrid model.
* **Active-active replication** for availability.
* **CDNs** and **edge caching** for frontend assets.

# 🧾 Use Case: Fund Transfer System (Real-Time Interbank Transfer)

## 🎯 Objective
Design a secure, real-time **fund transfer system** that allows users to send money from their bank account to another user’s account in the same or different bank.

## 🔧 Functional Requirements
* Authenticate sender using login + MFA
* Validate sender and recipient accounts
* Check balance
* Debit sender and credit recipient atomically
* Log transaction and generate reference number
* Notify sender and recipient
* Ensure ACID properties
* Support high TPS (Transactions Per Second)

## 🏗️ High-Level Architecture
```
User App (Mobile/Web)
      │
      ▼
[ API Gateway / BFF ]
      │
      ▼
[ Auth Service ] ───→ [ Token Service / MFA ]
      │
      ▼
[ Fund Transfer Service ]
      ├── Validate Accounts (Account Service)
      ├── Check Balance (Ledger Service)
      ├── Execute Transfer (Atomic Txn)
      ├── Log Transaction (Txn Log DB)
      ├── Emit Event (Kafka)
      └── Notify (Notification Service)
```

## 🗄️ Database Design (Relational - PostgreSQL / Oracle)
`accounts` Table

| account_id | customer_id | balance | status |
| ---------- | ----------- | ------- | ------ |
| 1234567890 | CUST001     | 1000.00 | ACTIVE |

`transactions` Table

| txn_id   | sender_id   | receiver_id | amount | status  | timestamp        |
|----------|-------------| ----------- | ------ | ------- | ---------------- |
| TXN001   | 1234567890  | 0987654321  | 100.00 | SUCCESS | 2025-06-03 12:00 |


## ⚙️ Transaction Logic (Pseudocode / Java-like)
```java
@Transactional
public void transferFunds(String senderId, String recipientId, BigDecimal amount) {
    Account sender = accountRepo.findById(senderId);
    Account recipient = accountRepo.findById(recipientId);

    if (sender.getBalance().compareTo(amount) < 0) {
        throw new InsufficientFundsException();
    }

    // Update balances
    sender.setBalance(sender.getBalance().subtract(amount));
    recipient.setBalance(recipient.getBalance().add(amount));

    accountRepo.save(sender);
    accountRepo.save(recipient);

    // Log transaction
    Transaction txn = new Transaction(senderId, recipientId, amount, SUCCESS);
    transactionRepo.save(txn);

    // Send event to Kafka
    kafkaTemplate.send("fund-transfer-events", txn);

    // Notify
    notificationService.sendSms(senderId, "Transfer Successful: " + amount);
}
```

## 🔐 Security Architecture
* **OAuth2 / JWT**: For secure token-based authentication.
* **MFA**: TOTP/SMS OTP for transaction authorization.
* **TLS Everywhere**: Encrypted in-transit data.
* **Row-Level Security**: In database, per account.
* **Audit Logs**: All changes recorded for compliance.

## 📈 Scalability & Performance
* Stateless services → Easily scaled with Kubernetes
* DB Sharding on `account_id` for large scale
* Kafka for **async transaction event handling** (notifications, analytics, audit trail)
* **Read replicas** for high volume balance queries
* **Idempotency keys** to avoid double transactions

## 🔔 Kafka Event: `fund-transfer-events`
```json
{
  "transactionId": "TXN001",
  "senderId": "1234567890",
  "receiverId": "0987654321",
  "amount": 100.00,
  "timestamp": "2025-06-03T12:00:00Z",
  "status": "SUCCESS"
}
```

## 📲 Notification Microservice
Handles:
* SMS via Twilio
* Email via SendGrid
* Push notifications via Firebase

Subscribes to Kafka topic `fund-transfer-events`.

## 🧪 Testing Strategy
* **Unit Tests** for validation, service methods
* **Integration Tests** with DB and Kafka
* **Contract Tests** for API compatibility
* **Load Testing** using **JMeter** or **K6** (simulate 1000+ TPS)
* **Penetration Testing** for security audit

## 📜 CI/CD and Deployment
* **GitHub Actions / Jenkins** for pipelines
* **Dockerized Microservices**
* **Kubernetes (GKE/EKS)** for orchestration
* Canary releases with feature flags for sensitive operations
* **Monitoring**: Prometheus + Grafana
* **Tracing**: OpenTelemetry + Jaeger

## 💸 Sequence Diagram – Real-Time Fund Transfer
```
  User                API Gateway                     Auth Service       Fund Transfer Service                   Account Service      Notification Service      Kafka
   │                       │                               │                        │                                   │                       │                 │
   │  ── Login Request ─▶  │                               │                        │                                   │                       │                 │
   │                       │ ── Validate JWT & OTP/MFA ──▶ │                        │                                   │                       │                 │
   │                       │ ◀──────────── OK ──────────── │                        │                                   │                       │                 │
   │  ◀─────── OK ───────  │                               │                        │                                   │                       │                 │
   │                       │                               │                        │                                   │                       │                 │
   │  ─────────────────────────────── Transfer Req ───────────────────────────────▶ │                                   │                       │                 │
   │                       │                               │                        │ ── Verify sender/recipient ID ──▶ │                       │                 │
   │                       │                               │                        │ ◀──────── Account Info ────────── │                       │                 │
   │                       │                               │                        │ ───────── Check balance ────────▶ │                       │                 │
   │                       │                               │                        │ ◀───────── Sufficient ──────────  │                       │                 │
   │                       │                  │                        │                       │                       │                 │
   │                       │                  │        Perform debit/credit (Tx) ───────────▶│                       │                 │
   │                       │                  │                        │     ◀──── Success Tx │                       │                 │
   │                │                  │                        │                       │                       │                 │
   │                │                  │    Save transaction record                     │                       │                 │
   │                │                  │──────────────────────────────────────────────▶ DB                      │                 │
   │                │                  │                        │                       │                       │                 │
   │                │                  │     Publish Kafka event ───────────────────────────────────────────────▶                 │
   │                │                  │                        │                       │                       │                 │
   │                │                  │ Notify sender/recipient ──────────────────────────────────────────────▶ │               │
   │                │                  │                        │                       │       SMS / Email     │                 │
   │                │                  │                        │                       │                       │                 │
   │       ◀─────── Transaction confirmation sent back ─────────────────────────────── │                       │                 │

```

## ✅ Steps Summary
1. **User** logs in and sends a transfer request via mobile/web.
2. **API Gateway** forwards request to Auth Service to validate JWT and OTP/MFA.
3. The Fund Transfer Service handles the business logic:
  * Calls Account Service to validate accounts and balance.
  * Performs atomic debit/credit.
  * Saves the transaction record.
  * Publishes an event to Kafka.
4. Notification Service listens to the Kafka topic and sends real-time SMS/Email to both sender and recipient.
5. Final confirmation is sent back to the user.

## Spring Boot code example
Here's a **Spring Boot microservice code example** for a **Fund Transfer Service** in a banking system. This service:
Validates sender/recipient.
Checks balance.
Performs debit/credit.
Publishes a Kafka event.
Returns transaction confirmation.

### 📦 Project Structure
```
fund-transfer-service/
├── controller/
│   └── FundTransferController.java
├── service/
│   └── FundTransferService.java
├── model/
│   └── FundTransferRequest.java
├── event/
│   └── TransactionEvent.java
├── config/
│   └── KafkaProducerConfig.java
└── FundTransferApplication.java
```

### 1. FundTransferRequest.java
```java
package com.bank.model;

public class FundTransferRequest {
    private String senderAccountId;
    private String recipientAccountId;
    private double amount;

    // Getters and setters
}
```

### 2. TransactionEvent.java
```java
package com.bank.event;

public class TransactionEvent {
    private String transactionId;
    private String senderAccountId;
    private String recipientAccountId;
    private double amount;
    private String status;

    // Constructors, getters, setters
}
```

### 3. KafkaProducerConfig.java
````java
package com.bank.config;

import com.bank.event.TransactionEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.*;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.*;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, TransactionEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, TransactionEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
````

### 4. FundTransferService.java
```java
package com.bank.service;

import com.bank.event.TransactionEvent;
import com.bank.model.FundTransferRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class FundTransferService {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public FundTransferService(KafkaTemplate<String, TransactionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String transferFunds(FundTransferRequest request) {
        // Simulate account validation and balance checks
        if (request.getAmount() <= 0) {
            return "Invalid amount";
        }

        // Simulate database operation to debit and credit
        String transactionId = UUID.randomUUID().toString();

        TransactionEvent event = new TransactionEvent();
        event.setTransactionId(transactionId);
        event.setSenderAccountId(request.getSenderAccountId());
        event.setRecipientAccountId(request.getRecipientAccountId());
        event.setAmount(request.getAmount());
        event.setStatus("SUCCESS");

        // Publish to Kafka topic
        kafkaTemplate.send("fund-transfer-events", event);

        return "Transaction successful with ID: " + transactionId;
    }
}
```

### 5. FundTransferController.java
```java
package com.bank.controller;

import com.bank.model.FundTransferRequest;
import com.bank.service.FundTransferService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfer")
public class FundTransferController {

    private final FundTransferService fundTransferService;

    public FundTransferController(FundTransferService fundTransferService) {
        this.fundTransferService = fundTransferService;
    }

    @PostMapping
    public String transfer(@RequestBody FundTransferRequest request) {
        return fundTransferService.transferFunds(request);
    }
}
```

### 6. FundTransferApplication.java
```java
package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FundTransferApplication {
   public static void main(String[] args) {
      SpringApplication.run(FundTransferApplication.class, args);
   }
}
```

### 7. Database Integration using Spring Data JPA
We'll create entities, repositories, and modify the service to persist transactions and accounts.

🧩 `Account.java` – JPA Entity
```java
package com.bank.model;

import jakarta.persistence.*;

@Entity
public class Account {
    @Id
    private String accountId;

    private double balance;

    // Getters and Setters
}
```

🧩 `TransactionRecord.java` – JPA Entity
```java
package com.bank.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class TransactionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String senderAccountId;
    private String recipientAccountId;
    private double amount;
    private String status;
    private LocalDateTime timestamp;

    // Getters and Setters
}
```

📂 `AccountRepository.java`
```java
package com.bank.repository;

import com.bank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
}
```

📂 `TransactionRepository.java`
```java
package com.bank.repository;

import com.bank.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionRecord, String> {
}
```

### 8. Update FundTransferService to Use DB
```java
@Autowired
private AccountRepository accountRepository;

@Autowired
private TransactionRepository transactionRepository;

public String transferFunds(FundTransferRequest request) {
    Account sender = accountRepository.findById(request.getSenderAccountId())
        .orElseThrow(() -> new RuntimeException("Sender not found"));

    Account recipient = accountRepository.findById(request.getRecipientAccountId())
        .orElseThrow(() -> new RuntimeException("Recipient not found"));

    if (sender.getBalance() < request.getAmount()) {
        return "Insufficient funds";
    }

    sender.setBalance(sender.getBalance() - request.getAmount());
    recipient.setBalance(recipient.getBalance() + request.getAmount());

    accountRepository.save(sender);
    accountRepository.save(recipient);

    TransactionRecord record = new TransactionRecord();
    record.setSenderAccountId(sender.getAccountId());
    record.setRecipientAccountId(recipient.getAccountId());
    record.setAmount(request.getAmount());
    record.setStatus("SUCCESS");
    record.setTimestamp(LocalDateTime.now());

    transactionRepository.save(record);

    kafkaTemplate.send("fund-transfer-events", new TransactionEvent(
        record.getId(), sender.getAccountId(), recipient.getAccountId(),
        request.getAmount(), "SUCCESS"));

    return "Transaction ID: " + record.getId();
}
```

### 9. Kafka Consumer for Notifications
```java
package com.bank.consumer;

import com.bank.event.TransactionEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @KafkaListener(topics = "fund-transfer-events", groupId = "notification-group")
    public void handleTransactionEvent(TransactionEvent event) {
        // In real-world, you'd use SMS/Email API here
        System.out.println("Notifying users of transaction: " + event.getTransactionId());
    }
}
```

### 10. Integration Test Example
🧪 `FundTransferIntegrationTest.java`
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class FundTransferIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void setup() {
        accountRepository.save(new Account("A1", 1000));
        accountRepository.save(new Account("A2", 500));
    }

    @Test
    public void testFundTransfer() throws Exception {
        FundTransferRequest req = new FundTransferRequest("A1", "A2", 200);

        mockMvc.perform(post("/api/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(content().string(Matchers.containsString("Transaction ID")));
    }
}
```

### 11. Docker & Deployment
🐳 `Dockerfile`
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/fund-transfer-service.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

🐳 `docker-compose.yml`
```yaml
version: '3'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - kafka
      - postgres

  kafka:
    image: bitnami/kafka:latest
    environment:
      - KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_CFG_LISTENERS=PLAINTEXT://:9092
      - KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092
    ports:
      - "9092:9092"

  zookeeper:
    image: bitnami/zookeeper:latest
    ports:
      - "2181:2181"

  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: bankdb
      POSTGRES_USER: bankuser
      POSTGRES_PASSWORD: bankpass
    ports:
      - "5432:5432"
```

### 12. OpenAPI / Swagger Documentation
Step 1: Add Dependency to `pom.xml`
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.1.0</version>
</dependency>
```

Step 2: Access Swagger UI
After running the app, go to:
```bash
http://localhost:8080/swagger-ui.html
```

### 13. Spring Security with JWT
Step 1: Add Dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>
```

Step 2: Create `JwtUtil.java`
```java
@Component
public class JwtUtil {
    private final String secret = "yourSecretKey";

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(SignatureAlgorithm.HS256, secret).compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token)
                .getBody().getSubject();
    }
}
```

Step 3: Configure Security
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
```

### 14. Kubernetes Manifests
`deployment.yaml`
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: fund-transfer-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: fund-transfer
  template:
    metadata:
      labels:
        app: fund-transfer
    spec:
      containers:
        - name: fund-transfer
          image: your-dockerhub/fund-transfer-service:latest
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_DATASOURCE_URL
              value: jdbc:postgresql://postgres:5432/bankdb
            - name: SPRING_DATASOURCE_USERNAME
              value: bankuser
            - name: SPRING_DATASOURCE_PASSWORD
              value: bankpass
```

`service.yaml`
```yaml
apiVersion: v1
kind: Service
metadata:
  name: fund-transfer-service
spec:
  type: LoadBalancer
  selector:
    app: fund-transfer
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
```

### 15. CI/CD using GitHub Actions
`.github/workflows/deploy.yml`
```yaml
name: Build and Deploy

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'

      - name: Build with Maven
        run: mvn clean install

      - name: Build Docker image
        run: docker build -t your-dockerhub/fund-transfer-service:latest .

      - name: Push Docker image
        run: |
          echo "${{ secrets.DOCKER_PASSWORD }}" | docker login -u "${{ secrets.DOCKER_USERNAME }}" --password-stdin
          docker push your-dockerhub/fund-transfer-service:latest

  deploy:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Kubernetes
        uses: Azure/k8s-deploy@v4
        with:
          namespace: default
          manifests: |
            deployment.yaml
            service.yaml
```

### 16. Redis Caching for Account Lookup
🔧 Step 1: Add Dependency
In `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

🔧 Step 2: Configure Redis in `application.yml`
```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

🧠 Step 3: Enable Caching
`Application.java`
```java
@SpringBootApplication
@EnableCaching
public class FundTransferApplication {
    public static void main(String[] args) {
        SpringApplication.run(FundTransferApplication.class, args);
    }
}
```

🗃 Step 4: Update Service with Redis Cache
```java
@Cacheable(value = "accounts", key = "#accountId")
public Account getAccount(String accountId) {
    return accountRepository.findById(accountId)
        .orElseThrow(() -> new RuntimeException("Account not found"));
}
```

To invalidate after update:
```java
@CacheEvict(value = "accounts", key = "#account.accountId")
public void updateAccount(Account account) {
    accountRepository.save(account);
}
```

### 17. Prometheus & Grafana Monitoring
📦 Step 1: Add Micrometer + Prometheus Dependency
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

⚙️ Step 2: Configure in `application.yml`
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  metrics:
    export:
      prometheus:
        enabled: true
  endpoint:
    prometheus:
      enabled: true
```

📊 Step 3: Expose Metrics
Visit:
```bash
http://localhost:8080/actuator/prometheus
```

🧠 Step 4: Use Custom Metrics
```java
@Autowired
private MeterRegistry meterRegistry;

public void recordTransactionMetric() {
    meterRegistry.counter("fund_transfer.count").increment();
}
```

### 18. Unit Testing + Code Coverage
🧪 Sample Unit Test
```java
@SpringBootTest
public class FundTransferServiceTest {

    @MockBean
    private AccountRepository accountRepository;

    @Autowired
    private FundTransferService fundTransferService;

    @Test
    public void testInsufficientFunds() {
        Account sender = new Account("A1", 50);
        Account recipient = new Account("A2", 100);

        when(accountRepository.findById("A1")).thenReturn(Optional.of(sender));
        when(accountRepository.findById("A2")).thenReturn(Optional.of(recipient));

        FundTransferRequest request = new FundTransferRequest("A1", "A2", 200);
        String result = fundTransferService.transferFunds(request);

        assertEquals("Insufficient funds", result);
    }
}
```

✅ Code Coverage with JaCoCo
📦 Add to `pom.xml`
```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.8</version>
  <executions>
    <execution>
      <goals>
        <goal>prepare-agent</goal>
      </goals>
    </execution>
    <execution>
      <id>report</id>
      <phase>verify</phase>
      <goals>
        <goal>report</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

📊 Generate Report
```bash
mvn clean verify
```

Open:
```bash
target/site/jacoco/index.html
```

### 19. Rate Limiting using Bucket4j
This helps prevent abuse by limiting how many transactions a user/IP can perform per time window.

🔧 Step 1: Add Dependency
```xml
<dependency>
  <groupId>com.github.vladimir-bukhtoyarov</groupId>
  <artifactId>bucket4j-core</artifactId>
  <version>8.1.0</version>
</dependency>
```

🔧 Step 2: Create Rate Limiting Filter (IP-Based)
```java
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(ip, this::newBucket);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded");
        }
    }

    private Bucket newBucket(String key) {
        return Bucket4j.builder()
                .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))))
                .build();
    }
}
```

### 20. Circuit Breaker & Retry using Resilience4j
Useful when calling external services like payment gateways.

📦 Step 1: Add Dependency
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.1.0</version>
</dependency>
```

⚙️ Step 2: Configure in `application.yml`
```yaml
resilience4j:
  circuitbreaker:
    instances:
      externalService:
        registerHealthIndicator: true
        slidingWindowSize: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
  retry:
    instances:
      externalService:
        maxAttempts: 3
        waitDuration: 2s
```

🧠 Step 3: Use in Code
```java
@CircuitBreaker(name = "externalService", fallbackMethod = "fallback")
@Retry(name = "externalService")
public String callExternalService() {
    // simulate call
    if (new Random().nextBoolean()) {
        throw new RuntimeException("Failed");
    }
    return "Success";
}

public String fallback(Throwable t) {
    return "Fallback response";
}
```

### 21. Multi-Tenant Architecture (Header-Based)
You can support multiple banks/customers using headers like X-Tenant-ID.

🔧 Step 1: Add Tenant Context
```java
public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        currentTenant.set(tenantId);
    }

    public static String getTenantId() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}
```

🔧 Step 2: Add Interceptor
```java
@Component
public class TenantInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader("X-Tenant-ID");
        TenantContext.setTenantId(tenantId != null ? tenantId : "default");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}
```

And register it:
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor);
    }
}
```

### 22. Kafka Event Logging for Auditing Fund Transfers
Kafka is widely used in banking to log every transaction securely and asynchronously.

📦 Step 1: Add Kafka Dependencies
In `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

⚙️ Step 2: Kafka Configuration in `application.yml`
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
    consumer:
      group-id: audit-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

✍️ Step 3: Create Kafka Producer
```java
@Service
public class KafkaAuditProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "fund-transfer-audit";

    public void sendAuditEvent(String message) {
        kafkaTemplate.send(TOPIC, message);
    }
}
```

🧠 Step 4: Call Producer from FundTransferService
```java
@Autowired
private KafkaAuditProducer kafkaAuditProducer;

public String transferFunds(FundTransferRequest request) {
    // ... perform validation and fund transfer logic ...

    kafkaAuditProducer.sendAuditEvent(
        "Transferred " + request.getAmount() +
        " from " + request.getFromAccountId() +
        " to " + request.getToAccountId() +
        " at " + LocalDateTime.now()
    );

    return "Transfer successful";
}
```

📥 Step 5: (Optional) Kafka Consumer for Auditing
```java
@Component
public class KafkaAuditConsumer {

    @KafkaListener(topics = "fund-transfer-audit", groupId = "audit-group")
    public void listen(String message) {
        System.out.println("AUDIT LOG RECEIVED: " + message);
        // optionally persist to DB or ElasticSearch
    }
}
```

### 23. Async Processing with @Async (Optional Enhancement)
To avoid blocking requests while logging or notifying.

🧱 Enable Async Support
In `Application.java`:
```java
@EnableAsync
```

🧪 Use it in Audit Producer
```java
@Async
public CompletableFuture<Void> sendAuditEventAsync(String message) {
    kafkaTemplate.send(TOPIC, message);
    return CompletableFuture.completedFuture(null);
}
```

### 24. Distributed Tracing with OpenTelemetry + Zipkin
This helps trace a user request across services (e.g., Auth → Transfer → Notification) — useful in microservice-based banking systems.

📦 Step 1: Add OpenTelemetry + Zipkin Dependencies
```xml
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-spring-boot-starter</artifactId>
    <version>2.0.1</version>
</dependency>
<dependency>
    <groupId>io.opentelemetry.exporter</groupId>
    <artifactId>opentelemetry-exporter-zipkin</artifactId>
    <version>2.0.1</version>
</dependency>
```

⚙️ Step 2: Configure `application.yml`
```yaml
management:
  tracing:
    sampling:
      probability: 1.0
    enabled: true
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

Make sure Zipkin is running at localhost:9411. You can run it with Docker:
```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

🧠 Step 3: Automatically Traced Endpoints
Once the OpenTelemetry starter is included and Zipkin is running:
* Each HTTP call is traced.
* The trace is forwarded to Zipkin.
* Kafka traces and Redis/MongoDB integrations can also be traced if enabled.

You’ll see trace data like:
* Incoming request → fund transfer
* Kafka audit call → Redis check
* External service fallback (Resilience4j)

🧪 Step 4: Custom Span Example (Optional)
You can manually trace internal logic:
```java
@Autowired
private Tracer tracer;

public void transferFunds(FundTransferRequest request) {
    Span span = tracer.spanBuilder("validate-accounts").startSpan();
    try (Scope scope = span.makeCurrent()) {
        validateAccounts(request);
    } finally {
        span.end();
    }

    // Transfer logic...
}
```

🔍 Zipkin UI
Go to http://localhost:9411, you’ll see:
* Trace timelines
* Service dependencies
* Execution latency
* Tags and logs per span

### 25. Notification Microservice (Email/SMS)
📌 Use Case:
After a successful fund transfer, notify the customer by email and/or SMS.

🧩 1. `notification-service` Spring Boot Setup
🔨 `pom.xml` (add mail + web):
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

⚙️ application.yml
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your_email@gmail.com
    password: your_password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

✉️ Email Service
```java
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String content) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(content);
        mailSender.send(msg);
    }
}
```

📨 REST Controller
```java
@RestController
@RequestMapping("/notify")
public class NotificationController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@RequestBody NotificationRequest request) {
        emailService.sendEmail(request.getTo(), request.getSubject(), request.getContent());
        return ResponseEntity.ok("Email sent");
    }
}
```

📦 `NotificationRequest.java`
```java
public class NotificationRequest {
    private String to;
    private String subject;
    private String content;
    // getters and setters
}
```

SMS can be done similarly using Twilio API or Nexmo (Vonage).

🔗 Integration From Fund Transfer
Call notification via `RestTemplate` or `WebClient`:
```java
NotificationRequest email = new NotificationRequest(
    userEmail, "Transfer Successful", "You transferred $" + amount
);

restTemplate.postForObject("http://notification-service:8082/notify/email", email, String.class);
```

### 26. Service-to-Service Auth (OAuth2 or mTLS)
For secure internal service calls in banking microservices.

Option 1: OAuth2 with Client Credentials (Spring Security)
🏦 Use Keycloak or Auth0 for identity provider (IdP)
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/bank
      client:
        registration:
          internal-client:
            client-id: fund-transfer-service
            client-secret: xyz123
            authorization-grant-type: client_credentials
            scope: internal.read
        provider:
          keycloak:
            token-uri: http://localhost:8080/realms/bank/protocol/openid-connect/token
```

Use `WebClient` with OAuth2 token:
```java
@Autowired
private OAuth2AuthorizedClientManager authorizedClientManager;

public void notifyEmail() {
    OAuth2AuthorizeRequest authRequest = OAuth2AuthorizeRequest
        .withClientRegistrationId("internal-client")
        .principal("internal")
        .build();

    OAuth2AuthorizedClient client = authorizedClientManager.authorize(authRequest);
    String token = client.getAccessToken().getTokenValue();

    webClient.post()
        .uri("http://notification-service/notify/email")
        .headers(h -> h.setBearerAuth(token))
        .bodyValue(request)
        .retrieve()
        .bodyToMono(String.class);
}
```

Option 2: Mutual TLS (mTLS)
* Generate client/server certs with OpenSSL
* Enable `server.ssl.*` in each Spring Boot app
* Configure `truststore` to trust the other service's certificate

This is highly secure and common in finance.

### 27. Fraud Detection Microservice
A rule engine or ML model flags suspicious transactions.

⚙️ Basic Rule-Based Fraud Check
```java
@Service
public class FraudDetectionService {

    public boolean isFraudulent(FundTransferRequest request) {
        // Rule: large transfer between unknown accounts
        if (request.getAmount() > 10000 && !isFrequentContact(request)) {
            return true;
        }

        return false;
    }

    private boolean isFrequentContact(FundTransferRequest request) {
        // stubbed logic
        return false;
    }
}
```

📦 Intercept Transfer in FundTransferService
```java
@Autowired
private FraudDetectionService fraudDetectionService;

public String transferFunds(FundTransferRequest request) {
    if (fraudDetectionService.isFraudulent(request)) {
        return "Transaction blocked: suspicious activity";
    }

    // continue transfer...
}
```

🚀 Advanced Option: Drools Rule Engine or ML Model
Use:
* Drools: for complex rules
* TensorFlow/Sklearn model: for behavioral fraud scoring
* Kafka + Spark: stream analysis

## Concurrent Fund Transfers
Handling **concurrent fund transfers** in a banking system is critical to ensure `data consistenc`y, `no overdraft`, and **race condition prevention**. Here's how to **safely handle concurrent fund transfers** in a Spring Boot microservice architecture:

### ✅ Goal
Ensure that when two or more fund transfers occur concurrently on the same account, **account balances remain accurate and consistent** — no double-spending, no overdraft.

### 🧠 Problem Example
Suppose two fund transfers try to debit Account A (balance: $1000) at the same time for $800 and $300 respectively.

Without concurrency control, both might pass the "balance check" and cause an overdraft (total $1100 withdrawn from $1000).

### ✅ Strategy for Handling Concurrent Transfers
We'll combine **database-level locking** + **transactional safety** + optional **application-level distributed locks**:

#### ✅ 1. Use `@Transactional` in Spring
Ensure **atomicity**:
```java
@Transactional
public String transferFunds(FundTransferRequest request) {
    // debit sender, credit recipient
    // audit, notify, etc.
}
```

#### ✅ 2. Use `PESSIMISTIC_LOCK` (recommended for strict consistency)
In JPA/Hibernate:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Account a WHERE a.accountNumber = :accNum")
Account findByAccountNumberForUpdate(@Param("accNum") String accNum);
```
Example usage:
```java
@Transactional
public String transferFunds(FundTransferRequest request) {
    Account sender = accountRepository.findByAccountNumberForUpdate(request.getFromAccount());
    Account receiver = accountRepository.findByAccountNumberForUpdate(request.getToAccount());

    if (sender.getBalance().compareTo(request.getAmount()) < 0) {
        throw new InsufficientFundsException("Not enough balance");
    }

    sender.setBalance(sender.getBalance().subtract(request.getAmount()));
    receiver.setBalance(receiver.getBalance().add(request.getAmount()));

    accountRepository.save(sender);
    accountRepository.save(receiver);

    return "Transfer Successful";
}
```

#### ✅ 3. Alternative: Use synchronized block (not scalable for distributed systems)
```java
synchronized (this) {
    // check balance, debit, credit
}
```
**Not recommended** for microservices or clusters.

#### ✅ 4. Advanced: Use Distributed Locks (Redis or Zookeeper)
For clustered/distributed apps:
* Use **Redisson**, **Hazelcast**, or **Zookeeper** for locks

Example with **Redisson**:
```java
RLock lock = redissonClient.getLock("lock:account:" + accountNumber);
boolean locked = lock.tryLock(10, 5, TimeUnit.SECONDS);
try {
    if (locked) {
        // safely transfer funds
    }
} finally {
    lock.unlock();
}
```

#### ✅ 5. Ensure Isolation Level (optional)
Configure DB transaction isolation to **REPEATABLE_READ** or **SERIALIZABLE**:
```yaml
spring:
  datasource:
    hikari:
      transaction-isolation: TRANSACTION_REPEATABLE_READ
```
But `PESSIMISTIC_WRITE` is more fine-grained and effective in this case.

#### ✅ 6. Add a Version Field (Optimistic Locking)
Add `@Version` to `Account.java`:
```java
@Version
private Long version;
```
Spring JPA will now throw an `OptimisticLockingFailureException` if two updates clash — good for retry logic.

### ✅ Summary
| Method                        | Best For                     | Notes                          |
| ----------------------------- | ---------------------------- | ------------------------------ |
| `@Transactional`              | All use cases                | Ensures atomicity              |
| `PESSIMISTIC_WRITE` Locking   | Critical consistency needed  | Prevents race conditions       |
| `@Version` Optimistic Locking | High-read, low-write systems | Add retry logic                |
| Redis/Zookeeper Locking       | Distributed systems          | Needed if accounts are sharded |
| `synchronized`                | Local use only               | Not for microservices          |

### ✅ Optional Retry Logic
If using optimistic locking or Redis:
```java
@Retryable(
    value = { OptimisticLockingFailureException.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 200)
)
@Transactional
public void safeTransfer(FundTransferRequest request) {
    // your logic
}
```

### Complete Java Spring Boot code
Created a complete Java Spring Boot code example with the following:
1. ✅ **Account Entity** with `@Version` for optimistic locking.
2. ✅ **FundTransferService** with:
   * `@Transactional` for atomic operations.
   * `@Retryable` for automatic retry on conflict.
   * `@Recover` fallback method for graceful failure.
   * Pessimistic locking to prevent race conditions.
3. ✅ **JUnit Integration Test** simulating two concurrent fund transfers using `ExecutorService`.

#### ✅ Fraud Detection Details Added to Spring Boot microservice:
1. 🔍 `FraudDetectionService` now includes simple **rule-based checks**:
   * Flags transfers > `$10,000`.
   * Flags destination accounts not starting with `ACC`.
2. 📋 `FundTransferAudit` logs every transaction with a `flaggedAsFraud` boolean.
3. ✅ Integrated fraud check directly into the `FundTransferService`.

```java
package com.bank.transfer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jpa.repository.*;
import org.springframework.retry.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Entity
public class Account {
    @Id
    private String accountNumber;

    private BigDecimal balance;

    @Version
    private Long version;

    // getters and setters
}

public interface AccountRepository extends JpaRepository<Account, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accNum")
    Optional<Account> findByAccountNumberForUpdate(@Param("accNum") String accNum);
}

@Entity
public class FundTransferAudit {
    @Id
    @GeneratedValue
    private UUID id;

    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private boolean flaggedAsFraud;

    // getters and setters
}

public interface FundTransferAuditRepository extends JpaRepository<FundTransferAudit, UUID> {}

public class FundTransferRequest {
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    // getters and setters
}

@Service
@EnableRetry
public class FundTransferService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private FundTransferAuditRepository auditRepository;

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Retryable(
        value = { OptimisticLockingFailureException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 200)
    )
    @Transactional
    public String transferFunds(FundTransferRequest request) {
        Account sender = accountRepository.findByAccountNumberForUpdate(request.getFromAccount())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Account receiver = accountRepository.findByAccountNumberForUpdate(request.getToAccount())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        boolean isFraud = fraudDetectionService.isFraudulent(request);

        sender.setBalance(sender.getBalance().subtract(request.getAmount()));
        receiver.setBalance(receiver.getBalance().add(request.getAmount()));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        FundTransferAudit audit = new FundTransferAudit();
        audit.setFromAccount(request.getFromAccount());
        audit.setToAccount(request.getToAccount());
        audit.setAmount(request.getAmount());
        audit.setTimestamp(LocalDateTime.now());
        audit.setFlaggedAsFraud(isFraud);

        auditRepository.save(audit);

        return isFraud ? "Transfer completed but flagged for fraud investigation" : "Transfer Successful";
    }

    @Recover
    public String recover(OptimisticLockingFailureException e, FundTransferRequest request) {
        return "Transfer failed due to concurrent modification. Please try again.";
    }
}

@Service
public class FraudDetectionService {

    public boolean isFraudulent(FundTransferRequest request) {
        // Basic rule-based example:
        // 1. High value transfer > $10,000
        // 2. Transfer to unknown account prefix

        BigDecimal highValueThreshold = new BigDecimal("10000");
        if (request.getAmount().compareTo(highValueThreshold) > 0) {
            return true;
        }

        // Example: any account not starting with 'ACC' is suspicious
        if (!request.getToAccount().startsWith("ACC")) {
            return true;
        }

        return false;
    }
}

// --- Integration Test to Simulate Concurrency ---

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.util.concurrent.*;

@SpringBootTest
public class ConcurrentTransferTest {

    @Autowired
    private FundTransferService fundTransferService;

    @Test
    public void testConcurrentTransfers() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        FundTransferRequest req1 = new FundTransferRequest();
        req1.setFromAccount("ACC1001");
        req1.setToAccount("ACC1002");
        req1.setAmount(new BigDecimal("800"));

        FundTransferRequest req2 = new FundTransferRequest();
        req2.setFromAccount("ACC1001");
        req2.setToAccount("ACC1003");
        req2.setAmount(new BigDecimal("300"));

        CountDownLatch latch = new CountDownLatch(2);

        executor.submit(() -> {
            try {
                System.out.println(fundTransferService.transferFunds(req1));
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                System.out.println(fundTransferService.transferFunds(req2));
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executor.shutdown();
    }
}
```

## RDBMS Concurrent Transactions
In relational database systems, **concurrent transactions** refer to multiple database operations executing at the same time. Managing concurrency is critical to maintain **data integrity**, **isolation**, and **performance**. Here's a theoretical overview of how databases handle concurrent transactions:

### 🔑 1. ACID Properties
Concurrency control ensures **ACID** properties are upheld:
* **Atomicity**: A transaction is all-or-nothing.
* **Consistency**: Transactions preserve database invariants.
* **Isolation**: Concurrent transactions don’t interfere with each other.
* **Durability**: Once committed, data changes persist even after crashes.

### 🔄 2. Transaction Isolation Levels (SQL Standard)
SQL defines **4 standard isolation levels**, each balancing performance and consistency:

| Isolation Level      | Dirty Read | Non-Repeatable Read | Phantom Read |
| -------------------- | ---------- | ------------------- | ------------ |
| **READ UNCOMMITTED** | ✅ Yes      | ✅ Yes               | ✅ Yes        |
| **READ COMMITTED**   | ❌ No       | ✅ Yes               | ✅ Yes        |
| **REPEATABLE READ**  | ❌ No       | ❌ No                | ✅ Yes        |
| **SERIALIZABLE**     | ❌ No       | ❌ No                | ❌ No         |

* **Dirty Read**: Reading uncommitted changes from other transactions.
* **Non-Repeatable Read**: Same query returns different results within a transaction.
* **Phantom Read**: New rows appear in a repeated query due to other committed transactions.

### 🔐 3. Concurrency Control Mechanisms
#### **a. Pessimistic Locking**
* Prevents conflicts by locking rows or tables.
* Types:
  * **Row-level locks**: Prevent other writes or reads.
  * **Table locks**: Prevent access to the entire table.
  * SQL: `SELECT ... FOR UPDATE`

🔒 Use when high contention is expected.

#### **b. Optimistic Locking**
* Allows concurrent reads, checks for conflict at write-time using a version or timestamp.
* If version changes during update, it fails and must retry.
* Requires application-level control (e.g., Spring JPA’s `@Version`).

⚡ Use when conflicts are rare but reads are frequent.

### 🧠 4. Deadlocks
Occurs when two or more transactions hold locks and wait for each other indefinitely.
🛠 **Prevention/Handling**:
* Detect and rollback one transaction.
* Access resources in a consistent order.
* Use timeouts or retries.

### 🧪 5. Example: SERIALIZABLE
```sql
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;

BEGIN;

-- Read account balance
SELECT balance FROM account WHERE id = '123' FOR UPDATE;

-- Perform logic
-- ...

-- Update balance
UPDATE account SET balance = balance - 100 WHERE id = '123';

COMMIT;
```
🔐 Ensures no other transaction can read/write the account row during this transaction.

### 📚 Summary
| Concept                 | Use Case                              |
| ----------------------- | ------------------------------------- |
| **Pessimistic Locking** | High-conflict environments            |
| **Optimistic Locking**  | Low-conflict, high-read environments  |
| **Isolation Levels**    | Tradeoff between consistency vs speed |
| **Deadlocks**           | Must be handled in design and code    |

## Practical SQL examples for PostgreSQL and MySQL
Let's go deeper with practical SQL examples for PostgreSQL and MySQL, and then show how to handle deadlocks with retry logic.

### ✅ PART 1: Practical SQL Examples for Concurrency
#### 🔒 A. Pessimistic Locking with `SELECT ... FOR UPDATE`
✅ PostgreSQL / MySQL
```sql
START TRANSACTION;

-- Lock the row to prevent others from modifying
SELECT balance FROM account WHERE id = 101 FOR UPDATE;

-- Simulate business logic
UPDATE account SET balance = balance - 100 WHERE id = 101;

COMMIT;
```

🧠 What happens:
* Row `id=101` is locked until `COMMIT` or `ROLLBACK`.
* Any other transaction trying to modify this row will wait or fail depending on timeout.

#### ⚡ B. Optimistic Locking with Version Column
Assume you have a `version` column in the `account` table.
```sql
-- Table setup
ALTER TABLE account ADD COLUMN version INT DEFAULT 0;

-- Application tries:
UPDATE account
SET balance = balance - 100,
    version = version + 1
WHERE id = 101 AND version = 3;
```

* If `version = 3`, the update happens.
* If another transaction updated it (version is now 4), this fails — the application can **retry**.

### 🔁 PART 2: Retry Logic for Deadlocks (Spring Boot Example)
Deadlocks can occur when transactions wait on each other’s locks. A retry mechanism helps recover.

#### 🧪 A. Spring Boot + `@Retryable` (Optimistic)
```java
@Retryable(
    value = { DeadlockLoserDataAccessException.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 200)
)
@Transactional
public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
    Account from = accountRepository.findById(fromId).orElseThrow();
    Account to = accountRepository.findById(toId).orElseThrow();

    from.debit(amount);
    to.credit(amount);

    accountRepository.save(from);
    accountRepository.save(to);
}
```

#### 🛡️ B. Fallback Recovery with `@Recover`
```java
@Recover
public void recover(DeadlockLoserDataAccessException ex, Long fromId, Long toId, BigDecimal amount) {
    log.warn("Transfer failed due to deadlock. Please retry.");
}
```
Requires `spring-retry` and `@EnableRetry`.

### 🔄 PART 3: Preventing Deadlocks – Design Tips
| Strategy                    | Description                                                 |
| --------------------------- | ----------------------------------------------------------- |
| **Consistent Lock Order**   | Always lock rows in the same order (e.g., lowest ID first). |
| **Keep Transactions Short** | Avoid long business logic inside transactions.              |
| **Retry Mechanism**         | Retry failed transactions due to deadlocks.                 |
| **Timeouts**                | Use timeouts to detect and fail early.                      |
| **Indexing**                | Index `WHERE` clause fields to avoid full table locks.      |

## ✅ Simulate a Deadlock in SQL (PostgreSQL & MySQL)
### 📍 Deadlock Scenario
Two transactions try to lock rows in **reverse order**, causing a circular wait.

### 👇 SQL Script to Simulate
```sql
-- Session A
BEGIN;
SELECT * FROM account WHERE id = 1 FOR UPDATE;
-- Wait...

-- Session B (in another terminal)
BEGIN;
SELECT * FROM account WHERE id = 2 FOR UPDATE;

-- Now back to Session A
SELECT * FROM account WHERE id = 2 FOR UPDATE;  -- 🧨 Deadlock risk here

-- Back to Session B
SELECT * FROM account WHERE id = 1 FOR UPDATE;  -- 🔥 Deadlock!
```
The DBMS will detect and **kill one of the transactions** to resolve the deadlock.

## ✅ PostgreSQL Isolation Level Tuning
You can set isolation level per session:
```sql
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;

BEGIN;

-- Safe operations
SELECT balance FROM account WHERE id = 1 FOR UPDATE;
UPDATE account SET balance = balance - 100 WHERE id = 1;

COMMIT;
```

Or globally (not recommended without testing):
```sql
ALTER DATABASE your_db SET default_transaction_isolation TO 'REPEATABLE READ';
```

🧠 Use `SERIALIZABLE` for highest safety, but it may reduce concurrency.

## ✅ Add Retry + Lock Handling in Spring Boot
Here’s a working **Spring Boot** configuration using `@Transactional`, `@Retryable`, and JPA optimistic locking:

### 🧱 Entity with Optimistic Locking
```java
@Entity
public class Account {

    @Id
    private Long id;

    private BigDecimal balance;

    @Version
    private int version;

    // getters/setters
}
```

### 🔁 Service with Retry
```java
@Service
@RequiredArgsConstructor
@EnableRetry
public class TransferService {

    private final AccountRepository accountRepository;

    @Retryable(
        value = { ObjectOptimisticLockingFailureException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 200)
    )
    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        accountRepository.save(from);
        accountRepository.save(to);
    }

    @Recover
    public void recover(ObjectOptimisticLockingFailureException ex, Long fromId, Long toId, BigDecimal amount) {
        log.warn("Failed to transfer after retries. Manual intervention may be required.");
    }
}
```

💡 Make sure to add:
```xml
<dependency>
  <groupId>org.springframework.retry</groupId>
  <artifactId>spring-retry</artifactId>
</dependency>
```

And:
```java
@SpringBootApplication
@EnableRetry
public class BankingApplication { }
```

### ✅ Summary
| Component                     | Purpose                                 |
| ----------------------------- | --------------------------------------- |
| `@Version`                    | Detect concurrent updates (optimistic)  |
| `@Retryable` + `@Recover`     | Handle retries for concurrency errors   |
| `FOR UPDATE`                  | Lock rows during transfer (pessimistic) |
| `SERIALIZABLE` / `REPEATABLE` | Prevent read anomalies / phantom rows   |

