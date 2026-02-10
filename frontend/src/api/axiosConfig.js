import axios from 'axios';

// ==========================================
// 1. Create Axios Instance
// ==========================================
const api = axios.create({
    // Matches your Spring Boot Backend URL
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

// ==========================================
// 2. Request Interceptor (The Security Gate)
// ==========================================
// This runs BEFORE every request is sent.
// It checks if we have a token and attaches it.
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// ==========================================
// 3. Response Interceptor (Error Handling)
// ==========================================
// This runs AFTER every response is received.
// If the token is expired (401), we auto-logout the user.
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            // Token expired or invalid
            localStorage.removeItem('token');
            localStorage.removeItem('role');
            // Optional: Redirect to login
            // window.location.href = '/';
        }
        return Promise.reject(error);
    }
);

export default api;