import { useState, useEffect, useMemo } from "react";
import { productApi, resolveImageUrl } from "../../api";
import { normalizeProductColors } from "../../constants/productColors";
import {
  catalogEntry,
  isCustomizable,
  normalizeCustomizationOptions,
} from "../../constants/productCustomization";
import { PRODUCTS } from "../../data/products";
import ProductCard from "./ProductCard";
import VariantsModal from "../Modal/VariantsModal";
import "./Products.css";

// Sensible visual defaults for categories the curated catalog doesn't cover
// (e.g. brand-new categories an admin creates).
const DEFAULT_FEATURES = [
  "Personalized just for you",
  "Premium materials",
  "Custom name & photo",
  "Carefully handcrafted",
];

// Reuse the curated emoji/image/features from the local catalog, keyed by name
// (the backend categories were seeded from these names). Colours are not taken
// from here — they come per-product from the admin's configuration.
const VISUALS = PRODUCTS.reduce((acc, p) => {
  acc[p.name] = { emoji: p.emoji, image: p.image, features: p.features, desc: p.desc };
  return acc;
}, {});

// The curated offline catalog predates per-product configuration, so give it the same
// starting form the migration backfills onto previously-customizable products. Only ever
// reached when the API is unreachable; live products carry their own configuration.
const FALLBACK_OPTIONS = [
  { ...catalogEntry("customerName"), required: true },
  { ...catalogEntry("photo"), required: false },
  { ...catalogEntry("message"), required: false },
].filter(Boolean);

/** Decorate the curated catalog so its variants behave like customizable products. */
function withFallbackCustomization(categories) {
  return categories.map((category) => ({
    ...category,
    variants: (category.variants || []).map((variant) => ({
      ...variant,
      productType: "CUSTOMIZABLE",
      customizationOptions: FALLBACK_OPTIONS,
      hasColors: Array.isArray(variant.colors) && variant.colors.length > 0,
      colors: normalizeProductColors(variant.colors),
    })),
  }));
}

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
    const v = VISUALS[cat] || {};
    const emoji = v.emoji || "🎁";
    // Prefer an admin-uploaded category image, then the curated local image,
    // then the first product's own primary image.
    const uploadedCat = resolveImageUrl(catImages[cat]);
    const firstProductImg = resolveImageUrl(items.find((p) => p.primaryImageUrl)?.primaryImageUrl);
    const cardImage = uploadedCat || v.image || firstProductImg || null;
    cards.push({
      id: cat,
      name: cat,
      emoji,
      image: cardImage,
      desc: v.desc || `Personalized ${cat.toLowerCase()} crafted just for you.`,
      tag: "Collection",
      features: v.features || DEFAULT_FEATURES,
      variants: items
        .map((p) => ({
          id: p.id,                       // real backend product id
          backendProductId: p.id,
          name: p.name,
          emoji,
          price: Number(p.sellingPrice),
          desc: p.shortDescription || p.description || "",
          badge: p.featured ? "Featured ✨" : (p.tags && p.tags[0]) || "New",
          // Exactly the colours the admin configured — absent for older products.
          hasColors: !!p.hasColors,
          colors: normalizeProductColors(p.colors),
          // Drives whether this variant gets a customization step at all, and which
          // fields that step shows. Nothing about the form is decided in the UI.
          productType: isCustomizable(p) ? "CUSTOMIZABLE" : "READYMADE",
          customizationOptions: normalizeCustomizationOptions(p.customizationOptions),
          image: resolveImageUrl(p.primaryImageUrl),
        }))
        .sort((a, b) => a.price - b.price),
      hasImage: !!cardImage,
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

  // Resilient fallback: if the API is unreachable (e.g. backend cold start),
  // show the curated local catalog so the page is never empty.
  const usingFallback = error || (apiProducts && categories.length === 0);
  const list = usingFallback ? withFallbackCustomization(PRODUCTS) : categories;
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
