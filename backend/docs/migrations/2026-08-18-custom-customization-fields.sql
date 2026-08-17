-- =====================================================================
-- Migration — admin-authored custom fields (TEXT / NUMBER / BOOLEAN /
-- TEXTAREA / DATE) on top of the built-in customization options, plus
-- label + field-type snapshots on cart and order lines.
--
-- *** RUN THIS BEFORE DEPLOYING THE NEW BACKEND ***
--
-- Like the previous customization migration, this one is NOT optional
-- under JPA_DDL_AUTO=update. Hibernate's update mode adds columns but
-- never drops anything, so it cannot replace the primary keys on the two
-- customization tables below. Those tables change from a key/value map
-- to an ordered list, which means their old (owner_id, option_key)
-- primary key has to go — left in place it rejects the second row of any
-- line whose fields happen to collide, and blocks the display_order the
-- new mapping writes.
--
-- Production isolates the tables in a dedicated schema (JPA_DEFAULT_SCHEMA,
-- default `hridaya`), so run `SET search_path TO hridaya;` there first.
--
-- Every statement is idempotent and safe to re-run.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. A configured option can now be a whole field definition, not just a
--    reference to a catalog entry. Built-in options leave all of these
--    null (bar is_custom) and keep reading their rules from the catalog.
-- ---------------------------------------------------------------------
ALTER TABLE product_customization_options
    ADD COLUMN IF NOT EXISTS is_custom   BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE product_customization_options
    ADD COLUMN IF NOT EXISTS field_type  VARCHAR(20);
ALTER TABLE product_customization_options
    ADD COLUMN IF NOT EXISTS placeholder VARCHAR(120);
ALTER TABLE product_customization_options
    ADD COLUMN IF NOT EXISTS max_length  INTEGER;
ALTER TABLE product_customization_options
    ADD COLUMN IF NOT EXISTS min_value   NUMERIC(18, 4);
ALTER TABLE product_customization_options
    ADD COLUMN IF NOT EXISTS max_value   NUMERIC(18, 4);

-- ---------------------------------------------------------------------
-- 2. Cart and order lines carry a snapshot of the field each value
--    answered, not just the value. This is what keeps a placed order
--    readable after the admin renames, retypes or deletes that field.
--
--    Both tables go from map (owner_id, option_key) -> value to an
--    ordered list, so each needs: the new columns, a backfill, the old
--    primary key dropped, and a new one on (owner_id, display_order).
-- ---------------------------------------------------------------------

-- Applied to both tables; `owner` is the FK column name.
CREATE OR REPLACE FUNCTION hc_upgrade_customization_table(tbl TEXT, owner TEXT)
RETURNS VOID AS $fn$
DECLARE
    pk_name TEXT;
BEGIN
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS label VARCHAR(120)', tbl);
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS field_type VARCHAR(20)', tbl);
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS display_order INTEGER', tbl);

    -- Existing rows predate the snapshot. Recover the label from the product's
    -- current configuration where it is still there, and derive the field type from
    -- the built-in key — the only kind of key that can exist before this migration.
    EXECUTE format($sql$
        UPDATE %I t
           SET label = COALESCE(t.label, t.option_key),
               field_type = COALESCE(t.field_type, CASE t.option_key
                   WHEN 'photo'               THEN 'IMAGE'
                   WHEN 'date'                THEN 'DATE'
                   WHEN 'color'               THEN 'COLOR'
                   WHEN 'font'                THEN 'SELECT'
                   WHEN 'size'                THEN 'SELECT'
                   WHEN 'specialInstructions' THEN 'TEXTAREA'
                   ELSE 'TEXT'
               END)
         WHERE t.label IS NULL OR t.field_type IS NULL
    $sql$, tbl);

    -- Ordering was never stored (a map has none), so fix one deterministically.
    EXECUTE format($sql$
        UPDATE %I t
           SET display_order = ranked.position
          FROM (SELECT %I AS owner_id, option_key,
                       ROW_NUMBER() OVER (PARTITION BY %I ORDER BY option_key) - 1 AS position
                  FROM %I) ranked
         WHERE t.%I = ranked.owner_id
           AND t.option_key = ranked.option_key
           AND t.display_order IS NULL
    $sql$, tbl, owner, owner, tbl, owner);

    EXECUTE format('ALTER TABLE %I ALTER COLUMN label SET NOT NULL', tbl);
    EXECUTE format('ALTER TABLE %I ALTER COLUMN field_type SET NOT NULL', tbl);
    EXECUTE format('ALTER TABLE %I ALTER COLUMN display_order SET NOT NULL', tbl);

    -- Swap the map's primary key for the list's.
    SELECT conname INTO pk_name
      FROM pg_constraint
     WHERE conrelid = tbl::regclass AND contype = 'p';

    IF pk_name IS NOT NULL AND pk_name <> tbl || '_ordered_pkey' THEN
        EXECUTE format('ALTER TABLE %I DROP CONSTRAINT %I', tbl, pk_name);
        pk_name := NULL;
    END IF;

    IF pk_name IS NULL THEN
        EXECUTE format('ALTER TABLE %I ADD CONSTRAINT %I PRIMARY KEY (%I, display_order)',
                       tbl, tbl || '_ordered_pkey', owner);
    END IF;
END;
$fn$ LANGUAGE plpgsql;

SELECT hc_upgrade_customization_table('cart_item_customization', 'cart_item_id');
SELECT hc_upgrade_customization_table('order_item_customization', 'order_item_id');

DROP FUNCTION hc_upgrade_customization_table(TEXT, TEXT);

-- =====================================================================
-- Verification
--
--   -- new configuration columns present:
--   SELECT column_name FROM information_schema.columns
--    WHERE table_name = 'product_customization_options' ORDER BY column_name;
--
--   -- both snapshot tables ordered, with no nulls left:
--   SELECT 'cart' AS t, COUNT(*) FILTER (WHERE label IS NULL OR field_type IS NULL) AS unsnapshotted
--     FROM cart_item_customization
--   UNION ALL
--   SELECT 'order', COUNT(*) FILTER (WHERE label IS NULL OR field_type IS NULL)
--     FROM order_item_customization;
--
--   -- primary keys now on (owner, display_order):
--   SELECT conrelid::regclass AS tbl, pg_get_constraintdef(oid)
--     FROM pg_constraint
--    WHERE contype = 'p'
--      AND conrelid IN ('cart_item_customization'::regclass,
--                       'order_item_customization'::regclass);
--
-- Rollback (custom field definitions are lost; built-in options and the
-- values themselves survive, but line ordering reverts to arbitrary):
--   ALTER TABLE product_customization_options
--       DROP COLUMN IF EXISTS is_custom, DROP COLUMN IF EXISTS field_type,
--       DROP COLUMN IF EXISTS placeholder, DROP COLUMN IF EXISTS max_length,
--       DROP COLUMN IF EXISTS min_value, DROP COLUMN IF EXISTS max_value;
--   -- then, for each of cart_item_customization / order_item_customization:
--   --   DELETE FROM <t> a USING <t> b
--   --    WHERE a.<owner> = b.<owner> AND a.option_key = b.option_key
--   --      AND a.display_order > b.display_order;   -- map cannot hold duplicates
--   --   ALTER TABLE <t> DROP CONSTRAINT <t>_ordered_pkey;
--   --   ALTER TABLE <t> ADD PRIMARY KEY (<owner>, option_key);
--   --   ALTER TABLE <t> DROP COLUMN label, DROP COLUMN field_type,
--   --                   DROP COLUMN display_order;
-- =====================================================================
