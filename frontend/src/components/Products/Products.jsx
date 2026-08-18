import { useState, useEffect, useMemo } from "react";
import { productApi, resolveImageUrl } from "../../api";
import { categoryVisuals, DEFAULT_FEATURES, toCatalogProduct } from "../../constants/catalog";
import { PRODUCTS } from "../../data/products";
import ProductCard from "./ProductCard";
import "./Products.css";

/**
 * The collections strip on the home page.
 *
 * Each card is one category and links through to that category's listing page. Browsing used
 * to happen in a stacked pair of modals, which capped the products at a 1100px panel however
 * wide the screen was; the listing is a real page now, so this component only has to describe
 * the collections.
 */

/** Group live backend products by category into the shape the cards expect. */
function groupByCategory(apiProducts, catImages = {}) {
  const groups = new Map();
  for (const p of apiProducts) {
    const cat = p.categoryName || "Other";
    if (!groups.has(cat)) groups.set(cat, []);
    groups.get(cat).push(p);
  }

  const cards = [];
  for (const [cat, items] of groups.entries()) {
    const visuals = categoryVisuals(cat);
    const emoji = visuals.emoji || "🎁";
    // Prefer an admin-uploaded category image, then the curated local one, then the
    // first product that has an image of its own.
    const uploadedCat = resolveImageUrl(catImages[cat]);
    const variants = items.map(toCatalogProduct).filter(Boolean);
    const firstProductImg = variants.find((v) => v.image)?.image || null;
    const cardImage = uploadedCat || visuals.image || firstProductImg || null;

    cards.push({
      id: cat,
      name: cat,
      emoji,
      image: cardImage,
      desc: visuals.desc || `Personalized ${cat.toLowerCase()} crafted just for you.`,
      tag: "Collection",
      features: visuals.features || DEFAULT_FEATURES,
      variants: variants.sort((a, b) => a.price - b.price),
      hasImage: !!cardImage,
    });
  }

  // Curated (image-backed) categories first, then the rest alphabetically.
  return cards.sort((a, b) =>
    a.hasImage === b.hasImage ? a.name.localeCompare(b.name) : a.hasImage ? -1 : 1
  );
}

export default function Products() {
  const [showAll, setShowAll] = useState(false);
  const [apiProducts, setApiProducts] = useState(null); // null = loading
  const [catImages, setCatImages] = useState({});
  const [error, setError] = useState(false);

  useEffect(() => {
    let active = true;
    productApi
      .list({ page: 0, size: 200, sortBy: "createdAt", sortDir: "desc" })
      .then((d) => { if (active) setApiProducts(d.content || []); })
      .catch(() => { if (active) setError(true); });
    // Category images (admin-uploaded) — best-effort enrichment.
    productApi
      .categories()
      .then((d) => {
        const items = Array.isArray(d) ? d : d.content || [];
        const map = {};
        for (const c of items) if (c.imageUrl) map[c.categoryName] = c.imageUrl;
        if (active) setCatImages(map);
      })
      .catch(() => {});
    return () => { active = false; };
  }, []);

  const categories = useMemo(
    () => (apiProducts ? groupByCategory(apiProducts, catImages) : []),
    [apiProducts, catImages]
  );

  // Resilient fallback: if the API is unreachable (e.g. backend cold start), show the
  // curated local catalog so the page is never empty.
  const usingFallback = error || (apiProducts && categories.length === 0);
  const list = usingFallback ? PRODUCTS : categories;
  const loading = apiProducts === null && !error;
  const visibleProducts = showAll ? list : list.slice(0, 4);

  return (
    <section className="products-section" id="products">
      <div className="section-label">✦ Our Collection ✦</div>
      <h2 className="section-title">Customize Anything &amp; Everything</h2>
      <p className="section-desc">
        Tap any collection to browse every design in it. Every piece is handcrafted with
        love — pick a style, personalize it, and it&rsquo;s yours.
      </p>

      {loading ? (
        <div className="products-loading">Loading our collection…</div>
      ) : (
        <>
          <div className="products-grid">
            {visibleProducts.map((product) => (
              <ProductCard key={product.id} product={product} />
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
    </section>
  );
}
