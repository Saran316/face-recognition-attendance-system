# Face Recognition Attendance System

A full-stack attendance management application that combines face recognition with secure authentication to support face enrollment, identity verification, and attendance tracking.

## Features

- **User Authentication:** Register and log in using JWT-based authentication.
- **Face Enrollment:** Register a user's face for subsequent verification.
- **Face Recognition:** Detect and compare faces using OpenCV and DeepFace.
- **Attendance Check-In:** Record attendance manually or through face verification.
- **Attendance History:** View previously recorded attendance.
- **REST APIs:** Separate backend services for attendance management and face recognition.
- **Duplicate Check-In Prevention:** Prevent duplicate attendance entries according to the backend's validation rules.

## Tech Stack

| Layer | Technologies |
|---|---|
| Frontend | React, JavaScript |
| Backend | Java, Spring Boot, Maven |
| Authentication | Spring Security, JWT |
| Database | MySQL |
| Face Recognition Service | Python, FastAPI, OpenCV, DeepFace |
| Face Embeddings | FaceNet512 |
| API Testing | Postman |
| Version Control | Git, GitHub |

## Architecture

```text
React Frontend
      |
      v
Spring Boot REST API
      |
      +------> MySQL Database
      |
      +------> FastAPI Face Service
                    |
                    +--> OpenCV Face Detection
                    +--> DeepFace / FaceNet512
                    +--> Face Comparison
```

## Project Structure

```text
face-attendance-system/
├── backend/
│   └── attendance-system/   # Spring Boot backend
├── face-service/            # FastAPI face recognition service
├── frontend/                # React frontend
├── .gitignore
└── README.md
```

## Prerequisites

Install the following before running the project:

- Java Development Kit compatible with the project's configured Java version
- Maven, or use the included Maven Wrapper
- Python 3.11
- Node.js and npm
- MySQL Server
- Git

## Setup and Installation

### 1. Clone the repository

```bash
git clone https://github.com/Saran316/face-recognition-attendance-system.git
cd face-recognition-attendance-system
```

### 2. Configure the MySQL database

Create the database in MySQL:

```sql
CREATE DATABASE face_attendance_db;
```

Configure the database connection in the backend's application configuration. Supply your local database username and password through environment variables or a local configuration file that is excluded from Git.

Do not commit database passwords, JWT secrets, or other credentials.

### 3. Start the Spring Boot backend

Open PowerShell:

```powershell
cd backend\attendance-system
```

Set the required environment variables using your own local credentials:

```powershell
$env:DB_PASSWORD = "YOUR_MYSQL_PASSWORD"
$env:JWT_SECRET = "YOUR_LOCAL_JWT_SECRET"
```

Start the backend using the Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend is configured to run at:

`http://localhost:8080`

### 4. Start the face recognition service

Open a second terminal from the project root:

```powershell
cd face-service
```

Create and activate a virtual environment:

```powershell
py -3.11 -m venv .venv
.\.venv\Scripts\Activate.ps1
```

Install the project's Python dependencies:

```powershell
pip install -r requirements.txt
```

Start the FastAPI service using the project's configured application module:

```powershell
uvicorn main:app --host 127.0.0.1 --port 8001 --reload
```

If your FastAPI entry-point file is not named `main.py`, replace `main:app` with the correct module and application variable.

The service is expected to run at:

`http://localhost:8001`

Interactive API documentation is available at:

`http://localhost:8001/docs`

### 5. Start the React frontend

Open a third terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open the local URL printed by Vite in your terminal.

## Face Recognition Workflow

1. The user registers and logs in.
2. The user enrolls their face using a suitable image.
3. The face service detects the face and generates a numerical embedding.
4. During verification, the service compares the new face embedding with the enrolled embedding.
5. If verification succeeds and the backend's validation rules are satisfied, attendance can be recorded.
6. The user can view their attendance history.

Recognition accuracy depends on image quality, lighting, face orientation, and the configured comparison threshold.

## Security and Privacy

- Keep database credentials and JWT secrets out of version control.
- Use authenticated endpoints for protected attendance operations.
- Store and process facial images and embeddings securely.
- Obtain appropriate consent before collecting biometric data.
- Use HTTPS and appropriate access controls before deploying publicly.
- Configure production secrets separately from development settings.

## Future Improvements

- Camera-based attendance capture
- Admin dashboard and attendance reports
- CSV or Excel attendance export
- Deployment with Docker
- Improved audit logging and biometric-data retention controls

## Author

**Saran Raj**

GitHub: [@Saran316](https://github.com/Saran316)

## License

Choose and add an appropriate open-source license before distributing the project for reuse.
