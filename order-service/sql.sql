CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store VARCHAR(100),
    timestamp TIMESTAMP,
    status VARCHAR(20),
    total_amount DOUBLE,
    items TEXT
);
