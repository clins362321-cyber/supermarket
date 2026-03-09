-- 增量迁移：为已有数据库添加角色权限、操作审计、采购审批人
-- 若使用 initial.sql 全新初始化则无需执行本文件
-- 执行前请备份数据；若列/表已存在，对应语句会报错可忽略

-- admin 表添加 role 列
ALTER TABLE `admin` ADD COLUMN `role` VARCHAR(30) NOT NULL DEFAULT 'ADMIN';

-- procurement_apply 表添加 approver_username 列
ALTER TABLE `procurement_apply` ADD COLUMN `approver_username` VARCHAR(50) NULL;

-- 创建 after_sales 表
CREATE TABLE IF NOT EXISTS `after_sales` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `order_item_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `product_name` VARCHAR(255) NULL,
  `username` VARCHAR(50) NOT NULL,
  `reason` VARCHAR(500) NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_after_sales_username` (`username`),
  KEY `idx_after_sales_order` (`order_id`),
  CONSTRAINT `fk_after_sales_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建 audit_log 表
CREATE TABLE IF NOT EXISTS `audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NULL,
  `role` VARCHAR(30) NULL,
  `module` VARCHAR(50) NULL,
  `action` VARCHAR(50) NULL,
  `detail` VARCHAR(500) NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_audit_created` (`created_at`),
  KEY `idx_audit_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
