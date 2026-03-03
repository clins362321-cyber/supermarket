-- 生鲜超市物流配送系统（MySQL）初始化脚本
-- 说明：本项目后端改为 MyBatis-Plus，不使用 Hibernate/JPA，不会自动建表。
-- 你可以在 MySQL 中执行本脚本来创建表并插入初始数据。

CREATE TABLE IF NOT EXISTS `admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL,
  `password` VARCHAR(128) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL,
  `password` VARCHAR(128) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL,
  `description` VARCHAR(255) NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `stock` INT NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 商品媒体（图片 / 视频）
CREATE TABLE IF NOT EXISTS `product_media` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `media_type` VARCHAR(10) NOT NULL,
  `url` VARCHAR(255) NOT NULL,
  `sort_order` INT DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_media_product` (`product_id`),
  CONSTRAINT `fk_media_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 商品评价
CREATE TABLE IF NOT EXISTS `product_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `username` VARCHAR(64) NOT NULL,
  `rating` INT NOT NULL,
  `content` VARCHAR(500) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_comment_product` (`product_id`),
  CONSTRAINT `fk_comment_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始账号
INSERT INTO `admin` (`username`, `password`)
SELECT 'lin', '123456'
WHERE NOT EXISTS (SELECT 1 FROM `admin` WHERE `username` = 'lin');

INSERT INTO `user` (`username`, `password`)
SELECT 'shu', '123456'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'shu');

-- 示例商品
INSERT INTO `product` (`name`, `description`, `price`, `stock`)
SELECT '新鲜苹果', '当日到货', 8.80, 200
WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `name` = '新鲜苹果');

