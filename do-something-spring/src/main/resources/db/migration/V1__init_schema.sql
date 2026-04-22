CREATE TABLE users
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50)  NOT NULL,
    password VARCHAR(255) NOT NULL,
    UNIQUE INDEX uq_username (username)
);

CREATE TABLE portfolio_assets
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol   VARCHAR(20) NOT NULL,
    quantity DOUBLE      NOT NULL,
    screener VARCHAR(20) NOT NULL,
    exchange VARCHAR(20) NOT NULL,
    preset   INT         NOT NULL DEFAULT 1,
    owner_id BIGINT      NOT NULL,
    INDEX idx_owner_preset (owner_id, preset),
    FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
);
