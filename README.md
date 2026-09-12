💼 LinkedIn Microservices — Spring Boot + React

A full-stack LinkedIn-style social networking application built from scratch with Spring Boot Microservices, React, Apache Kafka, Redis, Elasticsearch, MySQL, and AWS S3.

The project demonstrates a practical event-driven microservices architecture for authentication, professional profiles, connections, posts, personalized feeds, search, media storage, and persistent notifications.

Goal: build a realistic social networking platform where each major responsibility is isolated into its own service and services communicate through REST and Kafka events.

🏗️ Architecture



High-level request flow

React Frontend
      │
      │ HTTP / REST
      ▼
API Gateway :8080
      │
      ├──────────────► User Service :8081 ─────► MySQL
      │
      ├──────────────► Post Service :8082 ─────► MySQL
      │                                  └─────► AWS S3
      │
      ├──────────────► Feed Service :8083 ─────► Redis
      │
      ├──────────────► Search Service :8084 ──► Elasticsearch
      │
      └──────────────► Notification Service :8085 ─► MySQL

                 Apache Kafka
        ┌────────────┼─────────────┐
        ▼            ▼             ▼
   Feed updates   Search      Notifications

The API Gateway is the only backend entry point used by the frontend. It handles routing and JWT validation for protected routes.

There is no separate authentication microservice; authentication and JWT creation are handled by the User Service.

✨ Features

🔐 Authentication & Security

User registration and login

JWT access and refresh tokens

BCrypt password hashing

JWT validation at the API Gateway

Protected user, post, feed, search, and notification routes

Request user identity propagated through X-User-Id

👤 Profiles & Connections

View user profiles

Edit professional profile information

Add/update headline, about, location, and skills

Upload profile and cover images to AWS S3

Send connection requests

Accept connection requests

View pending requests

View connections from either side of a relationship

📝 Posts

Create text posts

Create posts with images

View individual posts

View posts created by a user

Like / unlike posts

Comment on posts

View comments

Delete posts

Publish post lifecycle events through Kafka

📰 Personalized Feed

Personalized feed for each user

Fan-out-on-write architecture

Feed entries stored as post IDs in Redis

New posts pushed to the author's connections

Author also receives the post in their own feed

Feed size capped using Redis trim

Deleted posts removed from Redis feeds through post.deleted

Paginated feed retrieval

🔎 Global Search

Search people

Browse all indexed users

Search users by skill

Search posts

Fuzzy matching for post content

Elasticsearch used as a dedicated search index

User and post documents kept synchronized through Kafka events

Deleted posts removed from Elasticsearch through post.deleted

🔔 Notifications

Persistent notifications are generated from Kafka events and stored in MySQL.

Welcome notifications

Connection request notifications

Connection acceptance notifications

Post like notifications

Post comment notifications

Unread notification count

Mark individual notification as read

Mark all notifications as read

The current implementation uses REST polling on the frontend for notification refresh; WebSockets are intentionally not used.

🖼️ Media Storage

Profile photo uploads

Cover photo uploads

Post image uploads

AWS S3 used for object storage

Public image URLs returned to the frontend

🛠️ Tech Stack

Technology

Purpose

Java 21

Backend programming language

Spring Boot 4.1.1

Microservices framework

Spring Cloud Gateway

API gateway, routing, JWT protection

Spring Security / JWT

Authentication and authorization

Spring Data JPA

MySQL persistence

Spring Data Redis

Feed storage/caching

Apache Kafka

Asynchronous event-driven communication

Spring Kafka

Kafka producers and consumers

OpenFeign

Feed Service → User Service communication

Elasticsearch 9.4.5

Search index and full-text search

MySQL 8

Relational persistence

Redis

Personalized feed storage

AWS S3

Profile, cover, and post image storage

React

Frontend application

Axios

Frontend HTTP client

React Router

Frontend routing

Docker Compose

Local infrastructure

📦 Microservices

Service

Port

Main Responsibility

Storage / Integration

API Gateway

8080

Routing + JWT validation

Spring Cloud Gateway + Redis rate-limit support

User Service

8081

Authentication, users, profiles, connections

MySQL + Kafka + S3

Post Service

8082

Posts, likes, comments, post images

MySQL + Kafka + S3

Feed Service

8083

Personalized feed generation and retrieval

Redis + Kafka + OpenFeign

Search Service

8084

People, skills, and post search

Elasticsearch + Kafka

Notification Service

8085

Persistent event-based notifications

MySQL + Kafka

🔄 Kafka Event-Driven Architecture

