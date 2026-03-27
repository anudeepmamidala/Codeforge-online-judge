# CodeForge – Online Coding Platform

CodeForge is a full-stack coding platform where users can solve problems by submitting code, which is executed and evaluated against test cases.

This project focuses on backend system design, code execution flow, and structured evaluation of submissions.

---

## Features

- JWT-based authentication
- Role-based access control (User/Admin)
- Problem management system
- Code execution engine (Python)
- Testcase-based evaluation (public + hidden)
- Submission tracking with detailed results

---

## Code Execution Flow

1. User submits code for a problem
2. Submission is stored with status `PENDING`
3. Backend fetches testcases
4. Code is executed using ProcessBuilder
5. Output is captured and compared with expected output
6. Each testcase result is stored
7. Final verdict is returned:
   - PASSED
   - FAILED
   - ERROR
   - TIME LIMIT EXCEEDED

---

## Tech Stack

- Backend: Java, Spring Boot
- Security: Spring Security, JWT
- Frontend: React
- Database: MySQL (or configurable)
- Build Tool: Maven

---

## Project Structure

- Controller → API endpoints  
- Service → business logic  
- Repository → database layer  
- DTO → request/response objects  
- Utils → execution and JWT helpers  

---

## Running Locally

```bash
# Backend
mvn spring-boot:run

# Frontend
npm install
npm run dev