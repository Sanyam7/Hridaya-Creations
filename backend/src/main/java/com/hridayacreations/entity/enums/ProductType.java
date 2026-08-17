package com.hridayacreations.entity.enums;

/**
 * How a product is sold. Exactly one value applies to a product at any time — a product is either
 * personalised by the customer before it is bought, or sold as-is.
 */
public enum ProductType {

    /** The customer configures the enabled customization options before adding it to the cart. */
    CUSTOMIZABLE,

    /** Sold as-is: no customization is offered, collected or accepted. */
    READYMADE
}
