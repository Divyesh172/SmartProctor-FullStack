import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './App.css'; // Main application styles

// ==========================================
// COMPONENT IMPORTS
// ==========================================
import Home from './components/Home';
import ProfessorLogin from './components/ProfessorLogin';
import StudentRegister from './components/StudentRegister';
import ProfessorDashboard from './components/ProfessorDashboard';
import ExamDashboard from './components/ExamDashboard';

function App() {
    return (
        <Router>
            <div className="app-content">
                <Routes>
                    {/* ==========================================
              PUBLIC ROUTES
          ========================================== */}
                    {/* Landing Page */}
                    <Route path="/" element={<Home />} />

                    {/* Authentication Pages */}
                    <Route path="/professor" element={<ProfessorLogin />} />
                    <Route path="/student" element={<StudentRegister />} />

                    {/* ==========================================
              PROTECTED ROUTES
          ========================================== */}
                    {/* Professor: Create Exams & View Results */}
                    <Route path="/dashboard" element={<ProfessorDashboard />} />

                    {/* Student: The Active Exam Environment */}
                    <Route path="/exam-dashboard" element={<ExamDashboard />} />
                </Routes>
            </div>
        </Router>
    );
}

export default App;