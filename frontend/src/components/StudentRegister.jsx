import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

const StudentRegister = () => {
    const navigate = useNavigate();

    // State for the form
    const [formData, setFormData] = useState({ name: '', email: '', examCode: '' });

    // State for the list of exams
    const [exams, setExams] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    // FETCH EXAMS ON LOAD
    useEffect(() => {
        const fetchExams = async () => {
            try {
                const response = await api.get('/active');
                setExams(response.data);
                // Automatically select the first exam if available
                if (response.data.length > 0) {
                    setFormData(prev => ({ ...prev, examCode: response.data[0].code }));
                }
            } catch (err) {
                console.error("Failed to load exams", err);
                setError("Could not load active exams.");
            }
        };
        fetchExams();
    }, []);

    const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setError('');

        try {
            const response = await api.post('/register', formData);
            const idMatch = response.data.match(/\d+/);
            const studentId = idMatch ? idMatch[0] : null;

            if (studentId) {
                setTimeout(() => {
                    navigate('/dashboard', { state: { studentId: studentId } });
                }, 1000);
            }
        } catch (err) {
            console.error(err);
            const errorMsg = err.response?.data?.message || "Registration Failed";
            setError(errorMsg);
            setIsLoading(false);
        }
    };

    return (
        <div className="container d-flex justify-content-center">
            <div className="glass-card p-5 fade-in" style={{ width: '420px' }}>

                <div className="text-center mb-4">
                    <div className="d-inline-flex align-items-center justify-content-center mb-3"
                         style={{
                             width: '70px', height: '70px',
                             background: 'linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)',
                             borderRadius: '20px',
                             boxShadow: '0 4px 15px rgba(124, 58, 237, 0.3)'
                         }}>
                        <span style={{ fontSize: '32px' }}>📝</span>
                    </div>
                    <h3 className="fw-bold text-white">Exam Registration</h3>
                    <p className="text-white-50 small">Select an active exam to join</p>
                </div>

                {error && (
                    <div className="alert alert-danger bg-danger bg-opacity-10 border-danger text-danger d-flex align-items-center" role="alert">
                        <small>⚠️ {error}</small>
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div className="form-floating mb-3">
                        <input
                            type="text" className="form-control" id="floatingName"
                            name="name" placeholder="John Doe"
                            onChange={handleChange} required
                        />
                        <label htmlFor="floatingName">Full Name</label>
                    </div>

                    <div className="form-floating mb-3">
                        <input
                            type="email" className="form-control" id="floatingEmail"
                            name="email" placeholder="name@example.com"
                            onChange={handleChange} required
                        />
                        <label htmlFor="floatingEmail">Email Address</label>
                    </div>

                    {/* NEW DROPDOWN MENU */}
                    <div className="form-floating mb-4">
                        <select
                            className="form-control"
                            id="floatingCode"
                            name="examCode"
                            value={formData.examCode}
                            onChange={handleChange}
                            required
                        >
                            <option value="" disabled>Select an Exam...</option>
                            {exams.map(exam => (
                                <option key={exam.code} value={exam.code}>
                                    {exam.subject} ({exam.code})
                                </option>
                            ))}
                        </select>
                        <label htmlFor="floatingCode">Select Exam</label>
                    </div>

                    <button type="submit" className="btn btn-primary w-100 py-3 rounded-pill" disabled={isLoading}>
                        {isLoading ? (
                            <>
                                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                Registering...
                            </>
                        ) : (
                            "Register & Start Exam"
                        )}
                    </button>
                </form>

                <div className="text-center mt-4">
                    <small className="text-white-50" style={{fontSize: '0.75rem'}}>
                        * Select your subject from the list above.
                    </small>
                </div>
            </div>
        </div>
    );
};

export default StudentRegister;