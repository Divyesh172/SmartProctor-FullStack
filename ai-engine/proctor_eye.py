import cv2
import mediapipe as mp
import numpy as np
import requests
import time
import threading
import os
import datetime
from ultralytics import YOLO

# ==========================================
# 1. CONFIGURATION & SETUP
# ==========================================
# Networking
JAVA_BACKEND_URL = "http://localhost:8080/api/proctor/report"
API_KEY = "PROCTOR_SECURE_123"

# File Storage (CRITICAL: Must match Java's "file.upload-dir")
# We assume the backend is running in a sibling folder.
# Adjust this path if your folder structure is different.
EVIDENCE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "../backend/uploads"))

# Thresholds
YAW_THRESHOLD = 25      # Degrees (Left/Right)
PITCH_THRESHOLD = 15    # Degrees (Up/Down)
MOUTH_OPEN_THRESHOLD = 25 # Pixels
CONFIDENCE_THRESHOLD = 0.6 # AI Confidence

# Initialize Models
print("⏳ Loading AI Models... (This may take a moment)")
mp_face_mesh = mp.solutions.face_mesh
face_mesh = mp_face_mesh.FaceMesh(min_detection_confidence=0.5, min_tracking_confidence=0.5, refine_landmarks=True)

# Load YOLOv8 Nano (Smallest & Fastest) for Object Detection
try:
    phone_model = YOLO("yolov8n.pt")
    print("✅ YOLO Model Loaded")
except Exception as e:
    print(f"⚠️ YOLO Model Failed: {e}. Object detection disabled.")
    phone_model = None

# Ensure evidence folder exists
if not os.path.exists(EVIDENCE_DIR):
    os.makedirs(EVIDENCE_DIR)
    print(f"📁 Created Evidence Directory: {EVIDENCE_DIR}")

# ==========================================
# 2. STATE MANAGEMENT
# ==========================================
class ProctorState:
    def __init__(self):
        self.student_id = None
        self.last_report_time = 0
        self.cooldown_seconds = 4.0

state = ProctorState()

# ==========================================
# 3. HELPER: EVIDENCE SAVER
# ==========================================
def save_evidence(frame, cheat_type):
    """Saves the current frame to disk and returns the Web URL"""
    timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"evidence_{state.student_id}_{cheat_type}_{timestamp}.jpg"
    filepath = os.path.join(EVIDENCE_DIR, filename)

    cv2.imwrite(filepath, frame)

    # Return the URL that the Frontend will use to display the image
    # Matches Java's static resource handler: /uploads/**
    return f"http://localhost:8080/uploads/{filename}"

# ==========================================
# 4. HELPER: API REPORTER
# ==========================================
def report_incident(cheat_type, description, confidence, frame):
    current_time = time.time()

    # Throttling
    if current_time - state.last_report_time < state.cooldown_seconds:
        return

    print(f"📸 Capturing Evidence for: {cheat_type}")

    # 1. Save Snapshot
    snapshot_url = save_evidence(frame, cheat_type)

    # 2. Prepare Payload
    payload = {
        "studentId": state.student_id,
        "cheatType": cheat_type,
        "description": description,
        "confidenceScore": confidence,
        "snapshotUrl": snapshot_url
    }

    headers = {
        "Content-Type": "application/json",
        "X-API-KEY": API_KEY
    }

    # 3. Send Async
    def send_async():
        try:
            response = requests.post(JAVA_BACKEND_URL, json=payload, headers=headers, timeout=5)
            if response.status_code == 200:
                print(f"✅ [SENT] {description} | Evidence: {snapshot_url}")
                state.last_report_time = current_time
            else:
                print(f"⚠️ [FAIL] Server Error: {response.status_code}")
        except Exception as e:
            print(f"❌ [ERR] Connection Failed: {e}")

    threading.Thread(target=send_async).start()

