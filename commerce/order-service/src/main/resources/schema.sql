CREATE TABLE IF NOT EXISTS orders
(
    id             BIGSERIAL PRIMARY KEY,
    customer_name  VARCHAR(255)   NOT NULL,
    customer_email VARCHAR(255)   NOT NULL,
    status         VARCHAR(50)    NOT NULL,
    total_price    NUMERIC(19, 2) NOT NULL,
    status_details VARCHAR(1000),
    created_at     TIMESTAMP      NOT NULL
    );

CREATE TABLE IF NOT EXISTS order_items
(
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT         NOT NULL,
    product_id   BIGINT         NOT NULL,
    product_name VARCHAR(255)   NOT NULL,
    quantity     INTEGER        NOT NULL,
    price        NUMERIC(19, 2) NOT NULL,

    CONSTRAINT fk_order_items_order
    FOREIGN KEY (order_id)
    REFERENCES orders (id),

    CONSTRAINT chk_order_items_quantity
    CHECK (quantity >= 1),

    CONSTRAINT chk_order_items_price
    CHECK (price >= 0.01)
    );

CREATE INDEX IF NOT EXISTS idx_orders_customer_email
    ON orders (customer_email);