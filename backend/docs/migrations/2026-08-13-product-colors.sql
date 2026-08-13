-- =====================================================================
-- Migration — product colour variants
--
-- Adds products.has_colors and the product_colors collection table.
-- Idempotent and safe to run against a populated database: existing
-- products are backfilled as "no colour variants" (has_colors = FALSE,
-- zero product_colors rows), which is exactly how the API and the admin
-- portal treat a product created before this feature existed.
--
-- Not needed when Hibernate runs with JPA_DDL_AUTO=update — it derives
-- the same DDL from the entity model. Run this when the environment uses
-- JPA_DDL_AUTO=validate (the production default), BEFORE deploying the
-- new backend, otherwise startup validation fails on the missing column.
--
-- Production isolates the tables in a dedicated schema (JPA_DEFAULT_SCHEMA,
-- default `hridaya`), so run `SET search_path TO hridaya;` first there.
-- =====================================================================

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS has_colors BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS product_colors (
    product_id    BIGINT      NOT NULL,
    display_order INTEGER     NOT NULL,
    color_id      VARCHAR(40) NOT NULL,
    color_name    VARCHAR(60) NOT NULL,
    hex_code      VARCHAR(7)  NOT NULL,
    PRIMARY KEY (product_id, display_order),
    CONSTRAINT fk_product_colors_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

-- Rollback (destroys every configured colour selection):
--   DROP TABLE IF EXISTS product_colors;
--   ALTER TABLE products DROP COLUMN IF EXISTS has_colors;