Kafka connects services asynchronously so that write operations do not require every downstream action to happen synchronously.

Events produced by User Service

user.created
user.updated
connection.requested
connection.accepted

Events produced by Post Service

post.created
post.liked
post.commented
post.deleted

Consumers

User Service
   │
   ├── user.created ──────────► Search Service
   │                         └► Notification Service
   │
   ├── user.updated ──────────► Search Service
   │
   ├── connection.requested ──► Notification Service
   │
   └── connection.accepted ───► Notification Service

Post Service
   │
   ├── post.created ──────────► Feed Service
   │                         └► Search Service
   │
   ├── post.liked ────────────► Notification Service
   │
   ├── post.commented ────────► Notification Service
   │
   └── post.deleted ──────────► Feed Service
                               Search Service

📰 Feed Generation — Fan-Out on Write

The Feed Service stores post IDs, not complete post objects.

When a user creates a post:

User
 │
 ▼
API Gateway :8080
 │
 ▼
Post Service :8082
 │
 ├── Save post ───────────────► MySQL
 │
 └── Publish post.created
             │
             ▼
           Kafka
             │
             ▼
        Feed Service :8083
             │
             ├── Ask User Service for connections
             │
             ├── Push post ID to each connection feed
             │
             └── Push post ID to author's own feed
                         │
                         ▼
                       Redis

When the frontend requests a feed:

GET /api/v1/feed/{userId}
        │
        ▼
Feed Service
        │
        ▼
Redis → [postId1, postId2, postId3, ...]
        │
        ▼
Frontend fetches full posts from Post Service

Redis lists are capped using feed.max-size so feeds do not grow indefinitely.

Post deletion cleanup

Post Service
    │
    ├── Delete from MySQL
    │
    └── publish post.deleted
             │
             ▼
           Kafka
          ┌────┴────┐
          ▼         ▼
    Feed Service  Search Service
          │         │
          ▼         ▼
       Redis      Elasticsearch
   remove postId   remove document

This keeps the feed cache and search index synchronized with the transactional post database.

🔎 Search Architecture

Search is separated from transactional MySQL storage.

User / Post changes
       │
       ▼
     Kafka
       │
       ▼
 Search Service :8084
       │
       ├── Users index ──────► Elasticsearch
       │
       └── Posts index ──────► Elasticsearch

Search APIs

GET /api/v1/search/people?q={query}
GET /api/v1/search/people/all
GET /api/v1/search/skills?skill={skill}
GET /api/v1/search/posts?q={query}

User search covers fields such as name, headline, and location. Skill search uses the indexed skills field, while post search uses Elasticsearch text matching with automatic fuzziness.

🔔 Notification Architecture

Notifications are persisted rather than only being logged.

User / Post Service
        │
        ▼
      Kafka
        │
        ▼
Notification Service :8085
        │
        ▼
      MySQL
        │
        ▼
Notification REST API
        │
        ▼
React Frontend

The notification record keeps IDs such as userId and actorId. The frontend resolves the actor through the User Service so users see names and profile images instead of raw IDs.

Notification APIs

GET /api/v1/notifications/{userId}
GET /api/v1/notifications/{userId}/unread
GET /api/v1/notifications/{userId}/unread/count
PUT /api/v1/notifications/{notificationId}/read
PUT /api/v1/notifications/{userId}/read-all

🌐 API Gateway Routes

All frontend requests go through http://localhost:8080.

Route

Service

JWT

