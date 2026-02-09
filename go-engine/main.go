package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
    // "time" removed because it was causing the error

	"github.com/gorilla/websocket"
)

// --- CONFIGURATION ---
// If running in Docker "host" mode, use localhost:8080.
const (
	JAVA_API_URL       = "http://localhost:8080/api/exam/report-cheat"
	CHEAT_THRESHOLD    = 30 // Frames (approx 1 second)
)

// --- DATA STRUCTURES ---
type EyeData struct {
	SessionID string `json:"session_id"`
	Status    string `json:"status"`    // "SAFE", "LOOKING_AWAY", "PHONE_DETECTED", "NO_FACE"
	Timestamp string `json:"timestamp"`
}

type JavaPayload struct {
	SessionID  string `json:"session_id"`
	Reason     string `json:"reason"`
	Timestamp  string `json:"timestamp"`
	Confidence string `json:"confidence"`
}

// Track state for each student connection
type StudentState struct {
	BadFrameCount int
	ViolationSent bool
}

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool { return true },
}

// --- HANDLER ---
func handleProctorStream(w http.ResponseWriter, r *http.Request) {
	ws, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		fmt.Println("❌ Upgrade Error:", err)
		return
	}
	defer ws.Close()

	fmt.Println("🔵 New Student Connected via Python Eye")

	state := StudentState{BadFrameCount: 0, ViolationSent: false}

	for {
		// 1. READ MESSAGE
		var data EyeData
		err := ws.ReadJSON(&data)
		if err != nil {
			fmt.Println("⚪ Student Disconnected")
			break
		}

		// 2. THE JUDGE (FILTER LOGIC)
		if data.Status != "SAFE" {
			state.BadFrameCount++

			// If they have been bad for 30+ frames AND we haven't reported it recently
			if state.BadFrameCount == CHEAT_THRESHOLD {
				fmt.Printf("⚠️  VIOLATION CONFIRMED: %s (%s). Alerting Java...\n", data.Status, data.SessionID)

				// Run in background (goroutine) so we don't block the video stream
				go sendToJava(data)

				state.ViolationSent = true
			}
		} else {
			// If they look safe, reduce the counter (forgiveness mechanism)
			if state.BadFrameCount > 0 {
				state.BadFrameCount--
			}
			state.ViolationSent = false
		}
	}
}

// --- SENDER (TO JAVA) ---
func sendToJava(data EyeData) {
	payload := JavaPayload{
		SessionID:  data.SessionID,
		Reason:     data.Status,
		Timestamp:  data.Timestamp,
		Confidence: "HIGH",
	}

	jsonPayload, _ := json.Marshal(payload)

	resp, err := http.Post(JAVA_API_URL, "application/json", bytes.NewBuffer(jsonPayload))
	if err != nil {
		fmt.Println("❌ Failed to contact Java Backend:", err)
		return
	}
	defer resp.Body.Close()

	fmt.Println("✅ Java Backend Acknowledged Violation.")
}

// --- MAIN SERVER ---
func main() {
	http.HandleFunc("/ws", handleProctorStream)
	fmt.Println("🚀 Go Referee Engine running on port 8081...")
	if err := http.ListenAndServe("0.0.0.0:8081", nil); err != nil {
		fmt.Println("Server failed:", err)
	}
}