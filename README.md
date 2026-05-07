# 🏥 Dar Ettabib — Advanced Clinical Management Platform

**Dar Ettabib** (The Healing Center) is a state-of-the-art medical management solution designed to bridge the gap between patients and medical professionals. Built with a modern architecture, it offers a seamless, secure, and highly interactive experience for managing clinical workflows, appointments, and medical histories.

---

## 🌟 Key Features

### 👨‍⚕️ For Doctors
- **Personalized Clinical Dashboard**: Real-time tracking of pending appointments, completed consultations, and active patient load.
- **Dynamic Scheduling**: Automated capacity management for morning and afternoon sessions with customizable days off.
- **Patient Follow-up**: Direct access to patient medical records and consultation history.
- **Secure Messaging**: Private activity center for direct communication with patients.

### 👤 For Patients
- **Smart Booking System**: Intuitive appointment scheduling with real-time doctor availability checks.
- **Health Journey Tracking**: Access to personal medical records and consultation summaries.
- **Profile Management**: Secure registration with National ID (CIN) verification.
- **Direct Support**: One-click messaging system to reach out to medical professionals.

### 🛠️ Core Functionalities
- **Role-Based Access Control**: Secure JWT-based authentication for Admin, Doctor, and Patient roles.
- **Interactive Messaging**: Real-time notifications and unread message tracking.
- **Medical Records**: Digital storage for prescriptions, diagnoses, and consultation notes.
- **Smart Analytics**: Automated calculation of clinic metrics and system stability.

---

## 🚀 Tech Stack

### Frontend
- **Framework**: Angular 17+ (Standalone Components)
- **Styling**: Bootstrap 5 + Custom Glassmorphism UI
- **Animations**: Angular Animations for smooth transitions
- **Icons**: Bootstrap Icons

### Backend
- **Framework**: Spring Boot 3+ (Java 17)
- **Security**: Spring Security + JWT (JSON Web Tokens)
- **Persistence**: Spring Data JPA
- **Database**: MySQL 8.0

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **Build Tool**: Maven

---

## 📦 Installation & Setup

### Prerequisites
- Docker & Docker Compose
- Node.js (for local frontend development)
- Java 17+ (for local backend development)

### Quick Start with Docker
```bash
# Clone the repository
git clone git@github.com:ahmad-bensalah/Dar-Ettabib.git
cd Dar-Ettabib

# Start the entire stack
docker-compose up -d --build
```
The platform will be available at:
- **Frontend**: `http://localhost:4200`
- **Backend API**: `http://localhost:8086`

---

## 👥 Developed By

This project was developed with ❤️ and precision by:

- **Ahmed Bensalah**
- **Mohamed Yassine Abid**

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
*© 2024 Dar Ettabib — Modernizing the path to recovery.*
