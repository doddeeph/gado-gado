# Deploying Java Spring Boot Microservices to Microsoft Azure
Step-by-step deployment guide using:
- Bitbucket (for version control & CI),
- Red Hat OpenShift (for container orchestration),
- Ansible Automation Platform (for infrastructure and deployment automation),
- Blue-Green Deployments (for zero-downtime rollouts).

## 🔧 Tech Stack Purpose Overview
| Tool                      | Purpose                                                         |
| ------------------------- | --------------------------------------------------------------- |
| **Bitbucket**             | Version control, CI/CD trigger, and pipeline execution          |
| **Spring Boot**           | Backend microservices framework                                 |
| **Docker**                | Containerization of Java apps                                   |
| **OpenShift on Azure**    | Hosting Kubernetes workloads (apps) on managed infrastructure   |
| **Ansible**               | Automation tool for infrastructure and deployment orchestration |
| **Blue-Green Deployment** | Deployment technique to eliminate downtime during releases      |

## 🧾 Code Repository (Bitbucket)
`Structure`:
```
project-root/
│
├── microservice-a/
│   └── src/
│   └── Dockerfile
│   └── pom.xml
│
├── microservice-b/
│   └── src/
│   └── Dockerfile
│   └── pom.xml
│
├── infrastructure/
│   └── ansible/
│       └── hosts
│       └── deploy.yml
│
└── bitbucket-pipelines.yml

```

## 🏗️ Bitbucket Pipelines (CI/CD)
Bitbucket CI will:
- Build your Spring Boot JAR
- Build Docker images
- Push images to a registry
- Call Ansible to deploy to OpenShift

`bitbucket-pipelines.yml`
```
image: maven:3.8.6-jdk-17

pipelines:
  default:
    - step:
        name: Build and Push Docker Image
        services:
          - docker
        caches:
          - maven
        script:
          - cd microservice-a
          - mvn clean package -DskipTests
          - docker build -t $DOCKER_REGISTRY/microservice-a:$BITBUCKET_COMMIT .
          - echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin $DOCKER_REGISTRY
          - docker push $DOCKER_REGISTRY/microservice-a:$BITBUCKET_COMMIT

    - step:
        name: Trigger Ansible Deployment
        script:
          - apt-get update && apt-get install -y ansible sshpass
          - sshpass -p $ANSIBLE_SSH_PASSWORD ansible-playbook -i infrastructure/ansible/hosts infrastructure/ansible/deploy.yml --extra-vars "image_tag=$BITBUCKET_COMMIT"
```

## ⚙️ Ansible Automation (CD + OpenShift Integration)
Ansible playbook will:
- Authenticate to OpenShift cluster
- Deploy new version of the microservice to "green" deployment
- Verify health (via readiness/liveness probes)
- Switch traffic from "blue" to "green"
- Optionally rollback if health fails

`deploy.yml` (**Ansible playbook**)
```
- name: Deploy to OpenShift with Blue-Green Strategy
  hosts: all
  become: yes
  tasks:

    - name: Switch context to OpenShift
      shell: |
        oc login https://<openshift-api-url> --token=$OPENSHIFT_TOKEN
        oc project myproject

    - name: Set image for green deployment
      shell: |
        oc set image deployment/microservice-a-green microservice-a=$DOCKER_REGISTRY/microservice-a:{{ image_tag }}

    - name: Wait for green pods to be ready
      shell: |
        oc rollout status deployment/microservice-a-green

    - name: Switch traffic from blue to green
      shell: |
        oc patch route microservice-a \
          -p '{"spec":{"to":{"name":"microservice-a-green"}}}'

    - name: Scale down blue deployment
      shell: |
        oc scale deployment/microservice-a-blue --replicas=0
```

## 🌀 Blue-Green Deployment Strategy on OpenShift
### What is Blue-Green?
- Blue = current live version
- Green = new version
- Deploy new version (green) → Test → Switch traffic → Shutdown blue

### Setup:
- Two deployments: microservice-a-blue, microservice-a-green
- One route: microservice-a pointing to either blue or green
- Traffic is switched via oc patch route

### Benefits:
- Zero-downtime deployment
- Easy rollback by switching route back

### Steps:
| Step | Action                                             |
| ---- | -------------------------------------------------- |
| 1    | Deploy microservice-a-green with new image         |
| 2    | Wait for it to become ready                        |
| 3    | Update route to point to green                     |
| 4    | Scale down microservice-a-blue                     |
| 5    | Optionally rollback by pointing route back to blue |

