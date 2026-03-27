import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./routes/ProtectedRoute";

import Login from "./auth/Login";
import Register from "./auth/Register";

import Navbar from "./components/navbar/Navbar";

import ProblemsList from "./pages/problems/ProblemsList";
import ProblemDetail from "./pages/problems/ProblemDetail";

import MySubmissions from "./pages/submission/MySubmissions";
import SubmissionDetail from "./pages/submission/SubmissionDetail";

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>

          {/* PUBLIC */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* PROBLEMS */}
          <Route
            path="/problems"
            element={
              <ProtectedRoute>
                <>
                  <Navbar />
                  <ProblemsList />
                </>
              </ProtectedRoute>
            }
          />

          <Route
            path="/problems/:id"
            element={
              <ProtectedRoute>
                <>
                  <Navbar />
                  <ProblemDetail />
                </>
              </ProtectedRoute>
            }
          />

          {/* SUBMISSIONS */}
          <Route
            path="/submissions"
            element={
              <ProtectedRoute>
                <>
                  <Navbar />
                  <MySubmissions />
                </>
              </ProtectedRoute>
            }
          />

          <Route
            path="/submissions/:id"
            element={
              <ProtectedRoute>
                <>
                  <Navbar />
                  <SubmissionDetail />
                </>
              </ProtectedRoute>
            }
          />

          {/* DEFAULT */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <>
                  <Navbar />
                  <ProblemsList />
                </>
              </ProtectedRoute>
            }
          />

        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;