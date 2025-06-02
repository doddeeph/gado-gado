# Load Balancer
A **Load Balancer** in the context of **Spring Cloud** is a critical component used in **microservices architectures** to distribute incoming traffic across multiple instances of services to ensure **high availability**, **reliability**, and **scalability**.
Spring Cloud provides multiple ways to implement load balancing. The most common approaches include:

## 🧩 1. Spring Cloud LoadBalancer
Since Spring Cloud 2020+, **Spring Cloud LoadBalancer** is the default client-side load balancer replacing **Netflix Ribbon**, which is now deprecated.

### ✅ Key Features:
* Client-side load balancing (like Ribbon, but modern and lighter)
* Integration with **Spring Cloud Discovery Client** (e.g., Eureka, Consul)
* Supports different strategies (Round Robin, Random, etc.)

### ✅ Basic Flow:
```
Client → LoadBalancer → Service Registry (Eureka) → Target Service Instance
```

## 🛠️ How to Use Spring Cloud LoadBalancer
### 🧱 1. Add dependencies (Maven):
```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>

<!-- If using Eureka -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### 🔧 2. Enable Service Discovery (e.g., Eureka)
```
@SpringBootApplication
@EnableDiscoveryClient
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 💡 3. Use @LoadBalanced RestTemplate or WebClient
RestTemplate Example:
```
@Bean
@LoadBalanced
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

Call another service:
```
@Autowired
private RestTemplate restTemplate;

public String getProductDetails() {
    return restTemplate.getForObject("http://product-service/api/products", String.class);
}
```

Here, `product-service` is the service name registered in Eureka/Consul, and Spring Cloud LoadBalancer chooses an instance to call.

## ⚙️ Load Balancing Strategies
Spring Cloud LoadBalancer uses `Round-Robin` by default. You can customize it by defining a `ReactorServiceInstanceLoadBalancer`.
```
@Bean
ReactorServiceInstanceLoadBalancer loadBalancer(Environment env, LoadBalancerClientFactory factory) {
    String name = env.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
    return new RandomLoadBalancer(factory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
}
```

## 🌐 If You Use Spring Cloud Gateway
It automatically uses Spring Cloud LoadBalancer when you define routes like:
```
spring:
    cloud:
        gateway:
            routes:
                - id: product_route
                    uri: lb://product-service
                    predicates:
                        - Path=/products/**
```

Here, `lb://` tells Gateway to use load balancing.

## 📦 Summary Table
| Component                 | Purpose                                 |
| ------------------------- | --------------------------------------- |
| `@LoadBalanced`           | Annotates RestTemplate/WebClient for LB |
| Spring Cloud LoadBalancer | Core LB library (client-side)           |
| Eureka/Consul             | Service discovery                       |
| Spring Cloud Gateway      | API Gateway with built-in LB support    |
| Round Robin / Random      | Load balancing strategies               |

## 🚀 Benefits of Load Balancing with Spring Cloud
* High Availability: Redirects traffic to healthy instances.
* Scalability: Handles more requests by spreading the load.
* Decoupling: Clients don’t need to know service instance addresses.
* Resilience: Can be extended with circuit breakers (Resilience4j).

