-- ============================================================
-- 企业固定资产全生命周期管理系统 (EAM) 数据库设计
-- 数据库: MySQL 8.x
-- 字符集: utf8mb4
-- 作者: 毕业设计项目组
-- 创建时间: 2026-03-09
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS eam_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE eam_system;

-- ============================================================
-- 第一部分：RBAC 权限体系表（5张表）
-- 设计理念：基于角色的访问控制（Role-Based Access Control）
-- 支持用户-角色-权限的多对多关系，实现细粒度权限控制
-- ============================================================

-- ------------------------------------------------------------
-- 表1: sys_user (用户表)
-- 说明: 存储系统用户基本信息，支持部门归属以实现跨部门调拨
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '用户主键ID',
    username        VARCHAR(50)     NOT NULL COMMENT '用户登录账号，唯一',
    password        VARCHAR(100)    NOT NULL COMMENT '用户密码（BCrypt加密存储）',
    real_name       VARCHAR(50)     NOT NULL COMMENT '用户真实姓名',
    dept_id         BIGINT          DEFAULT NULL COMMENT '所属部门ID，关联sys_dept表，支持跨部门调拨场景',
    phone           VARCHAR(20)     DEFAULT NULL COMMENT '联系电话',
    email           VARCHAR(100)    DEFAULT NULL COMMENT '电子邮箱',
    status          TINYINT         DEFAULT 1 COMMENT '账号状态：1-正常启用，0-禁用',
    avatar          VARCHAR(255)    DEFAULT NULL COMMENT '用户头像URL',
    is_deleted      TINYINT         DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间，自动填充',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间，自动填充',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表-存储用户基本信息与部门归属';

-- ------------------------------------------------------------
-- 表2: sys_dept (部门表)
-- 说明: 组织架构中的部门信息，支持跨部门资产调拨
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '部门主键ID',
    dept_name       VARCHAR(50)     NOT NULL COMMENT '部门名称',
    parent_id       BIGINT          DEFAULT 0 COMMENT '父部门ID，0表示顶级部门，支持多级组织架构',
    dept_path       VARCHAR(255)    DEFAULT NULL COMMENT '部门层级路径，如：/总部/财务部/核算组',
    sort            INT             DEFAULT 0 COMMENT '显示排序号，数值越小越靠前',
    leader          VARCHAR(50)     DEFAULT NULL COMMENT '部门负责人姓名',
    phone           VARCHAR(20)     DEFAULT NULL COMMENT '部门联系电话',
    status          TINYINT         DEFAULT 1 COMMENT '部门状态：1-正常，0-停用',
    is_deleted      TINYINT         DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表-组织架构信息，支持跨部门资产调拨';

-- ------------------------------------------------------------
-- 表3: sys_role (角色表)
-- 说明: 定义系统角色，如管理员、资产管理员、普通员工、财务人员等
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '角色主键ID',
    role_name       VARCHAR(50)     NOT NULL COMMENT '角色名称，如：系统管理员、资产管理员、财务人员',
    role_code       VARCHAR(50)     NOT NULL COMMENT '角色编码，用于程序判断，如：ADMIN、ASSET_MANAGER、FINANCE',
    description     VARCHAR(255)    DEFAULT NULL COMMENT '角色描述说明',
    status          TINYINT         DEFAULT 1 COMMENT '角色状态：1-正常启用，0-禁用',
    is_deleted      TINYINT         DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表-定义系统角色，实现RBAC权限控制';

