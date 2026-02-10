import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from '../api/axiosConfig'; // Auto-attaches JWT Token
import '../App.css';

const ProfessorDashboard = () => {
    const [exams, setExams] = useState([]);
    const [loading, setLoading] = useState(true);
    const [view, setView] = useState('list'); // 'list', 'create', 'results'
    const [selectedExam, setSelectedExam] = useState(null);
    const [examResults, setExamResults] = useState([]);

    // Form State for Creating Exam
    const [newExam, setNewExam] = useState({
        title: '',
        description: '',
        durationMinutes: 60,
        questions: []
    });

    // Temporary Question State
    const [currentQuestion, setCurrentQuestion] = useState({
        questionText: '',
        options: ['', '', '', ''],
        correctOptionIndex: 0
    });

    const navigate = useNavigate();

    // ==========================================
    // 1. FETCH EXAMS ON LOAD
    // ==========================================
    useEffect(() => {
        fetchExams();
    }, []);

    const fetchExams = async () => {
        try {
            // MATCHING BACKEND: ExamController.java -> GET /api/exams/my-exams
            const response = await axios.get('/exams/my-exams');
            setExams(response.data);
            setLoading(false);
        } catch (error) {
            console.error("Failed to fetch exams", error);
            setLoading(false);
        }
    };

    // ==========================================
    // 2. CREATE EXAM LOGIC
    // ==========================================
    const handleAddQuestion = () => {
        if (!currentQuestion.questionText) return alert("Question text is required");

        setNewExam({
            ...newExam,
            questions: [...newExam.questions, currentQuestion]
        });

        // Reset Question Form
        setCurrentQuestion({
            questionText: '',
            options: ['', '', '', ''],
            correctOptionIndex: 0
        });
    };

    const handleCreateExamSubmit = async () => {
        if (newExam.questions.length === 0) return alert("Add at least one question!");

        try {
            // MATCHING BACKEND: ExamController.java -> POST /api/exams/create
            await axios.post('/exams/create', newExam);
            alert("Exam Created Successfully!");
            setView('list');
            fetchExams(); // Refresh list
        } catch (error) {
            alert("Failed to create exam: " + (error.response?.data?.message || error.message));
        }
    };

    // ==========================================
    // 3. VIEW RESULTS LOGIC
    // ==========================================
    const handleViewResults = async (examCode) => {
        try {
            // MATCHING BACKEND: ExamController.java -> GET /api/exams/{code}/results
            const response = await axios.get(`/exams/${examCode}/results`);
            setExamResults(response.data); // Should return list of StudentResults
            setSelectedExam(examCode);
            setView('results');
        } catch (error) {
            alert("Could not fetch results. (Ensure students have submitted)");
        }
    };

    // ==========================================
    // 4. HELPER FUNCTIONS
    // ==========================================
    const copyCode = (code) => {
        navigator.clipboard.writeText(code);
        alert(`Copied Exam Code: ${code}`);
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        navigate('/');
    };

    // ==========================================
    // 5. RENDER UI
    // ==========================================
    return (
        <div className="dashboard-container">
            {/* SIDEBAR */}
            <aside className="sidebar">
                <div className="profile-section">
                    <h3>👨‍🏫 Professor</h3>
                    <p>Dashboard</p>
                </div>
                <nav>
                    <button onClick={() => setView('list')} className={view === 'list' ? 'active' : ''}>
                        📄 My Exams
                    </button>
                    <button onClick={() => setView('create')} className={view === 'create' ? 'active' : ''}>
                        ➕ Create New Exam
                    </button>
                    <button onClick={handleLogout} className="btn-logout">
                        🚪 Logout
                    </button>
                </nav>
            </aside>

            {/* MAIN CONTENT */}
            <main className="main-content">

                {/* VIEW 1: EXAM LIST */}
                {view === 'list' && (
                    <div className="exam-list">
                        <h2>My Exams</h2>
                        {loading ? <p>Loading...</p> : exams.length === 0 ? <p>No exams found. Create one!</p> : (
                            <div className="card-grid">
                                {exams.map((exam) => (
                                    <div key={exam.id} className="exam-card">
                                        <h3>{exam.title}</h3>
                                        <div className="exam-code-box" onClick={() => copyCode(exam.examCode)}>
                                            <code>{exam.examCode}</code>
                                            <span className="copy-icon">📋</span>
                                        </div>
                                        <p>{exam.questions ? exam.questions.length : 0} Questions</p>
                                        <p>⏱ {exam.durationMinutes} mins</p>
                                        <button className="btn-view-results" onClick={() => handleViewResults(exam.examCode)}>
                                            View Results 📊
                                        </button>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                )}

                {/* VIEW 2: CREATE EXAM FORM */}
                {view === 'create' && (
                    <div className="create-exam-form">
                        <h2>Create New Exam</h2>

                        <div className="form-section">
                            <input
                                type="text" placeholder="Exam Title"
                                value={newExam.title}
                                onChange={(e) => setNewExam({...newExam, title: e.target.value})}
                            />
                            <input
                                type="text" placeholder="Description"
                                value={newExam.description}
                                onChange={(e) => setNewExam({...newExam, description: e.target.value})}
                            />
                            <input
                                type="number" placeholder="Duration (Minutes)"
                                value={newExam.durationMinutes}
                                onChange={(e) => setNewExam({...newExam, durationMinutes: parseInt(e.target.value)})}
                            />
                        </div>

                        <hr />

                        <div className="question-builder">
                            <h3>Add Question ({newExam.questions.length + 1})</h3>
                            <input
                                type="text" placeholder="Question Text" className="q-input"
                                value={currentQuestion.questionText}
                                onChange={(e) => setCurrentQuestion({...currentQuestion, questionText: e.target.value})}
                            />

                            <div className="options-grid">
                                {currentQuestion.options.map((opt, idx) => (
                                    <div key={idx} className="option-row">
                                        <input
                                            type="radio" name="correctOpt"
                                            checked={currentQuestion.correctOptionIndex === idx}
                                            onChange={() => setCurrentQuestion({...currentQuestion, correctOptionIndex: idx})}
                                        />
                                        <input
                                            type="text" placeholder={`Option ${idx + 1}`}
                                            value={opt}
                                            onChange={(e) => {
                                                const newOpts = [...currentQuestion.options];
                                                newOpts[idx] = e.target.value;
                                                setCurrentQuestion({...currentQuestion, options: newOpts});
                                            }}
                                        />
                                    </div>
                                ))}
                            </div>

                            <button className="btn-secondary" onClick={handleAddQuestion}>
                                + Add Question
                            </button>
                        </div>

                        <div className="questions-preview">
                            <h4>Questions Added: {newExam.questions.length}</h4>
                            <ul>
                                {newExam.questions.map((q, i) => (
                                    <li key={i}>{i+1}. {q.questionText}</li>
                                ))}
                            </ul>
                        </div>

                        <button className="btn-primary btn-large" onClick={handleCreateExamSubmit}>
                            🚀 Publish Exam
                        </button>
                    </div>
                )}

                {/* VIEW 3: RESULTS & CHEAT REPORTS */}
                {view === 'results' && (
                    <div className="results-view">
                        <button className="btn-back" onClick={() => setView('list')}>← Back to Exams</button>
                        <h2>Results for: {selectedExam}</h2>

                        <table className="results-table">
                            <thead>
                            <tr>
                                <th>Student Name</th>
                                <th>Score</th>
                                <th>Status</th>
                                <th>Suspicion Score</th>
                                <th>Incidents</th>
                            </tr>
                            </thead>
                            <tbody>
                            {examResults.map((res) => (
                                <tr key={res.studentId} className={res.suspicionScore > 50 ? 'row-danger' : 'row-safe'}>
                                    <td>{res.studentName}</td>
                                    <td>{res.score}%</td>
                                    <td>{res.status}</td>
                                    <td>
                                            <span className={`badge ${res.suspicionScore > 50 ? 'badge-red' : 'badge-green'}`}>
                                                {res.suspicionScore}%
                                            </span>
                                    </td>
                                    <td>
                                        <button className="btn-details">View Details</button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                        {examResults.length === 0 && <p>No students have submitted this exam yet.</p>}
                    </div>
                )}

            </main>
        </div>
    );
};

export default ProfessorDashboard;