import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import Webcam from 'react-webcam';
import axios from '../api/axiosConfig';
import '../App.css'; // Ensure you have the CSS below

const ExamDashboard = () => {
    const navigate = useNavigate();

    // ==========================================
    // 1. STATE MANAGEMENT
    // ==========================================
    const [questions, setQuestions] = useState([]);
    const [currentQIndex, setCurrentQIndex] = useState(0);
    const [answers, setAnswers] = useState({}); // { questionId: selectedOptionIndex }
    const [timeLeft, setTimeLeft] = useState(3600); // Default 60 mins
    const [loading, setLoading] = useState(true);
    const [isFullscreen, setIsFullscreen] = useState(false);

    // Session Data
    const studentId = sessionStorage.getItem('studentId');
    const examCode = sessionStorage.getItem('examCode');
    const studentName = sessionStorage.getItem('studentName');
    // In a real app, the backend generates a random code.
    // For this demo, we use the Student ID as the Pairing Code.
    const pairingCode = studentId;

    // Webcam Ref
    const webcamRef = useRef(null);

    // ==========================================
    // 2. INITIALIZATION & SECURITY
    // ==========================================
    useEffect(() => {
        if (!studentId || !examCode) {
            alert("No active session. Please register first.");
            navigate('/student');
            return;
        }

        // 1. Enter Fullscreen
        enterFullscreen();

        // 2. Fetch Questions
        fetchExamQuestions();

        // 3. Anti-Cheat Listeners
        const handleVisibilityChange = () => {
            if (document.hidden) {
                alert("⚠️ WARNING: Tab switching is recorded! Do not leave the exam.");
                // Optional: Report to backend
                // reportCheat("TAB_SWITCH");
            }
        };

        const handleContextMenu = (e) => e.preventDefault(); // Disable Right Click

        document.addEventListener("visibilitychange", handleVisibilityChange);
        document.addEventListener("contextmenu", handleContextMenu);

        // 4. Timer Countdown
        const timer = setInterval(() => {
            setTimeLeft((prev) => {
                if (prev <= 1) {
                    clearInterval(timer);
                    handleSubmitExam();
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);

        return () => {
            document.removeEventListener("visibilitychange", handleVisibilityChange);
            document.removeEventListener("contextmenu", handleContextMenu);
            clearInterval(timer);
        };
    }, []);

    const enterFullscreen = () => {
        const elem = document.documentElement;
        if (elem.requestFullscreen) {
            elem.requestFullscreen().then(() => setIsFullscreen(true)).catch(() => {});
        }
    };

    // ==========================================
    // 3. API CALLS
    // ==========================================
    const fetchExamQuestions = async () => {
        try {
            // MATCHING BACKEND: StudentController.java -> GET /api/students/exam/{code}/questions
            const response = await axios.get(`/students/exam/${examCode}/questions`);
            setQuestions(response.data);
            setLoading(false);
        } catch (error) {
            console.error("Failed to load questions", error);
            alert("Error loading exam. Please contact proctor.");
        }
    };

    const handleOptionSelect = (qId, optionIndex) => {
        setAnswers({ ...answers, [qId]: optionIndex });
    };

    const handleSubmitAnswer = async () => {
        const currentQ = questions[currentQIndex];
        const selectedOpt = answers[currentQ.id];

        if (selectedOpt === undefined) return; // Don't submit if skipped

        try {
            // MATCHING BACKEND: StudentController.java -> POST /api/students/exam/submit-answer
            await axios.post('/students/exam/submit-answer', {
                studentId: studentId,
                examCode: examCode,
                questionId: currentQ.id,
                selectedOptionIndex: selectedOpt
            });
        } catch (error) {
            console.error("Failed to submit answer", error);
        }
    };

    const handleNext = async () => {
        // Save current answer before moving
        await handleSubmitAnswer();

        if (currentQIndex < questions.length - 1) {
            setCurrentQIndex(currentQIndex + 1);
        } else {
            handleSubmitExam();
        }
    };

    const handleSubmitExam = async () => {
        if(!window.confirm("Are you sure you want to finish the exam?")) return;

        try {
            // Save last answer
            await handleSubmitAnswer();

            // MATCHING BACKEND: StudentController.java -> POST /api/students/exam/finish
            await axios.post('/students/exam/finish', {
                studentId: studentId,
                examCode: examCode
            });

            alert("Exam Submitted Successfully! You may now close this window.");
            sessionStorage.clear();
            document.exitFullscreen();
            navigate('/');
        } catch (error) {
            alert("Submission failed. Try again.");
        }
    };

    // ==========================================
    // 4. HELPER: FORMAT TIME
    // ==========================================
    const formatTime = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
    };

    // ==========================================
    // 5. RENDER
    // ==========================================
    if (loading) return <div className="loading-screen">Loading Exam Environment...</div>;

    const currentQ = questions[currentQIndex];

    return (
        <div className="exam-dashboard">
            {/* HEADER: INFO & TIMER */}
            <header className="exam-header">
                <div className="student-info">
                    <h3>👤 {studentName}</h3>
                    <p>Exam: {examCode}</p>
                </div>

                <div className="timer-box" style={{ color: timeLeft < 300 ? 'red' : 'white' }}>
                    ⏳ {formatTime(timeLeft)}
                </div>

                <button className="btn-danger" onClick={handleSubmitExam}>Finish Exam</button>
            </header>

            <div className="exam-layout">
                {/* LEFT: QUESTION AREA */}
                <main className="question-area">
                    <div className="progress-bar">
                        Question {currentQIndex + 1} of {questions.length}
                    </div>

                    <div className="question-card">
                        <h2>{currentQ.questionText}</h2>

                        <div className="options-list">
                            {currentQ.options.map((opt, idx) => (
                                <label key={idx} className={`option-label ${answers[currentQ.id] === idx ? 'selected' : ''}`}>
                                    <input
                                        type="radio"
                                        name={`q-${currentQ.id}`}
                                        checked={answers[currentQ.id] === idx}
                                        onChange={() => handleOptionSelect(currentQ.id, idx)}
                                    />
                                    <span className="opt-text">{opt}</span>
                                </label>
                            ))}
                        </div>
                    </div>

                    <div className="nav-buttons">
                        <button
                            disabled={currentQIndex === 0}
                            onClick={() => setCurrentQIndex(currentQIndex - 1)}
                            className="btn-secondary"
                        >
                            Previous
                        </button>
                        <button
                            onClick={handleNext}
                            className="btn-primary"
                        >
                            {currentQIndex === questions.length - 1 ? 'Submit Exam' : 'Next Question'}
                        </button>
                    </div>
                </main>

                {/* RIGHT: PROCTORING SIDEBAR */}
                <aside className="proctor-sidebar">
                    {/* WEBCAM FEED */}
                    <div className="webcam-container">
                        <p className="rec-indicator">🔴 REC</p>
                        <Webcam
                            audio={false}
                            ref={webcamRef}
                            screenshotFormat="image/jpeg"
                            className="webcam-feed"
                        />
                        <p className="warning-text">Keep your face visible at all times.</p>
                    </div>

                    {/* MOBILE PAIRING */}
                    <div className="pairing-box">
                        <h4>📱 Mobile Sentinel</h4>
                        <p>Enter this code in your mobile app:</p>
                        <div className="code-display">{pairingCode}</div>
                        <p className="small-text">This connects your phone as a secondary camera.</p>
                    </div>

                    <div className="security-status">
                        <p>✅ Fullscreen Active</p>
                        <p>✅ Tab Monitoring Active</p>
                        <p>✅ Audio Monitoring Active</p>
                    </div>
                </aside>
            </div>
        </div>
    );
};

export default ExamDashboard;