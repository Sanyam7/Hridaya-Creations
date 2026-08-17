package com.hridayacreations.service.support;

import com.hridayacreations.dto.request.ProductCustomizationOptionRequest;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.ProductColor;
import com.hridayacreations.entity.ProductCustomizationOption;
import com.hridayacreations.entity.enums.CustomizationFieldType;
import com.hridayacreations.entity.enums.ProductType;
import com.hridayacreations.exception.BadRequestException;
import com.hridayacreations.service.support.CustomizationCatalog.Definition;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Enforces the customization contract on both sides of the system.
 *
 * <p><b>Admin side</b> — {@link #resolveConfiguration} turns a submitted configuration into the
 * exact option list to persist: unknown keys rejected, duplicates collapsed, labels defaulted from
 * the catalog, and always empty for a READYMADE product.
 *
 * <p><b>Customer side</b> — {@link #validateSubmission} checks a customer's values against the
 * product's <em>stored</em> configuration, never against anything the client claims. A customer who
 * crafts a request by hand cannot submit an option the admin did not enable, exceed a length limit,
 * pick a colour the product does not offer, or point an image field at a URL we did not issue.
 */
@Component
public class ProductCustomizationResolver {

    /**
     * Image values must be a URL this application issued (see {@code ImageStorageService}), so a
     * customer cannot make the admin's order view render an arbitrary remote image.
     */
    private static final Pattern STORED_IMAGE_URL = Pattern.compile("^/api/v1/images/[a-f0-9]{32}$");
    private static final int MAX_OPTIONS = CustomizationCatalog.all().size();

    /* ------------------------------ admin ------------------------------ */

    /**
     * Resolves the admin's configuration into the option list to store.
     *
     * @param type      the product's type; READYMADE always resolves to no options
     * @param requested the options being switched on
     * @param hasColors whether the product offers colours, which the colour option depends on
     * @throws BadRequestException on an unknown key, an empty customizable configuration, or a
     *                             colour option enabled on a product with no colours
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
        if (requested.size() > MAX_OPTIONS) {
            throw new BadRequestException("A product can have at most " + MAX_OPTIONS
                    + " customization options.");
        }

        List<ProductCustomizationOption> resolved = new ArrayList<>();
        Set<String> seenKeys = new LinkedHashSet<>();
        for (ProductCustomizationOptionRequest candidate : requested) {
            if (candidate == null) {
                continue;
            }
            String key = candidate.getKey() == null ? "" : candidate.getKey().trim();
            Definition definition = CustomizationCatalog.find(key).orElseThrow(() ->
                    new BadRequestException("'" + key + "' is not a supported customization option."));

            if (definition.fieldType() == CustomizationFieldType.COLOR && !hasColors) {
                throw new BadRequestException("Enable colour options for this product before "
                        + "offering colour as a customization.");
            }
            // A repeated key is a no-op rather than an error, matching how the picker behaves
            // when the same checkbox is toggled twice.
            if (!seenKeys.add(definition.key())) {
                continue;
            }

            String label = candidate.getLabel() == null || candidate.getLabel().isBlank()
                    ? definition.defaultLabel()
                    : candidate.getLabel().trim();
            resolved.add(ProductCustomizationOption.builder()
                    .optionKey(definition.key())
                    .label(label)
                    .required(Boolean.TRUE.equals(candidate.getRequired()))
                    .build());
        }
        if (resolved.isEmpty()) {
            throw new BadRequestException("Please select at least one customization option, "
                    + "or set the product type to Readymade.");
        }
        // Persist in catalog order so every customer sees the fields in a consistent sequence.
        resolved.sort(Comparator.comparingInt(o -> displayOrderOf(o.getOptionKey())));
        return resolved;
    }

    /* ----------------------------- customer ----------------------------- */

    /**
     * Validates a customer's customization against the product's stored configuration and returns
     * the sanitized values to persist. Keys the admin did not enable are rejected outright rather
     * than silently dropped, so a client bug or a tampered request is visible instead of quietly
     * losing the customer's input.
     *
     * @return the trimmed values, keyed by option, containing only enabled options
     * @throws BadRequestException if the product takes no customization, an unsupported key is
     *                             present, a required value is missing, or a value is invalid
     */
    public Map<String, String> validateSubmission(Product product, Map<String, String> submitted) {
        Map<String, String> values = submitted == null ? Map.of() : submitted;

        if (product.getProductType() != ProductType.CUSTOMIZABLE) {
            if (!values.isEmpty()) {
                throw new BadRequestException("'%s' is a readymade product and does not accept customization."
                        .formatted(product.getName()));
            }
            return Map.of();
        }

        Map<String, ProductCustomizationOption> enabled = new LinkedHashMap<>();
        for (ProductCustomizationOption option : product.getCustomizationOptions()) {
            enabled.put(option.getOptionKey(), option);
        }

        for (String key : values.keySet()) {
            if (!enabled.containsKey(key)) {
                throw new BadRequestException("'%s' is not a customization option offered for '%s'."
                        .formatted(key, product.getName()));
            }
        }

        Map<String, String> sanitized = new LinkedHashMap<>();
        for (ProductCustomizationOption option : product.getCustomizationOptions()) {
            String raw = values.get(option.getOptionKey());
            String value = raw == null ? "" : raw.trim();

            if (value.isEmpty()) {
                if (option.isRequired()) {
                    throw new BadRequestException("'%s' is required for '%s'."
                            .formatted(option.getLabel(), product.getName()));
                }
                continue; // optional and unset — store nothing
            }
            sanitized.put(option.getOptionKey(), validateValue(product, option, value));
        }
        return sanitized;
    }

    /* ----------------------------------------------------------------- */

    private String validateValue(Product product, ProductCustomizationOption option, String value) {
        Definition definition = CustomizationCatalog.find(option.getOptionKey()).orElseThrow(() ->
                new BadRequestException("'%s' is no longer a supported customization option."
                        .formatted(option.getOptionKey())));

        switch (definition.fieldType()) {
            case TEXT, TEXTAREA -> {
                Integer max = definition.maxLength();
                if (max != null && value.length() > max) {
                    throw new BadRequestException("'%s' must not exceed %d characters."
                            .formatted(option.getLabel(), max));
                }
            }
            case DATE -> {
                try {
                    LocalDate.parse(value);
                } catch (DateTimeParseException ex) {
                    throw new BadRequestException("'%s' must be a date in YYYY-MM-DD format."
                            .formatted(option.getLabel()));
                }
            }
            case SELECT -> {
                if (!definition.choices().contains(value)) {
                    throw new BadRequestException("'%s' must be one of: %s."
                            .formatted(option.getLabel(), String.join(", ", definition.choices())));
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
                    throw new BadRequestException(
                            "'%s' must be an image uploaded through this site."
                                    .formatted(option.getLabel()));
                }
            }
        }
        return value;
    }

    private int displayOrderOf(String key) {
        return CustomizationCatalog.find(key).map(Definition::displayOrder).orElse(Integer.MAX_VALUE);
    }
}
