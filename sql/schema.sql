-- 测试用例管理系统数据库初始化脚本
-- 适用版本：MySQL 8.0.46

CREATE DATABASE IF NOT EXISTS testcase_db
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'tcm'@'%' IDENTIFIED WITH mysql_native_password BY 'Tcm@123456';
GRANT ALL PRIVILEGES ON testcase_db.* TO 'tcm'@'%';
FLUSH PRIVILEGES;

USE testcase_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) DEFAULT NULL,
    role VARCHAR(20) DEFAULT NULL,
    created_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS projects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(30) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT NULL,
    created_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_projects_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS modules (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    name VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT NULL,
    created_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_modules_project (project_id),
    KEY idx_modules_parent (parent_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS test_cases (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    module_id BIGINT DEFAULT NULL,
    case_no VARCHAR(50) DEFAULT NULL,
    title VARCHAR(200) NOT NULL,
    priority VARCHAR(10) DEFAULT NULL,
    type VARCHAR(20) DEFAULT NULL,
    status VARCHAR(20) DEFAULT NULL,
    precondition TEXT,
    steps TEXT,
    tags VARCHAR(200) DEFAULT NULL,
    creator VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_test_cases_case_no (case_no),
    KEY idx_test_cases_project (project_id),
    KEY idx_test_cases_module (module_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS test_plans (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT NULL,
    start_date DATE DEFAULT NULL,
    end_date DATE DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT NULL,
    cases_initialized TINYINT(1) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_test_plans_project (project_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS test_plan_cases (
    id BIGINT NOT NULL AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_test_plan_cases_plan_case (plan_id, case_id),
    KEY idx_test_plan_cases_case (case_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS test_runs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT NULL,
    executor VARCHAR(50) DEFAULT NULL,
    remark TEXT,
    executed_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_test_runs_plan_case (plan_id, case_id),
    KEY idx_test_runs_case (case_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
