import { useId, useMemo, useState } from "react";
import {
  CUSTOMIZATION_CATALOG,
  COLOR_OPTION_KEY,
  PRODUCT_TYPES,
} from "../../constants/productCustomization";

/**
 * Product type (Customizable / Readymade) and, for customizable products, exactly which
 * customization options the customer will be offered.
 *
 * Controlled by the product form: it owns `productType` / `customizationOptions` and receives the
 * full next state from `onChange`, so these edits take part in the form's dirty tracking and
 * validation like every other field. Switching to Readymade with options configured asks for
 * confirmation first, since it discards the whole configuration.
 */
export default function ProductCustomizationSelector({
  productType,
  options,
  hasColors,
  onChange,
  disabled = false,
  error = "",
}) {
  const groupId = useId();
  const [confirmingReadymade, setConfirmingReadymade] = useState(false);

  const isCustomizable = productType === PRODUCT_TYPES.CUSTOMIZABLE;
  const byKey = useMemo(() => new Map(options.map((o) => [o.key, o])), [options]);

  const apply = (next) => onChange({ productType, options, ...next });

  const setType = (type) => {
    if (type === PRODUCT_TYPES.READYMADE && options.length > 0) {
      setConfirmingReadymade(true); // confirm before discarding the configuration
      return;
    }
    apply({ productType: type });
  };

  const confirmReadymade = () => {
    setConfirmingReadymade(false);
    apply({ productType: PRODUCT_TYPES.READYMADE, options: [] });
  };

  const toggleOption = (entry) => {
    if (byKey.has(entry.key)) {
      apply({ options: options.filter((o) => o.key !== entry.key) });
      return;
    }
    const next = [...options, { key: entry.key, label: entry.label, required: false }];
    next.sort((a, b) => orderOf(a.key) - orderOf(b.key));
    apply({ options: next });
  };

  const setRequired = (key, required) =>
    apply({ options: options.map((o) => (o.key === key ? { ...o, required } : o)) });

  return (
    <fieldset className="ad-colors" disabled={disabled}>
      <legend className="ad-colors-legend">Product type</legend>

      <div className="ad-colors-question" role="radiogroup" aria-label="Product type">
        <span className="ad-colors-prompt">How is this product sold?</span>
        <div className="ad-colors-choices">
          <label className="ad-check" htmlFor={`${groupId}-custom`}>
            <input
              id={`${groupId}-custom`}
              type="radio"
              name={`${groupId}-product-type`}
              checked={isCustomizable}
              onChange={() => setType(PRODUCT_TYPES.CUSTOMIZABLE)}
            />
            Customizable
          </label>
          <label className="ad-check" htmlFor={`${groupId}-ready`}>
            <input
              id={`${groupId}-ready`}
              type="radio"
              name={`${groupId}-product-type`}
              checked={!isCustomizable}
              onChange={() => setType(PRODUCT_TYPES.READYMADE)}
            />
            Readymade
          </label>
        </div>
      </div>

      {!isCustomizable ? (
        <p className="ad-colors-hint">
          This product will be sold as-is. Customers go straight to Add to Cart — no
          customization step, and the server rejects any customization sent for it.
        </p>
      ) : (
        <>
          <div className="ad-colors-block">
            <span className="ad-colors-label" id={`${groupId}-options`}>
              Customization options — select what customers can personalise
            </span>
            <ul className="ad-option-list" aria-labelledby={`${groupId}-options`}>
              {CUSTOMIZATION_CATALOG.map((entry) => {
                const selected = byKey.get(entry.key);
                const colorBlocked = entry.key === COLOR_OPTION_KEY && !hasColors;
                return (
                  <li key={entry.key} className={`ad-option${selected ? " is-selected" : ""}`}>
                    <label className="ad-option-main">
                      <input
                        type="checkbox"
                        checked={!!selected}
                        disabled={colorBlocked && !selected}
                        onChange={() => toggleOption(entry)}
                      />
                      <span className="ad-option-text">
                        <span className="ad-option-label">{entry.label}</span>
                        <span className="ad-option-hint">
                          {colorBlocked && !selected
                            ? "Enable colour options above to offer this."
                            : entry.hint}
                        </span>
                      </span>
                    </label>
                    {selected && (
                      <label className="ad-option-required">
                        <input
                          type="checkbox"
                          checked={!!selected.required}
                          onChange={(e) => setRequired(entry.key, e.target.checked)}
                        />
                        Required
                      </label>
                    )}
                  </li>
                );
              })}
            </ul>
          </div>

          {options.length === 0 && (
            <p className="ad-colors-hint">
              No options selected yet — pick at least one, or switch the type to Readymade.
            </p>
          )}
          {error && <p className="ad-colors-error" role="alert">{error}</p>}
        </>
      )}

      {confirmingReadymade && (
        <ConfirmReadymadeDialog
          count={options.length}
          onCancel={() => setConfirmingReadymade(false)}
          onConfirm={confirmReadymade}
        />
      )}
    </fieldset>
  );
}

function orderOf(key) {
  const entry = CUSTOMIZATION_CATALOG.find((o) => o.key === key);
  return entry ? entry.displayOrder : 999;
}

/** Guards the destructive Customizable -> Readymade switch, which drops the configuration. */
function ConfirmReadymadeDialog({ count, onCancel, onConfirm }) {
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
        <h3 id={titleId}>Make this product readymade?</h3>
        <p>
          This product currently has {count} customization option{count !== 1 ? "s" : ""}.
          Switching to Readymade will remove them, and customers will no longer be able to
          personalise it. Do you want to continue?
        </p>
        <div className="ad-modal-actions">
          <button type="button" className="ad-btn" onClick={onCancel} autoFocus>Cancel</button>
          <button type="button" className="ad-btn ad-btn--danger" onClick={onConfirm}>
            Remove customization
          </button>
        </div>
      </div>
    </div>
  );
}
