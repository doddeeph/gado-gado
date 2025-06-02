# Rate Limiting
Rate limiting is a **critical technique** to prevent abuse, control traffic, and protect backend systems from overuse. It ensures that a client (`by IP or user`) can only make **a limited number of requests in a defined time window**.

## ✅ Common Rate Limiting Strategies
| Strategy           | Description                                         | Example                         |
| ------------------ | --------------------------------------------------- | ------------------------------- |
| **Fixed Window**   | Count resets every fixed time period                | 100 requests per minute         |
| **Sliding Window** | More accurate, uses a sliding time window           | 100 requests in last 60 seconds |
| **Token Bucket**   | Tokens refill over time, user spends token per call | 1 token/sec, burst up to 10     |
| **Leaky Bucket**   | Queue-like, leaks at constant rate                  | Smoother flow, handles bursts   |

## 📍 Rate Limiting by IP vs. by User
| Scope          | Use Case                            | Example                                |
| -------------- | ----------------------------------- | -------------------------------------- |
| **IP Address** | Prevent abuse from bots/spam/DDOS   | 100 requests/IP per minute             |
| **User ID**    | Per-user quota or API usage control | Free users: 1000 req/day, Premium: 10k |

## 💡 How It Works: Step by Step
1. Client Makes Request
E.g., `GET /api/products`

2. Identify Client
You extract:
* **User ID** (from JWT/session)
* IP Address (`X-Forwarded-For`, `RemoteAddr`)

3. Check Limit Store
Query from cache/db:
```
KEY: rate_limit:user:123 OR rate_limit:ip:192.168.1.10
VALUE: { count: 95, expiresAt: 60s }
```

4. Allow or Reject
If limit not exceeded:
* Increment counter
* Proceed

If exceeded:
* Return 429 Too Many Requests + Retry-After header

## 🏗️ Where to Implement It
| Layer           | Tool/Lib                          | Use Case                            |
| --------------- | --------------------------------- | ----------------------------------- |
| **API Gateway** | Kong, NGINX, AWS API Gateway      | Global rate limits                  |
| **Spring Boot** | Spring filters, `Bucket4j`, Redis | Fine-grained, user-specific control |
| **Database**    | PostgreSQL/Mongo w/ TTLs          | If Redis not available (slower)     |

## 🛠 Example: Spring Boot + Redis + Bucket4j
```
@Bean
public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
    FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new RateLimitFilter(redisRateLimiter()));
    return registration;
}
```

Redis Key Example
```
rate_limit:user:USER123
rate_limit:ip:192.168.0.1
```
TTL = 60s, Value = Counter

## 📦 Redis Lua Script (Atomic Increment + Expire)
```
local key = KEYS[1]
local limit = tonumber(ARGV[1])
local current = redis.call("INCR", key)

if current == 1 then
  redis.call("EXPIRE", key, ARGV[2])
end

if current > limit then
  return 0
end
return 1
```

## 📊 Headers Example (RFC Standard)
```
HTTP/1.1 429 Too Many Requests
Retry-After: 60
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
```

## ✅ Summary
| Concept             | Description                                       |
| ------------------- | ------------------------------------------------- |
| IP-based limiting   | Controls abuse from anonymous traffic             |
| User-based limiting | Fair use, role-based quotas                       |
| Backed by           | Redis, in-memory caches, or rate-limit middleware |
| Tools               | Bucket4j, Spring Cloud Gateway, NGINX, Envoy      |

