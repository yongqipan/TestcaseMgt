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
    source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    ai_confidence INT DEFAULT NULL,
    ai_review_score INT DEFAULT NULL,
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

-- ============================================================
-- AI 集成数据层
-- ============================================================

-- 模型提供商
CREATE TABLE IF NOT EXISTS ai_provider (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    base_url VARCHAR(200) DEFAULT NULL,
    api_key_cipher VARCHAR(500) DEFAULT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_provider_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 模型定义
CREATE TABLE IF NOT EXISTS ai_model (
    id BIGINT NOT NULL AUTO_INCREMENT,
    provider_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    model_code VARCHAR(100) NOT NULL,
    scene VARCHAR(30) DEFAULT NULL,
    context_window INT DEFAULT NULL,
    input_price DECIMAL(10, 4) DEFAULT NULL,
    output_price DECIMAL(10, 4) DEFAULT NULL,
    is_default TINYINT(1) NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_ai_model_provider (provider_id),
    KEY idx_ai_model_scene (scene)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Prompt 模板
CREATE TABLE IF NOT EXISTS ai_prompt_template (
    id BIGINT NOT NULL AUTO_INCREMENT,
    scene VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    content MEDIUMTEXT,
    variables VARCHAR(500) DEFAULT NULL,
    version INT NOT NULL DEFAULT 1,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    updated_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_ai_prompt_scene (scene)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 生成任务
CREATE TABLE IF NOT EXISTS ai_generation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    module_id BIGINT DEFAULT NULL,
    scene VARCHAR(30) NOT NULL DEFAULT 'GENERATE_CASE',
    requirement MEDIUMTEXT,
    file_refs TEXT,
    model_id BIGINT DEFAULT NULL,
    prompt_version INT DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_msg VARCHAR(500) DEFAULT NULL,
    total_count INT DEFAULT 0,
    accepted_count INT DEFAULT 0,
    created_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT NULL,
    finished_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_ai_generation_project (project_id),
    KEY idx_ai_generation_status (status),
    KEY idx_ai_generation_creator (created_by)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 生成结果条目
CREATE TABLE IF NOT EXISTS ai_generation_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    generation_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    priority VARCHAR(10) DEFAULT NULL,
    type VARCHAR(20) DEFAULT NULL,
    precondition TEXT,
    steps_json TEXT,
    expected TEXT,
    confidence INT DEFAULT NULL,
    selected TINYINT(1) NOT NULL DEFAULT 1,
    accepted_case_id BIGINT DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_ai_generation_item_generation (generation_id),
    KEY idx_ai_generation_item_case (accepted_case_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 用例质量评审
CREATE TABLE IF NOT EXISTS ai_review (
    id BIGINT NOT NULL AUTO_INCREMENT,
    case_id BIGINT NOT NULL,
    model_id BIGINT DEFAULT NULL,
    total_score INT DEFAULT NULL,
    dimension_json TEXT,
    suggestions TEXT,
    created_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_ai_review_case (case_id),
    KEY idx_ai_review_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- AI 测试报告
CREATE TABLE IF NOT EXISTS ai_report (
    id BIGINT NOT NULL AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    model_id BIGINT DEFAULT NULL,
    report_type VARCHAR(30) DEFAULT NULL,
    content_md MEDIUMTEXT,
    conclusion TEXT,
    risk_level VARCHAR(20) DEFAULT NULL,
    release_advice VARCHAR(20) DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_ai_report_plan (plan_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- AI 调用日志
CREATE TABLE IF NOT EXISTS ai_call_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    trace_id VARCHAR(64) DEFAULT NULL,
    scene VARCHAR(30) DEFAULT NULL,
    model_id BIGINT DEFAULT NULL,
    model_name VARCHAR(50) DEFAULT NULL,
    prompt_tokens INT DEFAULT 0,
    completion_tokens INT DEFAULT 0,
    total_tokens INT DEFAULT 0,
    latency_ms INT DEFAULT 0,
    status VARCHAR(20) DEFAULT NULL,
    error_code VARCHAR(50) DEFAULT NULL,
    error_msg VARCHAR(500) DEFAULT NULL,
    biz_type VARCHAR(30) DEFAULT NULL,
    biz_id BIGINT DEFAULT NULL,
    user_id BIGINT DEFAULT NULL,
    username VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_ai_call_log_created (created_at),
    KEY idx_ai_call_log_scene (scene),
    KEY idx_ai_call_log_user (username),
    KEY idx_ai_call_log_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- AI 配额
CREATE TABLE IF NOT EXISTS ai_quota (
    id BIGINT NOT NULL AUTO_INCREMENT,
    quota_month VARCHAR(7) NOT NULL,
    token_quota BIGINT NOT NULL DEFAULT 0,
    token_used BIGINT NOT NULL DEFAULT 0,
    cost_used DECIMAL(12, 4) NOT NULL DEFAULT 0,
    alert_threshold INT NOT NULL DEFAULT 80,
    updated_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_quota_month (quota_month)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 已存在库的兼容升级（首次执行成功后请勿重复执行）
-- ALTER TABLE test_cases
--     ADD COLUMN source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
--     ADD COLUMN ai_confidence INT DEFAULT NULL,
--     ADD COLUMN ai_review_score INT DEFAULT NULL;