### Route Example:
```
oc patch route microservice-a \
  -p '{"spec":{"to":{"name":"microservice-a-green"}}}'
```

### Health Check Example:
Ensure deployment has readiness/liveness probes in YAML or Helm:
```
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
```

## ☁️ Hosting OpenShift on Azure
### OpenShift Setup on Azure
Use Azure Red Hat OpenShift (ARO) or install OpenShift yourself on Azure VMs.

You can deploy OpenShift on Azure using:
- Azure Red Hat OpenShift (ARO) for managed service
- Or custom setup using Azure VMs + OpenShift installer

Provision OpenShift cluster on Azure (ARO):
```
az aro create \
  --resource-group myResourceGroup \
  --name myAroCluster \
  --vnet vnetName \
  --master-subnet masterSubnet \
  --worker-subnet workerSubnet \
  --location eastus
```

### 🔐 Secrets & Config
Use OpenShift or Azure Key Vault to manage:
- DB passwords
- API keys
- Docker credentials
- OpenShift tokens

Inject them as:
- Environment variables
- Kubernetes secrets mounted to pods

## 🧪 Canary or Smoke Testing (Optional)
After deploying green version, you can:
- Route partial traffic to green (A/B testing)
- Use automated smoke tests
- Rollback if failure is detected

## ✅ Summary Workflow
- Code pushed to Bitbucket
- CI pipeline builds Docker images & pushes to registry
- Ansible playbook triggered to deploy to OpenShift using blue-green method
- OpenShift route switched to green deployment
- Old version (blue) scaled down safely
```
Developer Pushes Code ⟶ Bitbucket CI Build & Push Image
                             ⟶ Ansible Playbook Runs via SSH/API
                                 ⟶ OpenShift Deploys Green
                                     ⟶ Health Checks
                                     ⟶ Route Switched to Green
                                     ⟶ Blue Shutdown
```

## 🔁 Rollback Strategy
If green fails:
```
oc patch route microservice-a \
  -p '{"spec":{"to":{"name":"microservice-a-blue"}}}'

oc rollout undo deployment/microservice-a-green
```

## 💼 Your Scope as a Spring Boot Developer
Even if you don’t manage DevOps, your code directly affects the deployment process. You need to:
- ✅ Structure your application correctly
- 🧪 Ensure it’s cloud-native and container-ready
- 📦 Expose health endpoints
- 🔄 Support versioning and backward compatibility
- 🧘 Design for blue-green deployment safety


### ✅ Develop Clean, Container-Ready Spring Boot Code
You should structure your microservice as a clean REST API that can be packaged into a container:

Key things you write:
- Controller for handling requests
- Service layer for logic
- application.yml or application.properties
- Dockerfile for containerization

### 🐳 Dockerfile (You Should Know This Even If You Don’t Build It)
Your team may already provide a base image. A typical `Dockerfile` might look like this:
```
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/my-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Make sure:
- The app builds to a single JAR: mvn clean package
- You DO NOT hard-code config (use env vars or external configs)


### 📍Enable Actuator for Health and Readiness Probes
OpenShift (and most Kubernetes environments) use liveness and readiness probes to determine if your app is alive and ready for traffic.

Add to `pom.xml`:
```
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

In `application.yml`:
```
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

This gives endpoints like:
http://localhost:8080/actuator/health (used by probes)


### 📦 Use Environment Variables for Config
DevOps will inject secrets and configs through:
- OpenShift environment variables
- Kubernetes ConfigMaps/Secrets

Use this style:
```
@Value("${spring.datasource.url}")
private String dbUrl;
```

And in `application.yml`:
```
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
```

So they can pass them from deployment YAML:
```
env:
  - name: DB_URL
    valueFrom:
      secretKeyRef:
        name: db-secret
        key: url
