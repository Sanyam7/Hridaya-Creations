import { Link } from "react-router-dom";
import { categoryPath } from "../../constants/catalog";
import "./Products.css";

/**
 * One category on the home page.
 *
 * A link rather than a click-handling div, so the whole card is keyboard reachable and can be
 * opened in a new tab — it navigates to the category listing page, where the products get the
 * full width of the screen instead of a modal.
 */
export default function ProductCard({ product }) {
  const prices = (product.variants || []).map((v) => v.price).filter(Number.isFinite);
  const minPrice = prices.length ? Math.min(...prices) : null;
  const maxPrice = prices.length ? Math.max(...prices) : null;
  const count = product.variants?.length || 0;

  return (
    <Link className="product-card" to={categoryPath(product.name)} aria-label={product.name}>
      <div className="card-img-wrap">
        {product.image ? (
          <img
            className="card-image"
            src={product.image}
            alt={`${product.name} preview`}
            loading="lazy"
            decoding="async"
            onError={(e) => {
              e.target.style.display = "none";
              e.target.nextSibling.style.display = "flex";
            }}
          />
        ) : null}
        <span className="card-emoji" style={{ display: product.image ? "none" : "flex" }}>
          {product.emoji}
        </span>
        <div className="card-overlay">
          <span className="card-overlay-text">View Designs ✨</span>
        </div>
      </div>
      <div className="card-body">
        <div className="card-title">{product.name}</div>
        <div className="card-desc">{product.desc}</div>
        <div className="card-features">
          {(product.features || []).slice(0, 2).map((f) => (
            <span className="card-feature-chip" key={f}>✓ {f}</span>
          ))}
        </div>
        <div className="card-footer-row">
          <span className="card-tag">
            {count > 0 ? `${count} design${count === 1 ? "" : "s"}` : product.tag}
          </span>
          {minPrice != null && (
            <span className="card-price-range">
              {minPrice === maxPrice ? `₹${minPrice}` : `₹${minPrice} – ₹${maxPrice}`}
            </span>
          )}
        </div>
      </div>
    </Link>
  );
}
