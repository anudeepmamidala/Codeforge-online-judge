import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { useState } from "react";
import "./Navbar.css";

const UserNavbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate("/login");
    setMenuOpen(false);
  };

  if (!user) return null;

  return (
    <nav className="navbar navbar-user">
      <div className="navbar-container">
        
        {/* Logo */}
        <Link to="/problems" className="navbar-logo">
          <span>🚀</span>
          <span>CodeForge</span>
        </Link>

        {/* Menu */}
        <div className="navbar-menu">
          <Link to="/problems" className="navbar-link">Problems</Link>
          <Link to="/submissions" className="navbar-link">Submissions</Link>
        </div>

        {/* User Menu */}
        <div className="navbar-user-menu">
          <div
            className="navbar-user-info"
            onClick={() => setMenuOpen(!menuOpen)}
          >
            <span className="user-icon">👤</span>
            <span className="username">{user.username}</span>
            <span className="dropdown-arrow">▼</span>
          </div>

          {menuOpen && (
            <div className="navbar-dropdown">
              <button
                onClick={handleLogout}
                className="dropdown-item logout-btn"
              >
                🚪 Logout
              </button>
            </div>
          )}
        </div>

        {/* Mobile Toggle (optional, not used now) */}
        <button className="navbar-toggle">☰</button>
      </div>
    </nav>
  );
};

export default UserNavbar;