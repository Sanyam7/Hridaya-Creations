import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "./Auth.css";

export default function SignupPage() {
  const { signup, authError, clearError, currentUser } = useAuth();
  const navigate = useNavigate();

  const [form, setForm]       = useState({ name: "", email: "", password: "", confirm: "" });
  const [loading, setLoading] = useState(false);
  const [showPwd, setShowPwd] = useState(false);
  const [localErr, setLocalErr] = useState("");

  useEffect(() => {
    if (currentUser) navigate("/");
  }, [currentUser, navigate]);

  useEffect(() => { clearError(); }, []);

  const set = (k, v) => { setForm(p => ({ ...p, [k]: v })); setLocalErr(""); };

  const validate = () => {
    if (!form.name.trim())         return "Please enter your name.";
    if (form.password.length < 6)  return "Password must be at least 6 characters.";
    if (form.password !== form.confirm) return "Passwords do not match.";
    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const err = validate();
    if (err) { setLocalErr(err); return; }
    setLoading(true);
    await new Promise(r => setTimeout(r, 500));
    const ok = signup({ name: form.name.trim(), email: form.email, password: form.password });
    setLoading(false);
    if (ok) navigate("/");
  };

  const displayError = localErr || authError;

  const strength = () => {
    const p = form.password;
    if (!p) return null;
    if (p.length < 4)  return { label: "Weak",   cls: "weak" };
    if (p.length < 8)  return { label: "Fair",   cls: "fair" };
    return              { label: "Strong", cls: "strong" };
  };
  const str = strength();

  return (
    <div className="auth-page">
      <div className="auth-bg" />
      <div className="auth-orb auth-orb1" />
      <div className="auth-orb auth-orb2" />

      <div className="auth-card">
        <Link to="/" className="auth-logo">Hridaya Creations</Link>
        <p className="auth-tagline">Create your account and start personalizing!</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-field">
            <label className="auth-label">Full Name</label>
            <input
              className="auth-input"
              type="text"
              placeholder="Your full name"
              value={form.name}
              onChange={e => set("name", e.target.value)}
              required
            />
          </div>

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
                placeholder="Min. 6 characters"
                value={form.password}
                onChange={e => set("password", e.target.value)}
                required
              />
              <button type="button" className="pwd-toggle" onClick={() => setShowPwd(s => !s)}>
                {showPwd ? "🙈" : "👁️"}
              </button>
            </div>
            {str && (
              <div className="pwd-strength">
                <div className={`strength-bar ${str.cls}`} />
                <span className={`strength-label ${str.cls}`}>{str.label}</span>
              </div>
            )}
          </div>

          <div className="auth-field">
            <label className="auth-label">Confirm Password</label>
            <input
              className="auth-input"
              type="password"
              placeholder="Re-enter your password"
              value={form.confirm}
              onChange={e => set("confirm", e.target.value)}
              required
            />
          </div>

          {displayError && <div className="auth-error">⚠ {displayError}</div>}

          <button className="auth-submit" type="submit" disabled={loading}>
            {loading ? <span className="spinner" /> : "Create Account →"}
          </button>
        </form>

        <p className="auth-terms">
          By signing up you agree to our{" "}
          <span className="terms-link">Terms of Service</span> &amp;{" "}
          <span className="terms-link">Privacy Policy</span>.
        </p>

        <p className="auth-switch">
          Already have an account?{" "}
          <Link to="/login" className="auth-switch-link">Login here</Link>
        </p>

        <Link to="/" className="back-home">← Back to Home</Link>
      </div>
    </div>
  );
}
