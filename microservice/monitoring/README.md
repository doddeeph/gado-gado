# Microservice Monitoring
Microservice monitoring with **Prometheus**, **Grafana**, and **ELK (Elasticsearch, Logstash, Kibana**) is a powerful and widely adopted approach to ensure visibility into the health, performance, and behavior of distributed systems.

## 🔍 Overview of Monitoring Needs in Microservices
Microservices are distributed systems with many independently deployed components. Monitoring in this context usually focuses on three pillars:
1. **Metrics (Performance & Health Monitoring)**
2. **Logs (Troubleshooting & Auditing)**
3. **Tracing (Request Flow/Latency)** – Optional, e.g., with Jaeger or Zipkin

## 📊 Prometheus: Metrics Collection
Prometheus is an open-source monitoring and alerting system.

### 🔧 Key Features:
* **Pull-based**: Prometheus scrapes metrics from services at configurable intervals.
* Stores **time-series data**.
* Has **powerful query language (PromQL)**.
* Easily integrates with exporters like **Node Exporter**, **JMX Exporter**, and **custom exporters**.

### 📦 Integration in Microservices:
* Services expose metrics via HTTP (`/metrics` endpoint) in Prometheus format.
* Prometheus scrapes these metrics periodically.

### 🔔 Example Metrics:
* HTTP request durations
* Error rates
* CPU/memory usage (via node-exporter)

## 📈 Grafana: Metrics Visualization
**Grafana** is an open-source analytics & visualization platform.

### 🔧 Key Features:
* Dashboards and graphs built on Prometheus data.
* Supports alerts and notifications.
* Beautiful, customizable visualizations.

### 🧩 How It Fits:
* Grafana connects to Prometheus as a data source.
* You can create dashboards to visualize:
  * Service health (uptime, request latency)
  * System metrics (CPU, memory)
  * Business metrics (number of orders, etc.)

## 📃 ELK Stack: Logging System
The ELK Stack is a set of tools for log management:

### 🧱 Components:
1. **Elasticsearch** – Stores and indexes logs.
2. **Logstash** – Collects, parses, and transforms logs.
3. **Kibana** – Visualizes logs and allows search/filtering.

📝 Alternative: **Fluentd** or **Filebeat** can replace Logstash for log shipping.

### 📦 Integration in Microservices:
* Microservices write logs to files or stdout/stderr.
* Filebeat or Logstash reads logs and sends them to Elasticsearch.
* Kibana is used to:
  * Search logs
    * Set up alerts
  * View application errors and stack traces

## 🔁 How They Work Together
```
[ Microservices ]
|
|---> Expose metrics ---> [ Prometheus ] ---> [ Grafana ]
|
|---> Logs ---> [ Logstash/Filebeat ] ---> [ Elasticsearch ] ---> [ Kibana ]
```

## 📌 Use Case Example: Spring Boot App Monitoring
### Metrics:
* Add Prometheus dependency (`micrometer-registry-prometheus`)
* Expose `/actuator/prometheus` endpoint
* Prometheus scrapes it
* Grafana dashboard shows:
  * HTTP requests per second
  * Latency
  * Errors

### Logs:
* Logs written in JSON format
* Filebeat reads logs
* Logstash parses logs
* Elasticsearch stores logs
* Kibana shows:
  * Error logs with full trace
  * Filters by service, time, severity

## ✅ Benefits
* 🔍 Observability into microservices behavior
* ⚠️ Alerting when something goes wrong
* 📊 Visualization of trends over time
* 📁 Centralized logging for troubleshooting

## 📚 Tools Comparison
| Feature       | Prometheus + Grafana             | ELK Stack                      |
| ------------- | -------------------------------- | ------------------------------ |
| Data Type     | Metrics (numeric, time-series)   | Logs (textual, unstructured)   |
| Best For      | Performance monitoring, alerting | Debugging, auditing            |
| Storage       | Time-series DB (Prometheus TSDB) | Document index (Elasticsearch) |
| Visualization | Grafana                          | Kibana                         |


