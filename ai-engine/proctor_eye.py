import cv2
import numpy as np
import requests
import time
import sys

# --- THE UNIVERSAL IMPORT FIX ---
# We try every possible way to load the AI library
import mediapipe as mp
try:
    # Way 1: The Standard Way (Works on most PCs)
    mp_face_mesh = mp.solutions.face_mesh
except AttributeError:
    try:
        # Way 2: The "Explicit" Way (Forces Python to look inside)
        import mediapipe.solutions.face_mesh
        mp_face_mesh = mediapipe.solutions.face_mesh
    except (ImportError, AttributeError):
        # Way 3: The "Python Module" Way (For specific Linux setups)
        from mediapipe.python.solutions import face_mesh as mp_face_mesh

print("✅ AI Library Loaded Successfully.")
# --------------------------------

# --- 1. SETUP & CONFIGURATION ---
print("--- SMART PROCTOR SETUP ---")

# Ask for ID
try:
    user_input = input("Enter Session ID (Try 1 if unsure): ")
    STUDENT_ID = int(user_input)
except ValueError:
    print("❌ Error: ID must be a number.")
    sys.exit()

JAVA_URL = f"http://localhost:8080/api/exam/report-cheat?studentId={STUDENT_ID}"
API_KEY = "PROCTOR_SECURE_123"

# Sensitivity
YAW_THRESHOLD = 20    # Left/Right angle
PITCH_THRESHOLD = 15  # Up/Down angle

# --- 2. CONNECTION TEST ---
def test_connection():
    print(f"\n📡 Testing connection to: {JAVA_URL}...")
    try:
        headers = {'X-API-KEY': API_KEY}
        response = requests.post(JAVA_URL, headers=headers)

        if response.status_code == 200:
            print("✅ CONNECTION SUCCESSFUL! (Check Dashboard for 1 Strike)")
            return True
        elif response.status_code == 404:
            print("❌ ERROR: 404 Not Found. The Student ID is wrong.")
            print("   -> Try registering a new student to get a fresh ID.")
        else:
            print(f"❌ ERROR: Server returned status {response.status_code}")
    except requests.exceptions.ConnectionError:
        print("❌ CRITICAL ERROR: Connection Refused.")
        print("   -> Make sure your Java backend is running!")

    return False

# Run the test
if not test_connection():
    print("\n⚠️ Connection failed. Starting camera anyway for testing...")
    time.sleep(2)
else:
    print("\nStarting Camera in 2 seconds...")
    time.sleep(2)

# --- 3. AI MONITORING LOOP ---
# Initialize the AI
face_mesh = mp_face_mesh.FaceMesh(
    min_detection_confidence=0.5,
    min_tracking_confidence=0.5
)
cap = cv2.VideoCapture(0)

print("--- MONITORING ACTIVE ---")
print("Press 'q' to quit.")

last_report_time = 0

while cap.isOpened():
    success, image = cap.read()
    if not success:
        print("Ignoring empty camera frame.")
        continue

    # Flip the image (Mirror effect)
    image = cv2.flip(image, 1)
    img_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

    # SCAN THE FACE
    results = face_mesh.process(img_rgb)

    img_h, img_w, _ = image.shape
    face_3d = []
    face_2d = []

    if results.multi_face_landmarks:
        for face_landmarks in results.multi_face_landmarks:
            for idx, lm in enumerate(face_landmarks.landmark):
                # We only look at 6 key points (Nose, Chin, Eyes, Mouth)
                if idx == 33 or idx == 263 or idx == 1 or idx == 61 or idx == 291 or idx == 199:
                    if idx == 1:
                        nose_2d = (lm.x * img_w, lm.y * img_h)
                        nose_3d = (lm.x * img_w, lm.y * img_h, lm.z * 3000)

                    x, y = int(lm.x * img_w), int(lm.y * img_h)
                    face_2d.append([x, y])
                    face_3d.append([x, y, lm.z])

            face_2d = np.array(face_2d, dtype=np.float64)
            face_3d = np.array(face_3d, dtype=np.float64)

            # Camera Math (Simulating a real camera lens)
            focal_length = 1 * img_w
            cam_matrix = np.array([[focal_length, 0, img_h / 2],
                                   [0, focal_length, img_w / 2],
                                   [0, 0, 1]])
            dist_matrix = np.zeros((4, 1), dtype=np.float64)

            # SOLVE THE PUZZLE (Calculate Angles)
            success, rot_vec, trans_vec = cv2.solvePnP(face_3d, face_2d, cam_matrix, dist_matrix)
            rmat, jac = cv2.Rodrigues(rot_vec)

            # Get the angles (Yaw, Pitch, Roll)
            try:
                angles, mtxR, mtxQ, Qx, Qy, Qz = cv2.RQDecomp3x3(rmat)
            except:
                angles, mtxR, mtxQ, Qx, Qy, Qz = cv2.RQDecomp3x3(rmat)

            x_angle = angles[0] * 360 # Pitch (Up/Down)
            y_angle = angles[1] * 360 # Yaw (Left/Right)

            # JUDGMENT TIME
            text = "Focused"
            color = (0, 255, 0)
            is_cheating = False

            if y_angle < -YAW_THRESHOLD:
                text = "Looking Right"
                is_cheating = True
            elif y_angle > YAW_THRESHOLD:
                text = "Looking Left"
                is_cheating = True
            elif x_angle < -PITCH_THRESHOLD:
                text = "Looking Down"
                is_cheating = True

            # REPORTING
            if is_cheating:
                color = (0, 0, 255)
                current_time = time.time()
                # Wait 3 seconds before reporting again
                if current_time - last_report_time > 3:
                    print(f"!!! CHEATING DETECTED ({text}) !!!")
                    try:
                        requests.post(JAVA_URL, headers={'X-API-KEY': API_KEY})
                        last_report_time = current_time
                    except:
                        pass

            # DRAW ON SCREEN
            cv2.putText(image, text, (20, 50), cv2.FONT_HERSHEY_SIMPLEX, 1.5, color, 2)

            # Draw the "Pinocchio Nose" line to show direction
            p1 = (int(nose_2d[0]), int(nose_2d[1]))
            p2 = (int(nose_2d[0] + y_angle * 10), int(nose_2d[1] - x_angle * 10))
            cv2.line(image, p1, p2, (255, 0, 0), 3)

    cv2.imshow('Smart Proctor Eye', image)
    if cv2.waitKey(5) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()