import { useState, useEffect } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { useCart } from "../../context/CartContext";
import "./Navbar.css";
// import logo from "../../assets/Hridaya_logo.png";

export default function Navbar() {
  const { currentUser, logout } = useAuth();
  const { totalItems }          = useCart();
  const [scrolled, setScrolled] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const navigate                = useNavigate();
  const location                = useLocation();
  const isHome                  = location.pathname === "/";

  useEffect(() => {
    const handler = () => setScrolled(window.scrollY > 40);
    window.addEventListener("scroll", handler);
    return () => window.removeEventListener("scroll", handler);
  }, []);

  const scrollTo = (id) => {
    setMenuOpen(false);
    if (!isHome) {
      navigate("/");
      setTimeout(() => document.getElementById(id)?.scrollIntoView({ behavior: "smooth" }), 300);
    } else {
      document.getElementById(id)?.scrollIntoView({ behavior: "smooth" });
    }
  };

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <nav className={`navbar ${scrolled ? "navbar--scrolled" : ""}`}>
      {/* Logo */}
      <Link to="/" className="nav-logo">
        <img src="/assets/Hridaya.png" alt="Hridaya Creations"/>
      </Link>

      {/* Hamburger (mobile) */}
      <button className={`hamburger ${menuOpen ? "open" : ""}`} onClick={() => setMenuOpen(o => !o)}>
        <span /><span /><span />
      </button>

      {/* Links */}
      <ul className={`nav-links ${menuOpen ? "nav-links--open" : ""}`}>
        {[["Products","products"],["How It Works","how"],["Why Us","why"],["Testimonials","testi"]].map(([label, id]) => (
          <li key={id}>
            <button className="nav-anchor" onClick={() => scrollTo(id)}>{label}</button>
          </li>
        ))}
      </ul>

      {/* Right actions */}
      <div className="nav-actions">
        {/* Cart */}
        <Link to="/cart" className="cart-btn" title="Cart">
          🛒
          {totalItems > 0 && <span className="cart-badge">{totalItems}</span>}
        </Link>

        {currentUser ? (
          <div className="user-menu">
            <div className="user-avatar">{currentUser.avatar}</div>
            <span className="user-name">{currentUser.name.split(" ")[0]}</span>
            <button className="btn-logout" onClick={handleLogout}>Logout</button>
          </div>
        ) : (
          <div className="auth-btns">
            <Link to="/login"  className="btn-nav-outline">Login</Link>
            <Link to="/signup" className="btn-nav-primary">Sign Up</Link>
          </div>
        )}
      </div>
    </nav>
  );
}
