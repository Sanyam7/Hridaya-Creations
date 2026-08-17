import { useState } from "react";
import { useNavigate } from "react-router-dom";
import CustomizeModal from "./CustomizeModal";
import { useAuth } from "../../context/AuthContext";
import { useCart } from "../../context/CartContext";
import {
  colorSwatchStyle,
  normalizeProductColors,
  productHasColors,
} from "../../constants/productColors";
import { isCustomizable } from "../../constants/productCustomization";
import "./Modal.css";
import "./VariantsModal.css";

export default function VariantsModal({ product, onClose }) {
  const [selectedVariant, setSelectedVariant] = useState(null);
  const [addedName, setAddedName] = useState(null);
  const { currentUser } = useAuth();
  const { addToCart } = useCart();
  const navigate = useNavigate();

  // Readymade products have no customization step: straight to the cart.
  const addReadymade = (variant) => {
    if (!currentUser) {
      onClose();
      navigate("/login");
      return;
    }
    addToCart(variant, { qty: 1 });
    setAddedName(variant.name);
  };

  if (selectedVariant) {
    // Pass variant as the "product" to CustomizeModal, merged with parent features/colors
    const variantProduct = {
      ...product,
      ...selectedVariant,
      features: product.features,
    };
    return (
      <CustomizeModal
        product={variantProduct}
        onClose={() => setSelectedVariant(null)}
        onCloseAll={onClose}
      />
    );
  }

  if (addedName) {
    return (
      <div className="vm-overlay" onClick={onClose}>
        <div className="vm-panel vm-panel--message" onClick={e => e.stopPropagation()}>
          <button className="vm-close" onClick={onClose}>✕</button>
          <div className="modal-success">
            <div className="success-icon">🎉</div>
            <h3 className="success-title">Added to Cart!</h3>
            <p className="success-text"><strong>{addedName}</strong> is in your cart.</p>
            <div className="success-btns">
              <button className="btn-primary" onClick={() => { onClose(); navigate("/cart"); }}>
                View Cart 🛒
              </button>
              <button className="btn-outline" onClick={() => setAddedName(null)}>
                Continue Shopping
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="vm-overlay" onClick={onClose}>
      <div className="vm-panel" onClick={e => e.stopPropagation()}>

        {/* Header */}
        <div className="vm-header">
          <button className="vm-close" onClick={onClose}>✕</button>
          <div className="vm-header-inner">
            <span className="vm-product-emoji">{product.emoji}</span>
            <div>
              <h2 className="vm-title">{product.name}</h2>
              <p className="vm-subtitle">Choose a design to customize</p>
            </div>
          </div>
          <div className="vm-features">
            {product.features.map(f => (
              <span className="vm-feat-chip" key={f}>✓ {f}</span>
            ))}
          </div>
        </div>

        {/* Variants grid */}
        <div className="vm-grid">
          {product.variants.map(variant => (
            <div className="vm-card" key={variant.id}>
              {/* Badge */}
              <span className="vm-badge">{variant.badge}</span>

              {/* Image (admin-uploaded) with emoji fallback */}
              <div className="vm-card-icon">
                {variant.image ? (
                  <img
                    src={variant.image}
                    alt={variant.name}
                    style={{ width: "100%", height: "100%", objectFit: "cover", borderRadius: "inherit" }}
                    onError={(e) => { e.target.style.display = "none"; e.target.parentNode.textContent = variant.emoji; }}
                  />
                ) : variant.emoji}
              </div>

              {/* Info */}
              <div className="vm-card-name">{variant.name}</div>
              <div className="vm-card-desc">{variant.desc}</div>

              {/* Color swatches — only for variants the admin gave colour options */}
              <VariantSwatches variant={variant} />

              {/* Price + Button: customizable products get a customization step,
                  readymade ones go straight into the cart. */}
              <div className="vm-card-footer">
                <div className="vm-price">₹{variant.price.toLocaleString()}</div>
                {isCustomizable(variant) ? (
                  <button className="vm-customize-btn" onClick={() => setSelectedVariant(variant)}>
                    Customize ✨
                  </button>
                ) : (
                  <button className="vm-customize-btn" onClick={() => addReadymade(variant)}>
                    Add to Cart 🛒
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>

      </div>
    </div>
  );
}

/**
 * The colours a variant is available in. Renders nothing when it has none, and always exposes
 * the colour names as text so the row never relies on colour alone to convey information.
 */
function VariantSwatches({ variant }) {
  if (!productHasColors(variant)) return null;
  const colors = normalizeProductColors(variant.colors);

  return (
    <div className="vm-swatches">
      <span className="vm-sr-only">
        Available in {colors.map((c) => c.name).join(", ")}
      </span>
      {colors.map((color) => (
        <span
          key={color.id}
          className="vm-swatch"
          style={colorSwatchStyle(color.hexCode)}
          title={color.name}
          aria-hidden="true"
        />
      ))}
    </div>
  );
}
