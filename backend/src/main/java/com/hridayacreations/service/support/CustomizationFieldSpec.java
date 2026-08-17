package com.hridayacreations.service.support;

import com.hridayacreations.entity.ProductCustomizationOption;
import com.hridayacreations.entity.enums.CustomizationFieldType;
import com.hridayacreations.service.support.CustomizationCatalog.Definition;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * The fully-resolved definition of one field on one product: everything needed to render the
 * control, validate a submitted value and describe the field to a client, with no further lookups.
 *
 * <p>This is the single place the two kinds of option converge. A built-in option is its catalog
 * definition with the admin's per-product overrides applied; a custom option carries its own
 * definition already. Every consumer — the mapper, the validator, the order snapshot — works with
 * this type, which is why adding a field kind does not ripple outwards.
 *
 * @param key          stable identifier, and the key the customer's value is stored under
 * @param fieldType    input kind, which decides how the value is rendered and validated
 * @param label        what the customer sees
 * @param placeholder  hint text for free-text inputs; null when unset
 * @param required     whether the customer must supply a value
 * @param maxLength    character cap for textual fields; null when uncapped
 * @param choices      allowed values for {@link CustomizationFieldType#SELECT}; empty otherwise
 * @param minValue     inclusive lower bound for {@link CustomizationFieldType#NUMBER}; null if none
 * @param maxValue     inclusive upper bound for {@link CustomizationFieldType#NUMBER}; null if none
 * @param custom       whether the admin authored this field rather than enabling a catalog one
 * @param displayOrder position in the customization form
 */
public record CustomizationFieldSpec(
        String key,
        CustomizationFieldType fieldType,
        String label,
        String placeholder,
        boolean required,
        Integer maxLength,
        List<String> choices,
        BigDecimal minValue,
        BigDecimal maxValue,
        boolean custom,
        int displayOrder) {

    /**
     * Resolves a stored option into its effective spec.
     *
     * @param option   the stored configuration
     * @param position the option's index in the product's list, used as the display order
     * @return the resolved spec, or empty for a built-in option whose catalog entry no longer
     *         exists — such a field is dropped everywhere rather than half-described, so a client
     *         is never asked to render a field the server has no rules for
     */
    public static Optional<CustomizationFieldSpec> resolve(ProductCustomizationOption option, int position) {
        if (option == null) {
            return Optional.empty();
        }
        return option.isCustom()
                ? Optional.of(fromCustomField(option, position))
                : CustomizationCatalog.find(option.getOptionKey())
                        .map(definition -> fromBuiltIn(option, definition, position));
    }

    /**
     * A custom field's stored row is its whole definition, so it is taken at face value — it was
     * validated when the admin saved it.
     */
    private static CustomizationFieldSpec fromCustomField(ProductCustomizationOption option, int position) {
        return new CustomizationFieldSpec(
                option.getOptionKey(),
                option.getFieldType() == null ? CustomizationFieldType.TEXT : option.getFieldType(),
                option.getLabel(),
                option.getPlaceholder(),
                option.isRequired(),
                option.getMaxLength(),
                List.of(),
                option.getMinValue(),
                option.getMaxValue(),
                true,
                position);
    }

    /**
     * A built-in option takes its input kind, choices and hard limits from the catalog — the admin
     * can rename it and make it required, but cannot change what it is or relax its cap.
     */
    private static CustomizationFieldSpec fromBuiltIn(
            ProductCustomizationOption option, Definition definition, int position) {
        return new CustomizationFieldSpec(
                definition.key(),
                definition.fieldType(),
                option.getLabel() == null || option.getLabel().isBlank()
                        ? definition.defaultLabel() : option.getLabel(),
                option.getPlaceholder(),
                option.isRequired(),
                tightest(definition.maxLength(), option.getMaxLength()),
                definition.choices(),
                null,
                null,
                false,
                position);
    }

    /** The stricter of the catalog's cap and the admin's, so a per-product limit can only narrow. */
    private static Integer tightest(Integer catalogMax, Integer adminMax) {
        if (catalogMax == null) {
            return adminMax;
        }
        if (adminMax == null) {
            return catalogMax;
        }
        return Math.min(catalogMax, adminMax);
    }
}
