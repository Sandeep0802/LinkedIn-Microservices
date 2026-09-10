# 💼 LinkedIn System — Spring Boot Microservices

A LinkedIn-like social networking platform built from scratch using **Spring Boot Microservices**, with event-driven communication, distributed feed generation, full-text search, authentication, and media storage.

The project demonstrates how different microservices work together to handle users, connections, posts, feeds, search, and notifications.

---

## 🏗️ Architecture

```text
                         ┌─────────────────┐
                         │   React Client  │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │   API Gateway   │
                         │  JWT Validation │
                         └────────┬────────┘
                                  │
              ┌───────────────────┼───────────────────┐
              │                   │                   │
              ▼                   ▼                   ▼
       ┌─────────────┐     ┌─────────────┐    ┌─────────────┐
       │    User     │     │    Post     │    │    Feed     │
       │   Service   │     │   Service   │    │   Service   │
       │    8081     │     │    8082     │    │    8083     │
       │    MySQL    │     │ MySQL + S3  │    │    Redis    │
       └──────┬──────┘     └──────┬──────┘    └──────┬──────┘
              │                   │                   │
              └───────────────────┼───────────────────┘
                                  │
                                Kafka
                                  │
                    ┌─────────────┴─────────────┐
                    ▼                           ▼
             ┌─────────────┐             ┌─────────────┐
             │   Search    │             │Notification │
             │   Service   │             │   Service   │
             │    8084     │             │    8085     │
             │Elasticsearch│             │   Kafka     │
             └─────────────┘             └─────────────┘
```

---

## ✨ Features

### 🔐 Authentication

* User registration and login
* JWT-based authentication
* Gateway-level JWT validation
* Secure password handling

### 👤 User Management

* User profiles
* Profile and cover photo uploads
* Skills and professional information
* Connection requests
* Accept connections
* View connections

### 📝 Posts

* Create posts
* Upload post images
* Like posts
* Comment on posts
* View user posts
* Delete posts

### 📰 Feed

* Personalized user feed
* Kafka-based event processing
* Fan-out-on-write feed generation
* Redis-based feed storage

### 🔎 Search

* Search people
* Search by skills
* Search posts
* Elasticsearch full-text search
* Fuzzy post searching

### 🔔 Notifications

* Welcome notifications
* Connection request notifications
* Connection acceptance notifications
* Post like notifications
* Post comment notifications

---

## 🛠️ Tech Stack

| Technology               | Purpose                          |
| ------------------------ | -------------------------------- |
| **Java**                 | Programming language             |
| **Spring Boot 4.1.1**    | Microservices framework          |
| **Spring Cloud Gateway** | API Gateway                      |
| **Apache Kafka**         | Event-driven communication       |
| **Redis**                | Feed storage and caching         |
| **Elasticsearch 9.4.5**  | Full-text search                 |
| **MySQL**                | Persistent data storage          |
| **AWS S3**               | Image/media storage              |
| **JWT**                  | Authentication                   |
| **OpenFeign**            | Service-to-service communication |
| **Docker Compose**       | Infrastructure setup             |
| **React**                | Frontend                         |

---

## 📦 Microservices

| Service                  |   Port | Responsibility                 |
| ------------------------ | -----: | ------------------------------ |
| **API Gateway**          | `8080` | Routing + JWT validation       |
| **User Service**         | `8081` | Users, profiles & connections  |
| **Post Service**         | `8082` | Posts, likes & comments        |
| **Feed Service**         | `8083` | Personalized feeds using Redis |
| **Search Service**       | `8084` | People, skills & post search   |
| **Notification Service** | `8085` | Event-based notifications      |

---

## 🔄 Event-Driven Architecture

Kafka is used to communicate important events between services.

```text
User Service
     │
     ├── user.created ────────► Search Service
     │                         Notification Service
     │
     └── user.updated ────────► Search Service


Post Service
     │
     ├── post.created ────────► Feed Service
     │                         Search Service
     │
     ├── post.liked ──────────► Notification Service
     │
     └── post.commented ──────► Notification Service


User Service
     │
     ├── connection.requested ─► Notification Service
     │
     └── connection.accepted ──► Notification Service
```

---

## 📰 Feed Generation

The feed uses a **fan-out-on-write** approach.

When a user creates a post:

```text
Post Service
     │
     │ Save post
     ▼
   MySQL
     │
     │ post.created
     ▼
   Kafka
     │
     ▼
 Feed Service
     │
     │ Get author's connections
     ▼
 User Service
     │
     ▼
    Redis
     │
     ├── Feed of User A
     ├── Feed of User B
     └── Feed of User C
```

This allows feeds to be retrieved quickly from Redis instead of calculating the connections and posts every time a user opens the feed.

---

## 🔎 Search Architecture

Search is handled independently using Elasticsearch.

```text
Post/User changes
       │
       ▼
     Kafka
       │
       ▼
Search Service
       │
       ▼
Elasticsearch
       │
       ▼
 Search API
```

This keeps search workloads separate from the transactional MySQL databases.

---

## 🐳 Infrastructure

Docker Compose is used to run the required infrastructure:

* MySQL
* Redis
* Kafka
* Elasticsearch

Start the infrastructure with:

```bash
docker compose up -d
```

---

## 🚀 Running the Project

### 1. Start Infrastructure

```bash
docker compose up -d
```

### 2. Start Services

Start the following Spring Boot applications:

```text
API Gateway        → 8080
User Service       → 8081
Post Service       → 8082
Feed Service       → 8083
Search Service     → 8084
Notification       → 8085
```

### 3. Start Frontend

Start the React application and connect it to:

```text
http://localhost:8080
```

The API Gateway acts as the single entry point for the frontend.

---

## 🔑 Environment Variables

Sensitive configuration such as JWT secrets and AWS credentials should be provided through environment variables.

Example:

```text
JWT_SECRET=your-secret-key
AWS_ACCESS_KEY=your-access-key
AWS_SECRET_KEY=your-secret-key
```

Do **not** commit real secrets or credentials to GitHub.

---

## 📂 Project Structure

```text
LinkedIn-Microservices/
│
├── api-gateway/
├── user-service/
├── post-service/
├── feed-service/
├── search-service/
├── notification-service/
├── frontend/
│
└── docker-compose.yml
```

---

## 🎯 What I Learned

Through this project, I gained practical experience with:

* Designing microservices architecture
* Event-driven communication using Kafka
* JWT authentication through an API Gateway
* Redis-based feed generation
* Elasticsearch full-text search
* Service-to-service communication
* AWS S3 media storage
* Dockerized infrastructure
* Building a React frontend for a microservices backend

---

## 📌 Project Status

**Completed and tested successfully.** ✅

The core backend services, Kafka event flows, Redis feed generation, Elasticsearch search, authentication, media storage, and frontend integration have been implemented and tested.

---

⭐ If you found this project useful, feel free to explore the code and give the repository a star.
