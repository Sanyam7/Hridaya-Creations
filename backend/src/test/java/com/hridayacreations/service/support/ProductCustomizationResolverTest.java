package com.hridayacreations.service.support;

import com.hridayacreations.dto.request.ProductCustomizationOptionRequest;
import com.hridayacreations.entity.CustomizationEntry;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.ProductColor;
import com.hridayacreations.entity.ProductCustomizationOption;
import com.hridayacreations.entity.enums.CustomizationFieldType;
import com.hridayacreations.entity.enums.ProductType;
import com.hridayacreations.exception.BadRequestException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/**
 * The rules governing admin-authored custom fields, on both sides: what an admin is allowed to
 * configure, and what a customer is then allowed to submit against it.
 */
class ProductCustomizationResolverTest {

    private final ProductCustomizationResolver resolver = new ProductCustomizationResolver();

    /* ===================== admin: configuring fields ===================== */

    @Nested
    class Configuration {

        @Test
        void aCustomFieldGetsAGeneratedKeyDerivedFromItsLabel() {
            List<ProductCustomizationOption> resolved = resolve(
                    custom("Do You Want Gift Wrapping?", CustomizationFieldType.BOOLEAN, true));

            assertThat(resolved).singleElement().satisfies(option -> {
                assertThat(option.getOptionKey()).isEqualTo("cf_doYouWantGiftWrapping");
                assertThat(option.getLabel()).isEqualTo("Do You Want Gift Wrapping?");
                assertThat(option.getFieldType()).isEqualTo(CustomizationFieldType.BOOLEAN);
                assertThat(option.isCustom()).isTrue();
                assertThat(option.isRequired()).isTrue();
            });
        }

        @Test
        void anExistingCustomFieldKeepsItsKeyWhenRenamed() {
            // The key is what already-submitted values are attached to, so a rename must not
            // change it — otherwise a past order's "Lucky Number: 7" loses its field.
            ProductCustomizationOptionRequest renamed =
                    custom("Your Number", CustomizationFieldType.NUMBER, false);
            renamed.setKey("cf_luckyNumber");

            assertThat(resolve(renamed)).singleElement()
                    .extracting(ProductCustomizationOption::getOptionKey)
                    .isEqualTo("cf_luckyNumber");
        }

        @Test
        void aCustomKeyIsForcedIntoItsOwnNamespace() {
            // A client cannot claim a catalog key for a custom field: 'message' would otherwise
            // collide with the built-in option of the same name.
            ProductCustomizationOptionRequest sneaky =
                    custom("Message", CustomizationFieldType.TEXT, false);
            sneaky.setKey("message");

            assertThat(resolve(sneaky)).singleElement()
                    .extracting(ProductCustomizationOption::getOptionKey)
                    .isEqualTo("cf_message");
        }

        @Test
        void twoNewFieldsWithTheSameDerivedKeyAreKeptApart() {
            // Labels differ only by punctuation, so both slugify to the same base.
            List<ProductCustomizationOption> resolved = resolve(
                    custom("Lucky number", CustomizationFieldType.NUMBER, false),
                    custom("Lucky number!", CustomizationFieldType.NUMBER, false));

            assertThat(resolved).extracting(ProductCustomizationOption::getOptionKey)
                    .containsExactly("cf_luckyNumber", "cf_luckyNumber_2");
        }

        @Test
        void manyCustomFieldsAreAllowed() {
            List<ProductCustomizationOptionRequest> ten = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                ten.add(custom("Question " + i, CustomizationFieldType.TEXT, false));
            }

            assertThat(resolver.resolveConfiguration(ProductType.CUSTOMIZABLE, ten, false))
                    .hasSize(10);
        }

        @Test
        void beyondTheCapIsRejected() {
            List<ProductCustomizationOptionRequest> tooMany = new ArrayList<>();
            for (int i = 1; i <= 26; i++) {
                tooMany.add(custom("Question " + i, CustomizationFieldType.TEXT, false));
            }

            assertThatThrownBy(() -> resolver.resolveConfiguration(ProductType.CUSTOMIZABLE, tooMany, false))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("at most 25 custom fields");
        }