```

### 🔀 Blue-Green Safe Design
Your job is to make sure both blue and green versions of the app can run at the same time safely.

That means:
- Avoid hardcoded port numbers or external resource collisions
- Avoid schema-breaking DB changes (coordinate with DevOps and DBAs)
- Design endpoints to be backward compatible if possible

Example:
- ✅ Good: `/v1/users` → `/v2/users`
- ❌ Bad: Change `/users` response structure without versioning


### 🧪 Testing Before Handoff
Before telling DevOps to deploy:
- `mvn clean verify` should pass all unit/integration tests
- Run locally with:
  ```
  java -jar target/my-service.jar
  curl localhost:8080/actuator/health
  ```

### 🪄 Provide Version Tag or Artifact for DevOps
You usually:
- Push code to a specific Git branch (e.g., release)
- Or deliver a specific artifact (e.g., Docker tag or JAR name)

They use that version to deploy.


### 💡 Optional: Know the Deployment YAML
Even if you don’t write them, here’s what you should recognize in a typical Kubernetes/OpenShift deployment YAML so you can debug issues when asked.

Simplified Deployment YAML Example:
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-service-green
spec:
  replicas: 2
  selector:
    matchLabels:
      app: my-service
  template:
    metadata:
      labels:
        app: my-service
    spec:
      containers:
      - name: my-service
        image: my-registry/my-service:1.0.0
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
        env:
        - name: DB_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
```

You may be asked to debug:
- Why health check fails
- Why the container exits
- Why wrong config is loaded


### ✅ Checklist Before You Hand Over to DevOps
| Item                                | Check           |
| ----------------------------------- | --------------- |
| Spring Boot app builds cleanly      | ✅               |
| JAR runs standalone locally         | ✅               |
| Dockerfile works                    | ✅ (if you test) |
| Actuator `/health` endpoint enabled | ✅               |
| Configs use env vars, not hardcoded | ✅               |
| Version/tag info provided clearly   | ✅               |
| APIs are backward compatible        | ✅               |


### 🔚 Summary
You don’t need to write deployment YAMLs, Ansible scripts, or handle OpenShift login. But your application must be built in a deploy-friendly way, otherwise DevOps will hit issues deploying or operating your service.

You are responsible for:
- Code quality
- Container readiness
- Health monitoring endpoints
- Config management practices
- Safe design for blue-green deploys

## Bitbucket usage between engineers and DevOps teams can be structured in two main ways

### ✅ Option 1: Single Repository (Monorepo Approach)
#### 🔷 Structure:
Both Spring Boot source code and deployment configurations (YAML, Dockerfile, Helm charts, etc.) live in the same Bitbucket repository.
This is great for smaller teams where developers and DevOps collaborate closely.

📁 bitbucket.org/team/my-service
```
my-service/
├── .bitbucket-pipelines.yml        # CI/CD pipeline
├── Dockerfile                      # For containerizing the app
├── pom.xml                         # Spring Boot Maven config
├── src/                            # Your Spring Boot source code
│   ├── main/java/...
│   └── test/java/...
│
├── k8s/                            # OpenShift YAMLs
│   ├── deployment-blue.yaml
│   ├── deployment-green.yaml
│   ├── service.yaml
│   ├── configmap.yaml
│   └── route.yaml
│
├── ansible/                        # Optional: Ansible playbooks
│   ├── deploy-blue.yml
│   ├── switch-traffic.yml
│   └── destroy-green.yml
└── README.md
```

#### ✅ Pros:
- Easier collaboration: devs and DevOps can coordinate changes together.
- Full traceability in one place (e.g., Git tags, PRs).
- Code and deployment artifacts are version-aligned (e.g., v1.0.0 of code matches v1.0.0 of deployment YAML).

#### ❌ Cons:
- Slightly more access control complexity (devs might see sensitive deployment secrets, unless abstracted properly).
- Merge conflicts if too many contributors edit the same YAML files.

### ✅ Option 2: Separate Repositories (Microrepo Approach)
Used in enterprise setups, with DevOps owning deployment entirely.

#### 🔷 Structure:
- Repo A (owned by developers): contains only source code.
- Repo B (owned by DevOps): contains deployment configurations (Dockerfiles, Ansible playbooks, Helm charts, OpenShift YAMLs, etc.)

📁 bitbucket.org/team/my-service (owned by devs)
```
my-service/
├── Dockerfile
├── pom.xml
├── src/
└── README.md
```

You push your code, and optionally tag it with a version:
```
git tag v1.0.0
git push origin v1.0.0
```

📁 bitbucket.org/team/my-service-deployment (owned by DevOps)
```
my-service-deployment/
├── k8s/
│   ├── deployment-blue.yaml
│   ├── deployment-green.yaml
│   ├── service.yaml
│   ├── configmap.yaml
│   └── route.yaml
│
├── ansible/
│   ├── build-and-push-image.yml
│   ├── deploy-blue.yml
│   ├── switch-traffic.yml
│   └── cleanup-old.yml
│
├── vars/
│   ├── dev.env
│   └── prod.env
├── bitbucket-pipelines.yml         # DevOps CI/CD pipeline
└── README.md
```

