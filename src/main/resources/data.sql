-- Default categories seeded on startup (H2 in-memory mode with create-drop).
-- These categories have no user (user_id IS NULL) and is_default = TRUE,
-- making them available to all users.
-- Uses conditional inserts to avoid duplicates if the script runs multiple times.
INSERT INTO categories (name, icon, is_default, user_id, created_at)
SELECT 'Alimentacion', 'utensils', TRUE, NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Alimentacion');

INSERT INTO categories (name, icon, is_default, user_id, created_at)
SELECT 'Transporte', 'car', TRUE, NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Transporte');

INSERT INTO categories (name, icon, is_default, user_id, created_at)
SELECT 'Oficina', 'briefcase', TRUE, NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Oficina');

INSERT INTO categories (name, icon, is_default, user_id, created_at)
SELECT 'Servicios', 'zap', TRUE, NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Servicios');

INSERT INTO categories (name, icon, is_default, user_id, created_at)
SELECT 'Software', 'laptop', TRUE, NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Software');

INSERT INTO categories (name, icon, is_default, user_id, created_at)
SELECT 'Marketing', 'megaphone', TRUE, NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Marketing');

INSERT INTO categories (name, icon, is_default, user_id, created_at)
SELECT 'Impuestos', 'landmark', TRUE, NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Impuestos');

INSERT INTO categories (name, icon, is_default, user_id, created_at)
SELECT 'Salud', 'heart-pulse', TRUE, NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Salud');

INSERT INTO categories (name, icon, is_default, user_id, created_at)
SELECT 'Educacion', 'book-open', TRUE, NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Educacion');

INSERT INTO categories (name, icon, is_default, user_id, created_at)
SELECT 'Entretenimiento', 'film', TRUE, NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Entretenimiento');

INSERT INTO categories (name, icon, is_default, user_id, created_at)
SELECT 'Seguros', 'shield', TRUE, NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Seguros');

INSERT INTO categories (name, icon, is_default, user_id, created_at)
SELECT 'Otros', 'more-horizontal', TRUE, NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Otros');
