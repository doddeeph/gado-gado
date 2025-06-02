# Real-Time Notification System

## Breakdown Component and Interactions
```
+------------------+          +---------------------+          +------------------+
|                  |          |                     |          |                  |
|  API Gateway     +--------->+ Notification Service +-------->+   Kafka Broker   |
|  (REST / gRPC)   |          |   (Microservice)    |          | (Message Queue)  |
|                  |          |                     |          |                  |
+------------------+          +----------+----------+          +---------+--------+
                                         |                               |
                                         v                               v
                                 +-------------------+          +------------------+
                                 | Notification      |          | Notification     |
                                 | Workers           |          | Workers (scaled) |
                                 | (SMS, Email, Push)|          |                  |
                                 +---------+---------+          +---------+--------+
                                           |                              |
                                           v                              v
                                +----------------------+       +----------------------+
                                | External Providers   |       | External Providers   |
                                | (Twilio, Firebase)   |       | (Twilio, Firebase)   |
                                +----------------------+       +----------------------+

+--------------------+
| Data Store         |
| (PostgreSQL)       |
| Notification       |
| Logs & Metadata    |
+--------------------+

+--------------------+
| Monitoring &       |
| Alerting (Prom/    |
| Grafana)           |
+--------------------+

+--------------------+
| Kubernetes Cluster |
| (Autoscaling,      |
| Deployment)        |
+--------------------+
```

## Walk-through Explanation
1. API Gateway
* Serves as the entry point for all notification requests.
* Accepts requests via REST or gRPC from different services or user interfaces.
* Handles authentication (OAuth/JWT) and rate limiting to protect backend systems.

2. Notification Service
* Core microservice that receives requests and validates them.
* Publishes validated notification tasks to the Kafka broker for asynchronous processing.
* Decouples request intake from the delivery mechanism to improve scalability and reliability.

3. Kafka Broker
* Message queue system that reliably buffers notification tasks.
* Allows scalable consumers (workers) to process notifications asynchronously.
* Handles ordering and replayability of messages, ensuring no notifications are lost.

4. Notification Workers
* Multiple instances of worker microservices consume notification messages from Kafka.
* Each worker is responsible for delivering notifications through a specific channel (SMS, email, push).
* Workers are stateless and can be scaled horizontally based on load, managed by Kubernetes autoscaling.

5. External Providers
* Third-party services integrated for actual notification delivery (e.g., Twilio for SMS, Firebase for push notifications).
* Workers handle communication with these providers, including retry logic on failures.

6. Data Store
* Relational database (like PostgreSQL) stores logs, delivery status, and metadata.
* Used for auditing, retry mechanisms, and analytics.

7. Monitoring & Alerting
* Prometheus collects system metrics like queue length, processing latency, error rates.
* Grafana visualizes metrics for real-time monitoring.
* Alertmanager triggers alerts on anomalies or thresholds.

8. Kubernetes Cluster
* All components run as containerized microservices orchestrated by Kubernetes.
* Ensures high availability, automated deployment, rolling updates, and horizontal scaling.
* Health checks and probes maintain system robustness.

## Additional Notes
* Security: All external communication is encrypted via TLS. API Gateway handles authentication and authorization. Secrets for external provider credentials are managed via Kubernetes Secrets or Vault.
* Reliability: Dead-letter queues capture failed messages for manual review or reprocessing.
* Performance: Autoscaling workers ensure the system handles peak loads seamlessly.
* Extensibility: New notification channels can be added by implementing new worker services without changing the core Notification Service.

## API Gateway in a Notification System
1. Single Entry Point
It acts as the central access point for all clients (mobile, web, other systems) to interact with your backend microservices.
Example: Instead of clients directly calling `/notifications`, `/users`, `/auth`, they only call the API Gateway, which then routes the requests internally.

2. Routing & Load Balancing
The gateway routes requests to the appropriate internal service:
* `/send` → Notification Service
* `/register` → Device Registration Service
* `/subscribe` → User Preference Service
Also handles load balancing across service instances.

