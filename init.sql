-- ===============================
-- order-service (orderdb)
-- ===============================
CREATE DATABASE IF NOT EXISTS orderdb;
USE orderdb;

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id)
        ON DELETE CASCADE
);


-- ===============================
-- inventory-service (inventorydb)
-- ===============================
CREATE DATABASE IF NOT EXISTS inventorydb;
USE inventorydb;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    price DOUBLE NOT NULL,
    category_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    description TEXT,
    FOREIGN KEY (category_id) REFERENCES categories(id)
        ON DELETE CASCADE
);


-- ===============================
-- store-service (storedb_a)
-- ===============================
CREATE DATABASE IF NOT EXISTS storedb_a;
USE storedb_a;

-- Ajuste: quitamos AUTO_INCREMENT para que permita IDs explícitos desde el evento
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DOUBLE NOT NULL,
    category_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    description TEXT,
    FOREIGN KEY (category_id) REFERENCES categories(id)
        ON DELETE CASCADE
);

