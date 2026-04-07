import "./Products.css";

export default function ProductCard({ product, onSelect }) {
  const minPrice = Math.min(...product.variants.map((v) => v.price));
  const maxPrice = Math.max(...product.variants.map((v) => v.price));

  return (
    <div className="product-card" onClick={onSelect}>
      <div className="card-img-wrap">
        {product.image ? (
          <img
            className="card-image"
            src={product.image}
            alt={`${product.name} preview`}
            onError={(e) => {
              e.target.style.display = "none";
              e.target.nextSibling.style.display = "flex";
            }}
          />
        ) : null}
        <span
          className="card-emoji"
          style={{ display: product.image ? "none" : "flex" }}
        >
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
          {product.features.slice(0, 2).map((f) => (
            <span className="card-feature-chip" key={f}>✓ {f}</span>
          ))}
        </div>
        <div className="card-footer-row">
          <span className="card-tag">{product.tag}</span>
          <span className="card-price-range">₹{minPrice} – ₹{maxPrice}</span>
        </div>
      </div>
    </div>
  );
}