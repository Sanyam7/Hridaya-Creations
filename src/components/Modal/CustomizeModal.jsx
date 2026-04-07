import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { useCart } from "../../context/CartContext";
import "./Modal.css";

const FONTS = ["Classic Serif", "Script / Cursive", "Bold Modern", "Handwritten", "Elegant Thin"];
const QTYS  = ["1","2","3","5","10","25","50","100+"];

export default function CustomizeModal({ product, onClose, onCloseAll }) {
  const { currentUser }    = useAuth();
  const { addToCart }      = useCart();
  const navigate           = useNavigate();

  const [form, setForm]       = useState({
    printName: "", message: "", qty: "1",
    color: product.colors[0], font: FONTS[0], special: "",
  });
  const [fileName, setFileName]   = useState(null);
  const [submitted, setSubmitted] = useState(false);
  const [error, setError]         = useState("");

  const set = (k, v) => setForm(p => ({ ...p, [k]: v }));

  const handleSubmit = () => {
    if (!form.printName.trim()) { setError("Please enter a name or text to print."); return; }
    if (!currentUser) {
      onClose();
      navigate("/login");
      return;
    }
    addToCart(product, { ...form, fileName });
    setSubmitted(true);
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) setFileName(file.name);
  };

  if (submitted) {
    return (
      <div className="modal-overlay" onClick={onClose}>
        <div className="modal" onClick={e => e.stopPropagation()}>
          <button className="modal-close" onClick={onClose}>✕</button>
          <div className="modal-success">
            <div className="success-icon">🎉</div>
            <h3 className="success-title">Added to Cart!</h3>
            <p className="success-text">
              Your personalized <strong>{product.name}</strong> has been added to your cart.
            </p>
            <div className="success-btns">
              <button className="btn-primary" onClick={() => { onCloseAll ? onCloseAll() : onClose(); navigate("/cart"); }}>
                View Cart 🛒
              </button>
              <button className="btn-outline" onClick={onClose}>
                Continue Shopping
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <button className="modal-close" onClick={onClose}>✕</button>

        <div className="modal-header">
          <div className="modal-emoji">{product.emoji}</div>
          <h2 className="modal-title">Customize Your {product.name}</h2>
          <p className="modal-subtitle">Make it uniquely yours</p>
          <div className="modal-price">Starting at ₹{product.price}</div>
        </div>

        {/* Features */}
        <div className="modal-features">
          {product.features.map(f => <span className="feat-chip" key={f}>✓ {f}</span>)}
        </div>

        <div className="modal-divider" />

        {/* Form */}
        <div className="form-group">
          <label className="form-label">Name / Text to Print *</label>
          <input
            className="form-input"
            placeholder="e.g. Anjali ❤️  or  Happy Birthday Mom"
            value={form.printName}
            onChange={e => set("printName", e.target.value)}
          />
        </div>

        <div className="form-group">
          <label className="form-label">Personal Message (optional)</label>
          <input
            className="form-input"
            placeholder="e.g. With love, forever yours"
            value={form.message}
            onChange={e => set("message", e.target.value)}
          />
        </div>

        <div className="form-group">
          <label className="form-label">Upload Photo / Design</label>
          <div className="upload-zone" onClick={() => document.getElementById("modal-file-up").click()}>
            <div className="upload-icon">📷</div>
            <div className="upload-text">
              {fileName
                ? <span style={{ color: "#a78bfa" }}>{fileName}</span>
                : <><span className="upload-link">Click to upload</span> or drag & drop<br />PNG · JPG · PDF — up to 10 MB</>
              }
            </div>
            <input
              id="modal-file-up" type="file"
              accept="image/*,.pdf" style={{ display: "none" }}
              onChange={handleFileChange}
            />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group" style={{ flex: 1 }}>
            <label className="form-label">Color Theme</label>
            <div className="color-options">
              {product.colors.map((c, i) => (
                <button
                  key={i}
                  className={`color-swatch ${form.color === c ? "active" : ""}`}
                  style={{ background: c === "gold" ? "linear-gradient(135deg,#ffd700,#ff8c00)" : c }}
                  onClick={() => set("color", c)}
                  title={c}
                />
              ))}
            </div>
          </div>

          <div className="form-group" style={{ flex: 1 }}>
            <label className="form-label">Font Style</label>
            <select className="form-select" value={form.font} onChange={e => set("font", e.target.value)}>
              {FONTS.map(f => <option key={f}>{f}</option>)}
            </select>
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Quantity</label>
          <div className="qty-options">
            {QTYS.map(q => (
              <button
                key={q}
                className={`qty-chip ${form.qty === q ? "active" : ""}`}
                onClick={() => set("qty", q)}
              >{q}</button>
            ))}
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Special Instructions</label>
          <textarea
            className="form-textarea"
            placeholder="Occasion, placement preference, or anything else..."
            value={form.special}
            onChange={e => set("special", e.target.value)}
          />
        </div>

        {error && <div className="form-error">⚠ {error}</div>}

        {!currentUser && (
          <div className="login-notice">
            🔒 You'll be asked to log in before adding to cart.
          </div>
        )}

        <button className="modal-submit" onClick={handleSubmit}>
          {currentUser ? "✨ Add to Cart" : "🔑 Login & Add to Cart"}
        </button>
      </div>
    </div>
  );
}
