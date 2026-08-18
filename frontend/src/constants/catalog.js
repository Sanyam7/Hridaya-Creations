// =====================================================================
// Catalog presentation: URLs, category artwork, and the single adapter
// that turns an API product into the shape the customer UI renders.
//
// One adapter matters because the same product object is consumed by the
// listing tile, the detail page, the customization modal and the cart —
// if each built its own shape they would drift, and the cart would end up
// keyed on a different id than the page that filled it.
// =====================================================================
import { PRODUCTS } from "../data/products";
import { normalizeProductColors } from "./productColors";
import { isCustomizable, normalizeCustomizationOptions } from "./productCustomization";
import { productGallery } from "./productMedia";

/** Curated emoji/feature artwork, keyed by category name (the backend was seeded from these). */
const VISUALS = PRODUCTS.reduce((acc, p) => {
  acc[p.name] = { emoji: p.emoji, image: p.image, features: p.features, desc: p.desc };
  return acc;
}, {});

/** Shown for categories the curated catalog does not cover (e.g. one an admin just created). */
export const DEFAULT_FEATURES = [
  "Personalized just for you",
  "Premium materials",
  "Custom name & photo",
  "Carefully handcrafted",
];

export function categoryVisuals(categoryName) {
  return VISUALS[categoryName] || {};
}

/**
 * URL-safe form of a category name. Used as the category route parameter so the address bar
 * reads `/category/photo-frames`, and so a shared link keeps working if category ids ever
 * change. Names are the app's existing grouping key, so they are unique by construction.
 */
export function categorySlug(categoryName) {
  return String(categoryName || "")
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "") || "category";
}

export const categoryPath = (categoryName) => `/category/${categorySlug(categoryName)}`;
export const productPath = (productId) => `/product/${productId}`;

/**
 * Adapt one API product into the object the customer UI works with.
 *
 * `id` is deliberately the real backend id: the cart keys lines on it and checkout posts it
 * straight back, so inventing a synthetic id here would break both.
 */
export function toCatalogProduct(apiProduct) {
  if (!apiProduct) return null;
  const visuals = categoryVisuals(apiProduct.categoryName);
  const gallery = productGallery(apiProduct);

  return {
    id: apiProduct.id,
    backendProductId: apiProduct.id,
    name: apiProduct.name,
    price: Number(apiProduct.sellingPrice),
    originalPrice: apiProduct.originalPrice != null ? Number(apiProduct.originalPrice) : null,
    discountPercentage: apiProduct.discountPercentage ?? null,

    categoryId: apiProduct.categoryId,
    categoryName: apiProduct.categoryName,

    desc: apiProduct.shortDescription || apiProduct.description || "",
    description: apiProduct.description || "",
    shortDescription: apiProduct.shortDescription || "",

    emoji: visuals.emoji || "🎁",
    features: visuals.features || DEFAULT_FEATURES,
    gallery,
    image: gallery[0]?.url || null,

    badge: apiProduct.featured ? "Featured ✨" : (apiProduct.tags && apiProduct.tags[0]) || null,
    featured: !!apiProduct.featured,
    tags: Array.isArray(apiProduct.tags) ? apiProduct.tags : [],
    sku: apiProduct.sku || null,

    inStock: !!apiProduct.inStock,
    stockQuantity: apiProduct.stockQuantity ?? null,

    // Exactly what the admin configured — the UI decides nothing about the form itself.
    productType: isCustomizable(apiProduct) ? "CUSTOMIZABLE" : "READYMADE",
    customizationOptions: normalizeCustomizationOptions(apiProduct.customizationOptions),
    hasColors: !!apiProduct.hasColors,
    colors: normalizeProductColors(apiProduct.colors),

    averageRating: apiProduct.averageRating != null ? Number(apiProduct.averageRating) : null,
    ratingCount: apiProduct.ratingCount ?? 0,
  };
}

/** Price formatted the way the rest of the storefront writes it. */
export const formatPrice = (value) =>
  `₹${Number(value || 0).toLocaleString("en-IN")}`;
