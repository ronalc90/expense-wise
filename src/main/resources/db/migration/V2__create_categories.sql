CREATE TABLE categories (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL,
    icon       VARCHAR(50),
    user_id    BIGINT REFERENCES users(id) ON DELETE CASCADE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(name, user_id)
);

CREATE INDEX idx_categories_user_id ON categories(user_id);
