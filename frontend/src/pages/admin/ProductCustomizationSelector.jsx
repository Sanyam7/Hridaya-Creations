import { useId, useMemo, useState } from "react";
import {
  CUSTOMIZATION_CATALOG,
  CUSTOM_FIELD_TYPE_OPTIONS,
  COLOR_OPTION_KEY,
  FIELD_TYPES,
  MAX_CUSTOM_FIELDS,
  PRODUCT_TYPES,
  blankCustomField,
} from "../../constants/productCustomization";

/**
 * Product type (Customizable / Readymade) and, for customizable products, exactly which fields
 * the customer will be asked to fill in — both the built-in options and any number of fields the
 * admin writes themselves.
 *
 * Controlled by the product form: it owns `productType` / `customizationOptions` and receives the
 * full next state from `onChange`, so these edits take part in the form's dirty tracking and
 * validation like every other field. Switching to Readymade with fields configured asks for
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

  // The two kinds live in one list (its order is the customer's field order) but are edited
  // separately: built-ins are a fixed palette to tick, custom fields are authored.
  const builtIns = useMemo(() => options.filter((o) => !o.custom), [options]);
  const customFields = useMemo(() => options.filter((o) => o.custom), [options]);
  const byKey = useMemo(() => new Map(builtIns.map((o) => [o.key, o])), [builtIns]);

  const apply = (next) => onChange({ productType, options, ...next });

  /** Built-ins first in catalog order, then the custom fields as arranged. */
  const commit = (nextBuiltIns, nextCustom) =>
    apply({
      options: [
        ...[...nextBuiltIns].sort((a, b) => orderOf(a.key) - orderOf(b.key)),
        ...nextCustom,
      ],
    });

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
      commit(builtIns.filter((o) => o.key !== entry.key), customFields);
      return;
    }
    commit([...builtIns, { key: entry.key, label: entry.label, required: false, custom: false }],
      customFields);
  };

  const updateBuiltIn = (key, patch) =>
    commit(builtIns.map((o) => (o.key === key ? { ...o, ...patch } : o)), customFields);

  /* ----------------------------- custom fields ----------------------------- */

  const addCustomField = () =>
    commit(builtIns, [...customFields, blankCustomField()]);

  const updateCustomField = (index, patch) =>
    commit(builtIns, customFields.map((f, i) => (i === index ? { ...f, ...patch } : f)));

  const removeCustomField = (index) =>
    commit(builtIns, customFields.filter((_, i) => i !== index));

  const moveCustomField = (index, delta) => {
    const target = index + delta;
    if (target < 0 || target >= customFields.length) return;
    const next = [...customFields];
    [next[index], next[target]] = [next[target], next[index]];
    commit(builtIns, next);
  };

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
                          onChange={(e) => updateBuiltIn(entry.key, { required: e.target.checked })}
                        />
                        Required
                      </label>
                    )}
                    {selected && (
                      <BuiltInOptionSettings
                        entry={entry}
                        option={selected}
                        onChange={(patch) => updateBuiltIn(entry.key, patch)}
                      />
                    )}
                  </li>
                );
              })}
            </ul>
          </div>

          <div className="ad-colors-block">
            <span className="ad-colors-label" id={`${groupId}-custom-fields`}>
              Custom fields — ask for anything else this product needs
            </span>
            <p className="ad-colors-hint">
              Add as many as you need. Each one becomes a question on the customization form,
              in the order listed here.
            </p>

            <ul className="ad-customfield-list" aria-labelledby={`${groupId}-custom-fields`}>
              {customFields.map((field, index) => (
                <CustomFieldEditor
                  key={field.key || field.uid || index}
                  field={field}
                  index={index}
                  total={customFields.length}
                  onChange={(patch) => updateCustomField(index, patch)}
                  onRemove={() => removeCustomField(index)}
                  onMove={(delta) => moveCustomField(index, delta)}
                />
              ))}
            </ul>

            {customFields.length >= MAX_CUSTOM_FIELDS ? (
              <p className="ad-colors-hint">
                That's the maximum of {MAX_CUSTOM_FIELDS} custom fields for one product.
              </p>
            ) : (
              <button type="button" className="ad-btn ad-btn--add" onClick={addCustomField}>
                + Add custom field
              </button>
            )}
          </div>

          {options.length === 0 && (
            <p className="ad-colors-hint">
              Nothing selected yet — tick an option or add a custom field, or switch the type
              to Readymade.
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

/**
 * Per-product wording for a built-in option. What the field *is* stays fixed — the server owns a
 * built-in option's type, choices and hard limits — but how it reads to the customer is the
 * admin's, so "Personal Message" can become "Message for the bride" on one product.
 */
function BuiltInOptionSettings({ entry, option, onChange }) {
  const fieldId = useId();
  const [open, setOpen] = useState(false);
  const takesText = entry.fieldType === FIELD_TYPES.TEXT || entry.fieldType === FIELD_TYPES.TEXTAREA;
  const customised = (option.label && option.label !== entry.label)
    || !!option.placeholder || option.maxLength != null;

  if (!open) {
    return (
      <button type="button" className="ad-linkbtn" onClick={() => setOpen(true)}>
        {customised ? "Edit wording" : "Customise wording"}
      </button>
    );
  }

  return (
    <div className="ad-builtin-settings">
      <label className="ad-field" htmlFor={`${fieldId}-label`}>
        <span className="ad-field-label">Label shown to the customer</span>
        <input
          id={`${fieldId}-label`}
          className="ad-input"
          value={option.label || ""}
          maxLength={120}
          placeholder={entry.label}
          onChange={(e) => onChange({ label: e.target.value })}
        />
      </label>

      {takesText && (
        <div className="ad-customfield-grid">
          <label className="ad-field" htmlFor={`${fieldId}-placeholder`}>
            <span className="ad-field-label">Placeholder (optional)</span>
            <input
              id={`${fieldId}-placeholder`}
              className="ad-input"
              value={option.placeholder || ""}
              maxLength={120}
              onChange={(e) => onChange({ placeholder: e.target.value })}
            />
          </label>
          <label className="ad-field" htmlFor={`${fieldId}-maxlength`}>
            <span className="ad-field-label">Max characters (optional)</span>
            <input
              id={`${fieldId}-maxlength`}
              className="ad-input"
              type="number"
              min={1}
              max={entry.maxLength || undefined}
              value={option.maxLength ?? ""}
              placeholder={String(entry.maxLength ?? "")}
              onChange={(e) => onChange({ maxLength: e.target.value === "" ? null : Number(e.target.value) })}
            />
          </label>
        </div>
      )}

      <div className="ad-customfield-foot">
        {takesText && entry.maxLength && (
          <span className="ad-option-hint">
            Can be lowered below {entry.maxLength}, not raised above it.
          </span>
        )}
        <button type="button" className="ad-linkbtn" onClick={() => setOpen(false)}>Done</button>
      </div>
    </div>
  );
}

/** One admin-authored field: label, type, requiredness, plus the limits its type supports. */
function CustomFieldEditor({ field, index, total, onChange, onRemove, onMove }) {
  const fieldId = useId();
  const typeHint = CUSTOM_FIELD_TYPE_OPTIONS.find((t) => t.value === field.fieldType)?.hint;

  // A saved field keeps its key across renames so already-placed orders stay attached to it;
  // an unsaved one has no key yet because the server mints it.
  const isNew = !field.key;

  const numberOrNull = (raw) => (raw === "" ? null : Number(raw));

  return (
    <li className="ad-customfield">
      <div className="ad-customfield-head">
        <span className="ad-customfield-title">Custom field #{index + 1}</span>
        <div className="ad-customfield-tools">
          <button
            type="button"
            className="ad-iconbtn"
            aria-label={`Move ${field.label || `custom field ${index + 1}`} up`}
            disabled={index === 0}
            onClick={() => onMove(-1)}
          >↑</button>
          <button
            type="button"
            className="ad-iconbtn"
            aria-label={`Move ${field.label || `custom field ${index + 1}`} down`}
            disabled={index === total - 1}
            onClick={() => onMove(1)}
          >↓</button>
          <button
            type="button"
            className="ad-iconbtn ad-iconbtn--danger"
            aria-label={`Remove ${field.label || `custom field ${index + 1}`}`}
            onClick={onRemove}
          >✕</button>
        </div>
      </div>

      <div className="ad-customfield-grid">
        <label className="ad-field" htmlFor={`${fieldId}-label`}>
          <span className="ad-field-label">Field label *</span>
          <input
            id={`${fieldId}-label`}
            className="ad-input"
            value={field.label}
            maxLength={120}
            placeholder="Do you want gift wrapping?"
            onChange={(e) => onChange({ label: e.target.value })}
          />
        </label>

        <label className="ad-field" htmlFor={`${fieldId}-type`}>
          <span className="ad-field-label">Field type</span>
          <select
            id={`${fieldId}-type`}
            className="ad-input"
            value={field.fieldType}
            onChange={(e) => onChange({
              fieldType: e.target.value,
              // Limits belong to the type that defined them; carrying them across would
              // silently apply a character cap to a number, or a range to a date.
              maxLength: null,
              minValue: null,
              maxValue: null,
            })}
          >
            {CUSTOM_FIELD_TYPE_OPTIONS.map((t) => (
              <option key={t.value} value={t.value}>{t.label}</option>
            ))}
          </select>
        </label>
      </div>

      {typeHint && <p className="ad-option-hint">{typeHint}</p>}

      {(field.fieldType === FIELD_TYPES.TEXT || field.fieldType === FIELD_TYPES.TEXTAREA) && (
        <div className="ad-customfield-grid">
          <label className="ad-field" htmlFor={`${fieldId}-placeholder`}>
            <span className="ad-field-label">Placeholder (optional)</span>
            <input
              id={`${fieldId}-placeholder`}
              className="ad-input"
              value={field.placeholder || ""}
              maxLength={120}
              placeholder="Shown greyed out inside the box"
              onChange={(e) => onChange({ placeholder: e.target.value })}
            />
          </label>
          <label className="ad-field" htmlFor={`${fieldId}-maxlength`}>
            <span className="ad-field-label">Max characters (optional)</span>
            <input
              id={`${fieldId}-maxlength`}
              className="ad-input"
              type="number"
              min={1}
              max={1000}
              value={field.maxLength ?? ""}
              placeholder={field.fieldType === FIELD_TYPES.TEXTAREA ? "500" : "200"}
              onChange={(e) => onChange({ maxLength: numberOrNull(e.target.value) })}
            />
          </label>
        </div>
      )}

      {field.fieldType === FIELD_TYPES.NUMBER && (
        <div className="ad-customfield-grid">
          <label className="ad-field" htmlFor={`${fieldId}-min`}>
            <span className="ad-field-label">Minimum (optional)</span>
            <input
              id={`${fieldId}-min`}
              className="ad-input"
              type="number"
              value={field.minValue ?? ""}
              onChange={(e) => onChange({ minValue: numberOrNull(e.target.value) })}
            />
          </label>
          <label className="ad-field" htmlFor={`${fieldId}-max`}>
            <span className="ad-field-label">Maximum (optional)</span>
            <input
              id={`${fieldId}-max`}
              className="ad-input"
              type="number"
              value={field.maxValue ?? ""}
              onChange={(e) => onChange({ maxValue: numberOrNull(e.target.value) })}
            />
          </label>
        </div>
      )}

      <div className="ad-customfield-foot">
        <label className="ad-option-required">
          <input
            type="checkbox"
            checked={!!field.required}
            onChange={(e) => onChange({ required: e.target.checked })}
          />
          Required
        </label>
        {!isNew && <span className="ad-customfield-key">key: {field.key}</span>}
      </div>
    </li>
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
          This product currently has {count} customization field{count !== 1 ? "s" : ""}.
          Switching to Readymade will remove them, and customers will no longer be able to
          personalise it. Orders already placed keep the details they were placed with.
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