Here, the DevOps pipeline:
- Builds image from your tagged code
- Deploys blue version to OpenShift
- Tests liveness/readiness
- Switches traffic via OpenShift Route
- Deletes the green version (if successful)

#### ✅ Pros:
- Clear separation of concerns (developers vs DevOps).
- Tighter permission management (developers don’t need access to secrets or internal infra scripts).
- Deployment repo can apply to multiple environments (dev/stage/prod) using different automation layers.

#### ❌ Cons:
- Requires careful version coordination.
- More CI/CD complexity to sync image tags and config versions.

### 🚦 Communication Between Repos
When repos are split:
- You (developer) push a release tag to my-service repo (e.g. v1.2.0)
- DevOps pulls that tag in the deployment repo pipeline using Bitbucket or Ansible logic:
  ```
  git clone --branch v1.2.0 https://bitbucket.org/team/my-service.git
  ```

Or your CI/CD can be triggered via:
- Webhook from my-service
- Scheduled deployment job
- Manual approval in Bitbucket Pipelines or Jenkins

### 🧠 So Which Is Better?
| Use Case                                          | Recommended Approach                                  |
| ------------------------------------------------- | ----------------------------------------------------- |
| Small team or startup                             | ✅ **Single repo** (simpler and faster)                |
| Regulated, large-scale system                     | ✅ **Separate repos** for security and maintainability |
| You need full traceability                        | ✅ **Single repo** helps                               |
| CI/CD is managed by DevOps only                   | ✅ **Separate repos** with clear handoff               |
| Engineers need to test their Docker image locally | ✅ **Single repo** makes this easier                   |


### 🧩 Summary
If you’re a Java Spring Boot engineer and your team uses OpenShift + Ansible + Bitbucket:
- You might only work in the application repo (my-service)
- DevOps may own a separate infra repo (my-service-infra or deployment-pipelines)
- You may need to communicate the version/tag of your Docker image or JAR to the DevOps team to trigger a blue-green deployment on Azure via OpenShift

### 🛠️ Bonus: Monorepo Bitbucket Pipelines YAML (Example)
```
# .bitbucket-pipelines.yml
image: maven:3.8.5-openjdk-17

pipelines:
  branches:
    main:
      - step:
          name: Build and Push Docker Image
          script:
            - mvn clean package -DskipTests
            - docker build -t myregistry.com/my-service:$BITBUCKET_COMMIT .
            - docker push myregistry.com/my-service:$BITBUCKET_COMMIT

      - step:
          name: Deploy to OpenShift
          script:
            - ansible-playbook ansible/deploy-blue.yml
            - ansible-playbook ansible/switch-traffic.yml
```

## 🧠 What is Blue-Green Deployment?
It means you have two identical environments:
- Blue: the current live production version
- Green: the new version you want to deploy

You deploy Green, test it in production-like conditions, and then switch traffic from Blue to Green once it's confirmed stable. If anything goes wrong, you can roll back to Blue easily.

### 🔁 Deployment Flow (Engineer & DevOps Collaboration)
1. You push code to Bitbucket
- Spring Boot code in a Git branch (e.g., main)
- CI pipeline builds your JAR and creates a Docker image:
  `myregistry.io/my-service:v1.2.0`

2. DevOps pipeline deploys to Green slot
- OpenShift YAML files define two deployments:
    - `my-service-blue`
    - `my-service-green`
- DevOps uses Ansible or OpenShift CLI to deploy the new image to the green slot (non-live).
- The green service is connected to a test route like:
  `green.my-service.apps.cluster.com`

3. Smoke Test / Health Check
- The pipeline or QA team runs smoke tests on the green route.
- Spring Boot exposes health endpoints like /actuator/health.

4. Switch traffic to Green
- DevOps updates the OpenShift Route or uses a load balancer to direct all live traffic to green.
- This can be done instantly — no downtime.

5. Blue becomes idle (rollback option)
- If green fails after going live, switch back to blue.
- If green is stable, blue can be scaled to zero or removed.

### 📦 In OpenShift YAMLs (Example)
`deployment-blue.yaml`
```
metadata:
  name: my-service-blue
spec:
  replicas: 1
  template:
    spec:
      containers:
        - name: app
          image: myregistry.io/my-service:v1.1.0
```

