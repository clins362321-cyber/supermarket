## 项目功能概览

- 用户端：用户注册与登录、商品浏览与搜索、购物车、下单、订单售后查看、商品评价、个人资料和地址管理。
- 管理端：管理员登录、商品增删改查、用户与人员管理、配送管理。
- 媒体管理：商品图片和视频本地文件上传与访问。

## 环境依赖

- 操作系统：Windows 10 或以上。
- JDK：Java 17。
- 构建工具：Maven 3.9 或以上。
- 数据库：MySQL 8.x。
- Node.js：Node 18 或以上。
- 包管理器：npm。

## 数据库初始化

1. 启动 MySQL 并确认存在 root 用户和密码与 `application.properties` 中配置一致。
2. 在 MySQL 客户端中执行根目录下的 `initial.sql`:
   - 方式一：命令行 `mysql -u root -p < initial.sql`。
   - 方式二：在图形化工具中打开 `initial.sql` 并执行全部语句。
3. 执行完成后会创建 `supermarket` 库以及项目所需的所有表和示例数据：
   - 管理员：账号 `admin`，密码 `123456`。
   - 示例用户：账号 `user`，密码 `123456`。
   - 示例商品与基础表结构。

## 后端部署

1. 在 IDE 或命令行中打开后端工程目录 `supermarket`。
2. 检查配置文件 `src/main/resources/application.properties` 中的数据库连接信息是否与本地 MySQL 一致。
3. 配置文件 `application.yml` 中的文件上传目录和访问前缀可按需调整：
   - `file.upload-dir`：本地文件存储路径。
   - `file.access-prefix`：静态资源访问前缀，默认为 `/files`。
4. 在项目根目录执行：
   - `mvn clean package` 生成可运行包。
   - 或使用 IDE 直接运行 `SupermarketApplication` 主类。
5. 后端启动成功后默认监听 `http://localhost:8080`。

## 前端部署

1. 在 `supermarket-ui` 目录执行依赖安装：
   - `npm install`。
2. 启动开发服务器：
   - `npm run dev`。
3. 前端默认运行在 `http://localhost:5174`，通过 Vite 代理将以 `/api` 和 `/files` 开头的请求转发到后端：
   - 接口请求：`/api/...` → `http://localhost:8080/api/...`。
   - 媒体资源：`/files/...` → `http://localhost:8080/files/...`。

## 账号与角色

- 管理员登录：
  - 账号：`admin`
  - 密码：`123456`
  - 登录后进入管理后台，进行商品和人员管理。
- 普通用户登录：
  - 账号：`user`
  - 密码：`123456`
  - 或通过注册页面创建新用户。

## 核心业务流程

- 用户注册和登录：
  - 用户通过注册接口写入 `user` 表。
  - 登录时根据选择的身份验证 `user` 或 `admin` 表。
- 商品与媒体：
  - 管理员通过管理端维护 `product` 和 `product_media`。
  - 媒体文件上传至本地磁盘目录，并通过 `/files/...` URL 访问。
- 购物车与下单：
  - 前端将选购商品存入浏览器本地存储的购物车。
  - 结算时调用 `/api/orders` 创建订单并扣减库存。
- 商品评价与售后：
  - 用户下单后可对已购买商品进行评价。
  - 售后页面按用户展示订单及订单明细，为退换货等功能提供入口。

