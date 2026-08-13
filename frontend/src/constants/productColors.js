// =====================================================================
// Product colour options — the single source of truth for the palette the
// admin portal offers and for turning whatever the API (or the curated local
// catalog) returns into a predictable {id, name, hexCode} shape.
//
// Mirrors the backend palette in
// backend/src/main/java/com/hridayacreations/service/support/ProductColorResolver.java —
// keep the two in sync when adding colours. The backend also accepts colours
// outside this list as long as they carry a name and a valid hex code, so
// custom colours need no change here.
// =====================================================================

/** @typedef {{ id: string, name: string, hexCode: string }} ProductColor */

/** The colours offered out of the box. Add to this list to extend the palette. */
export const PRODUCT_COLORS = [
  { id: "red", name: "Red", hexCode: "#E53935" },
  { id: "blue", name: "Blue", hexCode: "#1E88E5" },
  { id: "black", name: "Black", hexCode: "#1A1A1A" },
  { id: "white", name: "White", hexCode: "#FFFFFF" },
  { id: "green", name: "Green", hexCode: "#2E7D32" },
  { id: "yellow", name: "Yellow", hexCode: "#FDD835" },
  { id: "pink", name: "Pink", hexCode: "#E0218A" },
  { id: "purple", name: "Purple", hexCode: "#8E24AA" },
  { id: "brown", name: "Brown", hexCode: "#6D4C41" },
  { id: "grey", name: "Grey", hexCode: "#9E9E9E" },
  { id: "beige", name: "Beige", hexCode: "#E8DCC8" },
  { id: "orange", name: "Orange", hexCode: "#FB8C00" },
];

const PALETTE_BY_ID = new Map(PRODUCT_COLORS.map((c) => [c.id, c]));

const HEX_PATTERN = /^#(?:[0-9a-f]{3}|[0-9a-f]{6})$/i;

/** Slugify a colour name or id: "Midnight Blue" -> "midnight-blue". Mirrors the backend. */
export function slugifyColorId(value) {
  if (typeof value !== "string") return "";
  return value
    .trim()
    .toLowerCase()
    .replace(/[\s_]+/g, "-")
    .replace(/[^a-z0-9-]/g, "")
    .replace(/-{2,}/g, "-")
    .replace(/^-|-$/g, "")
    .slice(0, 40);
}

/** True for "#abc" and "#aabbcc" — the two forms CSS understands. */
export function isValidHexCode(value) {
  return typeof value === "string" && HEX_PATTERN.test(value.trim());
}

/** Normalize a hex code to the six-digit uppercase form, or null when unusable. */
export function normalizeHexCode(value) {
  if (!isValidHexCode(value)) return null;
  const hex = value.trim().toUpperCase();
  if (hex.length !== 4) return hex;
  return `#${hex[1]}${hex[1]}${hex[2]}${hex[2]}${hex[3]}${hex[3]}`;
}

/**
 * Turn anything a product's `colors` field might hold into a clean, de-duplicated
 * list of {id, name, hexCode}. Safely absorbs null/undefined, non-arrays, malformed
 * entries, and the bare hex strings used by the curated local catalog — entries that
 * cannot yield a usable colour are dropped rather than rendered broken.
 *
 * @param {unknown} raw
 * @returns {ProductColor[]}
 */
export function normalizeProductColors(raw) {
  if (!Array.isArray(raw)) return [];

  const result = [];
  const seenIds = new Set();
  const usedNames = new Set();

  for (const entry of raw) {
    const color = toColor(entry, usedNames);
    if (!color || seenIds.has(color.id)) continue;
    seenIds.add(color.id);
    usedNames.add(color.name.toLowerCase());
    result.push(color);
  }
  return result;
}

/**
 * Whether a product should offer a colour choice. Treats a missing `hasColors`
 * (a product created before colours existed) as false, but still trusts a
 * populated colour list so locally-catalogued products keep working.
 */
export function productHasColors(product) {
  if (!product) return false;
  const colors = normalizeProductColors(product.colors);
  if (colors.length === 0) return false;
  return product.hasColors === undefined || product.hasColors === null
    ? true
    : !!product.hasColors;
}

/** Inline style for a colour swatch, including a border that keeps pale colours visible. */
export function colorSwatchStyle(hexCode) {
  return { background: normalizeHexCode(hexCode) || "transparent" };
}

/* ----------------------------------------------------------------- */

/** Coerce one entry — an object from the API or a bare hex string — into a colour. */
function toColor(entry, usedNames) {
  if (typeof entry === "string") {
    return fromHex(entry, usedNames);
  }
  if (!entry || typeof entry !== "object") return null;

  const id = slugifyColorId(entry.id) || slugifyColorId(entry.name);
  const known = PALETTE_BY_ID.get(id);
  if (known) return known;

  // Not in the palette: a custom colour, which needs both halves to be usable.
  const hexCode = normalizeHexCode(entry.hexCode);
  const name = typeof entry.name === "string" ? entry.name.trim() : "";
  if (!id || !hexCode || !name) {
    return hexCode ? fromHex(hexCode, usedNames) : null;
  }
  return { id, name, hexCode };
}

/**
 * Build a colour from a bare hex string, naming it after the closest palette entry so
 * legacy data still reads as text for screen readers instead of colour alone.
 */
function fromHex(value, usedNames) {
  const hexCode = normalizeHexCode(value);
  if (!hexCode) return null;

  const exact = PRODUCT_COLORS.find((c) => c.hexCode === hexCode);
  if (exact) return exact;

  let name = nearestPaletteName(hexCode);
  // Two different shades can round to the same name; disambiguate with the code.
  if (usedNames.has(name.toLowerCase())) name = `${name} ${hexCode}`;
  return { id: `hex-${hexCode.slice(1).toLowerCase()}`, name, hexCode };
}

/** Name of the palette colour with the smallest RGB distance to `hexCode`. */
function nearestPaletteName(hexCode) {
  const [r, g, b] = toRgb(hexCode);
  let best = PRODUCT_COLORS[0];
  let bestDistance = Infinity;
  for (const candidate of PRODUCT_COLORS) {
    const [cr, cg, cb] = toRgb(candidate.hexCode);
    const distance = (r - cr) ** 2 + (g - cg) ** 2 + (b - cb) ** 2;
    if (distance < bestDistance) {
      bestDistance = distance;
      best = candidate;
    }
  }
  return best.name;
}

function toRgb(hexCode) {
  const int = parseInt(hexCode.slice(1), 16);
  return [(int >> 16) & 255, (int >> 8) & 255, int & 255];
}