3. Security (Authentication & Authorization)
API Gateway can:
* Validate JWT tokens or API keys
* Enforce role-based access controls
* Integrate with OAuth2/OpenID
This offloads security logic from individual services.

4. Rate Limiting & Throttling
To protect the system from abuse or excessive calls (especially for real-time systems), the gateway can apply:
* Per-user rate limits
* Global request throttling
* IP filtering

5. Request Transformation / Aggregation
API Gateway can:

* Modify request/response formats (e.g., JSON to XML)
* Combine data from multiple microservices into one response

E.g., a `/notifications/summary` endpoint could aggregate unread counts, last notification, and user prefs.

6. Observability: Logging & Metrics
It can provide centralized:
* Logging
* Tracing (like Zipkin, Jaeger)
* Monitoring (via Prometheus, Grafana)

7. WebSocket and SSE Handling
For real-time features, the gateway can handle:
* WebSocket upgrade requests
* Server-Sent Events (SSE) connections
* Ensure proper routing to notification streaming service

8. Simplifies Client Logic
The client app (mobile/web) doesn't need to know about:
* Number of services
* Internal URLs
* Authentication logic for each service
API Gateway abstracts all that complexity.

## 📐 In System Design Diagram:
```
         [Mobile App / Web Client]
                    |
                [API Gateway]
                    |
  ------------------------------------------
  |                |                       |
[Auth Service] [Notification Service] [User Pref Service]
                    |
         [Message Queue / Push Provider]

```

## ✅ Why Retry Failed Notifications?
1. External Services Are Unreliable
SMS/Email/Push providers (e.g., Twilio, SendGrid, Firebase) can:
* Temporarily go down
* Throttle requests
* Return intermittent 5xx errors

2. Network Issues
* Transient network failures can occur
* DNS lookup failures, timeouts, etc.

3. Message Durability
In banking, healthcare, and financial services, notification delivery is critical, especially for:
* Fraud alerts
* Password resets
* OTPs (one-time passcodes)

## 🔁 Types of Retry Mechanisms
1. Immediate Retries (In-memory)
* If failure is temporary (like 5xx error), retry after short delay.
* Usually implemented with exponential backoff + jitter.
```
retrySpec = Retry.backoff(3, Duration.ofSeconds(2))
                 .jitter(0.5)
                 .filter(this::isRetryableException);

```
Retry 3 times with increasing delay (2s, 4s, 8s)

2. Delayed/Deferred Retries (Queue-based)
If retrying immediately doesn’t work:
* Move the message to a delayed retry queue (e.g., RabbitMQ delay exchange, Kafka retry topic, AWS SQS DLQ).
* Retry after 5 minutes, 15 minutes, etc.

🛠 Example Queues:
* `sms_retry_queue_5min`
* `email_retry_queue_15min`
* `push_retry_queue_30min`

3. Dead Letter Queue (DLQ)
If all retry attempts fail:
* Move the notification to a DLQ for manual inspection or alerts.
* Useful for investigating failed messages.

## 🔍 Retry Strategy by Channel
| Channel | Retry Needed? | Notes                                             |
| ------- | ------------- | ------------------------------------------------- |
| SMS     | ✅             | High cost, retry cautiously                       |
| Email   | ✅             | Often has higher tolerance for retries            |
| Push    | ✅             | May retry on device reconnect                     |
| In-App  | ✅             | Could be retried in real-time or on next app open |

## 🧠 Best Practices
* ✅ Use idempotency to avoid duplicate sends
* ✅ Add metadata like retry_count, last_attempt_at
* ✅ Log failure reasons for analysis
* ✅ Use circuit breakers to avoid flooding unstable providers
* ✅ Include alerting for too many failed messages

## 📌 Spring Boot Implementation Example
```
@Retryable(
  value = { NotificationException.class },
  maxAttempts = 3,
  backoff = @Backoff(delay = 2000, multiplier = 2))
public void sendEmailNotification(Email email) {
    emailService.send(email);
}
```
Fallback:
```
@Recover
public void recover(NotificationException e, Email email) {
    queueService.enqueueToRetry(email);
}
```

