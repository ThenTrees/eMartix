# eMartix Microservices Platform

A Java 17+ Spring Boot 3.x-based set of microservices powering the eMartix commerce platform. It demonstrates a production-grade setup with:

- **Service Registry** (Eureka)  
- **Config Server** (centralized, Git-backed)  
- **API Gateway** (Spring Cloud Gateway, circuit breakers & retries)  
- **Individual Services**  
  - `auth-service` (JWT auth, OAuth2 flows)  
  - `product-service`  
  - `cart-service` (Redis-backed cart)  
  - `order-service`  
  - `noti-service` (notifications via RabbitMQ)  
- **Commons** library (shared DTOs, errors, utils)  
- **Observability**: Prometheus, Grafana, Zipkin  
- **Docker Compose** (app + infra: Redis, RabbitMQ, Eureka, Config, Prometheus, Grafana, Zipkin)  

---

## 🏛 Architecture Overview

```text
                   +--------------+
                   |  Config      |
                   |  Server      |
                   +------+-------+
                          |
         +----------------+----------------+
         |                |                |
   +-----v------+   +-----v------+   +-----v------+
   | auth-      |   | product-   |   | order-     |
   | service    |   | service    |   | service    |
   +-----+------+   +-----+------+   +-----+------+
         |                |                |
      +--v----------------v----------------v--+
      |      API Gateway / Zuul / Gateway      |
      +--+----------------+----------------+--+
         |                |                |
   +-----v------+   +-----v------+   +-----v------+
   | cart-      |   | noti-      |   | service-   |
   | service    |   | service    |   | registry   |
   +------------+   +------------+   +------------+

Infra: Redis, RabbitMQ, Prometheus, Grafana, Zipkin
```

- **Service Registry** (`service-registry`): Eureka server  
- **Config Server** (`config-server`): centralized externalized configuration (Git-backed)  
- **Gateway** (`gateway-service`): routes traffic, implements CB/retry via Resilience4j  
- **Commons** (`commons`): DTOs, exceptions, utility classes shared across services  
- **Infra**:  
  - **Redis** for cart caching  
  - **RabbitMQ** for async notifications  
  - **Prometheus + Grafana** for metrics  
  - **Zipkin** for distributed tracing  

---

## 📦 Modules

| Module              | Description                                                                         |
|---------------------|-------------------------------------------------------------------------------------|
| **auth-service**    | User authentication, JWT generation/validation, email verification & password reset |
| **product-service** | CRUD product catalog, search                                                        |
| **cart-service**    | Per-user shopping cart (Redis)                                                      |
| **order-service**   | Create & manage orders, inventory checks                                            |
| **noti-service**    | Send notifications via RabbitMQ                                                     |
| **gateway-service** | API entry-point, routing, auth checks, rate limiting, circuit breakers              |
| **config-server**   | Centralized externalized configuration                                              |
| **service-registry**| Eureka server                                                                      |
| **commons**         | Shared DTOs, domain objects, exception hierarchy, utilities                         |
| **docker/**         | Dockerfiles for each service                                                        |
| **docker-compose.yml**         | Start core services locally                                                      |
| **docker-compose-logging.yml** | Bring up observability stack (Prometheus, Grafana, Zipkin)                      |

---

## 🚀 Quick Start

### Prerequisites

- Java 17+ JDK  
- Maven 3.8+  
- Docker & Docker Compose (for local infra)  

### 1. Clone & Build

```bash
git clone https://github.com/YourOrg/eMartix.git
cd eMartix
mvn clean install -DskipTests
```

### 2. Bring Up Infra + Services

```bash
docker-compose up --build
# Or full observability stack:
docker-compose -f docker-compose.yml -f docker-compose-logging.yml up --build
```

Starts:

- Redis (`6379`)  
- RabbitMQ (`5672`)  
- Eureka (`8761`)  
- Config Server (`8888`)  
- All microservices on configured ports  
- Prometheus (`9090`), Grafana (`3000`), Zipkin (`9411`)

### 3. Verify

- **Eureka UI**: http://localhost:8761  
- **Config Server**: http://localhost:8888  
- **Gateway**: http://localhost:8080  
- **Prometheus**: http://localhost:9090  
- **Grafana**: http://localhost:3000 (admin/admin)  
- **Zipkin**: http://localhost:9411  

---

## 🧪 Testing APIs with Postman

1. Import `postman/eMartix.postman_collection.json`  
2. Set `base_url = http://localhost:8080` env var  
3. Use pre-made requests for auth, product, cart, order flows.

---

## 🔎 Observability & Monitoring

- **Metrics**: `actuator/prometheus` endpoints  
- **Tracing**: Spring Cloud Sleuth + Zipkin  
- **Dashboards**: Grafana configs in `docker/grafana/`

---

## 🤝 Contributing

1. Branch off `main`: `git checkout -b feature/awesome main`  
2. Follow Conventional Commits  
3. Open PR for review  
4. Merge after CI & approvals

---

## 📄 License

MIT © YourOrg
