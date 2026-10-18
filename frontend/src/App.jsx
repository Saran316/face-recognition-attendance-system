

import { loginUser } from "./api/auth.js";
import "./App.css";
import { apiRequest } from "./api/client.js";
import { useRef, useState } from "react";

function App() {
  const [activeTab, setActiveTab] = useState("Dashboard");
  const [token, setToken] = useState(null);
  const [user, setUser] = useState(null);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [attendanceRecords, setAttendanceRecords] = useState([]);
const [historyLoading, setHistoryLoading] = useState(false);
const [historyError, setHistoryError] = useState("");
const today = new Date().toLocaleDateString("en-CA");
const todayRecord = attendanceRecords.find(
  (record) => record.date === today
);
const [cameraActive, setCameraActive] = useState(false);
const [cameraError, setCameraError] = useState("");
const [capturedImage, setCapturedImage] = useState(null);

const videoRef = useRef(null);
const streamRef = useRef(null);

async function startCamera() {
  setCameraError("");

  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: "user" },
      audio: false,
    });

    streamRef.current = stream;
    videoRef.current.srcObject = stream;
    await videoRef.current.play();
    setCameraActive(true);
  } catch (err) {
    setCameraError(
      "Camera access failed. Check browser permissions and try again."
    );
  }
}

function stopCamera() {
  streamRef.current?.getTracks().forEach((track) => track.stop());
  streamRef.current = null;
  setCameraActive(false);
}

function captureImage() {
  const video = videoRef.current;

  if (!video || !video.videoWidth) {
    setCameraError("Wait for the camera preview before capturing.");
    return;
  }

  const canvas = document.createElement("canvas");
  canvas.width = video.videoWidth;
  canvas.height = video.videoHeight;

  const context = canvas.getContext("2d");
  context.drawImage(video, 0, 0, canvas.width, canvas.height);

  canvas.toBlob((blob) => {
    if (blob) {
      setCapturedImage(
        new File([blob], "face-capture.jpg", { type: "image/jpeg" })
      );
      setCameraError("");
    } else {
      setCameraError("Could not capture the image. Please try again.");
    }
  }, "image/jpeg", 0.9);
}
async function handleFaceCheckIn() {
  if (!capturedImage) {
    setCameraError("Please capture your face image first.");
    return;
  }

  setCameraError("");
  setLoading(true);

  try {
    const formData = new FormData();
    formData.append("file", capturedImage);

    const result = await apiRequest(
      "/api/attendance/face-check-in",
      {
        method: "POST",
        body: formData,
      },
      token
    );

    setCameraError(
      `Check-in successful! Status: ${result.status}`
    );

    await loadAttendanceHistory();
    setActiveTab("Dashboard");
  } catch (err) {
    setCameraError(
      err.message || "Face verification failed. Please try again."
    );
  } finally {
    setLoading(false);
  }
}

  const navItems = [
    "Dashboard",
    "Face Attendance",
    "Attendance History",
  ];

 
async function handleLogin(event) {
  event.preventDefault();
  setError("");
  setLoading(true);

  try {
    const data = await loginUser(email, password);

    setToken(data.token);
    setUser({
      email: data.email,
      role: data.role,
    });
    setPassword("");
    setActiveTab("Dashboard");

    // Load attendance history immediately after login.
    const records = await apiRequest(
      "/api/attendance/my-history",
      { method: "GET" },
      data.token
    );

    setAttendanceRecords(
      Array.isArray(records) ? records : []
    );
  } catch (err) {
    setError(err.message || "Login failed. Please try again.");
  } finally {
    setLoading(false);
  }
}


