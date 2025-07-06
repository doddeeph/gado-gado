# Caching Strategies
Caching is a fundamental part of scalable system design and choosing the right strategy can significantly affect performance, consistency, and complexity. Caching is used to reduce load on backend systems and improve response time. Common strategies include:

## 🧠 A. Caching Layers
* **Client-side cache**: e.g., in browser (cookies/localStorage), app memory.
* **CDN (edge caching)**: e.g., Cloudflare, Akamai to cache static content near users.
* **Reverse proxy cache**: e.g., Varnish, NGINX for caching rendered pages.
* **Application-level cache**: e.g., Redis or Memcached for DB query results, computations.

## ⚙️ B. Caching Patterns
| Pattern                        | Description                                                              | Example                                                   |
| ------------------------------ | ------------------------------------------------------------------------ | --------------------------------------------------------- |
| **Read-Through**               | Cache sits in front of DB. On cache miss, fetch from DB, store in cache. | `cache.get(key)` → DB → `cache.set(key, value)`           |
| **Write-Through**              | Write goes to both cache and DB. Ensures cache is always up-to-date.     | `cache.set(key, value)` + `db.save(key, value)`           |
| **Write-Behind / Write-Back**  | Writes go to cache, then asynchronously written to DB.                   | High throughput, but risk of data loss.                   |
| **Cache-Aside (Lazy Loading)** | App reads from cache, on miss loads from DB and caches it.               | Manual caching, common pattern.                           |
| **TTL-based Expiry**           | Set expiration for each cached entry.                                    | Avoids stale data. E.g., `cache.set(key, value, ttl=60s)` |
| **Eviction Policies**          | LRU (Least Recently Used), LFU, FIFO                                     | Helps manage cache memory limits.                         |

### ✅ 1. Read-Through
![Read-Through Caching Strategy.png](Read-Through%20Caching%20Strategy.png)

🔧 **How it works**:
* Application only talks to the cache.
* The cache is configured to automatically load from DB on a miss (i.e., the cache is smart).

🧠 **Pros**:
* Centralized cache logic, consistent access pattern.
* Simplifies application code.

⚠️ **Cons**:
* Needs support from caching layer (like Spring Cache with @Cacheable, or tools like EHCache).
* May not handle writes well unless paired with write-through.

📌 **Use case**:
* Data grids, application-level cache frameworks.

### ✅ 2. Write-Trough
![Write-Through Caching Strategy.png](Write-Through%20Caching%20Strategy.png)

🔧 **How it works**:
* All writes go through the cache, which writes to the DB as part of the same operation.
* Cache is always consistent with DB.

🧠 **Pros**:
* High consistency between DB and cache.
* Ideal for low-latency reads and writes.

⚠️ **Cons**:
* Slower writes (because you update two systems).
* Cache becomes a critical dependency.

📌 **Use case**:
* Financial data, user metadata, where reads and writes are frequent and must be consistent.

### ✅ 3. Write-Behind / Write-Back
![Write-Back (Write-Behind) Caching Strategy.png](Write-Back%20%28Write-Behind%29%20Caching%20Strategy.png)

🔧 **How it works**:
* The application writes to the cache only.
* The cache batches writes and flushes to DB asynchronously.

🧠 **Pros**:
* Very fast writes.
* Can batch writes efficiently.

⚠️ **Cons**:
* Risk of data loss if the cache crashes before flush.
* More complex to implement.

📌 **Use case**:
* Logging systems, metrics, temporary data where eventual consistency is acceptable.

### ✅ 4. Cache-Aside (Lazy Loading)
![Cache-Aside Strategy (Lazy Loading).png](Cache-Aside%20Strategy%20%28Lazy%20Loading%29.png)

🔧 **How it works**:
* The application first checks the cache.
* If a cache miss, it fetches from DB, then stores it into the cache for next time.
* Cache is explicitly managed by the app.

🧠 **Pros**:
* Simple logic, works with any cache store (e.g. Redis).
* Good for read-heavy apps.

⚠️ **Cons**:
* Race conditions on concurrent cache misses.
* Stale cache if not properly invalidated on writes.

📌 **Use case**:
Product details, user profiles, catalog pages.

### ✅ 5. TTL-Based Cache Expiration
![TTL-Based Cache Expiration.png](TTL-Based%20Cache%20Expiration.png)

🔧 **How it works**:
* Cache entries have an expiration time (TTL).
* After TTL, the data is evicted and fetched fresh on next access.

🧠 **Pros**:
* Automatic cleanup of stale data.
* Good balance between freshness and performance.

