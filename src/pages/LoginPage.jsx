import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "./Auth.css";

export default function LoginPage() {
  const { login, authError, clearError, currentUser } = useAuth();
  const navigate = useNavigate();

  const [form, setForm]       = useState({ email: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [showPwd, setShowPwd] = useState(false);

  // Redirect if already logged in
  useEffect(() => {
    if (currentUser) navigate("/");
  }, [currentUser, navigate]);

  useEffect(() => { clearError(); }, []);

  const set = (k, v) => setForm(p => ({ ...p, [k]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    await new Promise(r => setTimeout(r, 500)); // small UX delay
    const ok = login({ email: form.email, password: form.password });
    setLoading(false);
    if (ok) navigate("/");
  };

  const fillDemo = () => setForm({ email: "demo@hridaya.com", password: "demo123" });

  return (
    <div className="auth-page">
      <div className="auth-bg" />
      <div className="auth-orb auth-orb1" />
      <div className="auth-orb auth-orb2" />

      <div className="auth-card">
        {/* Logo */}
        <Link to="/" className="auth-logo">Hridaya Creations</Link>
        <p className="auth-tagline">Welcome back! Log in to continue.</p>

        {/* Demo hint */}
        <div className="demo-hint">
          <span>🎯 Try the demo account:</span>
          <button className="demo-fill" onClick={fillDemo}>Fill credentials</button>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-field">
            <label className="auth-label">Email Address</label>
            <input
              className="auth-input"
              type="email"
              placeholder="you@example.com"
              value={form.email}
              onChange={e => set("email", e.target.value)}
              required
            />
          </div>

          <div className="auth-field">
            <label className="auth-label">Password</label>
            <div className="pwd-wrap">
              <input
                className="auth-input"
                type={showPwd ? "text" : "password"}
                placeholder="Your password"
                value={form.password}
                onChange={e => set("password", e.target.value)}
                required
              />
              <button type="button" className="pwd-toggle" onClick={() => setShowPwd(s => !s)}>
                {showPwd ? "🙈" : "👁️"}
              </button>
            </div>
          </div>

          {authError && <div className="auth-error">⚠ {authError}</div>}

          <button className="auth-submit" type="submit" disabled={loading}>
            {loading ? <span className="spinner" /> : "Login →"}
          </button>
        </form>

        <p className="auth-switch">
          Don't have an account?{" "}
          <Link to="/signup" className="auth-switch-link">Sign Up Free</Link>
        </p>

        <Link to="/" className="back-home">← Back to Home</Link>
      </div>
    </div>
  );
}
