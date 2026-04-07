import { useState } from "react";
import CustomizeModal from "./CustomizeModal";
import "./VariantsModal.css";

export default function VariantsModal({ product, onClose }) {
  const [selectedVariant, setSelectedVariant] = useState(null);

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

              {/* Emoji display */}
              <div className="vm-card-icon">{variant.emoji}</div>

              {/* Info */}
              <div className="vm-card-name">{variant.name}</div>
              <div className="vm-card-desc">{variant.desc}</div>

              {/* Color swatches */}
              <div className="vm-swatches">
                {variant.colors.map((c, i) => (
                  <span
                    key={i}
                    className="vm-swatch"
                    style={{ background: c === "gold" ? "linear-gradient(135deg,#ffd700,#ff8c00)" : c }}
                    title={c}
                  />
                ))}
              </div>

              {/* Price + Button */}
              <div className="vm-card-footer">
                <div className="vm-price">₹{variant.price.toLocaleString()}</div>
                <button
                  className="vm-customize-btn"
                  onClick={() => setSelectedVariant(variant)}
                >
                  Customize ✨
                </button>
              </div>
            </div>
          ))}
        </div>

      </div>
    </div>
  );
}