/api/v1/auth/**

User Service

No

/api/v1/users/**

User Service

Yes

/api/v1/posts/**

Post Service

Yes

/api/v1/feed/**

Feed Service

Yes

/api/v1/search/**

Search Service

Yes

/api/v1/notifications/**

Notification Service

Yes

🔌 Main REST APIs

Authentication

POST /api/v1/auth/register
POST /api/v1/auth/login

Users

GET  /api/v1/users/{userId}
PUT  /api/v1/users/{userId}/profile
POST /api/v1/users/{targetUserId}/connect
PUT  /api/v1/users/connection/{connectionId}/accept
GET  /api/v1/users/{userId}/connections
GET  /api/v1/users/{userId}/connections/pending
POST /api/v1/users/{userId}/profile-photo
POST /api/v1/users/{userId}/cover-photo

Posts

POST   /api/v1/posts
GET    /api/v1/posts/{postId}
GET    /api/v1/posts/user/{userId}
POST   /api/v1/posts/{postId}/like
POST   /api/v1/posts/{postId}/comment
GET    /api/v1/posts/{postId}/comments
DELETE /api/v1/posts/{postId}

Feed

GET    /api/v1/feed/{userId}?page=0&size=10
DELETE /api/v1/feed/{userId}/cache

Search

GET /api/v1/search/people?q={query}
GET /api/v1/search/people/all
GET /api/v1/search/skills?skill={skill}
GET /api/v1/search/posts?q={query}

Notifications

GET /api/v1/notifications/{userId}
GET /api/v1/notifications/{userId}/unread
GET /api/v1/notifications/{userId}/unread/count
PUT /api/v1/notifications/{notificationId}/read
PUT /api/v1/notifications/{userId}/read-all

🐳 Infrastructure with Docker Compose

The included docker-compose.yml provides the shared infrastructure:

MySQL 8.0 — 3306

Redis — 6379

Kafka (KRaft) — 9092

Elasticsearch 9.4.5 — 9200

Start infrastructure:

docker compose up -d

Check containers:

docker ps

Stop infrastructure:

docker compose down

Persistent Docker volumes are used for MySQL and Elasticsearch data.

🚀 Running the Project

1. Clone the repository

git clone https://github.com/Sandeep0802/LinkedIn-Microservices.git
cd LinkedIn-Microservices

2. Start infrastructure

docker compose up -d

3. Configure environment variables

Each service uses environment variables for database, Kafka, JWT, Elasticsearch, User Service, and AWS configuration.

Example values:

DB_URL=jdbc:mysql://localhost:3306/your_database
DB_USERNAME=root
DB_PASSWORD=your_password

KAFKA_BOOTSTRAP_SERVERS=localhost:9092

JWT_SECRET=your-secret
JWT_EXPIRE=...
JWT_REFRESH=...

AWS_ACCESS_KEY=your-access-key
AWS_SECRET_KEY=your-secret-key
AWS_REGION=your-region
AWS_BUCKET_NAME=your-bucket

ELASTIC_URI=http://localhost:9200
USER_SERVICE_URL=http://localhost:8081
FEED_SIZE=100

Never commit real credentials, JWT secrets, or AWS keys to GitHub.

4. Start the Spring Boot services

Run these applications from the IDE or Maven:

API Gateway          → 8080
User Service         → 8081
Post Service         → 8082
Feed Service         → 8083
Search Service       → 8084
Notification Service → 8085

5. Start the React frontend

cd frontend
npm install
npm run dev

The frontend communicates with the backend through:

http://localhost:8080

📂 Project Structure

LinkedIn-Microservices/
│
├── api-gateway/
│   └── src/
│
├── user-service/
│   └── src/
│
├── post-service/
│   └── src/
│
├── feed-service/
│   └── src/
│
├── search-service/
│   └── src/
│
├── notification-service/
│   └── src/
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
├── docs/
│   └── architecture.png
│
├── docker-compose.yml
└── README.md

🎯 Project Highlights

This project focuses on demonstrating practical microservices patterns rather than building a single monolithic application.

1. Database-per-service approach

Transactional data is separated by responsibility, for example:

User Service       → User DB
Post Service       → Post DB
Notification       → Notification DB

2. Event-driven communication

Kafka is used for operations that should be asynchronous, such as:

post.created
post.deleted
post.liked
post.commented
user.created
user.updated
connection.requested
connection.accepted

3. Specialized data stores

Each technology is used where it provides the most value:

MySQL          → transactional data
Redis          → personalized feeds
Elasticsearch  → search
AWS S3         → images/media
Kafka          → asynchronous events

4. Gateway-based security

The frontend talks only to the API Gateway, while JWT validation is centralized at the gateway layer for protected services.

📚 What I Learned

Building this system provided hands-on experience with:

Microservices decomposition

Spring Boot and Spring Cloud Gateway

JWT authentication and gateway security

Spring Data JPA and MySQL

Apache Kafka producers and consumers

Event-driven architecture

Redis-based fan-out-on-write feeds

Elasticsearch indexing and search queries

OpenFeign service-to-service communication

AWS S3 media storage

Dockerized infrastructure

Building a React frontend for a distributed backend

Keeping Redis and Elasticsearch consistent with database changes

📌 Project Status

Completed and integrated successfully. ✅

The application includes authentication, profile management, connections, posts, personalized feeds, search, media storage, and persistent notifications across a Spring Boot microservices backend with a React frontend.

This project is intended for learning, portfolio, and demonstration purposes.

⭐ Author

Sandeep

GitHub: Sandeep0802

⭐ If you find the project useful, consider giving the repository a star.