-- ------------------------------------------------------------
-- 表4: sys_menu (菜单/权限表)
-- 说明: 系统菜单与操作权限，支持树形结构，包含菜单权限和按钮权限
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '菜单/权限主键ID',
    parent_id       BIGINT          DEFAULT 0 COMMENT '父菜单ID，0表示顶级菜单，支持多级菜单树',
    menu_name       VARCHAR(50)     NOT NULL COMMENT '菜单名称',
    menu_code       VARCHAR(100)    DEFAULT NULL COMMENT '菜单权限标识，如：asset:list、asset:add',
    menu_type       TINYINT         DEFAULT 1 COMMENT '菜单类型：1-目录菜单，2-页面菜单，3-按钮权限',
    path            VARCHAR(255)    DEFAULT NULL COMMENT '前端路由路径，如：/asset/list',
    component       VARCHAR(255)    DEFAULT NULL COMMENT '前端组件路径，如：asset/AssetList',
    icon            VARCHAR(100)    DEFAULT NULL COMMENT '菜单图标名称',
    sort            INT             DEFAULT 0 COMMENT '显示排序号',
    visible         TINYINT         DEFAULT 1 COMMENT '是否可见：1-显示，0-隐藏',
    status          TINYINT         DEFAULT 1 COMMENT '菜单状态：1-正常，0-禁用',
    is_deleted      TINYINT         DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表-定义系统菜单与操作权限，支持树形结构';

-- ------------------------------------------------------------
-- 表5: sys_user_role (用户-角色关联表)
-- 说明: 用户与角色的多对多关系，一个用户可拥有多个角色
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '关联主键ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID，关联sys_user.id',
    role_id         BIGINT          NOT NULL COMMENT '角色ID，关联sys_role.id',
    is_deleted      TINYINT         DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表-实现用户与角色的多对多关系';

-- ------------------------------------------------------------
-- 表6: sys_role_menu (角色-菜单关联表)
-- 说明: 角色与菜单权限的多对多关系，定义角色拥有的菜单和操作权限
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '关联主键ID',
    role_id         BIGINT          NOT NULL COMMENT '角色ID，关联sys_role.id',
    menu_id         BIGINT          NOT NULL COMMENT '菜单ID，关联sys_menu.id',
    is_deleted      TINYINT         DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    KEY idx_role_id (role_id),
    KEY idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表-定义角色拥有的菜单和操作权限';

-- ============================================================
-- 第二部分：资产管理核心业务表
-- 设计理念：实现资产全生命周期管理（ALM）
-- 从入库、领用、调拨、维修到报废的完整闭环
-- ============================================================

