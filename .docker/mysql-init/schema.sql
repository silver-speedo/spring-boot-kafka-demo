CREATE TABLE customers
(
    id          CHAR(36) PRIMARY KEY,
    customer_id VARCHAR(255) UNIQUE NOT NULL,
    name        VARCHAR(255)        NOT NULL,
    street      VARCHAR(255)        NOT NULL,
    city        VARCHAR(100)        NOT NULL,
    postal_code VARCHAR(20)         NOT NULL,
    country     VARCHAR(100)        NOT NULL
);

CREATE TABLE orders
(
    id              CHAR(36) PRIMARY KEY,
    order_id        VARCHAR(255) UNIQUE NOT NULL,
    customer_id     CHAR(36)            NOT NULL,
    order_timestamp DATETIME(6)         NOT NULL,
    status          VARCHAR(50)         NOT NULL,
    version         INT                 NOT NULL,

    CONSTRAINT fk_customer FOREIGN KEY (customer_id)
        REFERENCES customers (id)
        ON DELETE RESTRICT
);

CREATE TABLE order_items
(
    id         CHAR(36) PRIMARY KEY,
    orders_id  CHAR(36),
    sku        VARCHAR(100)   NOT NULL,
    name       VARCHAR(255)   NOT NULL,
    quantity   INT            NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,

    CONSTRAINT fk_order FOREIGN KEY (orders_id)
        REFERENCES orders (id)
        ON DELETE CASCADE
);

-- Indexes
CREATE UNIQUE INDEX idx_customers_customer_id ON customers (customer_id);
CREATE UNIQUE INDEX idx_orders_order_id ON orders (order_id);
CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_order_items_sku ON order_items (sku);
