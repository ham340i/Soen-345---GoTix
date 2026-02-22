# 🎟️ GoTix – Cloud-Based Ticket Reservation Application  
**SOEN 345 – Software Testing, Verification & Quality Assurance (Winter 2026)**  
**Concordia University**

## 📌 Project Overview
Eventix is a cloud-based ticket reservation application that allows users to browse events, search and filter by category/date/location, reserve and cancel tickets, and receive digital confirmations. Event organizers (administrators) can manage events by adding, editing, or canceling them.

This project demonstrates software engineering best practices including system design, testing strategies, CI/CD, and quality assurance.

---

## 👥 Team Members

| Name                    | Student ID | Role                         |
|-------------------------|------------|------------------------------|
| Ahmad Al Habbal         | 40261029   | Team Lead / Full-Stack Dev   |
| Abd Al Rahman Al Kabani | 40247395   | Backend Developer            |
| Mena Boulus             | 40291619   | Frontend Developer           |
| Marc El Haddad          | 40231208   | Testing & QA Engineer        |
| Karim Mikhaeil          | 40233685   | DevOps & Documentation       |

---

## 🚀 Features

### Customer
- Register using email or phone number  
- Browse available events  
- Search and filter events by date, location, and category  
- Reserve tickets  
- Cancel reservations  
- Receive digital confirmations (email/SMS – mocked)

### Administrator
- Add new events  
- Edit existing events  
- Cancel events  

---

## 🧱 System Architecture
The system follows a layered architecture:
- Presentation Layer (Web Frontend)
- Application Layer (REST API – Java Spring Boot)
- Domain Layer (Business Logic)
- Data Layer (In-memory or Database)

---

## 🛠️ Tech Stack

**Backend**
- Java  
- Spring Boot  
- JUnit 5  

**Frontend**
- HTML  
- CSS  
- JavaScript  
- React (Maybe)

**DevOps & Tools**
- GitHub  
- GitHub Actions (CI/CD)  
- IntelliJ IDEA  

---

## ✅ Testing & Quality Assurance
- Unit tests for core services  
- Integration tests for main user flows  
- Automated CI pipeline running tests on every push  
- Test cases documented with results and screenshots  

---

## ⚙️ How to Run the Project

### Backend
```bash
cd backend
./mvnw spring-boot:run
