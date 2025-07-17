# Spring Boot Kafka Demo (Spring Boot + Kafka + Docker)

This project is a demonstration of **Spring Boot Kafka** and how to set it up using an API-based producer with a consumer 
transforming data into MySQL. It uses an orders system as an example, with a fairly complex canonical model containing 
the orders, order items and customer details.

## 🔨 Tech Stack

| Layer       | Tool                    |
|-------------|-------------------------|
| Language    | Java 21                 |
| Framework   | Spring Boot 3.5.3       |
| DB          | MySQL                   |
| Queues      | Kafka                   |
| Container   | Docker + Docker Compose |
| Docs        | Swagger / OpenAPI       |
| API Testing | Postman                 |
| Linter      | google-java-format      |

## 🏗️ Architecture & Scaling

This project demonstrates a clean and scalable Kafka-based ingestion and persistence flow, consisting of:

- A **producer service** that publishes validated JSON messages to a Kafka topic
- A **consumer service** that consumes messages, performs deduplication, and persists them to a relational database (MySQL)

Both components are designed with real-world scalability patterns in mind.

---

### 🔄 Producer

The producer service publishes structured `CanonicalOrder` messages to Kafka.

- Uses **asynchronous, non-blocking production**
- Messages are validated and enriched before publishing
- Correlation IDs are injected for traceability
- **Throughput benchmark:** ~**33,000 messages/sec** (20k messages in 600ms) on a local Docker-based setup

> The producer is designed to scale horizontally and could publish **hundreds of thousands of messages per second** in a production-grade deployment with appropriate batching and broker tuning.

---

### 🧵 Consumer

The consumer service subscribes to the topic and processes messages in parallel.

- Configured for **manual acknowledgment** and **deduplicated persistence**
- Each consumer thread handles one Kafka partition independently
- **Safe reprocessing** of already-processed messages using unique keys
- Integrated with a relational DB using transactional inserts and retry logic
- **Throughput benchmark:** ~**2,000 messages/sec** (20k messages in 10s) on a local Docker-based setup

> The topic is configured with **12 partitions**, enabling **12-way parallelism** during message consumption.  
> With **100 partitions**, this architecture could scale to support **~10,000 inserts/sec**, assuming adequate database throughput and consumer thread tuning.

---

### 🧪 Fault Tolerance & Delivery

- Automatic retries with backoff
- Failed messages are routed to a **dead-letter topic** after repeated failures
- Consumer gracefully handles duplicate messages and retries without duplication in the DB

## 🚀 Running the App

To run this App, you will need Docker / Docker Desktop on your machine. The included ```docker-compose.yml``` and 
```Dockerfile``` in each module will install any dependencies and run the programs.

---

### 🐳 Docker

```bash
# From project root
docker-compose up --build -d
```

_If testing maximum throughput on each container, you may wish to pause the consumer whilst producing to Kafka and vice 
versa when testing the consumer._

---

### 🧪 Test the System

Import the provided Postman collection found in the producer-service module root:

```
~/producer-service/postman_collection.json
```

Or when running the app, access the Swagger docs at:

```
http://localhost:8081/swagger-ui/index.html
```

Or access the GitHub pages at:

```
https://silver-speedo.github.io/spring-boot-kafka-demo/
```

## 🧠 Author

Created by **[@silver-speedo](https://github.com/silver-speedo)**

## 📄 License

This project is licensed under the MIT License. See `LICENSE` for more info.