## ✅ Summary
Yes — retrying failed notifications is essential for:
* Ensuring delivery
* Building resilience against transient failures
* Meeting SLAs and user expectations

## 🧠 Why Use PostgreSQL Instead of MongoDB?
### ✅ 1. ACID Transactions and Strong Consistency
PostgreSQL offers full ACID compliance, which is critical in financial and enterprise systems like banking.

* You want to track retry attempts, status updates, and scheduling in a transactional way:
  * Insert notification → schedule retry if failed → log reason
  * All in one transaction ✅

MongoDB is eventually consistent by default (unless using transactions, which are less mature).

### ✅ 2. Complex Queries & Joins
You may need to:
* Query retry status for a user
* Join notifications with user preferences
* Aggregate retry counts per provider

Relational queries like:
```
SELECT u.email, n.status, n.retry_count
FROM notifications n
JOIN users u ON n.user_id = u.id
WHERE n.status = 'FAILED'
```
are straightforward in PostgreSQL, but complex or inefficient in MongoDB.

### ✅ 3. Retry Scheduler Works Better with RDBMS
Assume you use a Spring Scheduler or Quartz job to scan and retry:
```
SELECT * FROM notifications
WHERE status = 'FAILED'
AND next_retry_at <= NOW()
AND retry_count < 5
LIMIT 100;
```

You need:
* Precise control over time-based retry
* Indexes on `status`, `next_retry_at`, `retry_count`
* Optimized locking and transactional updates

PostgreSQL excels at this.

### ✅ 4. Schema Enforcement & Data Integrity
In a banking-grade system:
* You want strict schemas for notification records
* Ensure all fields (user ID, status, provider, timestamps) are present and valid

MongoDB’s schema-less design can lead to:
* Inconsistent fields across records
* Harder validation and reporting

### ✅ 5. Tooling and Ecosystem for Scheduling
Using RDBMS + Spring Data JPA gives you:
* Easy batch processing
* Retry logic with `@Scheduled`, `@Transactional`, `@Retryable`
* Auditing, soft deletes, and temporal tracking

### 📦 Use Case: Retry Scheduler with Failed Status
🧾 Table: `notifications`

| id | user\_id | type  | status | retry\_count | next\_retry\_at     | last\_error      |
| -- | -------- | ----- | ------ | ------------ | ------------------- | ---------------- |
| 1  | 101      | EMAIL | FAILED | 2            | 2024-05-22 10:00:00 | TimeoutException |

### 🕑 Scheduler Code (Spring Boot)
```
@Scheduled(fixedRate = 60000) // every 1 minute
@Transactional
public void retryFailedNotifications() {
List<Notification> failed = notificationRepository
.findFailedReadyForRetry(LocalDateTime.now());

    for (Notification n : failed) {
        try {
            send(n);
            n.setStatus("SENT");
        } catch (Exception ex) {
            n.incrementRetryCount();
            n.setNextRetryAt(LocalDateTime.now().plusMinutes(10));
            n.setLastError(ex.getMessage());
        }
    }
}
```

### 🔄 Summary: PostgreSQL vs MongoDB for Notification + Retry System
| Feature                    | PostgreSQL ✅             | MongoDB ❌              |
| -------------------------- | ------------------------ | ---------------------- |
| ACID Transactions          | ✅ Excellent              | ⚠️ Weaker              |
| Retry Scheduling via Query | ✅ Easy with SQL          | ⚠️ Harder with MongoQL |
| Complex Joins & Reporting  | ✅ Supported              | ❌ Limited              |
| Schema & Validation        | ✅ Strongly typed         | ❌ Optional             |
| Integration with Spring    | ✅ JPA, Batch, Schedulers | ⚠️ Extra configuration |
| Suitable for Banking       | ✅ Yes                    | ❌ Often discouraged    |