`deployment-green.yaml`
```
metadata:
  name: my-service-green
spec:
  replicas: 1
  template:
    spec:
      containers:
        - name: app
          image: myregistry.io/my-service:v1.2.0
```

`route.yaml` (Points to one at a time)
```
spec:
  to:
    kind: Service
    name: my-service-green  # Switch here to blue or green
```

### 🧰 What DevOps Might Automate with Ansible
```
- name: Deploy to green environment
  shell: oc apply -f k8s/deployment-green.yaml

- name: Run smoke tests
  shell: curl http://green.my-service.apps.cluster.com/actuator/health

- name: Switch traffic to green
  shell: oc patch route my-service --patch '{"spec": {"to": {"name": "my-service-green"}}}' --type=merge
```

### ✅ Benefits
| Benefit                | Explanation                                    |
| ---------------------- | ---------------------------------------------- |
| Zero downtime          | New version is deployed before switching       |
| Easy rollback          | Just switch the route back to blue             |
| Confidence             | Smoke testing green before exposing to users   |
| Versioned environments | You can keep both versions running temporarily |


### 🚨 Challenges
| Challenge                        | Mitigation                                              |
| -------------------------------- | ------------------------------------------------------- |
| Double resource usage            | Only temporary, green replaces blue                     |
| State management (DB migrations) | Use **backward-compatible DB changes**, feature toggles |
| Route switching logic            | Automate it using Ansible or OpenShift Route patching   |


### 🧑‍💻 From Developer’s View
Even if you don’t manage YAMLs, here’s what you should do:

#### ✅ Ensure:
- Your Spring Boot app is stateless or handles graceful shutdowns
- You expose readiness and liveness probes (/actuator/health)
- You document release notes clearly in Bitbucket commits or tags
- You avoid breaking DB schema changes unless approved by DevOps

#### 🧪 Example Smoke Test Script
```
#!/bin/bash
echo "Testing green version..."
curl -s http://green.my-service.apps.cluster.com/actuator/health | grep '"status":"UP"' || exit 1
echo "Green is healthy. Ready to switch traffic."
```

### 🚀 Summary
| Role          | Responsibility in Blue-Green                                         |
| ------------- | -------------------------------------------------------------------- |
| **Engineer**  | Write reliable Spring Boot apps with `/health`, Dockerfile, config   |
| **DevOps**    | Define blue/green OpenShift YAMLs, automate traffic switch, rollback |
| **Bitbucket** | Triggers CI/CD pipeline to build, deploy, switch environments        |


## 🔀 Git Branching Strategy (for Blue-Green + Rollback Support)
This strategy ensures clean version control, quick rollbacks, and safe releases—especially in collaboration between engineers and DevOps.

### 🧱 Recommended Branches
| Branch          | Purpose                                           |
| --------------- | ------------------------------------------------- |
| `main`          | Stable production code (deployed in **Blue**)     |
| `develop`       | Ongoing development and integration               |
| `release/x.y.z` | Candidate version (used for **Green** deployment) |
| `hotfix/x.y.z`  | Emergency fix for rollback or critical bugs       |
| `feature/xxx`   | Individual features or tickets                    |


### 🔁 Example Flow
#### 🟢 Deploying a New Version (Green)
```
git checkout -b release/1.2.0 develop
# Final fixes, version bump
git push origin release/1.2.0
```
DevOps pipeline deploys release/1.2.0 as the Green environment.

#### 🔴 Rollback Case (Bug Found in Green)
```
# Back to main (blue)
oc patch route my-service -p '{"spec":{"to":{"name":"my-service-blue"}}}' --type=merge
```

#### 🔧 Hotfix After Rollback
```
git checkout -b hotfix/1.2.1 main
# Apply urgent fix
git commit -am "Fix critical issue after green failure"
git push origin hotfix/1.2.1
```

Then:
```
# Merge to main and develop
git checkout main
git merge hotfix/1.2.1
git checkout develop
git merge hotfix/1.2.1
```

Create a new release from hotfix if needed:
release/1.2.1 → Green again → Promote → Route switch

### ✅ Branch Naming Convention
| Branch Type | Example             |
| ----------- | ------------------- |
| Feature     | `feature/user-auth` |
| Release     | `release/1.2.0`     |
| Hotfix      | `hotfix/1.2.1`      |


