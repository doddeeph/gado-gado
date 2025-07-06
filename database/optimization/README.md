# Database Optimization

## ✅ 1. Indexing (B-tree, Hash, GIN, etc.)
**What it is**:

Think of an index like the **index in a book**. Instead of scanning every page to find a topic, you look at the index, which points you to the exact page.
In databases, an index allows the system to quickly **locate rows** matching a query condition **without scanning the entire table**.
Indexes allow faster lookups by avoiding full table scans.

**Types**:
* B-tree (default): Efficient for range and equality queries.
* Hash index: For fast equality lookups (not range).
* GIN (Generalized Inverted Index): Used in PostgreSQL for full-text search, JSONB fields.
* Composite index: Multi-column indexes (e.g., (`user_id`, `created_at`)).

**Best Practices**:
Index columns used in `WHERE`, `JOIN`, `ORDER BY`.
Monitor with `EXPLAIN ANALYZE`.
Avoid over-indexing (slows down inserts/updates).

## ✅ 2. Query Optimization
**What it is**:

Improving SQL queries themselves for better performance.

**Techniques**:
* Avoid `SELECT *`: Fetch only required columns.
* Use `EXISTS` instead of `IN` for subqueries.
* Batch inserts/updates: Avoid N+1 queries.
* Use `LIMIT` and pagination for large result sets.
* Avoid functions in WHERE clause: They break index usage.

## ✅ 3. Connection Pooling
**What it is**:

Reuse DB connections to reduce latency and overhead.

**Tools**:
* HikariCP (Java), pgBouncer (PostgreSQL), JDBC pool
* Useful in high-concurrency applications

## ✅ 4. Denormalization
**What it is**:

Store redundant data to avoid expensive joins.

**Use case**:
* Read-heavy applications (e.g., dashboard with aggregates).
* Example: Store `user_name` in `orders` table instead of joining users.

**Trade-off**:
* Data duplication
* Need for sync on update (may need triggers or eventual consistency)

## ✅ 5. Data Archiving & Purging
**What it is**:

Move or delete old/rarely used data to reduce table size.

**Techniques**:
* Archive to cold storage or separate table.
* Schedule regular purging (e.g., logs older than 1 year).

## ✅ 6. Table Partitioning (Horizontal/Vertical)
Covered before, but worth noting advanced types:
* **Range Partitioning**: e.g., `created_at BETWEEN date1 AND date2`
* **List Partitioning**: e.g., based on country or category
* **Composite Partitioning**: combine list and range

## ✅ 7. Caching Query Results
Instead of always hitting the DB:
* Use **Redis**, **Memcached**, or **in-memory LRU cache**.
* Combine with **side caching**, **write-through**, or **read-through**.

## ✅ 8. Materialized Views with Incremental Refresh
**What it is**:

Precomputed views refreshed incrementally, not full refreshes.
* Supported in **PostgreSQL 14+**, **Oracle**, **BigQuery**.
* Efficient for real-time analytics dashboards.

## ✅ 9. Read Replicas
**What it is**:

Clone the database for **read-only** traffic to offload primary.

**Use case**:
* Large-scale apps
* Analytics queries
* Reporting

**Tools**:
* PostgreSQL replication
* AWS RDS Read Replicas

## ✅ 10. Columnar Databases (for analytics)
**What it is**:

Store data by column instead of row for massive analytical speedup.

**Use case**:
* BI tools, dashboards, time-series, big aggregates.

Tools:
* **ClickHouse**, **Amazon Redshift**, **Google BigQuery**, **Apache Druid**, **TimescaleDB**

### Summary
| Optimization Technique | Best For                    |
| ---------------------- | --------------------------- |
| Indexing               | Fast lookup, joins, sorting |
| Query tuning           | Performance bottlenecks     |
| Connection pooling     | High concurrency            |
| Denormalization        | Read-heavy systems          |
| Archiving & purging    | Huge tables, cold data      |
| Partitioning           | Scalability and pruning     |
| Caching                | Reducing DB load            |
| Materialized views     | Precomputed aggregations    |
| Read replicas          | Scale read operations       |
| Columnar DBs           | Big data analytics          |


## ⚙️ A. Partitioning / Sharding
Split large tables into smaller, manageable parts to improve performance and scalability.

| Type                                   | Description                           | Use Case                     |
| -------------------------------------- | ------------------------------------- | ---------------------------- |
| **Horizontal Partitioning (Sharding)** | Split rows across databases or tables | User data by `user_id`       |
| **Vertical Partitioning**              | Split columns into multiple tables    | Large column blobs separated |
| **Directory-based Sharding**           | Use metadata table to locate shards   | Useful for dynamic shards    |

