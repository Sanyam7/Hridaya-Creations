package com.hridayacreations.service.support;

import com.hridayacreations.dto.request.ProductColorRequest;
import com.hridayacreations.entity.ProductColor;
import com.hridayacreations.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Single source of truth for product colour rules on the server: the predefined palette, id
 * normalization, hex validation, de-duplication and the "at least one colour" invariant.
 *
 * <p>Never trusts the client. Whatever the admin portal submits, the resolved list is guaranteed to
 * be duplicate-free, fully populated (id + name + hex) and empty whenever colours are disabled.
 *
 * <p>The palette mirrors {@code frontend/src/constants/productColors.js}; keep the two in sync when
 * adding colours. Colours outside the palette are still accepted as long as they carry a name and a
 * valid hex code, so custom colours work without a schema or API change.
 */
@Component
public class ProductColorResolver {

    /** Accepts {@code #RGB} and {@code #RRGGBB}, the two forms CSS understands. */
    private static final Pattern HEX_PATTERN = Pattern.compile("^#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})$");
    private static final Pattern ID_SEPARATORS = Pattern.compile("[\\s_]+");
    private static final Pattern ID_DISALLOWED = Pattern.compile("[^a-z0-9-]");
    private static final int MAX_COLORS = 40;

    private static final Map<String, ProductColor> PREDEFINED = predefinedPalette();

    /**
     * Resolves the submitted colour selection into the exact list that should be persisted.
     *
     * @param colorsEnabled the admin's "does this product have colour options?" answer
     * @param requested     the submitted colours; ignored entirely when {@code colorsEnabled} is false
     * @throws BadRequestException when colours are enabled but no usable colour was submitted, or
     *                             when a custom colour is missing a name or a valid hex code
     */
    public List<ProductColor> resolve(boolean colorsEnabled, List<ProductColorRequest> requested) {
        if (!colorsEnabled) {
            return List.of();
        }
        if (requested == null || requested.isEmpty()) {
            throw new BadRequestException("Please select at least one colour for this product.");
        }
        if (requested.size() > MAX_COLORS) {
            throw new BadRequestException("A product can have at most " + MAX_COLORS + " colours.");
        }

        List<ProductColor> resolved = new ArrayList<>();
        Set<String> seenIds = new LinkedHashSet<>();
        for (ProductColorRequest candidate : requested) {
            if (candidate == null) {
                continue;
            }
            ProductColor color = toColor(candidate);
            // First occurrence wins; a repeated colour is a no-op rather than an error, which
            // matches how the picker behaves when the same swatch is toggled twice.
            if (seenIds.add(color.getColorId())) {
                resolved.add(color);
            }
        }
        if (resolved.isEmpty()) {
            throw new BadRequestException("Please select at least one colour for this product.");
        }
        return resolved;
    }

    /* ----------------------------------------------------------------- */

    private ProductColor toColor(ProductColorRequest request) {
        String id = normalizeId(request.getId() != null ? request.getId() : request.getName());
        if (id.isEmpty()) {
            throw new BadRequestException("Each colour needs an id or a name.");
        }

        ProductColor known = PREDEFINED.get(id);
        if (known != null) {
            // Canonicalize predefined colours so the stored name/hex can never drift from the
            // palette, whatever the client sent along with the id.
            return ProductColor.builder()
                    .colorId(known.getColorId())
                    .name(known.getName())
                    .hexCode(known.getHexCode())
                    .build();
        }

        String name = request.getName() != null ? request.getName().trim() : "";
        if (name.isEmpty()) {
            throw new BadRequestException("Custom colour '" + id + "' needs a name.");
        }
        String hexCode = request.getHexCode() != null ? request.getHexCode().trim() : "";
        if (!HEX_PATTERN.matcher(hexCode).matches()) {
            throw new BadRequestException(
                    "Custom colour '" + name + "' needs a valid hex code such as #1E88E5.");
        }
        return ProductColor.builder()
                .colorId(id)
                .name(name)
                .hexCode(expandHex(hexCode).toUpperCase(Locale.ROOT))
                .build();
    }

    /** Lower-cases and slugifies an id or colour name: {@code "Midnight Blue" -> "midnight-blue"}. */
    private String normalizeId(String raw) {
        if (raw == null) {
            return "";
        }
        String slug = ID_SEPARATORS.matcher(raw.trim().toLowerCase(Locale.ROOT)).replaceAll("-");
        slug = ID_DISALLOWED.matcher(slug).replaceAll("");
        // Collapse and trim separators so "--red--" and "red" resolve to the same colour.
        while (slug.contains("--")) {
            slug = slug.replace("--", "-");
        }
        if (slug.startsWith("-")) {
            slug = slug.substring(1);
        }
        if (slug.endsWith("-")) {
            slug = slug.substring(0, slug.length() - 1);
        }
        return slug.length() > 40 ? slug.substring(0, 40) : slug;
    }

    /** Normalizes {@code #abc} to {@code #aabbcc} so stored hex codes are always six digits. */
    private String expandHex(String hex) {
        if (hex.length() != 4) {
            return hex;
        }
        StringBuilder expanded = new StringBuilder("#");
        for (int i = 1; i < 4; i++) {
            expanded.append(hex.charAt(i)).append(hex.charAt(i));
        }
        return expanded.toString();
    }

    private static Map<String, ProductColor> predefinedPalette() {
        Map<String, ProductColor> palette = new LinkedHashMap<>();
        addColor(palette, "red", "Red", "#E53935");
        addColor(palette, "blue", "Blue", "#1E88E5");
        addColor(palette, "black", "Black", "#1A1A1A");
        addColor(palette, "white", "White", "#FFFFFF");
        addColor(palette, "green", "Green", "#2E7D32");
        addColor(palette, "yellow", "Yellow", "#FDD835");
        addColor(palette, "pink", "Pink", "#E0218A");
        addColor(palette, "purple", "Purple", "#8E24AA");
        addColor(palette, "brown", "Brown", "#6D4C41");
        addColor(palette, "grey", "Grey", "#9E9E9E");
        addColor(palette, "beige", "Beige", "#E8DCC8");
        addColor(palette, "orange", "Orange", "#FB8C00");
        return Collections.unmodifiableMap(palette);
    }

    private static void addColor(Map<String, ProductColor> palette, String id, String name, String hex) {
        palette.put(id, ProductColor.builder().colorId(id).name(name).hexCode(hex).build());
    }
}
