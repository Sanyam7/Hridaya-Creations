import { useState } from "react";
import { PRODUCTS } from "../../data/products";
import ProductCard from "./ProductCard";
import VariantsModal from "../Modal/VariantsModal";
import "./Products.css";

export default function Products() {
  const [activeProduct, setActiveProduct] = useState(null);
  const [showAll, setShowAll] = useState(false); // NEW STATE

  const visibleProducts = showAll ? PRODUCTS : PRODUCTS.slice(0, 4);

  return (
    <section className="products-section" id="products">
      <div className="section-label">✦ Our Collection ✦</div>
      <h2 className="section-title">Customize Anything & Everything</h2>
      <p className="section-desc">
        Tap any product to explore all available designs. Every piece is
        handcrafted with love — pick a style, personalize it, and it's yours.
      </p>

      <div className="products-grid">
        {visibleProducts.map(product => (
          <ProductCard
            key={product.id}
            product={product}
            onSelect={() => setActiveProduct(product)}
          />
        ))}
      </div>

      {/* ✅ Toggle Button */}
      {PRODUCTS.length > 4 && (
        <div className="see-more-container">
          <button
            className="see-more-btn"
            onClick={() => setShowAll(prev => !prev)}
          >
            {showAll ? "Hide" : "See More"}
          </button>
        </div>
      )}

      {activeProduct && (
        <VariantsModal
          product={activeProduct}
          onClose={() => setActiveProduct(null)}
        />
      )}
    </section>
  );
}