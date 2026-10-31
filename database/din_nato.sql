-- DIN NATO: Loss and Found Management System
-- Database setup script (matches the existing Java models)

CREATE DATABASE IF NOT EXISTS din_nato
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE din_nato;

-- Table 1: users
-- password_hash holds "salt:hash" produced by UserService (about 70 chars)
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('Admin', 'Staff') NOT NULL DEFAULT 'Staff',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table 2: items (lost and found reports share one table)
-- reported_by is plain text because the app also uses names like "Security Desk"
CREATE TABLE IF NOT EXISTS items (
    item_id       INT AUTO_INCREMENT PRIMARY KEY,
    item_name     VARCHAR(100) NOT NULL,
    category      VARCHAR(50)  NOT NULL,
    description   TEXT,
    location      VARCHAR(150) NOT NULL,
    date_reported DATE NOT NULL,
    item_type     ENUM('Lost', 'Found') NOT NULL,
    status        VARCHAR(20) NOT NULL,
    reported_by   VARCHAR(100) NOT NULL DEFAULT '',
    INDEX idx_items_type_status (item_type, status),
    INDEX idx_items_name (item_name)
);

-- Table 3: claims
-- claimant_name is plain text because the claim form lets staff type the claimant
CREATE TABLE IF NOT EXISTS claims (
    claim_id      INT AUTO_INCREMENT PRIMARY KEY,
    item_id       INT NOT NULL,
    claimant_name VARCHAR(100) NOT NULL,
    claim_date    DATE NOT NULL,
    status        ENUM('Pending', 'Approved', 'Rejected') NOT NULL DEFAULT 'Pending',
    remarks       TEXT,
    CONSTRAINT fk_claims_item
        FOREIGN KEY (item_id) REFERENCES items(item_id)
        ON DELETE CASCADE,
    INDEX idx_claims_item (item_id)
);