### 🗂️ Summary Diagram
Git Strategy:
```
  feature/*   → develop → release/x.y.z → test (Green)
                                         ↑
                          rollback ← hotfix/x.y.z
                              ↓
                            main → stable (Blue)
```

Alert Flow:
```
  Release → Deploy (Green) → Test OK? → YES → Route Switch → Notify
                                          ↓
                                       NO → Rollback → Notify → Hotfix
```


## 🚀 Hybrid Cloud Strategy Overview
A hybrid cloud strategy integrates:
- Public cloud (e.g., Azure, AWS, GCP)
- Private cloud (e.g., on-prem OpenShift, VMware, bare metal)
- Sometimes multi-cloud services (more than one public provider)

### 🔑 Why Hybrid Cloud?
- Regulatory: Some data must stay on-prem (e.g., banking, healthcare)
- Flexibility: Mix performance, cost, and compliance
- High availability: Failover between clouds
- Gradual migration: Migrate to cloud in phases, not all at once


### 🛠 DevOps Practices in a Hybrid Cloud
| Practice                         | Purpose                                                          |
| -------------------------------- | ---------------------------------------------------------------- |
| **CI/CD Pipelines**              | Automate build/test/deploy across cloud boundaries               |
| **Infrastructure as Code (IaC)** | Provision and manage infra using Ansible, Terraform              |
| **Containerization**             | Deploy via Docker + Kubernetes/OpenShift for portability         |
| **Monitoring & Observability**   | Use centralized tools like Prometheus, Grafana, or Azure Monitor |
| **Automation & Orchestration**   | Automate app deployment and scaling via Ansible, Jenkins         |


### 🧩 Microservices Architecture in Hybrid Cloud
Each microservice is:
- Independently deployable
- Owns its own data
- Communicates via HTTP/gRPC/Event Bus

`Example Structure`:
| Component         | Location                            |
| ----------------- | ----------------------------------- |
| `user-service`    | On-prem OpenShift                   |
| `payment-service` | Azure Kubernetes Service (AKS)      |
| `auth-service`    | Shared across environments          |
| `frontend`        | Public cloud CDN (Azure Front Door) |


### 🔍 Distributed Tracing
Distributed tracing tracks requests across multiple services and environments.

#### 🔧 Tools:
- Jaeger
- OpenTelemetry
- Zipkin
- Azure Application Insights

#### 🔄 Example:
- A request hits the frontend → routed to auth-service → user-service → payment-service
- Each service adds trace IDs to headers
- These traces are visualized in Jaeger/Zipkin

#### 🔎 Helps identify:
- Latency bottlenecks
- Failing services
- End-to-end performance

#### 🔄 Streaming Data
In hybrid cloud, real-time event processing is critical.

#### ✅ Use Case:
- Real-time order processing
- Fraud detection
- Sensor data ingestion (IoT)

`Tools`:
| Tool                        | Purpose                                  |
| --------------------------- | ---------------------------------------- |
| **Apache Kafka / Redpanda** | Distributed event streaming              |
| **Azure Event Hubs**        | Fully managed Kafka-compatible streaming |
| **Debezium**                | Change Data Capture (CDC) for DBs        |
| **Flink / Spark Streaming** | Real-time data processing pipelines      |

Example Flow:
- On-prem app emits events to Kafka
- Azure functions or Flink jobs consume, enrich, or aggregate
- Stream results to dashboard or storage

#### 🗂️ Summary Diagram (Conceptual)
```
      ┌────────────┐      ┌───────────────┐
      │  Frontend  │ ---> │  Azure CDN    │
      └────────────┘      └───────────────┘
                                ↓
                          ┌───────────────┐
    ┌──────────────┐      │ Azure AKS     │ <----> Azure Event Hubs
    │ On-prem OCP  │ <--> │ Payment Svc   │
    │ User Svc     │      └───────────────┘
    │ Kafka Topic  │ <--- Event Stream ---┘
    └──────────────┘

  ┌──────────────────┐
  │ Jaeger / Grafana │ <-- Distributed Tracing
  └──────────────────┘

    Blue  <--- Route ---- Green
   (v1.0)               (v1.1)
    │                      │
    └-- rollback if fail --┘
```

## 🔐 What Is Zero Trust Architecture?
Zero Trust means no implicit trust is given to any user, system, or process—regardless of whether it is inside or outside your perimeter.