# ==========================================
# 5. CORE AI LOGIC
# ==========================================
def get_head_pose(landmarks, img_w, img_h):
    face_3d = []
    face_2d = []
    key_points = [33, 263, 1, 61, 291, 199]

    for idx, lm in enumerate(landmarks.landmark):
        if idx in key_points:
            x, y = int(lm.x * img_w), int(lm.y * img_h)
            face_2d.append([x, y])
            face_3d.append([x, y, lm.z])

    face_2d = np.array(face_2d, dtype=np.float64)
    face_3d = np.array(face_3d, dtype=np.float64)
    focal_length = 1 * img_w
    cam_matrix = np.array([[focal_length, 0, img_h / 2], [0, focal_length, img_w / 2], [0, 0, 1]])
    dist_matrix = np.zeros((4, 1), dtype=np.float64)

    success, rot_vec, trans_vec = cv2.solvePnP(face_3d, face_2d, cam_matrix, dist_matrix)
    rmat, jac = cv2.Rodrigues(rot_vec)
    angles, mtxR, mtxQ, Qx, Qy, Qz = cv2.RQDecomp3x3(rmat)

    return angles[0] * 360, angles[1] * 360, (int(face_2d[2][0]), int(face_2d[2][1]))

def check_mouth_open(landmarks, img_h):
    upper = landmarks.landmark[13].y * img_h
    lower = landmarks.landmark[14].y * img_h
    return (lower - upper) > MOUTH_OPEN_THRESHOLD

def detect_objects(frame):
    """Detects Cell Phones using YOLO"""
    if not phone_model: return False

    results = phone_model(frame, verbose=False)
    for r in results:
        boxes = r.boxes
        for box in boxes:
            cls_id = int(box.cls[0])
            label = phone_model.names[cls_id]
            if label == 'cell phone' and box.conf[0] > 0.5:
                return True
    return False

# ==========================================
# 6. MAIN LOOP
# ==========================================
def main():
    print("--- 🧠 SMART PROCTOR AI ENGINE v2.0 ---")

    while True:
        try:
            sid = input("Enter Active Student ID: ")
            state.student_id = int(sid)
            break
        except ValueError: pass

    cap = cv2.VideoCapture(0)

    while cap.isOpened():
        success, frame = cap.read()
        if not success: continue

        frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        results = face_mesh.process(frame_rgb)

        img_h, img_w, _ = frame.shape
        status_text = "SAFE"
        color = (0, 255, 0)

        # --- 1. OBJECT DETECTION (Phone) ---
        if detect_objects(frame):
            status_text = "⚠️ PHONE DETECTED"
            color = (0, 0, 255)
            report_incident("MOBILE_PHONE_DETECTED", "Cell phone visible in frame", 0.95, frame)

        # --- 2. FACE ANALYSIS ---
        if results.multi_face_landmarks:
            if len(results.multi_face_landmarks) > 1:
                status_text = "⚠️ MULTIPLE PEOPLE"
                color = (0, 0, 255)
                report_incident("MULTIPLE_FACES_DETECTED", "Multiple faces detected", 1.0, frame)
            else:
                for face_landmarks in results.multi_face_landmarks:
                    pitch, yaw, nose = get_head_pose(face_landmarks, img_w, img_h)

                    if yaw < -YAW_THRESHOLD:
                        status_text = "LOOKING RIGHT"
                        color = (0, 165, 255)
                        report_incident("LOOKING_AWAY", "Looking Right", 0.8, frame)
                    elif yaw > YAW_THRESHOLD:
                        status_text = "LOOKING LEFT"
                        color = (0, 165, 255)
                        report_incident("LOOKING_AWAY", "Looking Left", 0.8, frame)
                    elif pitch < -PITCH_THRESHOLD:
                        status_text = "LOOKING DOWN"
                        color = (0, 165, 255)
                        report_incident("LOOKING_AWAY", "Looking Down", 0.7, frame)
                    elif check_mouth_open(face_landmarks, img_h):
                        status_text = "TALKING / MOUTH OPEN"
                        color = (0, 0, 255)
                        report_incident("SUSPICIOUS_AUDIO", "Mouth open / Talking", 0.6, frame)

                    # Draw Nose Line
                    p2 = (int(nose[0] + yaw * 10), int(nose[1] - pitch * 10))
                    cv2.line(frame, nose, p2, (255, 0, 0), 3)
        else:
            status_text = "⚠️ NO FACE"
            color = (0, 0, 255)
            report_incident("NO_FACE_DETECTED", "User left frame", 0.9, frame)

        cv2.putText(frame, status_text, (20, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, color, 2)
        cv2.imshow('Smart Proctor Eye', frame)

        if cv2.waitKey(5) & 0xFF == 27: break

    cap.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    main()