⚠️ **Cons**:
* Risk of stale data just before expiry.
* Cache miss spike when many entries expire at once (cache stampede).

📌 **Use case**:
* API responses, stock prices, exchange rates, weather data.

### ✅ 6. Eviction Policy (e.g., LRU / LFU)
![Cache Eviction Policy (LRU Example).png](Cache%20Eviction%20Policy%20%28LRU%20Example%29.png)

🔧 **How it works**:
* Cache keeps only most recent (LRU = Least Recently Used) or most frequently used (LFU) items.
* Old entries evicted when cache is full.

🧠 **Pros**:
* Helps fit cache within memory limits.
* Self-managing under heavy load.

⚠️ **Cons**:
* No time control; might evict fresh data under pressure.
* Cache misses for cold items during peak load.

📌 **Use case**:
* In-memory caches (e.g., Caffeine, Guava), Redis with `maxmemory-policy`.

### ✅ 7. Refresh-Ahead Caching
![Refresh-Ahead Caching.png](Refresh-Ahead%20Caching.png)

🔧 **How it works**:
* Instead of waiting for TTL to expire, cache proactively refreshes the value before it expires.
* Ensures hot keys stay warm.

🧠 **Pros**:
* Avoids cache stampede.
* Keeps frequently accessed data fresh.

⚠️ **Cons**:
* More complex logic.
* Might waste resources on refreshing rarely-used keys.

📌 **Use case**:
* High-throughput APIs or hot keys with high read latency.

### ✅ 8. Side Cache (App-Controlled)
![Side Cache (App-Controlled).png](Side%20Cache%20%28App-Controlled%29.png)

🔧 **How it works**:
* Similar to Cache-Aside, but used more generally across the app for custom logic:
  * Token data
  * Rate limiting
  * Temporary storage

🧠 **Pros**:
* Total control by the app.
* Can mix different cache strategies.

⚠️ **Cons**:
* Can become complex and hard to maintain.
* Risk of inconsistent cache logic across the codebase.

📌 **Use case**:
* Microservices storing session metadata, auth tokens, or feature flags.

## 📊 Comparison Table
| Strategy         | App Fetches        | Cache Writes | TTL/Expiry          | Use Case                                |
| ---------------- | ------------------ | ------------ | ------------------- | --------------------------------------- |
| Cache-Aside      | App -> DB on miss  | App          | Optional            | Read-heavy apps                         |
| Read-Through     | Cache handles      | Cache        | Yes                 | Spring cache abstraction                |
| Write-Through    | App -> Cache -> DB | Cache auto   | Optional            | Must-write-consistent systems           |
| Write-Behind     | App -> Cache only  | Cache async  | Optional            | High-write/low-consistency needs        |
| TTL Expiry       | Any                | Any          | Yes                 | Periodic refresh, APIs                  |
| LRU/LFU Eviction | Any                | Any          | No (capacity-based) | Memory-constrained cache                |
| Refresh-Ahead    | App or cache       | Cache        | Yes                 | Hotspot keys, dashboard metrics         |
| Side Caching     | App manual         | App manual   | Optional            | Session, rate-limiting, temporary state |

## 🧠 How to Choose
| Scenario                         | Recommended Strategy          |
| -------------------------------- | ----------------------------- |
| Basic read optimization          | Cache-Aside                   |
| Centralized cache logic          | Read-Through                  |
| High consistency for writes      | Write-Through                 |
| Massive write throughput         | Write-Behind (w/ safety nets) |
| Frequently changing small data   | TTL-based w/ short expiry     |
| Cache hot keys under pressure    | Refresh-Ahead + LRU           |
| Custom distributed caching logic | Side Cache                    |

## Code Examples
### ✅ Java – Spring Boot with Spring Cache + RedisTemplate
[java_redis_cache_demo.java](java_redis_cache_demo.java)

### ✅ Go – Using Go-Redis (github.com/redis/go-redis/v9)
[go_redis_cache_demo.go](go_redis_cache_demo.go)

### 🔍 Summary
| Feature          | Java (Spring + RedisTemplate)       | Go (Go-Redis)                    |
| ---------------- | ----------------------------------- | -------------------------------- |
| Read Cache-Aside | `redisTemplate.opsForValue().get()` | `rdb.Get(...)`                   |
| Write-Through    | Manual save to DB and Redis         | Not shown — use Set then persist |
| Invalidate Cache | `redisTemplate.delete(...)`         | `rdb.Del(...)`                   |
| TTL Support      | `Duration.ofMinutes(10)`            | `10 * time.Minute`               |