🛠 In practice:
* You choose a sharding key (e.g., user ID).
* Each shard is a separate DB instance or table.
* Use consistent hashing or modulo (e.g., `user_id % N`).

## 📄 B. Materialized Views
* Precomputed views of complex joins or aggregations.
* Useful for reporting, dashboards, or slow queries.
* Must be refreshed periodically (or triggered by changes).
```
CREATE MATERIALIZED VIEW monthly_sales_summary AS
SELECT product_id, SUM(sales) FROM sales GROUP BY product_id;
```

## 🔍 C. Indexing
* Speeds up read queries.
* Use composite indexes for multi-column search.
* Beware: too many indexes slow down writes.

## 📦 D. Denormalization
* Store redundant data to avoid joins.
* Used in high-read systems for performance.

# Real-world Database Optimization Examples
Below are real-world database optimization examples using:

## ✅ 1. Partitioning – PostgreSQL / MySQL
![Partitioning Strategy – Orders Table.png](Partitioning%20Strategy%20%E2%80%93%20Orders%20Table.png)
💡 **Use Case**: 

E-commerce orders table grows to billions of rows.

💥 **Problem**: 

Slow queries when filtering by `created_at`.

✅ **Solution**: 

Use range partitioning by month on `created_at`.

🔧 **SQL Example** (PostgreSQL):
```
CREATE TABLE orders (
id BIGINT,
customer_id BIGINT,
amount NUMERIC,
created_at DATE
) PARTITION BY RANGE (created_at);

CREATE TABLE orders_2024_01 PARTITION OF orders
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE orders_2024_02 PARTITION OF orders
FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');
```

Question:
* orders table already creates, what should do to partition orders table by created_at?
* how partition table with multiple columns?

📈 Impact:
* Query scanning January only hits just one partition.
* Dramatically reduced **I/O** and **scan cost**.

## ✅ 2. Materialized Views – _Analytics Dashboards_
![Materialized View – Precompute Analytics.png](Materialized%20View%20%E2%80%93%20Precompute%20Analytics.png)
💡 **Use Case**:

Generating a report for **daily revenue per product** from millions of rows.

💥 **Problem**:

Expensive JOIN + GROUP BY query every dashboard refresh.

✅ **Solution**:

Precompute with a **materialized view**, **refresh periodically**.

🔧 **SQL Example**:
```
CREATE MATERIALIZED VIEW daily_revenue AS
SELECT
product_id,
DATE(order_date) AS day,
SUM(amount) AS total
FROM orders
GROUP BY product_id, DATE(order_date);
```
```
-- Refresh manually or on schedule
REFRESH MATERIALIZED VIEW daily_revenue;
```

📈 **Impact**:
* 10x+ faster dashboard loading.
* Frees up transactional DB.

## ✅ 3. Indexing – _User Lookup by Email_
![Indexing – Email Lookup Optimization.png](Indexing%20%E2%80%93%20Email%20Lookup%20Optimization.png)
💡 **Use Case**:

Login endpoint filters users table by email.

💥 **Problem**:

```sql
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  name TEXT,
  email TEXT,
  created_at TIMESTAMP
);
```
```sql
SELECT * FROM users WHERE email = 'john@example.com';
```
Without an index on `email`, the database does a **sequential scan** — checks **every row**.
Full table scan for every login attempt.

✅ **Solution**:

Add **B-Tree** index on `email`.

🔧 **SQL Example**:
```
CREATE INDEX idx_users_email ON users(email);
```

📈 **Impact**:
* Lookup becomes **O(log N)**.
* Drastically faster login and auth queries.

## ✅ 4. Redis Locking for Transactions – _Prevent Double Booking_
![Redis Distributed Lock – Booking Flow.png](Redis%20Distributed%20Lock%20%E2%80%93%20Booking%20Flow.png)
💡 **Use Case**:

Hotel booking system — prevent two users booking the same room.

💥 **Problem**:

Race condition between concurrent transactions.

✅ **Solution**:

Use Redis-based distributed lock.

🔧 **Pseudo-Code (Java / RedisTemplate)**:
```
boolean locked = redisTemplate.opsForValue().setIfAbsent("lock:room:123", "LOCKED", Duration.ofSeconds(10));
if (locked) {
    // Check room availability in DB
    // If available, book it
    // Release Redis lock
    redisTemplate.delete("lock:room:123");
} else {
    throw new RuntimeException("Room is being booked by someone else.");
}
```

📈 Impact:
* Prevents **overbooking** under concurrency.
* Enables safe distributed coordination.

## ✅ 5. Query Optimization via EXPLAIN – Detect Bad Plans
💡 **Use Case**:

Random slow report query.

💥 **Problem**:

Planner doing **nested loop join** over millions of rows.

✅ **Solution**:

Use `EXPLAIN ANALYZE` to inspect and fix.

🔧 **Example**:
```
EXPLAIN ANALYZE
SELECT u.name, o.amount
FROM users u
JOIN orders o ON o.user_id = u.id
WHERE o.created_at > CURRENT_DATE - INTERVAL '30 days';
```

Fix:
* Add **index** on `orders(user_id, created_at)`.
* Rewrite query using subquery pushdown.

## ✅ 6. Read Replicas – Scale Read Queries
![Read Replicas – Read or Write Split Architecture.png](Read%20Replicas%20%E2%80%93%20Read%20or%20Write%20Split%20Architecture.png)
💡 **Use Case**:

Heavy read traffic on users and orders.

✅ **Solution**:

Set up **PostgreSQL/MySQL read replicas**.
Route **reads to replicas**, **writes to primary**.

```
// In Spring Boot using DataSourceRouting
if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
    return readReplicaDataSource;
} else {
    return primaryDataSource;
}
```

📈 Impact:
* Offloads 80% of traffic from master.
* Horizontal read scalability.


# SQL Query EXPLAIN
![SQL Query Execution Plan (EXPLAIN Visualization).png](SQL%20Query%20Execution%20Plan%20%28EXPLAIN%20Visualization%29.png)

## 🔍 What It Shows:
* SQL request goes through the **Query Planner**
* **Optimizer** evaluates `indexes`, `filters`, and `join strategies`
* **Execution Engine** runs the physical plan
* Results are returned to the developer or tool (e.g., pgAdmin, MySQL Workbench)

## ✅ 1. Real-World EXPLAIN Examples
Let’s assume we have a PostgreSQL table:
```
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    customer_id INT,
    amount DECIMAL,
    status VARCHAR,
    created_at TIMESTAMP
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
```
### 🔍 Basic Query
```
EXPLAIN SELECT * FROM orders WHERE customer_id = 42;
```

### 🔎 Output (PostgreSQL)
```
Index Scan using idx_orders_customer_id on orders  (cost=0.29..8.50 rows=1 width=64)
    Index Cond: (customer_id = 42)
```

### 🧠 Explanation
* Index Scan: PostgreSQL uses idx_orders_customer_id
* cost=0.29..8.50: Estimated time to start and complete the query
* rows=1: Estimated number of matching rows

## ✅ 2. Complex Query Tree
### SQL
```
EXPLAIN SELECT o.id, c.name
FROM orders o
JOIN customers c ON o.customer_id = c.id
WHERE o.status = 'paid'
ORDER BY o.created_at DESC;
```

### Output
```
Sort  (cost=105.43..105.44 rows=1)
    Sort Key: o.created_at DESC
    -> Nested Loop  (cost=0.84..105.42 rows=1)
        -> Index Scan using idx_orders_status on orders o  (cost=0.42..8.44 rows=1)
            Index Cond: (status = 'paid')
        -> Index Scan using customers_pkey on customers c  (cost=0.42..96.93 rows=1)
            Index Cond: (c.id = o.customer_id)
```
![Query Execution Tree.png](Query%20Execution%20Tree.png)

### 🧠 Breakdown
| Step            | Meaning                                   |
| --------------- | ----------------------------------------- |
| **Sort**        | Performs `ORDER BY created_at DESC`       |
| **Nested Loop** | Joins `orders` and `customers` row by row |
| **Index Scan**  | Both tables use indexes for fast lookup   |

## ✅ 3. PostgreSQL EXPLAIN ANALYZE Example
### SQL
```
EXPLAIN ANALYZE
SELECT o.id, c.name
FROM orders o
JOIN customers c ON o.customer_id = c.id
WHERE o.status = 'paid'
ORDER BY o.created_at DESC;
```
### 🧪 Sample Output (shortened)
```
Sort  (actual time=0.168..0.169 rows=10 loops=1)
    Sort Key: o.created_at DESC
    Sort Method: quicksort  Memory: 25kB
    -> Nested Loop  (actual time=0.035..0.130 rows=10 loops=1)
        -> Index Scan using idx_orders_status on orders o  
            (actual time=0.015..0.030 rows=10 loops=1)
                Index Cond: (status = 'paid')
        -> Index Scan using customers_pkey on customers c  
            (actual time=0.005..0.006 rows=1 loops=10)
                Index Cond: (id = o.customer_id)
Planning Time: 0.220 ms
Execution Time: 0.210 ms
```

