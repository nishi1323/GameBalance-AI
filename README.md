# GameBalance AI – Smart Gaming Addiction Monitoring and Student Wellness System

GameBalance AI is a full-stack academic and student wellness web application designed to help college students maintain a healthy balance between gaming, academics, physical activity, and sleep. It combines real-time gaming session tracking, smart daily schedule planning, study task reminders, sleep pattern correlation, a Groq-powered AI Wellness Assistant, and an educational gaming habit risk estimator.

> **Disclaimer:** GameBalance AI is an educational student wellness tool. Its AI Risk Detector and AI Wellness Assistant do **not** provide medical or clinical diagnoses. Students experiencing severe distress or difficulty controlling gaming habits should consult a campus counselor or qualified healthcare professional.

---

## Key Features

1. **Authentication & Protected Routes**
   - Student registration (full name, email, password, confirm password, age, optional college name)
   - JWT-based stateless authentication & BCrypt password hashing
   - Protected React routes and user-scoped MongoDB records
2. **Wellness Dashboard Overview**
   - Real-time metrics: today's gaming time, daily gaming limit, remaining budget, weekly/daily study hours, sleep duration, and latest wellness risk level
   - Interactive 7-day gaming and study charts, progress bars, recent sessions, upcoming deadlines, sleep summary, and AI daily wellness suggestion
3. **Gaming Activity Tracker**
   - Start/Stop live gaming sessions with game selection (preset + custom) and notes
   - Persists active sessions in MongoDB so the live timer survives page refreshes
   - Daily, weekly, monthly, and total gaming stats + daily limit exceedance warnings
4. **AI Gaming Addiction Risk Detector**
   - Collects gaming hours, weekend play, sleep duration, late-night sessions, missed study tasks, and behavioral self-check items
   - Combines a deterministic weighted scoring engine (0–100) with Groq LLM insights
   - Outputs risk category (`LOW`, `MODERATE`, `HIGH`, `VERY_HIGH`), unhealthy patterns, sleep/academic impact, and personalized recommendations
5. **Study Reminder & Study Planner**
   - Full CRUD for study tasks with subject, description, due date/time, estimated hours, and priority (`LOW`, `MEDIUM`, `HIGH`)
   - Filterable pending/completed views, progress bar, weekly study chart
   - Browser `Notification` alerts for upcoming deadlines + in-app reminder banner
6. **Smart Gaming Planner**
   - Customize daily gaming limits and preferred gaming time windows
   - Schedule planned gaming sessions with automatic overlap conflict detection
   - Planned vs. actual 7-day comparison chart and balanced schedule recommendations
7. **Sleep & Health Tracker**
   - Log sleep time, wake-up time, sleep quality (1–5), and late-night gaming correlation
   - Detects irregular sleep patterns and compares rest on late-night gaming vs. non-late-night days
   - AI-powered sleep improvement guidance
8. **AI Wellness Assistant**
   - Interactive Groq-powered student wellness chatbot with persistent MongoDB chat history
   - Context-aware advice using the student's live gaming, study, sleep, and risk metrics
9. **Modern Dark & Light UI**
   - Responsive sidebar & navbar layout, Lucide icons, interactive SVG charts, and persistent dark/light theme toggle

---

## Technology Stack

- **Frontend:** React 18, Vite, React Router DOM, Axios, HTML5, CSS3, Lucide React
- **Backend:** Java 17, Spring Boot 3.2.5, Spring Web, Spring Security, JWT (`jjwt`), Bean Validation
- **Database:** MongoDB, Spring Data MongoDB
- **AI Integration:** Groq Chat Completions API (backend-only via environment variable `GROQ_API_KEY`)

---

## Project Folder Structure

```text
GameBalanceAI/
├── .env.example
├── README.md
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/gamebalance/
│       │   │   ├── GameBalanceApplication.java
│       │   │   ├── config/          # CorsConfig, RestTemplateConfig
│       │   │   ├── controller/      # REST API controllers
│       │   │   ├── dto/             # Request & response DTOs with validation
│       │   │   ├── exception/       # GlobalExceptionHandler & custom exceptions
│       │   │   ├── model/           # 7 MongoDB @Document models
│       │   │   ├── repository/      # Spring Data MongoDB repositories
│       │   │   ├── security/        # JwtUtil, JwtAuthenticationFilter, SecurityConfig
│       │   │   └── service/         # Business logic + GroqService
│       │   └── resources/
│       │       └── application.properties
│       └── test/java/com/gamebalance/
│           └── GameBalanceTests.java
└── frontend/
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── components/      # Sidebar, Navbar, StatCard, Charts, ProgressBar, Modal, ThemeToggle, etc.
        ├── context/         # AuthContext, ThemeContext
        ├── hooks/           # useStudyReminders
        ├── layouts/         # DashboardLayout
        ├── pages/           # All 10 pages (Login, Register, Dashboard, Tracker, Risk, Study, Planner, Sleep, Assistant, Profile)
        └── services/        # Axios API client & endpoint wrappers
```

