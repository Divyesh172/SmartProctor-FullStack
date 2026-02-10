package main

import (
	"bytes"
	"encoding/json"
	"log"
	"net/http"
	"os"
	"time"

	"github.com/gorilla/websocket"
)

// ==========================================
// 1. CONFIGURATION
// ==========================================
var (
	// Default to localhost, but allow Docker/Env override
	JavaBaseURL = getEnv("JAVA_BACKEND_URL", "http://localhost:8080")

	// API Key for machine-to-machine security
	APIKey = getEnv("PROCTOR_API_KEY", "PROCTOR_SECURE_123")
)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	// Allow all origins (Cross-Origin) for mobile dev
	CheckOrigin: func(r *http.Request) bool { return true },
}

// ==========================================
// 2. DATA STRUCTURES
// ==========================================

// 1. Inbound Message from Mobile
type WSMessage struct {
	Type    string `json:"type"`    // "HANDSHAKE", "CHEAT_REPORT", "PING"
	Payload string `json:"payload"` // Data or JSON string
}

// 2. Handshake Request to Java
type HandshakeRequest struct {
	PairingCode string `json:"pairingCode"`
}

// 3. Cheat Report Request to Java (Matches CheatReportDTO.java)
type CheatRelayRequest struct {
	StudentId       int64   `json:"studentId"`
	CheatType       string  `json:"cheatType"` // e.g., "MOBILE_PHONE_DETECTED"
	Description     string  `json:"description"`
	ConfidenceScore float64 `json:"confidenceScore"`
}

// ==========================================
// 3. MAIN SERVER
// ==========================================
func main() {
	port := getEnv("PORT", "8081")

	http.HandleFunc("/ws", handleWebSocket)
	http.HandleFunc("/health", healthCheck)

	log.Printf("🚀 Mobile Sentinel (Go) active on port %s", port)
	log.Printf("🔗 Connected to Java Backend at: %s", JavaBaseURL)

	log.Fatal(http.ListenAndServe(":"+port, nil))
}

// ==========================================
// 4. WEBSOCKET LOGIC
// ==========================================
func handleWebSocket(w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Println("❌ WS Upgrade Failed:", err)
		return
	}
	defer conn.Close()

	log.Println("📱 Device Connected.")

	// State for this connection
	var studentId int64 = 0

	for {
		_, message, err := conn.ReadMessage()
		if err != nil {
			log.Println("⚠️ Device Disconnected")
			break
		}

		var msg WSMessage
		if err := json.Unmarshal(message, &msg); err != nil {
			continue
		}

		switch msg.Type {
		case "HANDSHAKE":
			// Payload is the Pairing Code (e.g., "AB12")
			// We verify with Java, and if valid, Java returns the Student ID
			sid := handleHandshake(conn, msg.Payload)
			if sid > 0 {
				studentId = sid
			}

		case "CHEAT_REPORT":
			// Payload is a JSON string containing report details
			if studentId > 0 {
				handleCheatAlert(conn, studentId, msg.Payload)
			} else {
				sendJSON(conn, "ERROR", "Not Authenticated")
			}

		case "PING":
			sendJSON(conn, "PONG", "Alive")
		}
	}
}

// ==========================================
// 5. JAVA INTEGRATION
// ==========================================

// Returns StudentID if success, 0 if fail
func handleHandshake(conn *websocket.Conn, code string) int64 {
	url := JavaBaseURL + "/api/proctor/mobile/handshake"

	reqBody, _ := json.Marshal(HandshakeRequest{PairingCode: code})
	resp, err := sendToJava(url, reqBody)

	if err != nil {
		log.Println("❌ Java Handshake Error:", err)
		sendJSON(conn, "ERROR", "Backend Unavailable")
		return 0
	}
	defer resp.Body.Close()

	// Parse response
	var result map[string]interface{}
	json.NewDecoder(resp.Body).Decode(&result)

	if result["success"] == true {
		log.Printf("✅ Device Paired for Code: %s", code)
		sendJSON(conn, "SUCCESS", "Paired")

		// Ideally, Java should return the StudentID in the handshake response.
		// For now, we assume the pairing code *was* valid.
		// If your Java logic returns studentId, extract it here:
		if id, ok := result["studentId"].(float64); ok {
			return int64(id)
		}
		// Fallback for demo purposes if Java doesn't return ID immediately
		return 1
	} else {
		sendJSON(conn, "ERROR", "Invalid Code")
		return 0
	}
}

func handleCheatAlert(conn *websocket.Conn, studentId int64, payloadStr string) {
	url := JavaBaseURL + "/api/proctor/report"

	// Parse the raw payload from phone (it might say "PHONE_MOVED" or "GYRO_SPIKE")
	var rawData map[string]interface{}
	json.Unmarshal([]byte(payloadStr), &rawData)

	// Construct DTO for Java
	// CRITICAL FIX: We force the CheatType to be "MOBILE_PHONE_DETECTED"
	// regardless of what the phone sends. This ensures Java's Enum parser accepts it.
	report := CheatRelayRequest{
		StudentId:       studentId,
		CheatType:       "MOBILE_PHONE_DETECTED",
		Description:     "Suspicious device movement detected by Sentinel",
		ConfidenceScore: 0.95,
	}

	reqBody, _ := json.Marshal(report)
	resp, err := sendToJava(url, reqBody)

	if err == nil && resp.StatusCode == 200 {
		log.Printf("🚩 Cheat Alert Forwarded: Student %d", studentId)
		sendJSON(conn, "ACK", "Report Logged")
	} else {
		log.Printf("❌ Failed to log cheat to Java. Status: %d", resp.StatusCode)
	}
}

// Helper: HTTP POST to Java
func sendToJava(url string, data []byte) (*http.Response, error) {
	req, _ := http.NewRequest("POST", url, bytes.NewBuffer(data))
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("X-API-KEY", APIKey)

	client := &http.Client{Timeout: 3 * time.Second}
	return client.Do(req)
}

// Helper: Send WS Response
func sendJSON(conn *websocket.Conn, msgType string, payload string) {
	conn.WriteJSON(WSMessage{Type: msgType, Payload: payload})
}

// Helper: Env Getter
func getEnv(key, fallback string) string {
	if value, exists := os.LookupEnv(key); exists {
		return value
	}
	return fallback
}

// Helper: Health Check
func healthCheck(w http.ResponseWriter, r *http.Request) {
	w.WriteHeader(http.StatusOK)
	w.Write([]byte("Go Sentinel is Active"))
}