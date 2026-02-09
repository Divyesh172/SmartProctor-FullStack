import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';

const ProfessorDashboard = () => {
    // Form State
    const [examData, setExamData] = useState({ subject: '', examCode: '' });
    const [message, setMessage] = useState('');

    // List State (Live Exams)
    const [activeExams, setActiveExams] = useState([]);

    // 1. Fetch Active Exams on Load
    const fetchExams = async () => {
        try {
            const response = await api.get('/active'); // Reusing the endpoint we made for students
            setActiveExams(response.data);
        } catch (error) {
            console.error("Error loading exams", error);
        }
    };

    useEffect(() => {
        fetchExams();
    }, []);

    // 2. Handle Create Exam
    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            // Call the Java Backend
            await api.post('/create', examData);

            setMessage(`✅ Successfully created: ${examData.subject}`);
            setExamData({ subject: '', examCode: '' }); // Reset Form
            fetchExams(); // Refresh the list immediately
        } catch (error) {
            setMessage("❌ Error creating exam. Code might duplicate.");
        }
    };

    return (
        <div className="container">
            <div className="row justify-content-center">

                {/* LEFT COLUMN: Create Exam */}
                <div className="col-md-5 mb-4">
                    <div className="glass-card p-4 fade-in h-100">
                        <h4 className="text-white mb-4">🚀 Launch New Exam</h4>

                        {message && <div className="alert alert-info py-2">{message}</div>}

                        <form onSubmit={handleSubmit}>
                            <div className="form-floating mb-3">
                                <input
                                    type="text" className="form-control" name="subject" placeholder="Subject"
                                    value={examData.subject}
                                    onChange={(e) => setExamData({...examData, subject: e.target.value})}
                                    required
                                />
                                <label>Subject Name (e.g. Physics)</label>
                            </div>

                            <div className="form-floating mb-4">
                                <input
                                    type="text" className="form-control" name="examCode" placeholder="Code"
                                    value={examData.examCode}
                                    onChange={(e) => setExamData({...examData, examCode: e.target.value})}
                                    required
                                />
                                <label>Unique Code (e.g. PHY_101)</label>
                            </div>

                            <button className="btn btn-primary w-100 py-3 rounded-pill">
                                Create Session
                            </button>
                        </form>
                    </div>
                </div>

                {/* RIGHT COLUMN: Active List */}
                <div className="col-md-5 mb-4">
                    <div className="glass-card p-4 fade-in h-100">
                        <h4 className="text-white mb-4">📡 Live Sessions</h4>

                        {activeExams.length === 0 ? (
                            <p className="text-white-50">No exams active.</p>
                        ) : (
                            <div className="list-group list-group-flush rounded">
                                {activeExams.map((exam) => (
                                    <div key={exam.code} className="list-group-item bg-transparent text-white border-bottom border-secondary d-flex justify-content-between align-items-center">
                                        <div>
                                            <h5 className="mb-0">{exam.subject}</h5>
                                            <small className="text-white-50">Code: <span className="text-warning">{exam.code}</span></small>
                                        </div>
                                        <span className="badge bg-success rounded-pill">Active</span>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>

            </div>
        </div>
    );
};

export default ProfessorDashboard;