---

## Software Requirements

- **Java Development Kit (JDK):** 17 or higher
- **Apache Maven:** 3.8+
- **Node.js & npm:** Node 18+ and npm 9+
- **MongoDB:** Local MongoDB Community Server (`mongodb://localhost:27017`) or MongoDB Atlas cluster URI
- **Groq API Key:** Free or paid API key from [Groq Console](https://console.groq.com/)

---

## Environment Variables Configuration

Create a `.env` file (or export these variables in your terminal before starting the backend). See [`.env.example`](./.env.example):

| Variable | Description | Default |
| --- | --- | --- |
| `SERVER_PORT` | Spring Boot HTTP port | `8080` |
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/gamebalance_ai` |
| `JWT_SECRET` | Secret key (>= 32 chars) for signing JWTs | Dev fallback provided |
| `GROQ_API_KEY` | Your Groq API key (kept strictly on backend) | Empty (triggers safe rule-based fallback) |
| `GROQ_MODEL` | Groq-supported model ID | `llama-3.3-70b-versatile` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend origins | `http://localhost:5173,http://127.0.0.1:5173` |

---

## Setup & Run Instructions

### 1. Start MongoDB
Ensure MongoDB is running locally on port `27017`, or set `MONGODB_URI` to your MongoDB Atlas connection string.

### 2. Run the Spring Boot Backend
In PowerShell:
```powershell
cd backend
$env:MONGODB_URI="mongodb://localhost:27017/gamebalance_ai"
$env:JWT_SECRET="replace-with-a-strong-32-plus-char-secret-key"
$env:GROQ_API_KEY="your-groq-api-key"
$env:GROQ_MODEL="llama-3.3-70b-versatile"
mvn spring-boot:run
```
The backend starts at `http://localhost:8080`.

### 3. Run the React Frontend
In a separate terminal:
```powershell
cd frontend
npm install
npm run dev
```
The frontend starts at `http://localhost:5173` and proxies `/api` requests to `http://localhost:8080`.

---

## MongoDB Collections

- `users`
- `gaming_sessions`
- `risk_assessments`
- `study_tasks`
- `gaming_plans`
- `sleep_records`
- `chat_history`

---

## REST API Endpoints

### Health & Authentication
- `GET /api/health` – Service health & Groq status
- `POST /api/auth/register` – Register a new student
- `POST /api/auth/login` – Authenticate and receive JWT
- `GET /api/auth/me` – Get authenticated student profile
- `PUT /api/auth/profile` – Update student profile & limits

### Dashboard
- `GET /api/dashboard/overview` – Aggregated dashboard stats, charts, recent activity, AI daily tip

### Gaming Tracker
- `GET /api/gaming/stats` – Active session, today/weekly/monthly/total stats, 7-day chart, history
- `GET /api/gaming/active` – Current active gaming session
- `POST /api/gaming/start` – Start a gaming session
- `POST /api/gaming/stop/{id}` – Stop session and calculate duration
- `DELETE /api/gaming/{id}` – Delete a gaming session

### AI Risk Detector
- `POST /api/risk/analyze` – Compute rule-based + Groq AI educational wellness estimate
- `GET /api/risk/history` – Assessment history
- `GET /api/risk/latest` – Most recent assessment

### Study Planner
- `GET /api/study` – Tasks, pending/completed counts, upcoming deadlines/reminders, weekly study chart
- `POST /api/study` – Create study task
- `PUT /api/study/{id}` – Update study task
- `PATCH /api/study/{id}/toggle` – Toggle completion status
- `DELETE /api/study/{id}` – Delete study task

### Smart Gaming Planner
- `GET /api/planner` – Daily limits, planned sessions, planned vs actual chart, recommendations
- `POST /api/planner/plans` – Schedule a non-overlapping gaming session
- `DELETE /api/planner/plans/{id}` – Remove planned session
- `PUT /api/planner/settings` – Update daily gaming limit & preferred time window

### Sleep & Health Tracker
- `GET /api/sleep` – Sleep logs, averages, late-night gaming correlation, weekly chart, AI tip
- `POST /api/sleep` – Log a sleep record
- `DELETE /api/sleep/{id}` – Delete a sleep record

### AI Wellness Assistant
- `GET /api/chat/history` – Retrieve user conversation history
- `POST /api/chat/message` – Send message to Groq AI Wellness Assistant
- `DELETE /api/chat/history` – Clear conversation history
