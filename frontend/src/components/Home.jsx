import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../App.css'; // Uses the same CSS we defined earlier

const Home = () => {
    const navigate = useNavigate();

    return (
        <div className="home-container">
            {/* HERO SECTION */}
            <header className="hero">
                <div className="hero-content">
                    <h1>SmartProctor AI 👁️</h1>
                    <p className="subtitle">Next-Generation Anti-Cheating Exam Platform</p>
                    <div className="hero-badge">Powered by Computer Vision & Artificial Intelligence</div>
                </div>
            </header>

            {/* ROLE SELECTION CARDS */}
            <div className="role-selection">

                {/* STUDENT CARD */}
                <div className="role-card student-card" onClick={() => navigate('/student')}>
                    <div className="card-icon">👨‍🎓</div>
                    <h2>I am a Student</h2>
                    <p>Join an active exam session using your unique access code.</p>
                    <button className="btn-primary">Start Exam</button>
                </div>

                {/* PROFESSOR CARD */}
                <div className="role-card professor-card" onClick={() => navigate('/professor')}>
                    <div className="card-icon">👨‍🏫</div>
                    <h2>I am a Professor</h2>
                    <p>Create exams, monitor live sessions, and view cheat reports.</p>
                    <button className="btn-secondary">Admin Login</button>
                </div>

            </div>

            {/* FOOTER */}
            <footer className="home-footer">
                <p>System Status: <span className="status-ok">● Online</span></p>
                <p>© 2026 SmartProctor Inc.</p>
            </footer>
        </div>
    );
};

export default Home;