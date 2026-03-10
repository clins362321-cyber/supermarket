DROP DATABASE IF EXISTS `supermarket`;
CREATE DATABASE IF NOT EXISTS `supermarket` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `supermarket`;

DROP TABLE IF EXISTS `order_item`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `delivery_person`;
DROP TABLE IF EXISTS `delivery_route`;
DROP TABLE IF EXISTS `product_comment`;
DROP TABLE IF EXISTS `product_media`;
DROP TABLE IF EXISTS `product`;
DROP TABLE IF EXISTS `user_address`;
DROP TABLE IF EXISTS `admin`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `full_name` VARCHAR(100) NULL,
  `phone` VARCHAR(20) NULL,
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `role` VARCHAR(30) NOT NULL DEFAULT 'ADMIN',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `description` VARCHAR(500) NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `stock` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `product_media` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `media_type` VARCHAR(20) NOT NULL,
  `url` VARCHAR(500) NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_media_product` (`product_id`),
  CONSTRAINT `fk_media_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `product_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `username` VARCHAR(50) NOT NULL,
  `rating` INT NOT NULL,
  `content` VARCHAR(1000) NOT NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_comment_product` (`product_id`),
  CONSTRAINT `fk_comment_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `receiver_name` VARCHAR(50) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `detail` VARCHAR(255) NOT NULL,
  `is_default` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_address_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 配送员
CREATE TABLE `delivery_person` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 配送路线
CREATE TABLE `delivery_route` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `description` VARCHAR(255) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `total_price` DECIMAL(10,2) NOT NULL,
  `created_at` DATETIME NOT NULL,
  `address_id` BIGINT NULL,
  `delivery_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING_ACCEPT',
  `delivery_person_id` BIGINT NULL,
  `delivery_route_id` BIGINT NULL,
  `delivery_remark` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  KEY `idx_orders_username` (`username`),
  KEY `idx_orders_delivery_status` (`delivery_status`),
  KEY `idx_orders_delivery_person` (`delivery_person_id`),
  CONSTRAINT `fk_orders_address` FOREIGN KEY (`address_id`) REFERENCES `user_address` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_orders_delivery_person` FOREIGN KEY (`delivery_person_id`) REFERENCES `delivery_person` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_orders_delivery_route` FOREIGN KEY (`delivery_route_id`) REFERENCES `delivery_route` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `product_name` VARCHAR(255) NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `qty` INT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_item_order` (`order_id`),
  CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `delivery_person` (`name`, `phone`, `status`)
VALUES ('配送员A', '13800138001', 'ACTIVE'), ('配送员B', '13800138002', 'ACTIVE');

INSERT INTO `delivery_route` (`name`, `description`)
VALUES ('A区路线', '东区配送'), ('B区路线', '西区配送');

-- 密码使用 BCrypt 哈希，明文均为 123456
INSERT INTO `admin` (`username`, `password`)
VALUES ('lin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW');

INSERT INTO `user` (`username`, `password`, `full_name`, `phone`, `balance`)
VALUES ('shu', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '示例用户', '13800000000', 0.00);

INSERT INTO `product` (`name`, `description`, `price`, `stock`)
VALUES
  ('新鲜苹果', '脆甜多汁，当季水果', 8.80, 200),
  ('有机牛奶', '1L 装家庭装有机牛奶', 12.50, 150);

