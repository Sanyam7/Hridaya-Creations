package com.hridayacreations.service.support;

import com.hridayacreations.dto.request.ProductCustomizationOptionRequest;
import com.hridayacreations.entity.CustomizationEntry;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.ProductColor;
import com.hridayacreations.entity.ProductCustomizationOption;
import com.hridayacreations.entity.enums.CustomizationFieldType;
import com.hridayacreations.entity.enums.ProductType;
import com.hridayacreations.exception.BadRequestException;
import com.hridayacreations.service.support.CustomizationCatalog.Definition;
import com.hridayacreations.util.CustomizationValues;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Enforces the customization contract on both sides of the system.
 *
 * <p><b>Admin side</b> — {@link #resolveConfiguration} turns a submitted configuration into the
 * exact option list to persist. Built-in options are checked against the catalog; custom fields are
 * checked as whole definitions (label present, type supported, bounds sane) and given a generated,
 * collision-proof key. A READYMADE product always resolves to no options at all.
 *
 * <p><b>Customer side</b> — {@link #validateSubmission} checks a customer's values against the
 * product's <em>stored</em> configuration, never against anything the client claims. A customer who
 * crafts a request by hand cannot submit a field the admin did not configure, exceed a limit, put
 * text in a number, pick a colour the product does not offer, or point an image field at a URL we
 * did not issue.
 */
@Component
public class ProductCustomizationResolver {

    /**
     * Image values must be a URL this application issued (see {@code ImageStorageService}), so a
     * customer cannot make the admin's order view render an arbitrary remote image.
     */
    private static final Pattern STORED_IMAGE_URL = Pattern.compile("^/api/v1/images/[a-f0-9]{32}$");

    /**
     * Custom field keys are namespaced so they can never collide with a catalog key — including one
     * added years from now, which would otherwise silently shadow a field already in use on a live
     * product and reinterpret data already submitted against it.
     */
    private static final String CUSTOM_KEY_PREFIX = "cf_";

    /** Generous enough to be no practical limit, low enough to bound a hand-crafted payload. */
    private static final int MAX_CUSTOM_FIELDS = 25;

    private static final int MAX_KEY_LENGTH = 60;
    private static final int MAX_LABEL_LENGTH = 120;

    /** Default cap for a custom text field, so free text can never grow past the column. */
    private static final int DEFAULT_CUSTOM_TEXT_MAX = 200;
    private static final int DEFAULT_CUSTOM_TEXTAREA_MAX = 500;
    private static final int ABSOLUTE_TEXT_MAX = 1000;

    /* ------------------------------ admin ------------------------------ */

    /**
     * Resolves the admin's configuration into the option list to store.
     *
     * @param type      the product's type; READYMADE always resolves to no options
     * @param requested the options being switched on, built-in and custom alike, in display order
     * @param hasColors whether the product offers colours, which the colour option depends on
     * @throws BadRequestException on an unknown built-in key, an invalid custom field definition, a
     *                             duplicate label, an empty customizable configuration, or a colour
     *                             option enabled on a product with no colours
     */
    public List<ProductCustomizationOption> resolveConfiguration(
            ProductType type, List<ProductCustomizationOptionRequest> requested, boolean hasColors) {

        if (type != ProductType.CUSTOMIZABLE) {
            return List.of();
        }
        if (requested == null || requested.isEmpty()) {
            throw new BadRequestException("Please select at least one customization option, "
                    + "or set the product type to Readymade.");
        }

        List<ProductCustomizationOption> resolved = new ArrayList<>();
        Set<String> seenKeys = new LinkedHashSet<>();
        Set<String> seenLabels = new LinkedHashSet<>();
        // Built-in options sort by their catalog position; custom fields keep the admin's
        // arrangement, after every built-in one. Weights are fixed here rather than derived during
        // the sort, so the comparator stays consistent as elements move.
        Map<String, Integer> weightByKey = new LinkedHashMap<>();
        int customCount = 0;

        for (ProductCustomizationOptionRequest candidate : requested) {
            if (candidate == null) {
                continue;
            }
            boolean isCustom = Boolean.TRUE.equals(candidate.getCustom());
            if (isCustom && ++customCount > MAX_CUSTOM_FIELDS) {
                throw new BadRequestException("A product can have at most %d custom fields."
                        .formatted(MAX_CUSTOM_FIELDS));
            }
            ProductCustomizationOption option = isCustom
                    ? customField(candidate, customCount, seenKeys)
                    : builtInOption(candidate, hasColors);

            // A repeated key is a no-op rather than an error, matching how the picker behaves when
            // the same checkbox is toggled twice.
            if (!seenKeys.add(option.getOptionKey())) {
                continue;
            }
            // Two fields sharing a label would be indistinguishable to the customer answering them
            // and to the admin reading the order.
            if (!seenLabels.add(option.getLabel().toLowerCase(Locale.ROOT))) {
                throw new BadRequestException(
                        "Two customization fields are both labelled '%s'. Give each field a distinct label."
                                .formatted(option.getLabel()));
            }
            weightByKey.put(option.getOptionKey(), isCustom
                    ? 1_000 + resolved.size()
                    : CustomizationCatalog.find(option.getOptionKey())
                            .map(Definition::displayOrder).orElse(999));
            resolved.add(option);
        }

        if (resolved.isEmpty()) {
            throw new BadRequestException("Please select at least one customization option, "
                    + "or set the product type to Readymade.");
        }
        // The stored list order is the display order from here on.
        resolved.sort(Comparator.comparingInt(option -> weightByKey.get(option.getOptionKey())));
        return resolved;
    }

    private ProductCustomizationOption builtInOption(
            ProductCustomizationOptionRequest candidate, boolean hasColors) {

        String key = candidate.getKey() == null ? "" : candidate.getKey().trim();
        Definition definition = CustomizationCatalog.find(key).orElseThrow(() ->
                new BadRequestException("'" + key + "' is not a supported customization option."));

        if (definition.fieldType() == CustomizationFieldType.COLOR && !hasColors) {
            throw new BadRequestException("Enable colour options for this product before "
                    + "offering colour as a customization.");
        }
        String label = blankToNull(candidate.getLabel()) == null
                ? definition.defaultLabel()
                : candidate.getLabel().trim();
        requireLabelFits(label);

        return ProductCustomizationOption.builder()
                .optionKey(definition.key())
                .label(label)
                .required(Boolean.TRUE.equals(candidate.getRequired()))
                .custom(false)
                // Null: the catalog owns a built-in field's type, so no admin payload can change
                // what the field is — only how it is labelled, capped and whether it is required.
                .fieldType(null)
                .placeholder(blankToNull(candidate.getPlaceholder()))
                .maxLength(candidate.getMaxLength())
                .build();
    }

    /**
     * Validates an admin-authored field definition and settles its key. An existing field keeps the
     * key it was created with so submitted data stays attached to it across renames; a new one gets
     * a key derived from its label, uniquified against the fields already resolved.
     */
    private ProductCustomizationOption customField(
            ProductCustomizationOptionRequest candidate, int ordinal, Set<String> takenKeys) {

        String label = blankToNull(candidate.getLabel());
        if (label == null) {
            throw new BadRequestException("Custom field label is required.");
        }
        label = label.trim();
        requireLabelFits(label);

        CustomizationFieldType fieldType = candidate.getFieldType();
        if (fieldType == null) {
            throw new BadRequestException("Select a field type for '%s'.".formatted(label));
        }
        if (!CustomizationFieldType.CUSTOM_FIELD_TYPES.contains(fieldType)) {
            throw new BadRequestException("'%s' is not a field type a custom field can use."
                    .formatted(fieldType));
        }

        String key = resolveCustomKey(candidate.getKey(), label, ordinal, takenKeys);

        Integer maxLength = null;
        BigDecimal minValue = null;
        BigDecimal maxValue = null;

        if (fieldType.isTextual()) {
            maxLength = boundedMaxLength(candidate.getMaxLength(), fieldType, label);
        } else if (fieldType == CustomizationFieldType.NUMBER) {
            minValue = candidate.getMinValue();
            maxValue = candidate.getMaxValue();
            if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
                throw new BadRequestException(
                        "'%s' has a minimum greater than its maximum.".formatted(label));
            }
        }

        return ProductCustomizationOption.builder()
                .optionKey(key)
                .label(label)
                .required(Boolean.TRUE.equals(candidate.getRequired()))
                .custom(true)
                .fieldType(fieldType)
                .placeholder(blankToNull(candidate.getPlaceholder()))
                .maxLength(maxLength)
                .minValue(minValue)
                .maxValue(maxValue)
                .build();
    }

    /* ----------------------------- customer ----------------------------- */

    /**
     * Validates a customer's customization against the product's stored configuration and returns
     * the snapshot to persist. Keys the admin did not configure are rejected outright rather than
     * silently dropped, so a client bug or a tampered request is visible instead of quietly losing
     * the customer's input.
     *
     * @return one entry per answered field, each carrying the value in canonical form alongside the
     *         label and type it was answered under
     * @throws BadRequestException if the product takes no customization, an unconfigured key is
     *                             present, a required value is missing, or a value is invalid
     */
    public List<CustomizationEntry> validateSubmission(Product product, Map<String, Object> submitted) {
        Map<String, Object> values = submitted == null ? Map.of() : submitted;

        if (product.getProductType() != ProductType.CUSTOMIZABLE) {
            if (!values.isEmpty()) {
                throw new BadRequestException("'%s' is a readymade product and does not accept customization."
                        .formatted(product.getName()));
            }
            return List.of();
        }

        List<CustomizationFieldSpec> fields = configuredFields(product);
        Map<String, CustomizationFieldSpec> byKey = new LinkedHashMap<>();
        for (CustomizationFieldSpec field : fields) {
            byKey.put(field.key(), field);
        }

        for (String key : values.keySet()) {
            if (!byKey.containsKey(key)) {
                throw new BadRequestException("'%s' is not a customization option offered for '%s'."
                        .formatted(key, product.getName()));
            }
        }

        List<CustomizationEntry> snapshot = new ArrayList<>();
        for (CustomizationFieldSpec field : fields) {
            // Absent and explicitly null are the only ways to leave a field unanswered. For a
            // boolean that distinction is the whole point: `false` is an answer, not a blank.
            Object raw = values.get(field.key());
            String value = canonicalize(product, field, raw);

            if (value == null) {
                if (field.required()) {
                    throw new BadRequestException("'%s' is required for '%s'."
                            .formatted(field.label(), product.getName()));
                }
                continue; // optional and unanswered — store nothing
            }
            snapshot.add(CustomizationEntry.builder()
                    .optionKey(field.key())
                    .label(field.label())
                    .fieldType(field.fieldType())
                    .value(value)
                    .build());
        }
        return snapshot;
    }

    /** The product's configuration resolved into full field specs, in display order. */
    public List<CustomizationFieldSpec> configuredFields(Product product) {
        List<CustomizationFieldSpec> fields = new ArrayList<>();
        List<ProductCustomizationOption> options = product.getCustomizationOptions();
        for (int i = 0; i < options.size(); i++) {
            CustomizationFieldSpec.resolve(options.get(i), i).ifPresent(fields::add);
        }
        return fields;
    }

    /* ----------------------------------------------------------------- */

    /**
     * Validates one submitted value and reduces it to canonical text.
     *
     * @return the canonical value, or null when the field was left unanswered
     */
    private String canonicalize(Product product, CustomizationFieldSpec field, Object raw) {
        if (raw == null) {
            return null;
        }
        return switch (field.fieldType()) {
            case BOOLEAN -> canonicalizeBoolean(field, raw);
            case NUMBER -> canonicalizeNumber(field, raw);
            default -> canonicalizeText(product, field, raw);
        };
    }

    private String canonicalizeBoolean(CustomizationFieldSpec field, Object raw) {
        if (raw instanceof Boolean bool) {
            return bool ? CustomizationValues.TRUE : CustomizationValues.FALSE;
        }
        // A client that sends the choice as text still gets a real boolean stored, but anything
        // ambiguous is rejected rather than guessed at — silently reading "maybe" as "no" would
        // put a wrong answer on a real order.
        String text = String.valueOf(raw).trim().toLowerCase(Locale.ROOT);
        if (text.isEmpty()) {
            return null;
        }
        return switch (text) {
            case "true", "yes", "1" -> CustomizationValues.TRUE;
            case "false", "no", "0" -> CustomizationValues.FALSE;
            default -> throw new BadRequestException("'%s' must be answered yes or no."
                    .formatted(field.label()));
        };
    }

    private String canonicalizeNumber(CustomizationFieldSpec field, Object raw) {
        BigDecimal number;
        if (raw instanceof Number value) {
            number = new BigDecimal(value.toString());
        } else {
            String text = String.valueOf(raw).trim();
            if (text.isEmpty()) {
                return null;
            }
            try {
                number = new BigDecimal(text);
            } catch (NumberFormatException ex) {
                throw new BadRequestException("'%s' must be a number.".formatted(field.label()));
            }
        }
        if (field.minValue() != null && number.compareTo(field.minValue()) < 0) {
            throw new BadRequestException("'%s' must be at least %s."
                    .formatted(field.label(), field.minValue().stripTrailingZeros().toPlainString()));
        }
        if (field.maxValue() != null && number.compareTo(field.maxValue()) > 0) {
            throw new BadRequestException("'%s' must be at most %s."
                    .formatted(field.label(), field.maxValue().stripTrailingZeros().toPlainString()));
        }
        return number.stripTrailingZeros().toPlainString();
    }

    private String canonicalizeText(Product product, CustomizationFieldSpec field, Object raw) {
        String value = String.valueOf(raw).trim();
        if (value.isEmpty()) {
            return null;
        }
        switch (field.fieldType()) {
            case TEXT, TEXTAREA -> {
                Integer max = field.maxLength();
                if (max != null && value.length() > max) {
                    throw new BadRequestException("'%s' must not exceed %d characters."
                            .formatted(field.label(), max));
                }
            }
            case DATE -> {
                try {
                    LocalDate.parse(value);
                } catch (DateTimeParseException ex) {
                    throw new BadRequestException("'%s' must be a date in YYYY-MM-DD format."
                            .formatted(field.label()));
                }
            }
            case SELECT -> {
                if (!field.choices().contains(value)) {
                    throw new BadRequestException("'%s' must be one of: %s."
                            .formatted(field.label(), String.join(", ", field.choices())));
                }
            }
            case COLOR -> {
                boolean offered = product.isHasColors() && product.getColors().stream()
                        .map(ProductColor::getColorId)
                        .anyMatch(value::equals);
                if (!offered) {
                    throw new BadRequestException("'%s' is not a colour offered for '%s'."
                            .formatted(value, product.getName()));
                }
            }
            case IMAGE -> {
                if (!STORED_IMAGE_URL.matcher(value).matches()) {
                    throw new BadRequestException("'%s' must be an image uploaded through this site."
                            .formatted(field.label()));
                }
            }
            default -> throw new IllegalStateException("Unhandled field type " + field.fieldType());
        }
        return value;
    }

    /* --------------------------- key handling --------------------------- */

    /**
     * Keeps an existing custom field's key, or derives a new one from its label. Derivation is only
     * ever a starting point: the key is stored and reused from then on, so later renames leave
     * already-submitted values attached to the same field.
     */
    private String resolveCustomKey(String submittedKey, String label, int ordinal, Set<String> taken) {
        String existing = blankToNull(submittedKey);
        if (existing != null) {
            String key = existing.trim();
            if (!key.startsWith(CUSTOM_KEY_PREFIX)) {
                // A client cannot claim a catalog key (or anything outside the custom namespace)
                // for a custom field and have the two collide downstream.
                key = CUSTOM_KEY_PREFIX + slugify(key);
            }
            if (key.length() > MAX_KEY_LENGTH) {
                key = key.substring(0, MAX_KEY_LENGTH);
            }
            if (!taken.contains(key)) {
                return key;
            }
        }
        return generateKey(label, ordinal, taken);
    }

    private String generateKey(String label, int ordinal, Set<String> taken) {
        String slug = slugify(label);
        if (slug.isEmpty()) {
            slug = "field" + ordinal;
        }
        String base = CUSTOM_KEY_PREFIX + truncate(slug, MAX_KEY_LENGTH - CUSTOM_KEY_PREFIX.length() - 3);
        String key = base;
        int suffix = 2;
        while (taken.contains(key)) {
            key = base + "_" + suffix++;
        }
        return key;
    }

    /** "Do You Want Gift Wrapping?" -> "doYouWantGiftWrapping". */
    private String slugify(String label) {
        String[] words = label.toLowerCase(Locale.ROOT).split("[^a-z0-9]+");
        StringBuilder slug = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (slug.isEmpty()) {
                slug.append(word);
            } else {
                slug.append(Character.toUpperCase(word.charAt(0))).append(word, 1, word.length());
            }
        }
        return slug.toString();
    }

    private Integer boundedMaxLength(Integer requested, CustomizationFieldType fieldType, String label) {
        int fallback = fieldType == CustomizationFieldType.TEXTAREA
                ? DEFAULT_CUSTOM_TEXTAREA_MAX : DEFAULT_CUSTOM_TEXT_MAX;
        if (requested == null) {
            return fallback;
        }
        if (requested < 1) {
            throw new BadRequestException("'%s' must allow at least one character.".formatted(label));
        }
        return Math.min(requested, ABSOLUTE_TEXT_MAX);
    }

    private void requireLabelFits(String label) {
        if (label.length() > MAX_LABEL_LENGTH) {
            throw new BadRequestException("Customization label must not exceed %d characters."
                    .formatted(MAX_LABEL_LENGTH));
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }
}
