# 12 Golden Rules for Low Latency

## 1. Use database index to reduce access time
Indexes help the database find rows faster, like using a table of contents in a book. Without indexes, the database may have to scan every row, which is slow.
[Improving Database Performance](../database/performance/README.md)

## 2. Compress the payload to reduce data transfer time
Smaller data sizes mean faster transmission. Use formats like GZIP or Brotli to compress JSON/XML responses, especially for large payloads.

## 3. Group requests to reduce network overhead and round-trip time
Instead of sending multiple small requests, batch them into one. This reduces the number of network hops and protocol overhead (e.g., sending 10 requests vs 1 request with 10 items).

## 4. Use HTTP/2 to send requests in parallel through multiplexing
HTTP/2 allows multiple requests to be sent over a single connection simultaneously, avoiding the "head-of-line blocking" issue in HTTP/1.1 and improving efficiency.

## 5. Use CDN to keep data closer to the users and reduce round-trip time
Content Delivery Networks (CDNs) cache static content (images, scripts, etc.) near the user’s location, reducing latency by avoiding long-distance server calls.

## 6. Reduce external dependencies to minimize unnecessary network calls
Every external API or service call adds latency. Eliminate or consolidate them to avoid delays from third-party services.

## 7. Add a load balancer to distribute traffic uniformly and reduce server load
A load balancer ensures no single server is overwhelmed, distributing requests efficiently and avoiding bottlenecks.

## 8. Scale vertically with more memory and storage for faster processing time
Give servers more RAM, CPU, or SSDs. Faster hardware can reduce latency, especially for in-memory operations or I/O-heavy tasks.

## 9. Use cache to serve popular data from memory instead of querying the database
Caching (e.g., with Redis or Memcached) allows frequent data to be served almost instantly from memory, avoiding slower DB access.

## 10. Use connection pooling for databases and networks to avoid connection overhead
Creating a new connection every time is slow. Connection pools reuse existing connections, saving time and resources.

## 11. Use a message queue to handle computationally intensive tasks in the background
Offload long-running tasks (e.g., video processing, sending emails) to background jobs with systems like RabbitMQ or Kafka to keep user-facing services responsive.

## 12. Use an efficient data serialization format, such as Protobuf, to reduce processing time
JSON and XML are human-readable but slow to parse. Formats like Protocol Buffers (Protobuf) are binary and much faster for machines to encode/decode.