        @Test
        void builtInOptionsComeFirst_thenCustomFieldsAsArranged() {
            List<ProductCustomizationOption> resolved = resolve(
                    custom("Gift wrap?", CustomizationFieldType.BOOLEAN, false),
                    builtIn("message"),
                    custom("Lucky number", CustomizationFieldType.NUMBER, false),
                    builtIn("customerName"));

            assertThat(resolved).extracting(ProductCustomizationOption::getOptionKey)
                    .containsExactly("customerName", "message", "cf_giftWrap", "cf_luckyNumber");
        }

        @Test
        void aBlankLabelIsRejected() {
            assertThatThrownBy(() -> resolve(custom("   ", CustomizationFieldType.TEXT, false)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("label is required");
        }

        @Test
        void aMissingFieldTypeIsRejected() {
            ProductCustomizationOptionRequest noType = custom("Anything", null, false);

            assertThatThrownBy(() -> resolve(noType))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Select a field type");
        }

        @Test
        void aTypeOnlyBuiltInFieldsCanUseIsRejected() {
            // SELECT needs a choice list, which a free-form field definition cannot carry.
            assertThatThrownBy(() -> resolve(custom("Pick one", CustomizationFieldType.SELECT, false)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not a field type a custom field can use");
        }

        @Test
        void twoFieldsSharingALabelAreRejected() {
            assertThatThrownBy(() -> resolve(
                    custom("Gift note", CustomizationFieldType.TEXT, false),
                    custom("GIFT NOTE", CustomizationFieldType.TEXT, false)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("both labelled");
        }

        @Test
        void anInvertedNumericRangeIsRejected() {
            ProductCustomizationOptionRequest backwards =
                    custom("Guests", CustomizationFieldType.NUMBER, false);
            backwards.setMinValue(new BigDecimal("10"));
            backwards.setMaxValue(new BigDecimal("2"));

            assertThatThrownBy(() -> resolve(backwards))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("minimum greater than its maximum");
        }

        @Test
        void aCustomTextFieldIsCappedEvenWhenTheAdminSetsNoLimit() {
            assertThat(resolve(custom("Quote", CustomizationFieldType.TEXT, false)))
                    .singleElement()
                    .extracting(ProductCustomizationOption::getMaxLength)
                    .isEqualTo(200);
        }

        @Test
        void aBuiltInOptionCannotHaveItsTypeOverridden() {
            // The catalog owns a built-in field's type; a payload claiming otherwise is ignored.
            ProductCustomizationOptionRequest spoofed = builtIn("customerName");
            spoofed.setFieldType(CustomizationFieldType.BOOLEAN);

            ProductCustomizationOption stored = resolve(spoofed).get(0);
            assertThat(stored.getFieldType()).isNull();
            assertThat(CustomizationFieldSpec.resolve(stored, 0).orElseThrow().fieldType())
                    .isEqualTo(CustomizationFieldType.TEXT);
        }

        @Test
        void aBuiltInCapCanOnlyBeTightened() {
            ProductCustomizationOptionRequest loosened = builtIn("customerName");   // catalog cap 60
            loosened.setMaxLength(500);
            assertThat(specOf(resolve(loosened).get(0)).maxLength()).isEqualTo(60);

            ProductCustomizationOptionRequest tightened = builtIn("customerName");
            tightened.setMaxLength(20);
            assertThat(specOf(resolve(tightened).get(0)).maxLength()).isEqualTo(20);
        }

        @Test
        void aReadymadeProductDiscardsEveryField() {
            assertThat(resolver.resolveConfiguration(ProductType.READYMADE,
                    List.of(custom("Gift wrap?", CustomizationFieldType.BOOLEAN, true)), false))
                    .isEmpty();
        }
    }

    /* ================== customer: submitting values ===================== */

    @Nested
    class Submission {

        @Test
        void aNumberIsStoredCanonically() {
            List<CustomizationEntry> entries = submit(
                    productWith(customField("cf_luckyNumber", "Lucky Number",
                            CustomizationFieldType.NUMBER, false)),
                    Map.of("cf_luckyNumber", 7));

            assertThat(entries).singleElement()
                    .extracting(CustomizationEntry::getValue).isEqualTo("7");
        }

        @Test
        void aNumberSentAsTextIsAccepted() {
            // A form posts strings; the value is still parsed and stored as a number.
            assertThat(submit(productWith(customField("cf_n", "N", CustomizationFieldType.NUMBER, false)),
                    Map.of("cf_n", "7.50")))
                    .singleElement().extracting(CustomizationEntry::getValue).isEqualTo("7.5");
        }

        @Test
        void textInANumberFieldIsRejected() {
            assertThatThrownBy(() -> submit(
                    productWith(customField("cf_n", "Lucky Number", CustomizationFieldType.NUMBER, false)),
                    Map.of("cf_n", "hello")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("'Lucky Number' must be a number");
        }

        @Test
        void aNumberOutsideItsRangeIsRejected() {
            ProductCustomizationOption bounded =
                    customField("cf_guests", "Guests", CustomizationFieldType.NUMBER, false);
            bounded.setMinValue(new BigDecimal("1"));
            bounded.setMaxValue(new BigDecimal("10"));

            assertThatThrownBy(() -> submit(productWith(bounded), Map.of("cf_guests", 99)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("must be at most 10");
        }

        @Test
        void anOptionalNumberLeftBlankIsFine() {
            Product product = productWith(customField("cf_n", "N", CustomizationFieldType.NUMBER, false));

            assertThat(submit(product, Map.of())).isEmpty();
            assertThat(submit(product, Map.of("cf_n", ""))).isEmpty();
        }

        @Test
        void aRequiredNumberLeftBlankIsRejected() {
            assertThatThrownBy(() -> submit(
                    productWith(customField("cf_n", "Lucky Number", CustomizationFieldType.NUMBER, true)),
                    Map.of()))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("'Lucky Number' is required");
        }

        /* ---- the boolean rules: `false` is an answer, absence is not ---- */

        @Test
        void aRequiredBooleanAnsweredNoIsAccepted() {
            List<CustomizationEntry> entries = submit(
                    productWith(customField("cf_giftWrap", "Gift Wrap?",
                            CustomizationFieldType.BOOLEAN, true)),
                    Map.of("cf_giftWrap", false));

            assertThat(entries).singleElement()
                    .extracting(CustomizationEntry::getValue).isEqualTo("false");
        }

        @Test
        void aRequiredBooleanAnsweredYesIsAccepted() {
            assertThat(submit(productWith(customField("cf_giftWrap", "Gift Wrap?",
                            CustomizationFieldType.BOOLEAN, true)),
                    Map.of("cf_giftWrap", true)))
                    .singleElement().extracting(CustomizationEntry::getValue).isEqualTo("true");
        }

        @Test
        void aRequiredBooleanLeftUnansweredIsRejected() {
            Product product = productWith(customField("cf_giftWrap", "Gift Wrap?",
                    CustomizationFieldType.BOOLEAN, true));

            assertThatThrownBy(() -> submit(product, Map.of()))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("'Gift Wrap?' is required");

            Map<String, Object> explicitNull = new HashMap<>();
            explicitNull.put("cf_giftWrap", null);
            assertThatThrownBy(() -> submit(product, explicitNull))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("'Gift Wrap?' is required");
        }

        @Test
        void anOptionalBooleanAnsweredNoIsStored_notDroppedAsEmpty() {
            assertThat(submit(productWith(customField("cf_giftWrap", "Gift Wrap?",
                            CustomizationFieldType.BOOLEAN, false)),
                    Map.of("cf_giftWrap", false)))
                    .singleElement().extracting(CustomizationEntry::getValue).isEqualTo("false");
        }

        @Test
        void anAmbiguousBooleanIsRejectedRatherThanGuessed() {
            assertThatThrownBy(() -> submit(
                    productWith(customField("cf_giftWrap", "Gift Wrap?",
                            CustomizationFieldType.BOOLEAN, true)),
                    Map.of("cf_giftWrap", "maybe")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("must be answered yes or no");
        }

        /* --------------------------- security --------------------------- */

        @Test
        void aFieldTheAdminNeverConfiguredIsRejected() {
            assertThatThrownBy(() -> submit(
                    productWith(customField("cf_n", "N", CustomizationFieldType.NUMBER, false)),
                    Map.of("cf_n", 1, "cf_somethingElse", "free stuff")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("'cf_somethingElse' is not a customization option offered");
        }

        @Test
        void aFieldRemovedFromTheProductIsNoLongerAccepted() {
            Product product = productWith(customField("cf_kept", "Kept",
                    CustomizationFieldType.TEXT, false));

            assertThatThrownBy(() -> submit(product, Map.of("cf_deleted", "value")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not a customization option offered");
        }

        @Test
        void theSnapshotCarriesTheLabelAndTypeAnsweredUnder() {
            Product product = productWith(
                    customField("cf_note", "Special Note", CustomizationFieldType.TEXT, true),
                    customField("cf_wrap", "Gift Wrap?", CustomizationFieldType.BOOLEAN, true));

            assertThat(submit(product, Map.of("cf_note", "  Handle with care  ", "cf_wrap", true)))
                    .extracting(CustomizationEntry::getOptionKey, CustomizationEntry::getLabel,
                            CustomizationEntry::getFieldType, CustomizationEntry::getValue)
                    .containsExactly(
                            tuple("cf_note", "Special Note", CustomizationFieldType.TEXT, "Handle with care"),
                            tuple("cf_wrap", "Gift Wrap?", CustomizationFieldType.BOOLEAN, "true"));
        }

        @Test
        void aCustomTextFieldEnforcesItsCap() {
            ProductCustomizationOption capped =
                    customField("cf_quote", "Quote", CustomizationFieldType.TEXT, false);
            capped.setMaxLength(10);

            assertThatThrownBy(() -> submit(productWith(capped), Map.of("cf_quote", "x".repeat(11))))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("must not exceed 10 characters");
        }

        @Test
        void aReadymadeProductRejectsEvenAValidLookingField() {
            Product product = productWith(customField("cf_n", "N", CustomizationFieldType.NUMBER, false));
            product.setProductType(ProductType.READYMADE);

            assertThatThrownBy(() -> submit(product, Map.of("cf_n", 1)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("readymade product");
        }

        @Test
        void mixedBuiltInAndCustomFieldsValidateTogether() {
            Product product = productWith(
                    builtInOption("customerName", "Name", true),
                    customField("cf_luckyNumber", "Lucky Number", CustomizationFieldType.NUMBER, false),
                    customField("cf_giftWrap", "Gift Wrap?", CustomizationFieldType.BOOLEAN, true));

            assertThatCode(() -> submit(product,
                    Map.of("customerName", "John", "cf_luckyNumber", 7, "cf_giftWrap", false)))
                    .doesNotThrowAnyException();

            assertThat(submit(product, Map.of("customerName", "John", "cf_giftWrap", true)))
                    .extracting(CustomizationEntry::getOptionKey)
                    .containsExactly("customerName", "cf_giftWrap");   // optional number skipped
        }
    }

    /* ----------------------------- helpers ----------------------------- */

    private List<ProductCustomizationOption> resolve(ProductCustomizationOptionRequest... requests) {
        return resolver.resolveConfiguration(ProductType.CUSTOMIZABLE, List.of(requests), true);
    }

    private List<CustomizationEntry> submit(Product product, Map<String, Object> values) {
        return resolver.validateSubmission(product, values);
    }

    private CustomizationFieldSpec specOf(ProductCustomizationOption option) {
        return CustomizationFieldSpec.resolve(option, 0).orElseThrow();
    }

    private ProductCustomizationOptionRequest custom(
            String label, CustomizationFieldType type, boolean required) {
        return ProductCustomizationOptionRequest.builder()
                .label(label).fieldType(type).required(required).custom(true).build();
    }

    private ProductCustomizationOptionRequest builtIn(String key) {
        return ProductCustomizationOptionRequest.builder().key(key).required(false).build();
    }

    private ProductCustomizationOption customField(
            String key, String label, CustomizationFieldType type, boolean required) {
        return ProductCustomizationOption.builder()
                .optionKey(key).label(label).fieldType(type).required(required).custom(true).build();
    }

    private ProductCustomizationOption builtInOption(String key, String label, boolean required) {
        return ProductCustomizationOption.builder()
                .optionKey(key).label(label).required(required).custom(false).build();
    }

    private Product productWith(ProductCustomizationOption... options) {
        Product product = Product.builder().name("Personalised Bottle").build();
        product.replaceCustomization(ProductType.CUSTOMIZABLE, List.of(options));
        product.replaceColors(true, List.of(
                ProductColor.builder().colorId("red").name("Red").hexCode("#E53935").build()));
        return product;
    }
}
