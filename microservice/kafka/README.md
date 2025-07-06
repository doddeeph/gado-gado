# Apache Kafka
Apache **Kafka** is a **distributed event streaming platform** used to build **real-time data pipelines** and **streaming applications**. It is designed to be **scalable**, **fault-tolerant**, **high-throughput**, and **durable**.

## 🔍 What is Kafka?
Kafka is essentially **a message broker** like a publish-subscribe system but designed for handling **large volumes of data** across **distributed systems**. It's often used for:
* Logging & monitoring
* Real-time analytics
* Event sourcing
* Data integration between systems

## ⚙️ Kafka Core Components
| Component              | Description                                                                     |
| ---------------------- | ------------------------------------------------------------------------------- |
| **Producer**           | Sends data (messages) to Kafka topics.                                          |
| **Consumer**           | Subscribes to topics and processes the messages.                                |
| **Topic**              | A category/feed name to which records are published.                            |
| **Partition**          | Topics are split into partitions for parallelism.                               |
| **Broker**             | A Kafka server that stores data and serves clients.                             |
| **Zookeeper** (legacy) | Coordinates and manages Kafka brokers (now being phased out in favor of KRaft). |

## 🔄 How Kafka Works (Flow)
Let’s break it down step by step:
1. **Producer sends a message** to a Kafka topic (e.g., "user-events").
2. The topic is **partitioned** (e.g., partition 0, 1, 2), and Kafka writes the message to one of them (often based on a key).
3. Each **message is appended** to a partition in an immutable, ordered log.
4. Kafka stores the message on disk, **retaining it for a configurable time** (e.g., 7 days), regardless of whether it's consumed.
5. **Consumer(s)** subscribe to the topic and **read messages sequentially** from each partition.
6. Kafka tracks consumer offsets (position in partition), so it knows what’s already been consumed.

## 📌 Key Kafka Concepts
### ✅ Topics and Partitions
* **A topic** is like a log file name.
* Each topic has **multiple partitions** (for scaling).
* Each partition is **ordered** and **immutable**.

### ✅ Offset
* A unique ID per message in a partition.
* Consumers use offsets to keep track of which messages they've read.

### ✅ Consumer Groups
* Multiple consumers can form a **group** to read from a topic in **parallel**.
* Kafka assigns each partition to **one consumer per group** for **load balancing**.

## 📉 Example Use Case: User Activity Tracking
1. A user logs in → the **app (producer)** sends a `login` event to Kafka.
2. Kafka stores it in the `user-activity` topic.
3. A **monitoring service (consumer)** reads from that topic and updates dashboards.
4. An **analytics service (another consumer group)** processes the same topic for behavior patterns.

## 🚀 Kafka Advantages
* High throughput and low latency
* Horizontal scalability
* Persistent and durable logs
* Decouples producers and consumers
* Replayable data (using offsets)

## Example
Let's walk through how Kafka distributes messages when you have:
* 1 Topic
* 3 Partitions: `P0`, `P1`, `P2`
* 2 Consumers in 1 Consumer Group

### 📦 Setup Summary
| Kafka Entity   | Quantity                |
| -------------- | ----------------------- |
| Topic          | 1 (e.g., `user-events`) |
| Partitions     | 3 (`P0`, `P1`, `P2`)    |
| Consumers      | 2 (`C1`, `C2`)          |
| Consumer Group | 1 (e.g., `group-A`)     |

### 🔄 Kafka Partition Assignment
When multiple consumers are part of the **same consumer group**, Kafka **balances partitions among them**.
In your case:
* You have 3 partitions.
* You have 2 consumers (C1 and C2) in 1 consumer group.

Kafka will assign partitions like this:

| Consumer | Partitions Assigned |
| -------- | ------------------- |
| `C1`     | `P0`, `P1`          |
| `C2`     | `P2`                |

✅ **Rule**: A partition can be read by **only one consumer per group** at a time, but a consumer can read from **multiple partitions**.

### 📤 Message Distribution Example
Suppose a producer sends 6 messages (`M1` to `M6`) into the topic.
If the producer:
* Uses **round-robin (no key)**: Kafka distributes across partitions evenly.
* Uses **keyed partitioning**: All messages with the same key go to the same partition.

Assuming round-robin or balanced logic:

| Message | Partition |
| ------- | --------- |
| M1      | P0        |
| M2      | P1        |
| M3      | P2        |
| M4      | P0        |
| M5      | P1        |
| M6      | P2        |

Then:
* `C1` (assigned to `P0`, `P1`) processes: `M1`, `M2`, `M4`, `M5`
* `C2` (assigned to `P2`) processes: `M3`, `M6`

### 📌 What Happens If You Add/Remove Consumers?
* Add a **3rd consumer**: Kafka **rebalances**, and each consumer gets **1 partition**.
* Remove `C2`: Kafka reassigns `P2` to `C1`, so `C1` handles all 3 partitions.

### 🧠 Key Takeaways
* Kafka distributes **partitions**, not individual messages, to consumers.
* **More partitions than consumers** → some consumers get **multiple partitions**.
* **More consumers than partitions** → some consumers remain **idle**.

