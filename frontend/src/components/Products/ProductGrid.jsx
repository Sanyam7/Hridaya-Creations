import ProductTile from "./ProductTile";
import "./ProductGrid.css";

/**
 * A responsive product listing.
 *
 * The column count is never hardcoded: `auto-fill` with a minimum tile width lets the grid
 * pick the count from the space it is actually given, so the same component fills an
 * ultrawide monitor and a phone without a breakpoint per screen size.
 *
 * Loading and empty are rendered here rather than by each caller so every listing in the app
 * behaves the same way, and so the grid keeps its shape while products are on their way.
 */
export default function ProductGrid({ products = [], loading = false, skeletonCount = 8, emptyMessage }) {
  if (loading) {
    return (
      <div className="product-grid" aria-busy="true" aria-live="polite">
        {Array.from({ length: skeletonCount }).map((_, i) => (
          <div className="pt pt--skeleton" key={i} aria-hidden="true">
            <div className="pt-media" />
            <div className="pt-body">
              <span className="sk sk--title" />
              <span className="sk sk--line" />
              <span className="sk sk--price" />
            </div>
          </div>
        ))}
        <span className="sr-only">Loading products…</span>
      </div>
    );
  }

  if (products.length === 0) {
    return (
      <div className="product-empty">
        <div className="product-empty-icon">🎁</div>
        <p className="product-empty-text">{emptyMessage || "No products available here yet."}</p>
      </div>
    );
  }

  return (
    <div className="product-grid">
      {products.map((product) => <ProductTile key={product.id} product={product} />)}
    </div>
  );
}
