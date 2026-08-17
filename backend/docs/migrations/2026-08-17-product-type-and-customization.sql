-- =====================================================================
-- Migration — product type (customizable vs readymade) + per-product
-- customization options + customization on cart and order lines.
--
-- *** RUN THIS BEFORE DEPLOYING THE NEW BACKEND ***
--
-- Unlike the earlier colour migration, this one is NOT optional under
-- JPA_DDL_AUTO=update. Hibernate's update mode creates missing tables and
-- columns but never DROPS anything, so it cannot remove the old
-- uk_cart_item constraint below. Left in place, that constraint blocks a
-- customer from putting two differently-personalised copies of the same
-- product in their cart (the exact behaviour this release adds).
--
-- Production isolates the tables in a dedicated schema (JPA_DEFAULT_SCHEMA,
-- default `hridaya`), so run `SET search_path TO hridaya;` there first.
--
-- Every statement is idempotent and safe to re-run.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Product type. Backfilled from the legacy `customizable` flag so
--    existing products keep behaving exactly as they do today.
-- ---------------------------------------------------------------------
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS product_type VARCHAR(20) NOT NULL DEFAULT 'READYMADE';

-- `customizable` is now derived from product_type and is no longer read by the
-- application. It is deliberately left in place so a rollback to the previous
-- release still works. Drop it once you are confident you will not roll back:
--   ALTER TABLE products DROP COLUMN IF EXISTS customizable;

-- ---------------------------------------------------------------------
-- 2. Per-product customization configuration.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_customization_options (
    product_id    BIGINT       NOT NULL,
    display_order INTEGER      NOT NULL,
    option_key    VARCHAR(60)  NOT NULL,
    label         VARCHAR(120) NOT NULL,
    required      BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (product_id, display_order),
    CONSTRAINT fk_product_cust_options_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

-- One-time backfill of the type, guarded on the legacy column still existing so
-- re-running this file after `customizable` has been dropped is a no-op rather than an
-- error, and on the product having no configuration yet so a re-run cannot undo later
-- admin edits. Runs here (not in section 1) because it reads the table created above.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_name = 'products' AND column_name = 'customizable') THEN
        EXECUTE $backfill$
            UPDATE products p
               SET product_type = 'CUSTOMIZABLE'
             WHERE p.customizable IS TRUE
               AND p.product_type = 'READYMADE'
               AND NOT EXISTS (SELECT 1 FROM product_customization_options o
                                WHERE o.product_id = p.id)
        $backfill$;
    END IF;
END $$;

-- Products that were already customizable get the previous hardcoded storefront
-- form as their starting configuration (name required, photo and message optional),
-- so nothing a customer could fill in before silently disappears on deploy.
INSERT INTO product_customization_options (product_id, display_order, option_key, label, required)
SELECT p.id, v.display_order, v.option_key, v.label, v.required
  FROM products p
 CROSS JOIN (VALUES
        (0, 'customerName', 'Name / Text to Print', TRUE),
        (1, 'photo',        'Photograph / Design',  FALSE),
        (2, 'message',      'Personal Message',     FALSE)
 ) AS v(display_order, option_key, label, required)
 WHERE p.product_type = 'CUSTOMIZABLE'
   AND NOT EXISTS (SELECT 1 FROM product_customization_options o WHERE o.product_id = p.id);

-- ---------------------------------------------------------------------
-- 3. Cart line identity now includes the customization.
-- ---------------------------------------------------------------------
ALTER TABLE cart_items
    ADD COLUMN IF NOT EXISTS customization_signature VARCHAR(64) NOT NULL DEFAULT 'none';

CREATE TABLE IF NOT EXISTS cart_item_customization (
    cart_item_id BIGINT        NOT NULL,
    option_key   VARCHAR(60)   NOT NULL,
    option_value VARCHAR(1000),
    PRIMARY KEY (cart_item_id, option_key),
    CONSTRAINT fk_cart_item_customization_item FOREIGN KEY (cart_item_id) REFERENCES cart_items (id) ON DELETE CASCADE
);

-- The critical step: swap the old (cart_id, product_id) constraint for one that
-- also takes the personalisation into account.
ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS uk_cart_item;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_cart_item_customized') THEN
        ALTER TABLE cart_items
            ADD CONSTRAINT uk_cart_item_customized
            UNIQUE (cart_id, product_id, customization_signature);
    END IF;
END $$;

-- ---------------------------------------------------------------------
-- 4. Order lines snapshot the customization they were bought with.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_item_customization (
    order_item_id BIGINT        NOT NULL,
    option_key    VARCHAR(60)   NOT NULL,
    option_value  VARCHAR(1000),
    PRIMARY KEY (order_item_id, option_key),
    CONSTRAINT fk_order_item_customization_item FOREIGN KEY (order_item_id) REFERENCES order_items (id) ON DELETE CASCADE
);

-- =====================================================================
-- Rollback (discards every customization configuration and cart/order
-- personalisation; `customizable` still holds the pre-migration value):
--   DROP TABLE IF EXISTS order_item_customization;
--   DROP TABLE IF EXISTS cart_item_customization;
--   DROP TABLE IF EXISTS product_customization_options;
--   ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS uk_cart_item_customized;
--   ALTER TABLE cart_items DROP COLUMN IF EXISTS customization_signature;
--   ALTER TABLE cart_items ADD CONSTRAINT uk_cart_item UNIQUE (cart_id, product_id);
--   ALTER TABLE products DROP COLUMN IF EXISTS product_type;
-- =====================================================================
