import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { useCart } from "../../context/CartContext";
import { imageApi, resolveImageUrl } from "../../api";
import { colorSwatchStyle, normalizeProductColors } from "../../constants/productColors";
import { FIELD_TYPES, customerFacingOptions } from "../../constants/productCustomization";
import "./Modal.css";

const QTYS = ["1", "2", "3", "5", "10", "25", "50", "100+"];

/**
 * The customization step for a personalised product.
 *
 * Every field rendered here comes from the product's own `customizationOptions`, so the customer
 * sees exactly what the admin enabled and nothing else — there is no hardcoded field list. A
 * product with only a name option shows only a name box.
 */
export default function CustomizeModal({ product, onClose, onCloseAll }) {
  const { currentUser } = useAuth();
  const { addToCart } = useCart();
  const navigate = useNavigate();

  const options = useMemo(() => customerFacingOptions(product), [product]);
  const colors = useMemo(() => normalizeProductColors(product.colors), [product.colors]);

  const [values, setValues] = useState(() => initialValues(options, colors));
  const [qty, setQty] = useState("1");
  const [uploading, setUploading] = useState({});
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState("");

  const set = (key, value) => {
    setValues((prev) => ({ ...prev, [key]: value }));
    setError("");
  };

  const handleUpload = async (option, file) => {
    if (!file) return;
    setUploading((p) => ({ ...p, [option.key]: true }));
    setError("");
    try {
      const stored = await imageApi.upload(file);
      set(option.key, stored.url);
    } catch (e) {
      // Keep the rest of the form intact so the customer does not lose their typing.
      setError(e.message || `Could not upload your ${option.label.toLowerCase()}. Please try again.`);
    } finally {
      setUploading((p) => ({ ...p, [option.key]: false }));
    }
  };

  const validate = () => {
    for (const option of options) {
      const value = (values[option.key] || "").trim();
      if (option.required && !value) {
        return `${option.label} is required.`;
      }
      if (option.maxLength && value.length > option.maxLength) {
        return `${option.label} must not exceed ${option.maxLength} characters.`;
      }
    }
    return null;
  };

  const handleSubmit = () => {
    const problem = validate();
    if (problem) {
      setError(problem);
      return;
    }
    if (Object.values(uploading).some(Boolean)) {
      setError("Please wait for your upload to finish.");
      return;
    }
    if (!currentUser) {
      onClose();
      navigate("/login");
      return;
    }
    // Drop blanks so optional fields the customer skipped are not stored as empty strings.
    const customization = {};
    for (const option of options) {
      const value = (values[option.key] || "").trim();
      if (value) customization[option.key] = value;
    }
    addToCart(product, { customization, qty: parseInt(qty, 10) || 1 });
    setSubmitted(true);
  };

  if (submitted) {
    return (
      <div className="modal-overlay" onClick={onClose}>
        <div className="modal" onClick={(e) => e.stopPropagation()}>
          <button className="modal-close" onClick={onClose}>✕</button>
          <div className="modal-success">
            <div className="success-icon">🎉</div>
            <h3 className="success-title">Added to Cart!</h3>
            <p className="success-text">
              Your personalized <strong>{product.name}</strong> has been added to your cart.
            </p>
            <div className="success-btns">
              <button className="btn-primary" onClick={() => { (onCloseAll || onClose)(); navigate("/cart"); }}>
                View Cart 🛒
              </button>
              <button className="btn-outline" onClick={onClose}>Continue Shopping</button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <button className="modal-close" onClick={onClose}>✕</button>

        <div className="modal-header">
          {product.image ? (
            <img
              src={product.image}
              alt={product.name}
              style={{ width: 96, height: 96, objectFit: "cover", borderRadius: 14, margin: "0 auto .6rem", display: "block" }}
              onError={(e) => { e.target.style.display = "none"; }}
            />
          ) : (
            <div className="modal-emoji">{product.emoji}</div>
          )}
          <h2 className="modal-title">Customize Your {product.name}</h2>
          <p className="modal-subtitle">Make it uniquely yours</p>
          <div className="modal-price">Starting at ₹{product.price}</div>
        </div>

        {product.features?.length > 0 && (
          <div className="modal-features">
            {product.features.map((f) => <span className="feat-chip" key={f}>✓ {f}</span>)}
          </div>
        )}

        <div className="modal-divider" />

        {options.map((option) => (
          <CustomizationField
            key={option.key}
            option={option}
            value={values[option.key] || ""}
            colors={colors}
            uploading={!!uploading[option.key]}
            onChange={(v) => set(option.key, v)}
            onUpload={(file) => handleUpload(option, file)}
          />
        ))}

        <div className="form-group">
          <label className="form-label">Quantity</label>
          <div className="qty-options">
            {QTYS.map((q) => (
              <button
                key={q}
                type="button"
                className={`qty-chip ${qty === q ? "active" : ""}`}
                aria-pressed={qty === q}
                onClick={() => setQty(q)}
              >{q}</button>
            ))}
          </div>
        </div>

        {error && <div className="form-error" role="alert">⚠ {error}</div>}

        {!currentUser && (
          <div className="login-notice">🔒 You'll be asked to log in before adding to cart.</div>
        )}

        <button className="modal-submit" onClick={handleSubmit}>
          {currentUser ? "✨ Add to Cart" : "🔑 Login & Add to Cart"}
        </button>
      </div>
    </div>
  );
}

/** Renders one admin-configured option as the control its field type calls for. */
function CustomizationField({ option, value, colors, uploading, onChange, onUpload }) {
  const label = (
    <label className="form-label" htmlFor={`cf-${option.key}`}>
      {option.label}{option.required ? " *" : ""}
    </label>
  );

  switch (option.fieldType) {
    case FIELD_TYPES.TEXTAREA:
      return (
        <div className="form-group">
          {label}
          <textarea
            id={`cf-${option.key}`}
            className="form-textarea"
            maxLength={option.maxLength || undefined}
            value={value}
            required={option.required}
            onChange={(e) => onChange(e.target.value)}
          />
        </div>
      );

    case FIELD_TYPES.DATE:
      return (
        <div className="form-group">
          {label}
          <input
            id={`cf-${option.key}`}
            type="date"
            className="form-input"
            value={value}
            required={option.required}
            onChange={(e) => onChange(e.target.value)}
          />
        </div>
      );

    case FIELD_TYPES.SELECT:
      return (
        <div className="form-group">
          {label}
          <select
            id={`cf-${option.key}`}
            className="form-select"
            value={value}
            required={option.required}
            onChange={(e) => onChange(e.target.value)}
          >
            {!option.required && <option value="">No preference</option>}
            {option.choices.map((choice) => <option key={choice} value={choice}>{choice}</option>)}
          </select>
        </div>
      );

    case FIELD_TYPES.COLOR: {
      const selected = colors.find((c) => c.id === value);
      return (
        <div className="form-group">
          <span className="form-label" id={`cf-${option.key}-label`}>
            {option.label}{option.required ? " *" : ""}
            {selected ? <span className="color-selected-name"> — {selected.name}</span> : null}
          </span>
          <div className="color-options" role="radiogroup" aria-labelledby={`cf-${option.key}-label`}>
            {colors.map((color) => (
              <button
                key={color.id}
                type="button"
                role="radio"
                aria-checked={value === color.id}
                aria-label={color.name}
                title={color.name}
                className={`color-swatch ${value === color.id ? "active" : ""}`}
                style={colorSwatchStyle(color.hexCode)}
                onClick={() => onChange(color.id)}
              />
            ))}
          </div>
        </div>
      );
    }

    case FIELD_TYPES.IMAGE: {
      const preview = resolveImageUrl(value);
      return (
        <div className="form-group">
          {label}
          {preview ? (
            <div className="upload-preview">
              <img src={preview} alt={`${option.label} preview`} className="upload-preview-img" />
              <button type="button" className="btn-outline upload-remove" onClick={() => onChange("")}>
                Remove
              </button>
            </div>
          ) : (
            <div
              className="upload-zone"
              onClick={() => document.getElementById(`cf-${option.key}`)?.click()}
            >
              <div className="upload-icon">{uploading ? "⏳" : "📷"}</div>
              <div className="upload-text">
                {uploading ? (
                  <span style={{ color: "#a78bfa" }}>Uploading…</span>
                ) : (
                  <>
                    <span className="upload-link">Click to upload</span> or drag &amp; drop
                    <br />PNG · JPG · WEBP · GIF — up to 10 MB
                  </>
                )}
              </div>
            </div>
          )}
          <input
            id={`cf-${option.key}`}
            type="file"
            accept="image/*"
            style={{ display: "none" }}
            disabled={uploading}
            onChange={(e) => {
              const file = e.target.files?.[0];
              e.target.value = "";
              onUpload(file);
            }}
          />
        </div>
      );
    }

    case FIELD_TYPES.TEXT:
    default:
      return (
        <div className="form-group">
          {label}
          <input
            id={`cf-${option.key}`}
            className="form-input"
            maxLength={option.maxLength || undefined}
            value={value}
            required={option.required}
            onChange={(e) => onChange(e.target.value)}
          />
        </div>
      );
  }
}

/** Pickers start on a real choice; free-text and uploads start empty. */
function initialValues(options, colors) {
  const initial = {};
  for (const option of options) {
    if (option.fieldType === FIELD_TYPES.COLOR) {
      initial[option.key] = colors[0]?.id || "";
    } else if (option.fieldType === FIELD_TYPES.SELECT && option.required) {
      initial[option.key] = option.choices[0] || "";
    } else {
      initial[option.key] = "";
    }
  }
  return initial;
}
