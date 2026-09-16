-- =============================================================
-- 《JAVA框架技术(一)》实验一  数据库初始化脚本
-- 数据库：ssm_emp   字符集：utf8mb4
-- 对应实验一 步骤 1.1「数据库准备」
-- 执行方式：mysql -uroot -p < sql/init.sql
-- =============================================================

CREATE DATABASE IF NOT EXISTS ssm_emp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ssm_emp;

DROP TABLE IF EXISTS emp;
CREATE TABLE emp (
    emp_id     INT PRIMARY KEY AUTO_INCREMENT COMMENT '员工编号',
    emp_name   VARCHAR(50)  NOT NULL COMMENT '姓名',
    gender     CHAR(1)      DEFAULT '男' COMMENT '性别',
    dept       VARCHAR(50)  COMMENT '部门',
    post       VARCHAR(50)  COMMENT '岗位',
    salary     DECIMAL(10,2) COMMENT '薪资',
    hire_date  DATE         COMMENT '入职时间',
    status     TINYINT      DEFAULT 1 COMMENT '状态：1在职 0离职',
    -- 实验二 任务 4.1 要求：为 emp_name 建立普通索引（提前建好，便于对比优化效果）
    KEY idx_emp_name (emp_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

INSERT INTO emp (emp_name, gender, dept, post, salary, hire_date, status) VALUES
('张伟', '男', '研发部', 'Java工程师', 12000.00, '2024-07-01', 1),
('李娜', '女', '研发部', '前端工程师', 10000.00, '2024-08-15', 1),
('王强', '男', '市场部', '市场专员', 8000.00, '2023-03-10', 1),
('赵敏', '女', '人事部', '人事专员', 7500.00, '2022-06-20', 0);