### 📌 Highlights:
* `actual time=...` shows real execution timing
* Loop counts: customers table scanned 10 times (once per matched order)
* Memory: indicates sort buffer used

## ✅ 4. Cost-Based vs Rule-Based Optimizer
| Optimizer Type       | Description                                            | Example                          |
| -------------------- | ------------------------------------------------------ | -------------------------------- |
| **Rule-Based (RBO)** | Uses fixed rules like "use index if available"         | Older Oracle versions            |
| **Cost-Based (CBO)** | Chooses best plan using statistics (rows, I/O, memory) | PostgreSQL, MySQL, modern Oracle |

CBO will prefer full scan over index scan if the index is not selective enough.

## ✅ 5. Diagram Match
The PlantUML EXPLAIN diagram from earlier maps to this process:

| Diagram Component         | Real DB Role                                      |
| ------------------------- | ------------------------------------------------- |
| `Query Planner`           | Parses SQL, generates tree                        |
| `Optimizer`               | Chooses plan (e.g., index vs scan)                |
| `Executor`                | Runs physical plan                                |
| `TableScan / Join / Sort` | Real physical operations seen in `EXPLAIN` output |

## ✅ Bonus: Tips for Tuning
* Run `ANALYZE` to update stats.
* Use `EXPLAIN (ANALYZE)` to get actual vs estimated cost.
* Avoid `SELECT *` for large joins.
* Create `composite indexes` for multi-column WHERE clauses.

## ✅ Materialized Views vs Indexing (Bonus Use Case)
### 🔍 Use Case: Heavy daily report query
```
SELECT customer_id, SUM(amount)
FROM orders
WHERE created_at >= current_date - interval '1 day'
GROUP BY customer_id;
```

### Options:
| Option                    | Pros                   | Cons                              |
| ------------------------- | ---------------------- | --------------------------------- |
| **Index on `created_at`** | Fast scan              | Still recalculated                |
| **Materialized View**     | Pre-aggregated results | Needs `REFRESH MATERIALIZED VIEW` |
| **Partitioned Table**     | Isolates today’s data  | More complex maintenance          |

## ✅ Window Function Query Plan Example
```
EXPLAIN SELECT id, amount,
RANK() OVER (PARTITION BY customer_id ORDER BY amount DESC)
FROM orders;
```
Output Summary:
```
WindowAgg
-> Sort (customer_id, amount DESC)
-> Seq Scan on 
```
Meaning:
* Sort needed per partition
* Seq Scan if no useful index
* WindowAgg is Postgres’ internal operator for RANK, DENSE_RANK, etc.


# Combined Diagram
Here's a combined PlantUML architecture diagram that includes all the requested database optimization strategies, including:

## Query Caching
![Query Caching – Cache-Aside Pattern.png](Query%20Caching%20%E2%80%93%20Cache-Aside%20Pattern.png)

## Sharding vs Partitioning
![Sharding vs Partitioning.png](Sharding%20vs%20Partitioning.png)

## Time-Series DB with InfluxDB
![Time-Series DB Optimization (InfluxDB).png](Time-Series%20DB%20Optimization%20%28InfluxDB%29.png)

## Diagram Sections Explained
| Section         | Strategy                 | Use Case                                    |
| --------------- | ------------------------ | ------------------------------------------- |
| 🧠 Query Cache  | Redis/Memcached          | Reduce repeated reads, cache SELECT results |
| 🔀 Sharding     | App-controlled DB split  | User base split across multiple DBs         |
| 🧩 Partitioning | DB-controlled split      | One large table, divided by date/key        |
| 📈 InfluxDB     | Time-series optimized DB | IoT metrics, logs, CPU usage                |

# E-Commerce Platform with Sharded Architecture
Let's design a Live System Architecture for a sharded e-commerce platform using the database optimization strategies discussed earlier — especially Sharding, Caching, and Redis Locking.

## 🛒 Use Case: E-Commerce Platform with Sharded Architecture
🧩 **Key Features**
* Millions of users
* High read/write throughput
* Product browsing, shopping cart, order processing
* Sharded user & order data
* Redis caching and locking
* Read replicas for analytics

## 📘 Architecture Diagram (Component View)
![Sharded E-Commerce Architecture (Component View).png](Sharded%20E-Commerce%20Architecture%20%28Component%20View%29.png)



## ⚙️ Optimization Strategies Used
| Layer     | Optimization                            |
| --------- | --------------------------------------- |
| DB        | **Sharding**, **Read Replicas**         |
| App       | **Redis Cache**, **Redis Lock (SETNX)** |
| Queue     | **Kafka** for async background jobs     |
| Analytics | Separate DB for BI reads                |

