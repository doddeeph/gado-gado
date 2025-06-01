# Distributed Tracing
Distributed Tracing is a technique used to **track and visualize the flow of requests** as they move through a system composed of multiple microservices. 
It is essential for understanding **performance issues**, **identifying bottlenecks**, and **debugging failures** in microservice architectures, where a single client request may span many services and network hops.

## 🔍 What Is Distributed Tracing?
Distributed tracing collects data about individual operations (called **spans**) performed during a request and links them together into a trace, which represents the end-to-end journey of that request through the system.

🧱 Key Concepts
| Term              | Description                                                                                                            |
| ----------------- | ---------------------------------------------------------------------------------------------------------------------- |
| **Trace**         | The entire journey of a request through a system, made up of many spans.                                               |
| **Span**          | A single unit of work in a service (e.g., a database call, HTTP request). Contains start time, end time, and metadata. |
| **Parent Span**   | The original request span; spans can have a hierarchical relationship.                                                 |
| **Trace Context** | Metadata (like trace ID and span ID) passed along with requests so they can be linked together.                        |

## 🛠️ How It Works
1. **Request Starts**: A client sends a request to Service A. 
2. **Trace Context Created**: Service A creates a trace ID and its own span. 
3. **Propagation**: When Service A calls Service B, it passes along the trace context (usually via HTTP headers). 
4. **Child Spans**: Service B continues the trace by creating a child span using the context from Service A. 
5. **Collection**: Tracing libraries collect these spans and send them to a tracing backend. 
6. **Visualization**: The tracing backend visualizes the trace as a tree or waterfall diagram.

## 🧰 Popular Tools
| Tool                                | Description                                                                         |
| ----------------------------------- | ----------------------------------------------------------------------------------- |
| **OpenTelemetry**                   | Open standard for collecting traces, metrics, and logs.                             |
| **Jaeger**                          | Distributed tracing system, originally by Uber. Integrates well with OpenTelemetry. |
| **Zipkin**                          | Distributed tracing system by Twitter. Simple and lightweight.                      |
| **AWS X-Ray**                       | Tracing service from AWS for distributed applications.                              |
| **Datadog / New Relic / Dynatrace** | Commercial solutions with tracing capabilities.                                     |

## 📦 Common Use Cases
- Detect **slow service calls** 
- Pinpoint **where errors happen** 
- Analyze **latency between services** 
- Understand **dependency graphs** 
- Troubleshoot **complex production issues**

## ✍️ Example (HTTP Headers Used in Tracing)
```
traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01
x-b3-traceid: 4bf92f3577b34da6a3ce929d0e0e4736
x-b3-spanid: 00f067aa0ba902b7
x-b3-parentspanid: 3a2fb8be9f6c77b8
x-b3-sampled: 1
```

These headers are used by systems like **OpenTelemetry**, **Zipkin**, and **Jaeger** to propagate trace context.

## ✅ Benefits
- Better **observability** in microservices 
- Easier **debugging and root cause analysis** 
- Real-time **performance monitoring** 
- Improved **resilience and reliability**

