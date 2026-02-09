import axios from "axios";
const api = axios.create({
    baseURL: "http://localhost:8080/api/exam",
    headers: {
        'Content-Type': 'application/json',
        'X-API-KEY' : 'PROCTOR_SECURE_123'
    }
});
export default api;