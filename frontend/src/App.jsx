import "bootstrap/dist/css/bootstrap.min.css";
import { Routes, Route } from 'react-router-dom';
import StudentRegister from "./components/StudentRegister";
import ExamDashboard from "./components/ExamDashboard";
import ProfessorLogin from "./components/ProfessorLogin"; // <--- Import
import ProfessorDashboard from "./components/ProfessorDashboard"; // <--- Import

function App() {
    return (
        <Routes>
            {/* Student Routes */}
            <Route path="/" element={<StudentRegister />} />
            <Route path="/dashboard" element={<ExamDashboard />} />

            {/* Professor Routes */}
            <Route path="/admin" element={<ProfessorLogin />} />
            <Route path="/professor/dashboard" element={<ProfessorDashboard />} />
        </Routes>
    );
}

export default App;