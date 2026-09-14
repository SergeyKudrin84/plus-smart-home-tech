CREATE TABLE IF NOT EXISTS categories
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(500)
    );

CREATE TABLE IF NOT EXISTS products
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    description VARCHAR(2000),
    price       NUMERIC(19, 2) NOT NULL,
    category_id BIGINT         NOT NULL,
    image_url   VARCHAR(255),
    active      BOOLEAN        NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_products_category
    FOREIGN KEY (category_id)
    REFERENCES categories (id)
    );