## 🚀 Bonus: Tracing (Optional)
If you want full **distributed tracing**, you can also integrate tools like:
* **Jaeger**
* **Zipkin**
* **OpenTelemetry**

They help trace requests across services and identify latency bottlenecks.

## 🧪 Example Stack for Kubernetes Microservices
* **Prometheus Operator** – Manages Prometheus on Kubernetes.
* **Grafana** – With dashboards imported from JSON or Helm charts.
* **Filebeat** – Reads logs from pods.
* **ELK Stack** – For storing and analyzing logs.

# Microservice Monitoring Stack
Below is a complete example using **Docker Compose** to run a microservice monitoring stack with:
* **Prometheus** (metrics)
* **Grafana** (visualization)
* **ELK stack** (logs: Elasticsearch, Logstash, Kibana)
* A sample Spring Boot app exposing metrics and logs

## 📁 Project Structure
```
monitoring-stack/
├── docker-compose.yml
├── prometheus/
│   └── prometheus.yml
├── logstash/
│   └── logstash.conf
└── spring-app/
└── (optional Spring Boot app with Actuator & logging)
```

### 📦 1. docker-compose.yml
```
version: '3.8'

services:
    # Prometheus
    prometheus:
        image: prom/prometheus
        volumes:
            - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
        ports:
            - "9090:9090"
    
    # Grafana
    grafana:
        image: grafana/grafana
        ports:
            - "3000:3000"
        environment:
            - GF_SECURITY_ADMIN_PASSWORD=admin
        depends_on:
            - prometheus    
    
    # Elasticsearch
    elasticsearch:
        image: docker.elastic.co/elasticsearch/elasticsearch:8.12.0
        environment:
            - discovery.type=single-node
            - xpack.security.enabled=false
        ports:
            - "9200:9200"
    
    # Logstash
    logstash:
        image: docker.elastic.co/logstash/logstash:8.12.0
        volumes:
            - ./logstash/logstash.conf:/usr/share/logstash/pipeline/logstash.conf
        ports:
            - "5044:5044"
        depends_on:
            - elasticsearch
    
    # Kibana
    kibana:
        image: docker.elastic.co/kibana/kibana:8.12.0
        ports:
            - "5601:5601"
        depends_on:
            - elasticsearch
```

### 🧾 2. prometheus/prometheus.yml
```
global:
    scrape_interval: 15s

scrape_configs:
    - job_name: 'spring-app'
        static_configs:
            - targets: ['host.docker.internal:8080']
```

If you're using Linux, replace `host.docker.internal` with your host IP or run your Spring Boot app inside Docker as well.

### 🪵 3. logstash/logstash.conf
```
input {
    tcp {
        port => 5044
        codec => json_lines
    }
}

output {
    elasticsearch {
        hosts => ["http://elasticsearch:9200"]
        index => "spring-logs-%{+YYYY.MM.dd}"
    }
}
```

### ☕ 4. Sample Spring Boot App Setup
Add these dependencies:
```
<!-- pom.xml -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

`application.properties`
```
# Actuator endpoints
management.endpoints.web.exposure.include=*
management.endpoint.prometheus.enabled=true
management.metrics.export.prometheus.enabled=true

# Logging in JSON format
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

Logs should be forwarded to **Logstash** (e.g., using **Filebeat** or a TCP appender).

## ✅ Running It
```
docker-compose up -d
```

* Prometheus → http://localhost:9090
* Grafana → http://localhost:3000 (login: admin / admin)
* Kibana → http://localhost:5601
* Elasticsearch → http://localhost:9200

## 📊 Grafana Setup
* Open Grafana.
* Add data source → Prometheus → http://prometheus:9090.
* Import a dashboard (e.g., ID: `4701` for Spring Boot metrics).

## 🧪 Log Test (Optional)
Send test logs:
```
echo '{"@timestamp":"2025-05-31T12:00:00Z","message":"Hello from Spring Boot!"}' | nc localhost 5044
```
