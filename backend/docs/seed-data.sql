-- =====================================================================
-- Hridaya Creations — reference seed data
-- For environments that manage the schema manually (schema.sql) instead
-- of the built-in Java DataSeeder. Idempotent via ON CONFLICT.
--
-- Admin login: admin@ridhayacreations.com / Admin@12345
-- (password_hash below is BCrypt strength-12 for "Admin@12345")
-- =====================================================================

-- ----- Roles -----
INSERT INTO roles (version, created_at, created_by, name, description)
VALUES
    (0, now(), 'SYSTEM', 'ROLE_ADMIN', 'Platform administrator with full management access'),
    (0, now(), 'SYSTEM', 'ROLE_USER',  'Standard customer')
ON CONFLICT (name) DO NOTHING;

-- ----- Default admin -----
INSERT INTO users (version, created_at, created_by, first_name, last_name, email, mobile_number,
                   password, enabled, account_non_locked)
VALUES
    (0, now(), 'SYSTEM', 'Hridaya', 'Admin', 'admin@ridhayacreations.com', '9999999999',
     '$2a$12$1Z2uwmA0mNdwKK4XC4Gqs.DBoUnqPxqdo9ASvdMobg5Fx1nhpru3m', TRUE, TRUE)
ON CONFLICT (email) DO NOTHING;

-- ----- Grant ADMIN role to the admin user -----
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
         JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE u.email = 'admin@ridhayacreations.com'
ON CONFLICT DO NOTHING;

-- ----- Sample categories -----
INSERT INTO categories (version, created_at, created_by, category_name, description, status)
VALUES
    (0, now(), 'SYSTEM', 'Personalized Mugs', 'Custom printed mugs with names, photos and messages', 'ACTIVE'),
    (0, now(), 'SYSTEM', 'Photo Frames',      'Personalized photo frames for every memory',          'ACTIVE'),
    (0, now(), 'SYSTEM', 'Cushions',          'Custom printed cushions and pillows',                 'ACTIVE'),
    (0, now(), 'SYSTEM', 'Keychains',         'Personalized name and photo keychains',               'ACTIVE'),
    (0, now(), 'SYSTEM', 'Gifts For Him',     'Thoughtful personalized gifts for him',               'ACTIVE'),
    (0, now(), 'SYSTEM', 'Gifts For Her',     'Thoughtful personalized gifts for her',               'ACTIVE'),
    (0, now(), 'SYSTEM', 'Anniversary Gifts', 'Celebrate love with personalized anniversary gifts',  'ACTIVE'),
    (0, now(), 'SYSTEM', 'Birthday Gifts',    'Make birthdays special with custom gifts',            'ACTIVE')
ON CONFLICT (category_name) DO NOTHING;

-- ----- Sample products -----
INSERT INTO products (version, created_at, created_by, name, description, short_description,
                      category_id, selling_price, original_price, stock_quantity, sku,
                      product_status, featured, customizable, average_rating, rating_count)
SELECT 0, now(), 'SYSTEM', v.name, v.description, v.short_description, c.id,
       v.selling_price, v.original_price, v.stock_quantity, v.sku,
       'ACTIVE', v.featured, TRUE, 0, 0
FROM (VALUES
    ('Personalized Photo Mug', 'A classic ceramic mug printed with your favourite photo and a custom message.',
     'Custom photo ceramic mug', 'Personalized Mugs', 299.00, 399.00, 120, 'HC-PER-1001', TRUE),
    ('Magic Heat Reveal Mug', 'A heat-sensitive magic mug that reveals your photo when filled with a hot beverage.',
     'Heat-reveal magic mug', 'Personalized Mugs', 449.00, 599.00, 80, 'HC-MAG-1002', TRUE),
    ('LED Personalized Photo Frame', 'An elegant LED-lit frame customised with your photo and name.',
     'LED-lit personalized frame', 'Photo Frames', 899.00, 1199.00, 50, 'HC-LED-1003', TRUE),
    ('Wooden Engraved Photo Frame', 'A premium wooden frame laser-engraved with your photo and a message.',
     'Laser-engraved wooden frame', 'Photo Frames', 749.00, 999.00, 60, 'HC-WOO-1004', FALSE),
    ('Custom Printed Cushion', 'A soft cushion printed with your photo and message — perfect for gifting.',
     'Custom photo cushion', 'Cushions', 499.00, 699.00, 100, 'HC-CUS-1005', TRUE),
    ('Personalized Name Keychain', 'A durable metal keychain engraved with a name of your choice.',
     'Engraved name keychain', 'Keychains', 149.00, 249.00, 200, 'HC-NAM-1006', FALSE),
    ('Anniversary Photo Collage Frame', 'A multi-photo collage frame to celebrate years of togetherness.',
     'Multi-photo collage frame', 'Anniversary Gifts', 1099.00, 1499.00, 40, 'HC-ANN-1007', TRUE),
    ('Birthday Surprise Gift Box', 'A curated personalized gift box to make birthdays unforgettable.',
     'Personalized birthday gift box', 'Birthday Gifts', 1299.00, 1699.00, 35, 'HC-BIR-1008', TRUE)
) AS v(name, description, short_description, category_name,
       selling_price, original_price, stock_quantity, sku, featured)
JOIN categories c ON c.category_name = v.category_name
ON CONFLICT (sku) DO NOTHING;

-- ----- Sample tags -----
INSERT INTO product_tags (product_id, tag)
SELECT p.id, t.tag
FROM products p
JOIN (VALUES
    ('HC-PER-1001', 'mug'), ('HC-PER-1001', 'photo'), ('HC-PER-1001', 'birthday'),
    ('HC-MAG-1002', 'mug'), ('HC-MAG-1002', 'magic'),
    ('HC-LED-1003', 'frame'), ('HC-LED-1003', 'led'), ('HC-LED-1003', 'anniversary'),
    ('HC-CUS-1005', 'cushion'), ('HC-CUS-1005', 'home'),
    ('HC-NAM-1006', 'keychain'), ('HC-NAM-1006', 'name'),
    ('HC-ANN-1007', 'anniversary'), ('HC-ANN-1007', 'collage'),
    ('HC-BIR-1008', 'birthday'), ('HC-BIR-1008', 'giftbox')
) AS t(sku, tag) ON t.sku = p.sku;
