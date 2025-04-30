eMartix Microservices Platform

A Java 17+ Spring Boot 3.x-based set of microservices powering the eMartix commerce platform. It demonstrates a production-grade setup with:

    Service Registry (Eureka)

    Config Server (centralized, Git-backed)

    API Gateway (Spring Cloud Gateway, circuit breakers & retries)

    Individual Services

        auth-service (JWT auth, OAuth2 flows)

        product-service

        cart-service (Redis-backed cart)

        order-service

        noti-service (notifications via RabbitMQ)

    Commons library (shared DTOs, errors, utils)

    Observability: Prometheus, Grafana, Zipkin

    Docker Compose (app + infra: Redis, RabbitMQ, Eureka, Config, Prometheus, Grafana, Zipkin)

🏛 Architecture Overview

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

    Service Registry (service-registry): Eureka server

    Config Server (config-server): centralized application.yml per-service (Git-backed)

    Gateway (gateway-service): routes traffic, implements CB/retry via Resilience4j

    Commons (commons): DTOs, exceptions, utility classes shared across services

    Infra:

        Redis for cart caching

        RabbitMQ for async notifications

        Prometheus + Grafana for metrics

        Zipkin for distributed tracing

📦 Modules
Module	Description
auth-service	User authentication, JWT generation/validation, email verification & password reset
product-service	CRUD product catalog, search
cart-service	Per-user shopping cart (Redis)
order-service	Create & manage orders, inventory checks
noti-service	Send e-mail/SMS/push notifications via Rabbit
gateway-service	API entry-point, routing, auth checks, rate limiting, circuit breakers
config-server	Centralized externalized configuration (Spring Cloud Config)
service-registry	Eureka server
commons	Shared DTOs, domain objects, exception hierarchy, utilities
docker/	Dockerfiles for each service
docker-compose.yml	Bring up entire stack locally (Redis, Rabbit, Eureka, Config, services…)
docker-compose-logging.yml	Prometheus, Grafana, Zipkin plus app stack via extends
🚀 Quick Start
Prerequisites

    Java 17+ JDK

    Maven 3.8+

    Docker & Docker Compose (for local infra)

1. Clone & Build

git clone https://github.com/YourOrg/eMartix.git
cd eMartix
mvn clean install -DskipTests

2. Bring Up Infra + Services

docker-compose up --build
# Or full observability stack:
docker-compose -f docker-compose.yml -f docker-compose-logging.yml up --build

This will start:

    Redis (6379)

    RabbitMQ (5672)

    Eureka (8761)

    Config Server (8888)

    All microservices on ports 80xx (see compose file)

    Prometheus (9090), Grafana (3000), Zipkin (9411)

3. Verify

    Eureka UI: http://localhost:8761

    Config Server: http://localhost:8888

    Gateway: http://localhost:8080

    Prometheus: http://localhost:9090

    Grafana: http://localhost:3000 (default: admin/admin)

    Zipkin: http://localhost:9411

🧪 Testing APIs with Postman

A sample Postman collection is provided under postman/:

    Import postman/eMartix.postman_collection.json

    Set environment variable base_url = http://localhost:8080

    Use pre-defined requests for signup/login, product CRUD, cart flows, order flows.

🔎 Observability & Monitoring

    Metrics: all services expose actuator/prometheus

    Tracing: Spring Cloud Sleuth + Zipkin

    Dashboard: Grafana dashboards defined under docker/grafana/

📁 Folder Structure

eMartix/
├── auth-service/
├── cart-service/
├── commons/
├── config-server/
├── docker/                       # Service Dockerfiles
├── docker-compose.yml
├── docker-compose-logging.yml    # Prometheus, Grafana, Zipkin
├── gateway-service/
├── noti-service/
├── order-service/
├── product-service/
├── service-registry/
└── pom.xml                       # Parent POM

🤝 Contributing

    Create a feature branch off main:

    git checkout -b feature/your-feature main

    Commit code following Conventional Commits

    Open a PR against main, request reviews from the team

    Merge once CI passes and approvals are granted

📄 License

This project is licensed under the MIT License. See LICENSE for details.