Core Tenets:
- Verify explicitly (authentication & authorization)
- Use least privilege access
- Assume breach (micro-segmentation, logging)


### 🧱 Components of Zero Trust in Spring Boot
1. Strong Identity and Authentication
   Use centralized identity providers (IdPs) to authenticate all users and services.

✅ Implementation
- Use OAuth2/OpenID Connect (OIDC) with Spring Security
- Integrate with providers like:
    - Keycloak
    - Auth0
    - Azure AD
    - Okta
```
// application.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-idp.com/auth/realms/yourrealm
```

2. Authorization: Role and Attribute-Based Access Control (RBAC/ABAC)
   Use fine-grained authorization:
- RBAC: roles like ADMIN, USER, etc.
- ABAC: attributes like region, department, clearance level

✅ Implementation in Spring Boot
```
@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
public ResponseEntity<?> getUser(String userId) {
    // Only allow if the user is admin or accessing own record
}
```

3. Mutual TLS (mTLS) for Service-to-Service Communication
   Spring Boot microservices can use mutual TLS to verify identities of both clients and servers.

✅ Setup:
- Use Spring Cloud Gateway with mutual TLS configured
- Create trusted CA-signed certificates per service
- Use Istio/Linkerd (service mesh) for automatic mTLS in Kubernetes/OpenShift

4. Micro-Segmentation
   Break your application into logical zones where each microservice must explicitly authenticate and authorize others.

✅ Example:
- `order-service` cannot access `payment-service` without a valid JWT token and scope

Use:
- Spring Cloud Security + Gateway
- Service Mesh policies (e.g., Istio AuthorizationPolicies)

5. Least Privilege Principle
   Every component (user, service, script) gets minimum necessary access.

✅ Implementation:
- Use scopes/claims in tokens (read:profile, write:invoice)
- Use Spring Security expressions or AccessDecisionVoter

6. Device and Network Awareness (Optional)
   Incorporate device posture (e.g., location, device ID) if available.

Use:
- Headers with device fingerprint
- Attributes in tokens

7. Comprehensive Logging and Auditing
   Track all access attempts, both successful and failed.

✅ Tools:
- Spring Boot Actuator + Sleuth
- Logstash/ELK or Azure Monitor
- Audit logs per endpoint and user

8. Policy Enforcement Point (PEP) and Policy Decision Point (PDP)
- PEP: Your Spring Boot microservice (enforces)
- PDP: Central auth service like Keycloak, OPA (decides)

✅ With OPA:
Use Open Policy Agent (OPA) with REST API to authorize every request based on policy.

### 🔄 Request Flow with ZTA in Spring Boot
```
Client → Gateway (JWT, mTLS)
       → Authn via OIDC (Keycloak)
       → Authz via Spring Security + OPA
       → Service A (verifies token, logs request)
          → Service B (uses mTLS + JWT forward)
```

### 🛠️ Tools & Stack
| Purpose           | Tool                                |
| ----------------- | ----------------------------------- |
| Authentication    | Spring Security, Keycloak, Azure AD |
| Authorization     | Spring Security, OPA, ABAC          |
| Logging & Tracing | Sleuth, Zipkin, ELK                 |
| mTLS              | Istio, Spring Cloud Gateway         |
| Auditing          | Actuator, AuditTrail logs           |
| Token Management  | JWT, OAuth2, PASETO (optional)      |


### 📌 Example Use Case: `order-service` Calling `payment-service`
- `order-service` calls `payment-service` via REST
- Includes:
    - JWT token in Authorization header
    - Mutual TLS handshake

- `payment-service`:
    - Validates token (issuer, audience, expiration, scopes)
    - 0Checks TLS cert
    - Logs request
    - Applies RBAC/ABAC policies


### ✅ Best Practices Summary
| Practice                    | Description                              |
| --------------------------- | ---------------------------------------- |
| 🔐 Authenticate Everything  | Users, services, scripts                 |
| 🛂 Use Centralized Identity | OIDC with Keycloak/Auth0                 |
| 🔒 Enforce mTLS             | Use for internal microservice calls      |
| 📜 Use ABAC or RBAC         | Attribute-based better for microservices |
| 🔍 Enable Tracing & Logging | End-to-end visibility                    |
| 🪪 Validate Tokens          | Check scope, role, issuer, etc.          |
| 🧠 Centralized Policies     | Use PDP like OPA                         |
| 🧰 Keep It Lean             | Use least privilege access               |
