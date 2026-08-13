import { useId, useMemo, useState } from "react";
import {
  PRODUCT_COLORS,
  colorSwatchStyle,
  isValidHexCode,
  normalizeHexCode,
  slugifyColorId,
} from "../../constants/productColors";

/**
 * "Does this product have colour options?" plus the colour picker that appears when it does.
 *
 * Controlled by the product form: it owns `hasColors` / `colors` and receives the full next
 * state from `onChange`, so colour edits participate in the form's dirty tracking and
 * validation like every other field. Turning colours off while some are selected asks for
 * confirmation first, since it discards the whole selection.
 */
export default function ProductColorSelector({
  hasColors,
  colors,
  onChange,
  disabled = false,
  error = "",
}) {
  const groupId = useId();
  const [confirmingDisable, setConfirmingDisable] = useState(false);
  const [customOpen, setCustomOpen] = useState(false);
  const [custom, setCustom] = useState({ name: "", hexCode: "#" });
  const [customError, setCustomError] = useState("");

  const selectedIds = useMemo(() => new Set(colors.map((c) => c.id)), [colors]);

  const apply = (next) => onChange({ hasColors, colors, ...next });

  const setHasColors = (enabled) => {
    if (!enabled && colors.length > 0) {
      setConfirmingDisable(true); // confirm before discarding the selection
      return;
    }
    setCustomError("");
    apply({ hasColors: enabled });
  };

  const confirmDisable = () => {
    setConfirmingDisable(false);
    setCustomOpen(false);
    setCustomError("");
    apply({ hasColors: false, colors: [] });
  };

  const toggleColor = (color) => {
    setCustomError("");
    apply({
      colors: selectedIds.has(color.id)
        ? colors.filter((c) => c.id !== color.id)
        : [...colors, color],
    });
  };

  const removeColor = (id) => {
    setCustomError("");
    apply({ colors: colors.filter((c) => c.id !== id) });
  };

  const addCustomColor = () => {
    const name = custom.name.trim();
    const hexCode = normalizeHexCode(custom.hexCode);
    const id = slugifyColorId(name);
    if (!name) return setCustomError("Give the colour a name.");
    if (!id) return setCustomError("Use letters or numbers in the colour name.");
    if (!hexCode) return setCustomError("Enter a hex code such as #191970.");
    if (selectedIds.has(id)) return setCustomError(`"${name}" is already selected.`);

    setCustomError("");
    setCustom({ name: "", hexCode: "#" });
    setCustomOpen(false);
    apply({ colors: [...colors, { id, name, hexCode }] });
  };

  return (
    <fieldset className="ad-colors" disabled={disabled}>
      <legend className="ad-colors-legend">Product colour options</legend>

      <div className="ad-colors-question" role="radiogroup" aria-label="Does this product have colour options?">
        <span className="ad-colors-prompt">Does this product have colour options?</span>
        <div className="ad-colors-choices">
          <label className="ad-check" htmlFor={`${groupId}-no`}>
            <input
              id={`${groupId}-no`}
              type="radio"
              name={`${groupId}-has-colors`}
              checked={!hasColors}
              onChange={() => setHasColors(false)}
            />
            No
          </label>
          <label className="ad-check" htmlFor={`${groupId}-yes`}>
            <input
              id={`${groupId}-yes`}
              type="radio"
              name={`${groupId}-has-colors`}
              checked={hasColors}
              onChange={() => setHasColors(true)}
            />
            Yes
          </label>
        </div>
      </div>

      {!hasColors ? (
        <p className="ad-colors-hint">
          Customers won't see a colour choice for this product.
        </p>
      ) : (
        <>
          <div className="ad-colors-block">
            <span className="ad-colors-label" id={`${groupId}-available`}>Available colours</span>
            <div className="ad-swatch-grid" role="group" aria-labelledby={`${groupId}-available`}>
              {PRODUCT_COLORS.map((color) => {
                const selected = selectedIds.has(color.id);
                return (
                  <button
                    key={color.id}
                    type="button"
                    className={`ad-swatch-btn${selected ? " is-selected" : ""}`}
                    aria-pressed={selected}
                    onClick={() => toggleColor(color)}
                  >
                    <span className="ad-swatch" style={colorSwatchStyle(color.hexCode)} aria-hidden="true" />
                    <span className="ad-swatch-name">{color.name}</span>
                    <span className="ad-swatch-tick" aria-hidden="true">{selected ? "✓" : ""}</span>
                  </button>
                );
              })}
            </div>
          </div>

          <div className="ad-colors-block">
            <span className="ad-colors-label" id={`${groupId}-selected`}>
              Selected colours ({colors.length})
            </span>
            {colors.length === 0 ? (
              <p className="ad-colors-hint">No colours selected yet — pick at least one above.</p>
            ) : (
              <ul className="ad-chip-list" aria-labelledby={`${groupId}-selected`}>
                {colors.map((color) => (
                  <li key={color.id} className="ad-chip">
                    <span className="ad-swatch ad-swatch--sm" style={colorSwatchStyle(color.hexCode)} aria-hidden="true" />
                    <span>{color.name}</span>
                    <button
                      type="button"
                      className="ad-chip-remove"
                      aria-label={`Remove ${color.name}`}
                      onClick={() => removeColor(color.id)}
                    >
                      ✕
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          {customOpen ? (
            <div className="ad-colors-block ad-custom-color">
              <span className="ad-colors-label">Add a custom colour</span>
              <div className="ad-custom-row">
                <input
                  className="ad-input"
                  placeholder="Colour name, e.g. Midnight Blue"
                  value={custom.name}
                  aria-label="Custom colour name"
                  onChange={(e) => { setCustom((p) => ({ ...p, name: e.target.value })); setCustomError(""); }}
                />
                <input
                  className="ad-input ad-input--hex"
                  placeholder="#191970"
                  value={custom.hexCode}
                  aria-label="Custom colour hex code"
                  onChange={(e) => { setCustom((p) => ({ ...p, hexCode: e.target.value })); setCustomError(""); }}
                />
                <span
                  className="ad-swatch"
                  aria-hidden="true"
                  style={isValidHexCode(custom.hexCode) ? colorSwatchStyle(custom.hexCode) : undefined}
                />
                <button type="button" className="ad-btn ad-btn--sm ad-btn--primary" onClick={addCustomColor}>Add</button>
                <button
                  type="button"
                  className="ad-btn ad-btn--sm"
                  onClick={() => { setCustomOpen(false); setCustomError(""); }}
                >
                  Cancel
                </button>
              </div>
              {customError && <p className="ad-colors-error" role="alert">{customError}</p>}
            </div>
          ) : (
            <button type="button" className="ad-btn ad-btn--sm" onClick={() => setCustomOpen(true)}>
              + Add custom colour
            </button>
          )}

          {error && <p className="ad-colors-error" role="alert">{error}</p>}
        </>
      )}

      {confirmingDisable && (
        <ConfirmDisableDialog
          count={colors.length}
          onCancel={() => setConfirmingDisable(false)}
          onConfirm={confirmDisable}
        />
      )}
    </fieldset>
  );
}

/** Guards the destructive Yes -> No switch, which drops every selected colour. */
function ConfirmDisableDialog({ count, onCancel, onConfirm }) {
  const titleId = useId();
  return (
    <div
      className="ad-modal-overlay ad-modal-overlay--nested"
      onClick={onCancel}
      onKeyDown={(e) => e.key === "Escape" && onCancel()}
      role="presentation"
    >
      <div
        className="ad-modal ad-modal--sm"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby={titleId}
        onClick={(e) => e.stopPropagation()}
      >
        <h3 id={titleId}>Remove colour options?</h3>
        <p>
          This product currently has {count} colour{count !== 1 ? "s" : ""}. Switching to
          "No" will remove all colour variants from it. Do you want to continue?
        </p>
        <div className="ad-modal-actions">
          <button type="button" className="ad-btn" onClick={onCancel} autoFocus>Cancel</button>
          <button type="button" className="ad-btn ad-btn--danger" onClick={onConfirm}>Remove colours</button>
        </div>
      </div>
    </div>
  );
}
