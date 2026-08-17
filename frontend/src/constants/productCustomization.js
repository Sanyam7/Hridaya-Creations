// =====================================================================
// Product type and customization fields.
//
// A product's customization is made of two kinds of field:
//
//   * BUILT-IN options, picked from the palette below, which mirrors the
//     server-side catalog in
//     backend/src/main/java/com/hridayacreations/service/support/CustomizationCatalog.java
//     — keep the two in sync when adding one.
//
//   * CUSTOM fields, authored by the admin per product. These have no
//     catalog entry at all: the product's own configuration is their whole
//     definition, which is why there is no list of them here and never
//     will be.
//
// The STOREFRONT reads neither. It renders whatever `customizationOptions`
// the API returns for a product, each field carrying its own type, label,
// limits and choices. That is what keeps the customer form driven by the
// admin's configuration rather than by hardcoded field lists, and what
// lets a brand-new field — built-in or custom — ship without touching the
// renderer.
// =====================================================================

/** @typedef {{ key: string, fieldType: string, label: string, required: boolean,
 *              custom: boolean, placeholder: (string|null), maxLength: (number|null),
 *              choices: string[], minValue: (number|null), maxValue: (number|null),
 *              displayOrder: number }} CustomizationOption */

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
  NUMBER: "NUMBER",
  BOOLEAN: "BOOLEAN",
};

/**
 * The field types an admin can choose when creating a custom field. Mirrors
 * CustomizationFieldType.CUSTOM_FIELD_TYPES on the server, which rejects anything else.
 * The remaining types are built-in only: SELECT needs a choice list, IMAGE needs the
 * upload pipeline, COLOR reads the product's own palette.
 */
export const CUSTOM_FIELD_TYPE_OPTIONS = [
  { value: FIELD_TYPES.TEXT, label: "Text", hint: "A single line of text." },
  { value: FIELD_TYPES.TEXTAREA, label: "Long text", hint: "A multi-line box." },
  { value: FIELD_TYPES.NUMBER, label: "Number", hint: "Digits only; letters are rejected." },
  { value: FIELD_TYPES.BOOLEAN, label: "Yes / No", hint: "The customer picks Yes or No." },
  { value: FIELD_TYPES.DATE, label: "Date", hint: "A calendar date." },
];

/** The built-in options an admin can switch on, in the order the admin form lists them. */
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

/** Matches the server's namespace for admin-authored field keys. */
export const CUSTOM_KEY_PREFIX = "cf_";

/** As many as the server accepts on one product. */
export const MAX_CUSTOM_FIELDS = 25;

const toNumberOrNull = (value) => {
  if (value === null || value === undefined || value === "") return null;
  const n = Number(value);
  return Number.isFinite(n) ? n : null;
};

/**
 * Normalize a product's customization fields into a clean, ordered, de-duplicated list.
 * Safely absorbs null/undefined/non-arrays and entries missing a key, and drops anything
 * whose field type we cannot render rather than showing the customer a broken control.
 *
 * Built-in options fall back to the catalog for anything the API left out; a custom field
 * has no catalog entry to fall back to, so it is taken entirely from the API.
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

    const custom = !!entry.custom || key.startsWith(CUSTOM_KEY_PREFIX);
    const fallback = custom ? null : CATALOG_BY_KEY.get(key);
    const fieldType = entry.fieldType || fallback?.fieldType;
    if (!fieldType || !FIELD_TYPES[fieldType]) continue;

    seen.add(key);
    result.push({
      key,
      fieldType,
      custom,
      label: entry.label || fallback?.label || key,
      placeholder: entry.placeholder || null,
      required: !!entry.required,
      maxLength: entry.maxLength ?? fallback?.maxLength ?? null,
      choices: Array.isArray(entry.choices) && entry.choices.length
        ? entry.choices
        : fallback?.choices ?? [],
      minValue: toNumberOrNull(entry.minValue),
      maxValue: toNumberOrNull(entry.maxValue),
      displayOrder: Number.isFinite(entry.displayOrder)
        ? entry.displayOrder
        : result.length,
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

/** The fields a customer should actually be shown for this product. */
export function customerFacingOptions(product) {
  if (!isCustomizable(product)) return [];
  return normalizeCustomizationOptions(product.customizationOptions);
}

export function catalogEntry(key) {
  return CATALOG_BY_KEY.get(key) || null;
}

/** A fresh, unsaved custom field for the admin form. */
export function blankCustomField() {
  return {
    // No key yet: the server mints a stable one on save and returns it, which is what
    // later edits send back. `uid` is only for React's list identity in the meantime.
    uid: `new-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    key: "",
    label: "",
    fieldType: FIELD_TYPES.TEXT,
    required: false,
    custom: true,
    placeholder: "",
    maxLength: null,
    minValue: null,
    maxValue: null,
  };
}

/**
 * Validate the admin's custom field definitions the same way the server will, so the
 * problem is named in the form instead of coming back as a failed save.
 *
 * @returns {string} the first problem found, or "" when the set is valid
 */
export function validateCustomFields(fields) {
  const labels = new Set();
  for (let i = 0; i < fields.length; i++) {
    const field = fields[i];
    const label = (field.label || "").trim();
    const position = `Custom field #${i + 1}`;

    if (!label) return `${position}: a label is required.`;
    if (label.length > 120) return `${position}: the label must not exceed 120 characters.`;

    const lower = label.toLowerCase();
    if (labels.has(lower)) return `Two custom fields are both labelled "${label}". Give each a distinct label.`;
    labels.add(lower);

    if (!CUSTOM_FIELD_TYPE_OPTIONS.some((t) => t.value === field.fieldType)) {
      return `${position}: choose a field type.`;
    }
    if (field.fieldType === FIELD_TYPES.NUMBER
        && field.minValue !== null && field.maxValue !== null
        && Number(field.minValue) > Number(field.maxValue)) {
      return `${position}: the minimum is greater than the maximum.`;
    }
  }
  return "";
}

/** The payload shape the product API expects for one configured field. */
export function toOptionPayload(option) {
  if (!option.custom) {
    return {
      key: option.key,
      label: option.label,
      required: !!option.required,
      custom: false,
      // Wording only. The server owns a built-in field's type, choices and hard cap;
      // a maxLength here can tighten that cap but never loosen it.
      placeholder: (option.placeholder || "").trim() || undefined,
      maxLength: option.maxLength ?? undefined,
    };
  }
  return {
    // Empty for a field the admin just added — the server generates the key.
    key: option.key || undefined,
    label: (option.label || "").trim(),
    required: !!option.required,
    custom: true,
    fieldType: option.fieldType,
    placeholder: (option.placeholder || "").trim() || undefined,
    maxLength: option.maxLength ?? undefined,
    minValue: option.minValue ?? undefined,
    maxValue: option.maxValue ?? undefined,
  };
}

/** Render a submitted customization value for display. */
export function formatCustomizationValue(fieldType, value) {
  if (fieldType === FIELD_TYPES.BOOLEAN) return value === true || value === "true" ? "Yes" : "No";
  if (fieldType === FIELD_TYPES.IMAGE) return "image attached";
  return String(value ?? "");
}