-- ------------------------------------------------------------
-- 表7: ast_category (资产分类表)
-- 说明: 资产分类信息，决定折旧年限和残值率
-- 支持多级分类树形结构
-- ------------------------------------------------------------
DROP TABLE IF EXISTS ast_category;
CREATE TABLE ast_category (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '分类主键ID',
    parent_id       BIGINT          DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
    category_name   VARCHAR(50)     NOT NULL COMMENT '分类名称，如：电子设备、办公家具、运输工具',
    category_code   VARCHAR(50)     DEFAULT NULL COMMENT '分类编码，如：ELECTRONIC、FURNITURE',
    depreciation_years INT          DEFAULT 5 COMMENT '折旧年限（年），用于计算折旧',
    residual_rate   DECIMAL(5,2)    DEFAULT 5.00 COMMENT '预计净残值率（%），如：5.00表示5%',
    sort            INT             DEFAULT 0 COMMENT '显示排序号',
    status          TINYINT         DEFAULT 1 COMMENT '分类状态：1-正常，0-禁用',
    is_deleted      TINYINT         DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产分类表-定义资产类别，决定折旧年限和残值率';

-- ------------------------------------------------------------
-- 表8: ast_asset (资产主表) - 核心中的核心
-- 说明: 存储资产完整信息，包含UUID唯一标识、乐观锁版本号、价值字段、状态字段
-- 实现资产全生命周期状态管理
-- ------------------------------------------------------------
DROP TABLE IF EXISTS ast_asset;
CREATE TABLE ast_asset (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '资产主键ID',
    uuid                VARCHAR(36)     NOT NULL COMMENT '资产全局唯一标识符（UUID），用于二维码生成和跨系统识别',
    asset_code          VARCHAR(50)     DEFAULT NULL COMMENT '资产编码，企业内部编号',
    asset_name          VARCHAR(100)    NOT NULL COMMENT '资产名称',
    category_id         BIGINT          NOT NULL COMMENT '资产分类ID，关联ast_category.id，决定折旧参数',
    brand               VARCHAR(100)    DEFAULT NULL COMMENT '品牌',
    model               VARCHAR(100)    DEFAULT NULL COMMENT '型号规格',
    specification       VARCHAR(255)    DEFAULT NULL COMMENT '详细规格参数',
    
    -- 价值相关字段（财务核心）
    original_value      DECIMAL(12,2)   NOT NULL COMMENT '资产原值（元），购置时的初始价值',
    net_value           DECIMAL(12,2)   NOT NULL COMMENT '资产净值（元），原值减去累计折旧后的价值',
    residual_rate       DECIMAL(5,2)    DEFAULT 5.00 COMMENT '预计净残值率（%），从分类继承或单独设定',
    accumulated_depreciation DECIMAL(12,2) DEFAULT 0.00 COMMENT '累计折旧金额（元）',
    
    -- 时间相关字段
    purchase_date       DATE            DEFAULT NULL COMMENT '购置日期',
    warranty_expire_date DATE           DEFAULT NULL COMMENT '保修到期日期',
    depreciation_start_date DATE        DEFAULT NULL COMMENT '折旧开始日期，通常为入库次月',
    
    -- 状态与位置字段
    status              TINYINT         DEFAULT 0 COMMENT '资产状态：0-闲置，1-在用，2-维修中，3-报废',
    dept_id             BIGINT          DEFAULT NULL COMMENT '当前使用部门ID，关联sys_dept.id',
    location            VARCHAR(255)    DEFAULT NULL COMMENT '存放位置，如：A栋3楼财务部',
    user_id             BIGINT          DEFAULT NULL COMMENT '当前使用人ID，关联sys_user.id',
    
    -- 来源与供应商
    source_type         TINYINT         DEFAULT 1 COMMENT '来源类型：1-采购入库，2-调拨入库，3-捐赠',
    supplier            VARCHAR(100)     DEFAULT NULL COMMENT '供应商名称',
    invoice_number      VARCHAR(50)     DEFAULT NULL COMMENT '发票号码',
    
    -- 图片与附件
    image_url           VARCHAR(255)    DEFAULT NULL COMMENT '资产图片URL',
    
    -- 并发控制（乐观锁）
    version             INT             DEFAULT 0 COMMENT '乐观锁版本号，MyBatis-Plus自动维护，防止并发更新覆盖',
    
    -- 通用字段
    remark              VARCHAR(500)    DEFAULT NULL COMMENT '备注说明',
    is_deleted          TINYINT         DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_uuid (uuid),
    KEY idx_asset_code (asset_code),
    KEY idx_category_id (category_id),
    KEY idx_status (status),
    KEY idx_dept_id (dept_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产主表-存储资产完整信息，包含UUID、乐观锁、价值、状态等核心字段';

-- ------------------------------------------------------------
-- 表9: ast_operation_record (资产操作记录表)
-- 说明: 记录资产全生命周期的所有变动操作，实现责任可追溯
-- 操作类型包括：入库、领用、退库、调拨、维修、报废等
-- ------------------------------------------------------------
DROP TABLE IF EXISTS ast_operation_record;
CREATE TABLE ast_operation_record (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '操作记录主键ID',
    asset_id            BIGINT          NOT NULL COMMENT '资产ID，关联ast_asset.id',
    asset_uuid          VARCHAR(36)     NOT NULL COMMENT '资产UUID，冗余存储便于查询',
    operate_type        TINYINT         NOT NULL COMMENT '操作类型：1-入库，2-领用，3-退库，4-调拨，5-维修，6-报废',
    
    -- 操作详情
    from_dept_id        BIGINT          DEFAULT NULL COMMENT '原部门ID（调拨时使用）',
    to_dept_id          BIGINT          DEFAULT NULL COMMENT '目标部门ID（领用、调拨时使用）',
    from_user_id        BIGINT          DEFAULT NULL COMMENT '原使用人ID',
    to_user_id          BIGINT          DEFAULT NULL COMMENT '目标使用人ID',
    from_status         TINYINT         DEFAULT NULL COMMENT '变更前状态',
    to_status           TINYINT         DEFAULT NULL COMMENT '变更后状态',
    
    -- 操作信息
    description         VARCHAR(500)    DEFAULT NULL COMMENT '操作描述说明',
    operator_id         BIGINT          NOT NULL COMMENT '操作人ID，关联sys_user.id',
    operator_name       VARCHAR(50)     DEFAULT NULL COMMENT '操作人姓名，冗余存储',
    operate_time        DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    
    -- 审批相关
    approval_status     TINYINT         DEFAULT 0 COMMENT '审批状态：0-无需审批，1-待审批，2-已通过，3-已驳回',
    approver_id         BIGINT          DEFAULT NULL COMMENT '审批人ID',
    approval_time       DATETIME        DEFAULT NULL COMMENT '审批时间',
    approval_remark     VARCHAR(255)    DEFAULT NULL COMMENT '审批意见',
    
    -- 通用字段
    is_deleted          TINYINT         DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    
    PRIMARY KEY (id),
    KEY idx_asset_id (asset_id),
    KEY idx_asset_uuid (asset_uuid),
    KEY idx_operate_type (operate_type),
    KEY idx_operator_id (operator_id),
    KEY idx_operate_time (operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产操作记录表-记录资产全生命周期变动，实现责任可追溯';

-- ------------------------------------------------------------
-- 表10: ast_depreciation (折旧记录表)
-- 说明: 记录每次自动折旧的详细信息，用于财务审计
-- 支持多种折旧算法，默认采用平均年限法（直线法）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS ast_depreciation;
CREATE TABLE ast_depreciation (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '折旧记录主键ID',
    asset_id            BIGINT          NOT NULL COMMENT '资产ID，关联ast_asset.id',
    asset_uuid          VARCHAR(36)     NOT NULL COMMENT '资产UUID，冗余存储',
    asset_name          VARCHAR(100)    DEFAULT NULL COMMENT '资产名称，冗余存储便于查询',
    
    -- 折旧计算参数
    original_value      DECIMAL(12,2)   NOT NULL COMMENT '本次折旧时的资产原值',
    net_value_before    DECIMAL(12,2)   NOT NULL COMMENT '折旧前净值',
    depreciation_amount DECIMAL(12,2)   NOT NULL COMMENT '本次折旧金额（元）',
    net_value_after     DECIMAL(12,2)   NOT NULL COMMENT '折旧后净值',
    
    -- 折旧方法与周期
    depreciation_method VARCHAR(50)     DEFAULT 'STRAIGHT_LINE' COMMENT '折旧方法：STRAIGHT_LINE-直线法，DOUBLE_DECLINING-双倍余额递减法',
    depreciation_year   INT             NOT NULL COMMENT '折旧年度',
    depreciation_month  INT             NOT NULL COMMENT '折旧月份',
    
    -- 计算依据
    residual_rate       DECIMAL(5,2)    DEFAULT NULL COMMENT '计算时使用的残值率',
    depreciation_years  INT             DEFAULT NULL COMMENT '计算时使用的折旧年限',
    
    -- 执行信息
    execute_time        DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '折旧计算执行时间',
    execute_type        TINYINT         DEFAULT 1 COMMENT '执行类型：1-定时自动执行，2-手动补录',
    
    -- 通用字段
    remark              VARCHAR(255)    DEFAULT NULL COMMENT '备注说明',
    is_deleted          TINYINT         DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    
    PRIMARY KEY (id),
    KEY idx_asset_id (asset_id),
    KEY idx_depreciation_period (depreciation_year, depreciation_month),
    KEY idx_execute_time (execute_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='折旧记录表-记录每次自动折旧详情，用于财务审计';

-- ------------------------------------------------------------
-- 表11: ast_inventory (盘点任务表)
-- 说明: 资产盘点任务与结果记录，支持移动端扫码盘点
-- 实现账实一致性保障
-- ------------------------------------------------------------
DROP TABLE IF EXISTS ast_inventory;
CREATE TABLE ast_inventory (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '盘点记录主键ID',
    inventory_no        VARCHAR(50)     NOT NULL COMMENT '盘点单号，如：PD202603001',
    inventory_name      VARCHAR(100)    NOT NULL COMMENT '盘点任务名称',
    
    -- 盘点范围
    dept_id             BIGINT          DEFAULT NULL COMMENT '盘点部门ID，NULL表示全公司盘点',
    category_id         BIGINT          DEFAULT NULL COMMENT '盘点分类ID，NULL表示全部分类',
    
    -- 盘点时间
    plan_start_date     DATE            DEFAULT NULL COMMENT '计划开始日期',
    plan_end_date       DATE            DEFAULT NULL COMMENT '计划结束日期',
    actual_start_time   DATETIME        DEFAULT NULL COMMENT '实际开始时间',
    actual_end_time     DATETIME        DEFAULT NULL COMMENT '实际结束时间',
    
    -- 盘点统计
    total_count         INT             DEFAULT 0 COMMENT '应盘资产总数',
    inventoried_count   INT             DEFAULT 0 COMMENT '已盘资产数量',
    normal_count        INT             DEFAULT 0 COMMENT '正常资产数量（账实相符）',
    surplus_count       INT             DEFAULT 0 COMMENT '盘盈数量（账外资产）',
    loss_count          INT             DEFAULT 0 COMMENT '盘亏数量（账上有实物无）',
    
    -- 盘点状态
    status              TINYINT         DEFAULT 0 COMMENT '盘点状态：0-未开始，1-进行中，2-已完成，3-已取消',
    
    -- 责任人
    creator_id          BIGINT          NOT NULL COMMENT '盘点任务创建人ID',
    creator_name        VARCHAR(50)     DEFAULT NULL COMMENT '创建人姓名',
    
    -- 通用字段
    remark              VARCHAR(500)    DEFAULT NULL COMMENT '备注说明',
    is_deleted          TINYINT         DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_inventory_no (inventory_no),
    KEY idx_dept_id (dept_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点任务表-记录资产盘点任务与结果，实现账实一致性保障';

-- ------------------------------------------------------------
-- 表12: ast_inventory_detail (盘点明细表)
-- 说明: 盘点任务中每项资产的盘点结果明细
-- 支持移动端扫码实时记录
-- ------------------------------------------------------------
DROP TABLE IF EXISTS ast_inventory_detail;
CREATE TABLE ast_inventory_detail (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '盘点明细主键ID',
    inventory_id        BIGINT          NOT NULL COMMENT '盘点任务ID，关联ast_inventory.id',
    asset_id            BIGINT          NOT NULL COMMENT '资产ID，关联ast_asset.id',
    asset_uuid          VARCHAR(36)     NOT NULL COMMENT '资产UUID',
    asset_name          VARCHAR(100)    DEFAULT NULL COMMENT '资产名称，冗余存储',
    
    -- 盘点结果
    inventory_result    TINYINT         DEFAULT 0 COMMENT '盘点结果：0-正常（账实相符），1-盘盈，2-盘亏，3-状态异常',
    book_status         TINYINT         DEFAULT NULL COMMENT '账面状态（盘点前）',
    actual_status       TINYINT         DEFAULT NULL COMMENT '实际状态（盘点发现）',
    book_location       VARCHAR(255)    DEFAULT NULL COMMENT '账面存放位置',
    actual_location     VARCHAR(255)    DEFAULT NULL COMMENT '实际存放位置',
    
    -- 盘点信息
    scan_time           DATETIME        DEFAULT NULL COMMENT '扫码盘点时间',
    scanner_id          BIGINT          DEFAULT NULL COMMENT '盘点人ID',
    scanner_name        VARCHAR(50)     DEFAULT NULL COMMENT '盘点人姓名',
    
    -- 图片证据
    image_url           VARCHAR(255)    DEFAULT NULL COMMENT '盘点照片URL',
    
    -- 异常说明
    remark              VARCHAR(500)    DEFAULT NULL COMMENT '异常情况说明',
    
    -- 通用字段
    is_deleted          TINYINT         DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    
    PRIMARY KEY (id),
    KEY idx_inventory_id (inventory_id),
    KEY idx_asset_id (asset_id),
    KEY idx_inventory_result (inventory_result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点明细表-记录每项资产的盘点结果，支持移动端扫码盘点';

-- ------------------------------------------------------------
-- 表13: ast_maintenance (维修记录表)
-- 说明: 资产维修保养记录，实现规范化日常维护
-- ------------------------------------------------------------
DROP TABLE IF EXISTS ast_maintenance;
CREATE TABLE ast_maintenance (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '维修记录主键ID',
    asset_id            BIGINT          NOT NULL COMMENT '资产ID，关联ast_asset.id',
    asset_uuid          VARCHAR(36)     NOT NULL COMMENT '资产UUID',
    asset_name          VARCHAR(100)    DEFAULT NULL COMMENT '资产名称',
    
    -- 报修信息
    fault_description   VARCHAR(500)    NOT NULL COMMENT '故障描述',
    report_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '报修时间',
    reporter_id         BIGINT          NOT NULL COMMENT '报修人ID',
    reporter_name       VARCHAR(50)     DEFAULT NULL COMMENT '报修人姓名',
    
    -- 维修信息
    maintenance_type    TINYINT         DEFAULT 1 COMMENT '维修类型：1-故障维修，2-预防性保养，3-改造升级',
    maintenance_status  TINYINT         DEFAULT 0 COMMENT '维修状态：0-待维修，1-维修中，2-已完成，3-无法修复',
    start_time          DATETIME        DEFAULT NULL COMMENT '维修开始时间',
    end_time            DATETIME        DEFAULT NULL COMMENT '维修结束时间',
    maintenance_cost    DECIMAL(10,2)   DEFAULT 0.00 COMMENT '维修费用（元）',
    maintenance_vendor  VARCHAR(100)    DEFAULT NULL COMMENT '维修服务商',
    
    -- 结果记录
    maintenance_result  VARCHAR(500)    DEFAULT NULL COMMENT '维修结果描述',
    maintainer          VARCHAR(50)     DEFAULT NULL COMMENT '维修人员姓名',
    
    -- 通用字段
    remark              VARCHAR(255)    DEFAULT NULL COMMENT '备注说明',
    is_deleted          TINYINT         DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    
    PRIMARY KEY (id),
    KEY idx_asset_id (asset_id),
    KEY idx_maintenance_status (maintenance_status),
    KEY idx_report_time (report_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修记录表-记录资产维修保养信息，实现规范化日常维护';

-- ------------------------------------------------------------
-- 表14: ast_idle_activation (闲置资产盘活记录表)
-- 说明: 记录闲置资产的盘活调拨情况，实现资源流转激励
-- ------------------------------------------------------------
DROP TABLE IF EXISTS ast_idle_activation;
CREATE TABLE ast_idle_activation (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '盘活记录主键ID',
    asset_id            BIGINT          NOT NULL COMMENT '资产ID，关联ast_asset.id',
    asset_uuid          VARCHAR(36)     NOT NULL COMMENT '资产UUID',
    asset_name          VARCHAR(100)    DEFAULT NULL COMMENT '资产名称',
    
    -- 闲置信息
    idle_start_time     DATETIME        DEFAULT NULL COMMENT '开始闲置时间',
    idle_days           INT             DEFAULT 0 COMMENT '闲置天数',
    original_dept_id    BIGINT          DEFAULT NULL COMMENT '原所属部门ID',
    
    -- 调拨信息
    apply_dept_id       BIGINT          NOT NULL COMMENT '申请调拨部门ID',
    apply_user_id       BIGINT          NOT NULL COMMENT '申请人ID',
    apply_time          DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    apply_reason        VARCHAR(255)    DEFAULT NULL COMMENT '申请理由',
    
    -- 审批信息
    approval_status     TINYINT         DEFAULT 1 COMMENT '审批状态：1-待审批，2-已通过，3-已驳回',
    approver_id         BIGINT          DEFAULT NULL COMMENT '审批人ID',
    approval_time       DATETIME        DEFAULT NULL COMMENT '审批时间',
    approval_remark     VARCHAR(255)    DEFAULT NULL COMMENT '审批意见',
    
    -- 完成信息
    complete_time       DATETIME        DEFAULT NULL COMMENT '调拨完成时间',
    
    -- 通用字段
    is_deleted          TINYINT         DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    create_time         DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    
    PRIMARY KEY (id),
    KEY idx_asset_id (asset_id),
    KEY idx_apply_dept_id (apply_dept_id),
    KEY idx_approval_status (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='闲置资产盘活记录表-记录闲置资产调拨情况，实现资源流转激励';

-- ============================================================
-- 第三部分：初始化数据
-- 插入系统基础数据，包括默认角色、管理员账号等
-- ============================================================

-- 插入默认部门
INSERT INTO sys_dept (id, dept_name, parent_id, dept_path, sort, leader, status) VALUES
(1, '总公司', 0, '/总公司', 1, '张总', 1),
(2, '财务部', 1, '/总公司/财务部', 1, '李财务', 1),
(3, '行政部', 1, '/总公司/行政部', 2, '王行政', 1),
(4, '信息技术部', 1, '/总公司/信息技术部', 3, '赵技术', 1),
(5, '生产部', 1, '/总公司/生产部', 4, '钱生产', 1);

-- 插入默认角色
INSERT INTO sys_role (id, role_name, role_code, description, status) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '系统最高权限，可管理所有功能', 1),
(2, '资产管理员', 'ASSET_MANAGER', '负责资产的入库、调拨、盘点等管理', 1),
(3, '财务人员', 'FINANCE', '负责资产折旧核算、财务报表', 1),
(4, '普通员工', 'EMPLOYEE', '可查看本部门资产、发起领用申请', 1),
(5, '部门经理', 'DEPT_MANAGER', '管理本部门资产，审批领用调拨申请', 1);

-- 插入默认管理员账号（密码：123456，BCrypt加密后的值）
INSERT INTO sys_user (id, username, password, real_name, dept_id, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 1, 1);

-- 插入用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1);

-- 插入默认资产分类
INSERT INTO ast_category (id, parent_id, category_name, category_code, depreciation_years, residual_rate, status) VALUES
(1, 0, '电子设备', 'ELECTRONIC', 3, 5.00, 1),
(2, 0, '办公家具', 'FURNITURE', 5, 5.00, 1),
(3, 0, '运输工具', 'VEHICLE', 10, 5.00, 1),
(4, 0, '机械设备', 'MACHINE', 10, 5.00, 1),
(5, 1, '计算机设备', 'COMPUTER', 3, 5.00, 1),
(6, 1, '办公电子设备', 'OFFICE_ELECTRONIC', 3, 5.00, 1),
(7, 2, '办公桌椅', 'DESK_CHAIR', 5, 5.00, 1),
(8, 2, '文件柜', 'CABINET', 5, 5.00, 1);

-- ============================================================
-- 数据库设计说明
-- ============================================================
-- 
-- 1. 表命名规范：
--    - sys_* : 系统基础表（用户、角色、权限等）
--    - ast_* : 资产业务表（资产、分类、操作记录等）
--
-- 2. 字段命名规范：
--    - 使用下划线命名法（snake_case）
--    - 主键统一使用 id
--    - 外键统一使用 xxx_id 格式
--
-- 3. 核心设计理念：
--    - UUID全局唯一标识：支持二维码生成和跨系统识别
--    - Version乐观锁：防止高并发下的数据覆盖问题
--    - 逻辑删除：保留历史数据，支持数据恢复
--    - 自动填充：create_time、update_time 自动维护
--    - 全生命周期记录：ast_operation_record 记录所有变动
--
-- 4. 状态枚举说明：
--    - 资产状态(status)：0-闲置，1-在用，2-维修中，3-报废
--    - 操作类型(operate_type)：1-入库，2-领用，3-退库，4-调拨，5-维修，6-报废
--
-- ============================================================
