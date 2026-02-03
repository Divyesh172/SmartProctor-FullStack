import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const ProfessorLogin = () => {
    const navigate = useNavigate();
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    const handleLogin = (e) => {
        e.preventDefault();
        // Hardcoded "Gatekeeper" for the prototype
        if (password === 'admin123') {
            // Save a flag so they don't get kicked out on refresh (Optional)
            localStorage.setItem('isAdmin', 'true');
            navigate('/professor/dashboard');
        } else {
            setError('Invalid Admin Credentials');
        }
    };

    return (
        <div className="container d-flex justify-content-center">
            <div className="glass-card p-5 fade-in" style={{ width: '400px' }}>
                <div className="text-center mb-4">
                    <div className="d-inline-flex align-items-center justify-content-center mb-3"
                         style={{
                             width: '70px', height: '70px',
                             background: 'linear-gradient(135deg, #ef4444 0%, #b91c1c 100%)', // Red/Admin Theme
                             borderRadius: '20px',
                             boxShadow: '0 4px 15px rgba(239, 68, 68, 0.3)'
                         }}>
                        <span style={{ fontSize: '32px' }}>🔒</span>
                    </div>
                    <h3 className="fw-bold text-white">Admin Access</h3>
                    <p className="text-white-50 small">Faculty Authorization Required</p>
                </div>

                {error && (
                    <div className="alert alert-danger bg-danger bg-opacity-10 border-danger text-danger text-center">
                        <small>{error}</small>
                    </div>
                )}

                <form onSubmit={handleLogin}>
                    <div className="form-floating mb-4">
                        <input
                            type="password"
                            className="form-control"
                            id="adminPass"
                            placeholder="Password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                        <label htmlFor="adminPass">Admin Password</label>
                    </div>

                    <button type="submit" className="btn btn-danger w-100 py-3 rounded-pill fw-bold">
                        Unlock Dashboard
                    </button>
                </form>

                <div className="text-center mt-4">
                    <button onClick={() => navigate('/')} className="btn btn-link text-white-50 text-decoration-none small">
                        ← Back to Student Portal
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ProfessorLogin;