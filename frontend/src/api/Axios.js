// src/api/Axios.js
import axios from "axios";

const API_BASE_URL = "http://localhost:8081/api";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000,
});

/**
 * REQUEST INTERCEPTOR
 * - Attach JWT if present
 * - Log outgoing request (dev only)
 */
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // DEV DEBUG (remove in prod)
    console.log(
      `[API REQUEST] ${config.method?.toUpperCase()} ${config.url}`,
      config.data || ""
    );

    return config;
  },
  (error) => Promise.reject(error)
);

/**
 * RESPONSE INTERCEPTOR
 * - Centralized auth failure handling
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (!error.response) {
      console.error("Network error or server down");
      return Promise.reject(error);
    }

    const { status } = error.response;

    if (status === 401) {
      console.warn("401 Unauthorized → clearing token");
      localStorage.removeItem("token");
      // optional: window.location.href = "/login";
    }

    return Promise.reject(error);
  }
);

export default api;