async function loadAttendanceHistory() {
  setHistoryLoading(true);
  setHistoryError("");

  try {
    const records = await apiRequest(
      "/api/attendance/my-history",
      { method: "GET" },
      token
    );

    setAttendanceRecords(
      Array.isArray(records) ? records : []
    );
  } catch (err) {
    setHistoryError(
      err.message || "Could not load attendance history"
    );
  } finally {
    setHistoryLoading(false);
  }
}


  function handleLogout() {
  stopCamera();
  setCapturedImage(null);
  setToken(null);
  setUser(null);
  setEmail("");
  setPassword("");
  setActiveTab("Dashboard");
  setError("");
}

  if (!token) {
    return (
      <div className="login-page">
        <form className="login-card" onSubmit={handleLogin}>
          <h1>FaceAttend</h1>
          <p>Sign in to your attendance dashboard</p>

          <label htmlFor="email">Email address</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="Enter your email"
            autoComplete="username"
            required
          />

          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="Enter your password"
            autoComplete="current-password"
            required
          />

          {error && <p className="login-error" role="alert">{error}</p>}

          <button type="submit" disabled={loading}>
            {loading ? "Signing in..." : "Sign In"}
          </button>

          <p className="login-note">
            Use an account already registered in your backend.
          </p>
        </form>
      </div>
    );
  }

  return (
    <div className="app">
      <aside className="sidebar">
        <h2>FaceAttend</h2>
        <p className="subtitle">Attendance Management</p>

        <nav>
          {navItems.map((item) => (
            <button
              key={item}
              className={activeTab === item ? "active" : ""}
              onClick={() => {
  setActiveTab(item);

  if (item === "Attendance History") {
    loadAttendanceHistory();
  }
}}
            >
              {item}
            </button>
          ))}
        </nav>
      </aside>

      <main className="main">
        <header className="topbar">
          <div>
            <h1>{activeTab}</h1>
            <p>Face Recognition Attendance System</p>
          </div>

          <div className="profile">
            {user?.email}
            <button type="button" onClick={handleLogout}>
              Logout
            </button>
          </div>
        </header>

        {activeTab === "Dashboard" && (
          <>
            <section className="welcome">
              <div>
                <h2>Welcome back!</h2>
                <p>Manage your attendance in one place.</p>
              </div>
              <button onClick={() => setActiveTab("Face Attendance")}>
                Check In
              </button>
            </section>

            <section className="cards">
              <article className="card">
                <p>Today's Status</p>
                <h3 className={todayRecord ? "success" : ""}>
  {todayRecord ? "Present" : "Not checked in"}
</h3>
              </article>

              <article className="card">
                <p>Attendance Method</p>
                <h3>Face Recognition</h3>
              </article>

              <article className="card">
                <p>System</p>
                <h3 className="success">Backend Connected</h3>
              </article>
            </section>
          </>
        )}
{activeTab === "Face Attendance" && (
  <section className="panel">
    <h2>Face Recognition Check-In</h2>
    <p>Position your face in the camera and capture an image.</p>

    <div className="camera">
      <video
        ref={videoRef}
        autoPlay
        playsInline
        muted
        style={{
          width: "100%",
          maxWidth: "480px",
          borderRadius: "12px",
          display: cameraActive ? "block" : "none",
        }}
      />

      {!cameraActive && <p>Camera is not active.</p>}
    </div>

    {cameraError && (
      <p className="login-error" role="alert">
        {cameraError}
      </p>
    )}

    <div className="camera-actions">
      {!cameraActive ? (
        <button type="button" onClick={startCamera}>
          Start Camera
        </button>
      ) : (
        <>
          <button type="button" onClick={captureImage}>
            Capture Image
          </button>
          <button type="button" onClick={stopCamera}>
            Stop Camera
          </button>
        </>
      )}
    </div>

   
{capturedImage && (
  <div>
    <h3>Captured Image</h3>

    <img
      src={URL.createObjectURL(capturedImage)}
      alt="Captured face"
      style={{ width: "240px", borderRadius: "12px" }}
    />

    <p>Image captured successfully.</p>

    <button
      type="button"
      onClick={handleFaceCheckIn}
      disabled={loading}
    >
      {loading ? "Verifying Face..." : "Verify Face & Check In"}
    </button>
  </div>
    )}
  </section>
)}


        
{activeTab === "Attendance History" && (
  <section className="panel">
    <h2>Attendance History</h2>

    {historyLoading && <p>Loading attendance records...</p>}

    {historyError && (
      <p className="login-error" role="alert">
        {historyError}
      </p>
    )}

    {!historyLoading &&
      !historyError &&
      attendanceRecords.length === 0 && (
        <p>No attendance records found.</p>
      )}

    {!historyLoading &&
      !historyError &&
      attendanceRecords.length > 0 && (
        <div className="history-table-wrapper">
          <table className="history-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Check-in Time</th>
                <th>Status</th>
                <th>Method</th>
              </tr>
            </thead>

            <tbody>
              {attendanceRecords.map((record) => (
                <tr key={record.id ?? record.attendanceId}>
                  <td>{record.date ?? "—"}</td>
                  <td>
                    {record.checkInTime
                      ? new Date(record.checkInTime).toLocaleTimeString()
                      : "—"}
                  </td>
                  <td>{record.status ?? "—"}</td>
                  <td>{record.method ?? "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
  </section>
)}

      </main>
    </div>
  );
}

export default App;
