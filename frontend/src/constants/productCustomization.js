// =====================================================================
// Product type and customization options.
//
// The ADMIN palette below mirrors the server-side catalog in
// backend/src/main/java/com/hridayacreations/service/support/CustomizationCatalog.java
// — keep the two in sync when adding an option.
//
// The STOREFRONT never reads this palette. It renders whatever
// `customizationOptions` the API returns for a product, each option carrying
// its own field type, label, limits and choices. That is what keeps the
// customer form driven by the admin's configuration rather than by hardcoded
// field lists, and what lets a new option ship without touching the renderer.
// =====================================================================

/** @typedef {{ key: string, fieldType: string, label: string, required: boolean,
 *              maxLength: (number|null), choices: string[], displayOrder: number }} CustomizationOption */

export const PRODUCT_TYPES = {
  CUSTOMIZABLE: "CUSTOMIZABLE",
  READYMADE: "READYMADE",
};

export const FIELD_TYPES = {
  TEXT: "TEXT",
  TEXTAREA: "TEXTAREA",
  IMAGE: "IMAGE",
  DATE: "DATE",
  SELECT: "SELECT",
  COLOR: "COLOR",
};

/** The options an admin can switch on, in the order the admin form lists them. */
export const CUSTOMIZATION_CATALOG = [
  { key: "customerName", fieldType: FIELD_TYPES.TEXT, label: "Name / Text to Print", maxLength: 60, choices: [], displayOrder: 1,
    hint: "A short line of text printed on the product." },
  { key: "photo", fieldType: FIELD_TYPES.IMAGE, label: "Photograph / Design", maxLength: null, choices: [], displayOrder: 2,
    hint: "Lets the customer upload an image." },
  { key: "message", fieldType: FIELD_TYPES.TEXT, label: "Personal Message", maxLength: 200, choices: [], displayOrder: 3,
    hint: "A longer greeting or dedication." },
  { key: "date", fieldType: FIELD_TYPES.DATE, label: "Special Date", maxLength: null, choices: [], displayOrder: 4,
    hint: "Birthday, anniversary or event date." },
  { key: "color", fieldType: FIELD_TYPES.COLOR, label: "Colour", maxLength: null, choices: [], displayOrder: 5,
    hint: "Uses the colours configured above; enable colour options first." },
  { key: "font", fieldType: FIELD_TYPES.SELECT, label: "Font Style", maxLength: null,
    choices: ["Classic Serif", "Script / Cursive", "Bold Modern", "Handwritten", "Elegant Thin"], displayOrder: 6,
    hint: "Lets the customer pick the lettering style." },
  { key: "size", fieldType: FIELD_TYPES.SELECT, label: "Size", maxLength: null,
    choices: ["XS", "S", "M", "L", "XL", "XXL"], displayOrder: 7,
    hint: "For apparel and other sized products." },
  { key: "specialInstructions", fieldType: FIELD_TYPES.TEXTAREA, label: "Special Instructions", maxLength: 500, choices: [], displayOrder: 8,
    hint: "Free-form notes about placement, occasion, etc." },
];

const CATALOG_BY_KEY = new Map(CUSTOMIZATION_CATALOG.map((o) => [o.key, o]));

/** The option key whose availability depends on the product's colour configuration. */
export const COLOR_OPTION_KEY = "color";

/**
 * Normalize a product's customization options into a clean, ordered, de-duplicated list.
 * Safely absorbs null/undefined/non-arrays and entries missing a key, and drops anything
 * whose field type we cannot render rather than showing the customer a broken control.
 *
 * @param {unknown} raw
 * @returns {CustomizationOption[]}
 */
export function normalizeCustomizationOptions(raw) {
  if (!Array.isArray(raw)) return [];

  const seen = new Set();
  const result = [];
  for (const entry of raw) {
    if (!entry || typeof entry !== "object") continue;
    const key = typeof entry.key === "string" ? entry.key.trim() : "";
    if (!key || seen.has(key)) continue;

    const fallback = CATALOG_BY_KEY.get(key);
    const fieldType = entry.fieldType || fallback?.fieldType;
    if (!fieldType || !FIELD_TYPES[fieldType]) continue;

    seen.add(key);
    result.push({
      key,
      fieldType,
      label: entry.label || fallback?.label || key,
      required: !!entry.required,
      maxLength: entry.maxLength ?? fallback?.maxLength ?? null,
      choices: Array.isArray(entry.choices) && entry.choices.length
        ? entry.choices
        : fallback?.choices ?? [],
      displayOrder: Number.isFinite(entry.displayOrder)
        ? entry.displayOrder
        : fallback?.displayOrder ?? 999,
    });
  }
  return result.sort((a, b) => a.displayOrder - b.displayOrder);
}

/**
 * Whether a product is personalised by the customer. Treats a missing productType
 * (a product served by a backend that predates this feature) as readymade unless the
 * legacy `customizable` flag says otherwise, so nothing regresses mid-deploy.
 */
export function isCustomizable(product) {
  if (!product) return false;
  if (product.productType) return product.productType === PRODUCT_TYPES.CUSTOMIZABLE;
  return !!product.customizable;
}

/** The options a customer should actually be shown for this product. */
export function customerFacingOptions(product) {
  if (!isCustomizable(product)) return [];
  return normalizeCustomizationOptions(product.customizationOptions);
}

export function catalogEntry(key) {
  return CATALOG_BY_KEY.get(key) || null;
}
