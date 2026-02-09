import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import api from '../api/axiosConfig';

const ExamDashboard = () => {
    const location = useLocation();
    const studentId = location.state?.studentId;

    const [status, setStatus] = useState({
        name: 'Loading...',
        strikeCount: 0,
        banned: false
    });

    // Polling Logic (Same as before)
    useEffect(() => {
        if (!studentId) return;

        const interval = setInterval(async () => {
            try {
                const response = await api.get(`/status?studentId=${studentId}`);
                setStatus(response.data);
            } catch (error) {
                console.error("Error fetching status:", error);
            }
        }, 2000);

        return () => clearInterval(interval);
    }, [studentId]);

    if (!studentId) {
        return (
            <div className="container d-flex justify-content-center">
                <div className="glass-card p-5 text-center text-danger">
                    <h3>⚠️ Access Denied</h3>
                    <p>Please register first.</p>
                </div>
            </div>
        );
    }

    // DYNAMIC STYLES based on status
    const isBanned = status.banned;

    return (
        <div className="container d-flex justify-content-center">
            <div className={`glass-card p-5 fade-in ${isBanned ? 'border-danger' : 'border-primary'}`}
                 style={{ width: '500px', transition: 'all 0.5s ease' }}>

                {isBanned ? (
                    <div className="text-center">
                        <div className="display-1 mb-3">🚫</div>
                        <h1 className="fw-bold text-danger mb-3">DISQUALIFIED</h1>
                        <h4 className="text-white">Cheating Detected</h4>
                    </div>
                ) : (
                    <div className="text-center">
                        <div className="badge bg-success bg-opacity-25 text-success border border-success mb-4 px-3 py-2 rounded-pill">
                            ● Live Monitoring Active
                        </div>

                        <h2 className="fw-bold text-white mb-1">Welcome, {status.name}</h2>

                        {/* --- THIS IS THE MISSING PART --- */}
                        <p className="text-white-50 mb-4">
                            Session ID: <span className="text-warning fw-bold fs-5"> {studentId} </span>
                        </p>
                        {/* ------------------------------- */}

                        <div className="row g-2 mb-5 justify-content-center">
                            {[1, 2, 3].map((strike) => (
                                <div key={strike} className="col-auto">
                                    <div style={{
                                        width: '60px', height: '10px',
                                        borderRadius: '5px',
                                        background: strike <= status.strikeCount ? '#ef4444' : 'rgba(255,255,255,0.1)',
                                        boxShadow: strike <= status.strikeCount ? '0 0 10px #ef4444' : 'none',
                                        transition: 'all 0.3s'
                                    }}></div>
                                </div>
                            ))}
                        </div>

                        <div className="mt-5 d-flex justify-content-center align-items-center flex-column">
                            <div className="spinner-grow text-primary" role="status" style={{ width: '3rem', height: '3rem' }}>
                                <span className="visually-hidden">Scanning...</span>
                            </div>
                            <p className="mt-3 text-primary small">AI Eye is Watching</p>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ExamDashboard;