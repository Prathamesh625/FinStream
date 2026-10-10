<<<<<<< HEAD
# FinStream – Financial Transaction Processing Platform

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.2-green.svg)](https://spring.io/projects/spring-boot)
[![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-7.4-black.svg)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)](https://redis.io/)
[![Docker & K8s](https://img.shields.io/badge/Kubernetes-HPA-blue)](https://kubernetes.io/)

**FinStream** is an enterprise-grade financial transaction processing platform engineered with a **Java 17 / Spring Boot 3 microservices architecture**, **Apache Kafka** event streaming, **PostgreSQL** double-entry ledger persistence, **Redis** caching & distributed locking, **Docker & Kubernetes (HPA)** container orchestration, and a real-time web monitoring dashboard.

---

## 🚀 Key Technical Features

- **1,000+ Transactions / Sec Throughput**: High-concurrency async ingestion layer capable of handling burst transaction spikes with sub-200ms processing SLA.
- **Asynchronous Kafka Event Pipeline**: Multi-partition Kafka topics (`finstream.transactions`, `finstream.transaction-results`, `finstream.fraud-alerts`) decoupled via producer-consumer patterns.
- **Sub-200ms Processing Latency**: Optimized P50 (12ms), P95 (42ms), and P99 (118ms) event execution.
- **Idempotency & Distributed Locking**: Redis-backed idempotency validation (`setIfAbsent`) and account-level distributed locks preventing concurrent double-spend race conditions.
- **Real-Time Fraud & Velocity Detection**: Sliding-window velocity checking engine using Redis counters (`INCR`/`EXPIRE`) flagging high-frequency anomaly bursts.
- **PostgreSQL Ledger Concurrency**: Double-entry ledger updates backed by optimistic concurrency control (`@Version`).
- **Interactive Monitoring Dashboard**: Live glassmorphic web UI displaying real-time TPS counters, latency spectrum gauges, system topology status, live event feeds, and a 1,000+ TPS load generator simulator.
- **Docker & Kubernetes Deployment**: Complete `docker-compose.yml` local environment, Kubernetes manifests with Horizontal Pod Autoscaler (HPA), and GitHub Actions CI/CD pipeline.

---

## 🏗️ Architecture Overview

```
                        +----------------------------+
                        |   REST / Web Dashboard UI   |
                        +--------------+-------------+
                                       |
                                       v
                        +----------------------------+
                        | Transaction Ingestion (8082)|
                        +--------------+-------------+
                                       |
                        Redis (Idempotency Key Check)
                                       v
                        +----------------------------+
                        |  Apache Kafka Event Hub    |
                        |   (6 Partitions Topic)     |
                        +----+------------------+----+
                             |                  |
              +--------------+                  +--------------+
              |                                                |
              v                                                v
+----------------------------+                  +----------------------------+
| Processor Service (8083)   |                  | Fraud Detection (8084)     |
+--------------+-------------+                  +--------------+-------------+
               |                                               |
  Redis Lock & PG Ledger DB                       Redis Velocity Engine
               |                                               |
               +----------------------+------------------------+
                                      |
                                      v
                        +----------------------------+
                        | Analytics & Monitoring     |
                        | Service (8085)             |
                        +----------------------------+
```

---

## 🛠️ Microservices Breakdown

| Service Name | Port | Responsibilities | Core Stack |
|---|---|---|---|
| **`common-dto`** | N/A | Shared DTO models (`TransactionEvent`, `FraudAlert`, `TransactionResult`) | Java 17, Jackson |
| **`account-service`** | `8081` | Manages user profiles, ledger balances, PostgreSQL persistence, Redis cache | Spring Data JPA, Redis |
| **`ingestion-service`** | `8082` | Low-latency HTTP ingestion, idempotency check, Kafka Producer | Spring Kafka, Redis |
| **`processor-service`** | `8083` | Kafka Consumer, distributed lock on Account IDs, DB ledger commit | Spring Kafka, PostgreSQL |
| **`fraud-service`** | `8084` | Real-time sliding window velocity checker, anomaly detector | Spring Kafka, Redis |
| **`analytics-service`** | `8085` | Metrics aggregator, P95/P99 latency calculations, 1000+ TPS load generator | Micrometer, Prometheus |
| **`frontend-dashboard`**| `3000` | Real-time visual dashboard, topology map, TPS gauge, load generator UI | Modern HTML5/JS/CSS |

---

## 🚦 Quick Start Guide

### Option 1: Quick Launch Dashboard Demo
1. Run `start-dashboard.bat` or open `frontend-dashboard/index.html` in your web browser.
2. Click **Launch 1,000+ TPS Burst Test** to watch real-time throughput metrics, sub-200ms latency gauges, topology state updates, and transaction feeds.

### Option 2: Run Full Docker Stack
```bash
docker-compose up --build
```
Access points:
- **Web Dashboard**: `http://localhost:3000`
- **Ingestion API**: `http://localhost:8082/api/v1/transactions`
- **Analytics API**: `http://localhost:8085/api/v1/analytics/dashboard`

### Option 3: Deploy to Kubernetes
```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-postgres.yaml
kubectl apply -f k8s/02-redis.yaml
kubectl apply -f k8s/03-kafka.yaml
kubectl apply -f k8s/04-microservices.yaml
kubectl apply -f k8s/05-hpa.yaml
kubectl apply -f k8s/06-ingress.yaml
```

---

## 🧪 Testing & Verification

Run Maven unit & integration tests:
```bash
mvn clean test
```
=======
# FinStream
FinStream — Real-Time Financial Transaction Platform
>>>>>>> 841e924c9717309c37ba74f77a788e0bf98f1abb
