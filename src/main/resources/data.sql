-- Default categories seeded on startup (H2 in-memory mode with create-drop).
-- These categories have no user (user_id IS NULL) and is_default = TRUE,
-- making them available to all users.
INSERT INTO categories (name, icon, is_default, user_id, created_at) VALUES
    ('Food & Dining',    'utensils',        TRUE, NULL, CURRENT_TIMESTAMP),
    ('Transportation',   'car',             TRUE, NULL, CURRENT_TIMESTAMP),
    ('Housing',          'home',            TRUE, NULL, CURRENT_TIMESTAMP),
    ('Utilities',        'zap',             TRUE, NULL, CURRENT_TIMESTAMP),
    ('Healthcare',       'heart-pulse',     TRUE, NULL, CURRENT_TIMESTAMP),
    ('Entertainment',    'film',            TRUE, NULL, CURRENT_TIMESTAMP),
    ('Shopping',         'shopping-bag',    TRUE, NULL, CURRENT_TIMESTAMP),
    ('Education',        'book-open',       TRUE, NULL, CURRENT_TIMESTAMP),
    ('Travel',           'plane',           TRUE, NULL, CURRENT_TIMESTAMP),
    ('Software & Tools', 'laptop',          TRUE, NULL, CURRENT_TIMESTAMP),
    ('Office Supplies',  'paperclip',       TRUE, NULL, CURRENT_TIMESTAMP),
    ('Insurance',        'shield',          TRUE, NULL, CURRENT_TIMESTAMP),
    ('Taxes',            'landmark',        TRUE, NULL, CURRENT_TIMESTAMP),
    ('Other',            'more-horizontal', TRUE, NULL, CURRENT_TIMESTAMP);
