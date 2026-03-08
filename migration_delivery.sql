-- 配送模块迁移脚本（适用于已有数据库，不删除数据）
-- 执行前请备份数据库

-- 1. 创建配送员表
CREATE TABLE IF NOT EXISTS `delivery_person` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 创建配送路线表
CREATE TABLE IF NOT EXISTS `delivery_route` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `description` VARCHAR(255) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 扩展 orders 表（若列已存在会报错，可跳过该条）
ALTER TABLE `orders` ADD COLUMN `address_id` BIGINT NULL;
ALTER TABLE `orders` ADD COLUMN `delivery_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING_ACCEPT';
ALTER TABLE `orders` ADD COLUMN `delivery_person_id` BIGINT NULL;
ALTER TABLE `orders` ADD COLUMN `delivery_route_id` BIGINT NULL;
ALTER TABLE `orders` ADD COLUMN `delivery_remark` VARCHAR(255) NULL;

-- 4. 添加外键（需先有 user_address 表）
-- ALTER TABLE `orders` ADD CONSTRAINT `fk_orders_address` FOREIGN KEY (`address_id`) REFERENCES `user_address` (`id`) ON DELETE SET NULL;
-- ALTER TABLE `orders` ADD CONSTRAINT `fk_orders_delivery_person` FOREIGN KEY (`delivery_person_id`) REFERENCES `delivery_person` (`id`) ON DELETE SET NULL;
-- ALTER TABLE `orders` ADD CONSTRAINT `fk_orders_delivery_route` FOREIGN KEY (`delivery_route_id`) REFERENCES `delivery_route` (`id`) ON DELETE SET NULL;

-- 5. 插入示例配送员和路线
INSERT INTO `delivery_person` (`name`, `phone`, `status`)
SELECT '配送员A', '13800138001', 'ACTIVE' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `delivery_person` LIMIT 1);

INSERT INTO `delivery_route` (`name`, `description`)
SELECT 'A区路线', '东区配送' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `delivery_route` LIMIT 1);
