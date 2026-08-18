// =====================================================================
// Product imagery.
//
// The admin can upload any number of images per product; the API returns
// them on `images[]` (each with a primary flag and a display order) plus a
// convenience `primaryImageUrl`. Everything customer-facing goes through
// `productGallery` so the card, the detail gallery and the cart all agree
// on which image comes first and how many there are.
// =====================================================================
import { resolveImageUrl } from "../api";

/** @typedef {{ id: (number|string), url: string, primary: boolean }} GalleryImage */

/**
 * A product's images as one ordered, de-duplicated gallery — primary first, then the
 * admin's display order.
 *
 * Safely absorbs every shape the app can hand it: a full API product, the curated offline
 * catalog (which carries a single bundled `image`), a product whose `images` is missing or
 * null, and entries that are bare URL strings. A product with no usable image yields an
 * empty array rather than a slot holding `null`, so callers branch on `length` alone and
 * never render a broken <img>.
 *
 * @param {object} product
 * @returns {GalleryImage[]}
 */
export function productGallery(product) {
  if (!product) return [];

  const raw = Array.isArray(product.images) ? product.images : [];
  const seen = new Set();
  const gallery = [];

  for (const entry of raw) {
    if (!entry) continue;
    const source = typeof entry === "string" ? { imageUrl: entry } : entry;
    const url = resolveImageUrl(source.imageUrl || source.url);
    // The same file uploaded twice would otherwise show as two identical thumbnails.
    if (!url || seen.has(url)) continue;
    seen.add(url);
    gallery.push({
      id: source.id ?? url,
      url,
      primary: !!source.primaryImage,
      order: Number.isFinite(source.displayOrder) ? source.displayOrder : Number.MAX_SAFE_INTEGER,
    });
  }

  gallery.sort((a, b) => Number(b.primary) - Number(a.primary) || a.order - b.order);

  // Older payloads (and the offline catalog) expose one image and no `images[]` at all.
  if (gallery.length === 0) {
    const single = resolveImageUrl(product.primaryImageUrl || product.image);
    if (single) gallery.push({ id: single, url: single, primary: true, order: 0 });
  }

  return gallery.map(({ id, url, primary }) => ({ id, url, primary }));
}

/** The one image to show where there is only room for one. */
export function primaryImage(product) {
  return productGallery(product)[0] || null;
}

/** Badge text for a card, or null when there is nothing extra to advertise. */
export function galleryBadge(count) {
  return count > 1 ? `${count} photos` : null;
}
