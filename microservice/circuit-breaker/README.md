# Circuit Breaker Pattern
Let’s dive into how the **Circuit Breaker** pattern works in a microservices architecture using Spring Boot, especially with **Resilience4j**, a popular lightweight fault-tolerance library.

## 🧱 Microservices Architecture with Circuit Breaker
In a microservices system:
- **Service A** depends on **Service B** (e.g., a product service calls an inventory service).
- If Service B is slow or down, Service A could be blocked.
- **Circuit Breaker** helps **Service A avoid repeated calls to the failing Service B**.

## 🔄 Request Flow with Circuit Breaker
```            
┌──────────────────┐       HTTP Request        ┌────────────────────┐
│  Product Service │ ────────────────────────▶ │  Inventory Service │
│  (Port 8080)     │                           │  (Port 8081)       │
└──────────────────┘                           └────────────────────┘
        │
        │
        ▼
┌────────────────────────────────────────────────────┐
│ Resilience4j Circuit Breaker                       │
│ - Monitors failures                                │
│ - Opens if >50% fail (configurable)                │
│ - Short-circuits failed calls (skip call Inventory)│
│ - Fallback: return “Unavailable”                   │
└────────────────────────────────────────────────────┘
```

## 🔧 Spring Boot Example Using Resilience4j
**Step 1**: Add Dependencies (Maven)
```
<dependency>
  <groupId>io.github.resilience4j</groupId>
  <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

**Step 2**: Configure Circuit Breaker in `application.yml`
```
resilience4j:
    circuitbreaker:
        instances:
            inventoryService:
                registerHealthIndicator: true
                slidingWindowSize: 5
                minimumNumberOfCalls: 5
                failureRateThreshold: 50
                waitDurationInOpenState: 10s
                permittedNumberOfCallsInHalfOpenState: 2
                automaticTransitionFromOpenToHalfOpenEnabled: true
```

**Step 3**: Use `@CircuitBreaker` Annotation
```
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/{id}")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "inventoryFallback")
    public String getProductWithInventory(@PathVariable String id) {
        // Calls an external inventory service
        return restTemplate.getForObject("http://inventory-service/inventory/" + id, String.class);
    }

    public String inventoryFallback(String id, Throwable throwable) {
        return "Inventory service is unavailable. Please try again later.";
    }
}
```

## 🔍 Behavior in Real Usage
| Scenario                         | Circuit Breaker Action                             |
| -------------------------------- | -------------------------------------------------- |
| A few failures                   | Monitors, stays in **Closed**                      |
| Failure rate > 50% (e.g., 3/5)   | Transitions to **Open**, blocks calls to inventory |
| After 10s (configured wait time) | Moves to **Half-Open**, allows limited test calls  |
| Test calls succeed               | Transitions back to **Closed**                     |
| Test calls fail                  | Back to **Open** again                             |

## ✅ Benefits
- Fast failure instead of waiting for timeouts.
- Prevents overloading downstream services.
- Fallbacks ensure user-friendly degradation.

## 📊 Optional: Add Metrics & Dashboard
Add this dependency for Spring Boot Actuator support:
```
<dependency>
  <groupId>io.github.resilience4j</groupId>
  <artifactId>resilience4j-micrometer</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Use `/actuator/health` and `/actuator/circuitbreakerevents` endpoints to monitor circuit breaker status.
