## 💡 Motivation

Traditional online judges execute code synchronously and tightly couple execution with the main server, leading to scalability and security issues. CodeForge separates execution into a distributed worker system, enabling asynchronous processing and safer sandboxed execution.

# CodeForge — Distributed Online Judge

A **distributed online judge system** that executes user-submitted code asynchronously using a **Redis-backed queue** and a **worker service**, with **Docker-based sandboxed execution** and a **React frontend**.

---

## 🚀 Features

* 🔐 **JWT Authentication** (Spring Security)
* 🧠 **Asynchronous execution** using Redis queue
* ⚙️ **Worker microservice** for background job processing
* 🐳 **Docker-based sandboxing** for secure code execution
* 🌐 **Multi-language support** (Python, Java, C++)
* 📊 **Submission tracking** with real-time status (PENDING → RUNNING → RESULT)
* 🧪 **Testcase-level results** (pass/fail, output, execution time)
* 🧾 **Problem-wise & user-wise submissions**
* 💻 **Monaco Editor** integration for code input

---

## 🏗️ Architecture

```
Client (React)
        │
        ▼
Backend (Spring Boot API)
        │
        ▼
Redis Queue  ─────────▶ Worker Service
                         │
                         ▼
                   Docker Execution
                         │
                         ▼
                      MySQL
```

---

## 🧩 Tech Stack

**Backend**

* Java, Spring Boot
* Spring Security (JWT)
* Spring Data JPA
* Redis (queue)

**Worker**

* Spring Boot
* Redis consumer
* Docker CLI for code execution

**Frontend**

* React
* Axios
* Monaco Editor

**Infra**

* Docker, Docker Compose
* MySQL

---

## 📦 Project Structure

```
codeforge/
├── backend/      # Main API (Spring Boot)
├── worker/       # Worker service (Spring Boot)
├── frontend/     # React app
├── docker-compose.yml
```

---

## ⚙️ Local Setup (Dev)

### 1. Clone repo

```bash
git clone <your-repo-url>
cd codeforge
```

### 2. Start dependencies

```bash
docker compose up -d redis mysql
```

### 3. Run backend

```bash
cd backend
mvn spring-boot:run
```

### 4. Run worker

```bash
cd worker
mvn spring-boot:run
```

### 5. Run frontend

```bash
cd frontend
npm install
npm run dev
```

---

## 🐳 Full Docker Setup

> Recommended for deployment

```bash
docker compose up --build
```

---

## 🔑 Environment Configuration

### Backend / Worker (`application.properties`)

```properties
spring.datasource.url=jdbc:mysql://mysql:3306/codeforge_db
spring.datasource.username=root
spring.datasource.password=root123

spring.redis.host=redis
spring.redis.port=6379
```

---

## 🔄 Execution Flow

1. User submits code
2. Backend stores submission (PENDING)
3. Submission ID pushed to Redis queue
4. Worker consumes job
5. Code executed inside Docker container
6. Results saved to DB
7. Frontend polls until completion

---

## 📊 Sample Status Flow

```plaintext
PENDING → RUNNING → PASSED / FAILED / ERROR
```

---

## 🛡️ Security

* JWT-based authentication
* Role-based access (Admin/User)
* Isolated code execution using Docker

---

## 📌 Future Improvements

* Rate limiting (per user/IP)
* Execution time & memory limits
* Plagiarism detection
* Horizontal scaling (multiple workers)
* Kubernetes deployment

---

## 📄 Resume Highlight

> Built a distributed online judge system using Spring Boot, Redis, and Docker, enabling asynchronous multi-language code execution with a worker-based architecture and real-time submission tracking.

---

## 🤝 Contributing

Pull requests are welcome. For major changes, open an issue first.

---

## 📜 License

MIT License
