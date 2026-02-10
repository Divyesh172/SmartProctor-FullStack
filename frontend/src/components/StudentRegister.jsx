import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from '../api/axiosConfig';

const StudentRegister = () => {
    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        examCode: ''
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            // MATCHING BACKEND: StudentController.java -> /api/students/register
            const response = await axios.post('/students/register', formData);

            // Save Session Data (Temporary)
            sessionStorage.setItem('studentId', response.data.id);
            sessionStorage.setItem('examCode', response.data.examCode);
            sessionStorage.setItem('studentName', response.data.fullName);

            // Redirect to Waiting Room / Instructions
            navigate('/exam-dashboard');
        } catch (err) {
            setError(err.response?.data?.message || 'Invalid Exam Code or Server Error');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container student-theme">
            <div className="auth-box">
                <h2>Student Exam Portal</h2>
                <p className="sub-text">Enter your details to join the session.</p>

                {error && <div className="error-banner">{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Full Name</label>
                        <input
                            name="fullName"
                            required
                            value={formData.fullName}
                            onChange={handleChange}
                        />
                    </div>

                    <div className="form-group">
                        <label>University Email</label>
                        <input
                            type="email"
                            name="email"
                            required
                            value={formData.email}
                            onChange={handleChange}
                        />
                    </div>

                    <div className="form-group">
                        <label>Exam Access Code</label>
                        <input
                            name="examCode"
                            required
                            placeholder="e.g. CS101-FIN"
                            value={formData.examCode}
                            onChange={handleChange}
                            className="code-input"
                        />
                    </div>

                    <button type="submit" className="btn-block btn-student" disabled={loading}>
                        {loading ? 'Verifying...' : 'Join Exam'}
                    </button>
                </form>

                <p className="link-text" onClick={() => navigate('/')}>
                    ← Back to Home
                </p>
            </div>
        </div>
    );
};

export default StudentRegister;