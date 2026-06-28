import { useState, useEffect, useMemo } from "react";
import { productApi } from "../../api";
import { PRODUCTS } from "../../data/products";
import ProductCard from "./ProductCard";
import VariantsModal from "../Modal/VariantsModal";
import "./Products.css";

// Sensible visual defaults for categories the curated catalog doesn't cover
// (e.g. brand-new categories an admin creates).
const DEFAULT_COLORS = ["#e0218a", "#457b9d", "#2d6a4f", "#f77f00", "#9d4edd"];
const DEFAULT_FEATURES = [
  "Personalized just for you",
  "Premium materials",
  "Custom name & photo",
  "Carefully handcrafted",
];

// Reuse the curated emoji/image/colors/features from the local catalog,
// keyed by name (the backend categories were seeded from these names).
const VISUALS = PRODUCTS.reduce((acc, p) => {
  acc[p.name] = { emoji: p.emoji, image: p.image, colors: p.colors, features: p.features, desc: p.desc };
  return acc;
}, {});

/** Group live backend products by category into the shape the cards expect. */
function groupByCategory(apiProducts) {
  const groups = new Map();
  for (const p of apiProducts) {
    const cat = p.categoryName || "Other";
    if (!groups.has(cat)) groups.set(cat, []);
    groups.get(cat).push(p);
  }
  const cards = [];
  for (const [cat, items] of groups.entries()) {
    const v = VISUALS[cat] || {};
    const emoji = v.emoji || "🎁";
    const colors = v.colors || DEFAULT_COLORS;
    cards.push({
      id: cat,
      name: cat,
      emoji,
      image: v.image || null,
      desc: v.desc || `Personalized ${cat.toLowerCase()} crafted just for you.`,
      tag: "Collection",
      features: v.features || DEFAULT_FEATURES,
      colors,
      variants: items
        .map((p) => ({
          id: p.id,                       // real backend product id
          backendProductId: p.id,
          name: p.name,
          emoji,
          price: Number(p.sellingPrice),
          desc: p.shortDescription || p.description || "",
          badge: p.featured ? "Featured ✨" : (p.tags && p.tags[0]) || "New",
          colors,
          image: p.primaryImageUrl || null,
        }))
        .sort((a, b) => a.price - b.price),
      hasImage: !!v.image,
    });
  }
  // Curated (image-backed) categories first, then the rest alphabetically.
  return cards.sort((a, b) =>
    a.hasImage === b.hasImage ? a.name.localeCompare(b.name) : a.hasImage ? -1 : 1
  );
}

export default function Products() {
  const [activeProduct, setActiveProduct] = useState(null);
  const [showAll, setShowAll] = useState(false);
  const [apiProducts, setApiProducts] = useState(null); // null = loading
  const [error, setError] = useState(false);

  useEffect(() => {
    let active = true;
    productApi
      .list({ page: 0, size: 200, sortBy: "createdAt", sortDir: "desc" })
      .then((d) => { if (active) setApiProducts(d.content || []); })
      .catch(() => { if (active) setError(true); });
    return () => { active = false; };
  }, []);

  const categories = useMemo(
    () => (apiProducts ? groupByCategory(apiProducts) : []),
    [apiProducts]
  );

  // Resilient fallback: if the API is unreachable (e.g. backend cold start),
  // show the curated local catalog so the page is never empty.
  const usingFallback = error || (apiProducts && categories.length === 0);
  const list = usingFallback ? PRODUCTS : categories;
  const loading = apiProducts === null && !error;
  const visibleProducts = showAll ? list : list.slice(0, 4);

  return (
    <section className="products-section" id="products">
      <div className="section-label">✦ Our Collection ✦</div>
      <h2 className="section-title">Customize Anything &amp; Everything</h2>
      <p className="section-desc">
        Tap any product to explore all available designs. Every piece is
        handcrafted with love — pick a style, personalize it, and it's yours.
      </p>

      {loading ? (
        <div style={{ textAlign: "center", padding: "3rem 1rem", color: "var(--pink-light, #c77dba)", fontSize: "1.05rem" }}>
          Loading our collection…
        </div>
      ) : (
        <>
          <div className="products-grid">
            {visibleProducts.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onSelect={() => setActiveProduct(product)}
              />
            ))}
          </div>

          {list.length > 4 && (
            <div className="see-more-container">
              <button className="see-more-btn" onClick={() => setShowAll((p) => !p)}>
                {showAll ? "Hide" : "See More"}
              </button>
            </div>
          )}
        </>
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