## 🛒 Real-World Scenario: E-Commerce Order Processing
![Kafka Shared Consumer Group.png](Kafka%20Shared%20Consumer%20Group.png)

Let’s go through a real-world example using Kafka with:
* **Topic**: `order-events`
* **3 partitions**: `P0`, `P1`, `P2`
* **2 consumers**: `Consumer-A`, `Consumer-B`
* **Consumer group**: `order-processor-group`

### 🧩 Components:
* An **e-commerce app** acts as the **producer**.
* Each time a user places an order, the app sends **a message (order event)** to Kafka topic `order-events`.
* Two microservices act as **consumers** to process the orders:
  * `Consumer-A` handles **inventory updates**.
  * `Consumer-B` handles **payment processing**.

But both consumers are **in the same group** (`order-processor-group`) and share the work of processing **all orders**.

### 🔄 Kafka Setup
| Entity         | Detail                     |
| -------------- | -------------------------- |
| Kafka Topic    | `order-events`             |
| Partitions     | `P0`, `P1`, `P2`           |
| Producers      | E-commerce app             |
| Consumers      | `Consumer-A`, `Consumer-B` |
| Consumer Group | `order-processor-group`    |

### 💡 Message Flow Example
🛍️ **Producer sends 6 messages**:
```
Order1, Order2, Order3, Order4, Order5, Order6
```

Kafka distributes them across partitions like this (**round-robin** or **hashing**):

| Order ID | Partition |
| -------- | --------- |
| Order1   | P0        |
| Order2   | P1        |
| Order3   | P2        |
| Order4   | P0        |
| Order5   | P1        |
| Order6   | P2        |

🧠 **Kafka assigns partitions to consumers**:

| Partition | Consumer Assigned |
| --------- | ----------------- |
| P0        | Consumer-A        |
| P1        | Consumer-A        |
| P2        | Consumer-B        |

So now:
* **Consumer-A** reads from `P0` and `P1` → processes `Order1`, `Order2`, `Order4`, `Order5`
* **Consumer-B** reads from `P2` → processes `Order3`, `Order6`

### 🔁 What If You Add a 3rd Consumer?
Add `Consumer-C` to the same group → Kafka rebalances:

| Partition | Consumer Assigned |
| --------- | ----------------- |
| P0        | Consumer-A        |
| P1        | Consumer-B        |
| P2        | Consumer-C        |

Now all **3 consumers** are actively working, and **each one handles one partition**.

### ✅ Benefits of This Architecture
* You can **scale consumers horizontally**.
* Consumers **don’t process duplicate messages** within the same group.
* Messages are **durably stored**, so if one consumer crashes, another takes over.
* You can **replay orders** by resetting offsets.

### ❗ Misunderstanding to Clear Up
In the previous setup, both `Consumer-A` and `Consumer-B` were in the same consumer group (`order-processor-group`). That means:
They are cooperating to process the same workload — i.e., only one of them processes each message.

So if:
* `Consumer-A` gets `P0` and `P1`
* `Consumer-B` gets `P2`

Then:
* `Consumer-A` processes messages in `P0` and `P1` (Order1, Order2, Order4, Order5)
* `Consumer-B` processes messages in `P2` (Order3, Order6)

In this setup, **Consumer-B would NOT see Order1, Order2, Order4, or Order5 at all**.

### 🧠 Correct Way If You Need Both Inventory & Payment Logic
You have two options, depending on the architecture.

#### ✅ Option 1: Use Separate Consumer Group (Recommended for Microservices)
![Kafka Separate Consumer Groups.png](Kafka%20Separate%20Consumer%20Groups.png)

Each service has its own consumer group and listens to the same data (i.e., both services process all orders independently):

Setup:
Topic: `order-events` (still 3 partitions)
Producer sends order events here
**Inventory Service** = `Consumer-A` in group `inventory-group`
**Payment Service** = `Consumer-B` in group `payment-group`

Kafka delivers all messages to both consumers because they are in different consumer groups.

So now:
* `Consumer-A` (Inventory) handles every order
* `Consumer-B` (Payment) also handles every order

Each service gets its own copy of all the events!

#### ✅ Option 2: One Consumer Does All, Then Calls Other Services
![Kafka Event Chaining.png](Kafka%20Event%20Chaining.png)

Let’s say:
* `Consumer-A` reads all order events
* It then:
  * Updates inventory directly
  * Sends an event/message to another Kafka topic: `payment-requests`
* `Consumer-B` (Payment service) listens to `payment-requests`

This is called the event-chaining pattern, or event-driven orchestration.

#### 🔁 Summary Comparison
| Approach                  | Message Duplication | Services Work Independently? | Use Case                               |
| ------------------------- | ------------------- | ---------------------------- | -------------------------------------- |
| Same group                | ❌ No                | ❌ No                         | Load-balanced processing               |
| Different groups          | ✅ Yes               | ✅ Yes                        | Event fan-out to multiple services     |
| Event chaining (2 topics) | ✅ Yes               | ✅ Yes                        | Workflow step-by-step via Kafka topics |

