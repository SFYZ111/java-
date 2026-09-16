# mybatis-lab

> 《JAVA框架技术（一）》实验一 —— **AI 赋能 MyBatis 与 MyBatis-Plus 环境搭建及 CRUD 实操**
> 课程代码 1960229 ｜ 6 学时 ｜ 验证性 + 设计性 ｜ 对应第一章第 1、2、4、5 节

---

## 一、实验环境

| 项目 | 本机实际版本 |
|---|---|
| JDK | Oracle JDK **1.8.0_202**（`D:\Java\jdk1.8.0_202`，已设为 `JAVA_HOME`） |
| 构建工具 | Apache Maven **3.9.6**（`D:\apache-maven-3.9.6`，本地仓库 `D:\maven-repository`，已配阿里云镜像） |
| 数据库 | MySQL **8.0.46**（服务名 `MYSQL80`，端口 3306） |
| 持久层框架 | MyBatis **3.5.13** + MyBatis-Plus **3.5.3.1** |
| 日志 | Log4j **1.2.17** |
| 单元测试 | JUnit **4.13.2** |
| AI 编程插件 | CodeGeeX（IDEA 插件） |
| IDE | IntelliJ IDEA 2025.3.3 |

## 二、功能清单

| 任务 | 内容 | 主要文件 |
|---|---|---|
| 任务 1 | 环境搭建与项目初始化（数据库、Maven 镜像、Git、AI 插件、依赖） | `pom.xml`、`sql/init.sql` |
| 任务 2 | MyBatis 环境搭建与基础 CRUD、Log4j 日志、JUnit 单测 | `mybatis-config.xml`、`EmpMapper.xml`、`EmpMapperTest` |
| 任务 3 | 动态 SQL（`if/where/set/trim/foreach/choose`）、多条件模糊查询、批量操作 | `EmpDynamicSqlTest` |
| 任务 4 | MyBatis-Plus 通用 CRUD、`QueryWrapper`/`LambdaQueryWrapper`、分页插件 | `EmpPlusMapper`、`EmpPlusMapperTest` |
| 任务 5 | 日志排查演练、ApiFox 准备、AI 生成代码人工审查清单 | `docs/` 目录 |

## 三、目录结构

```
mybatis-lab
├── pom.xml
├── sql/init.sql                          # 建库建表与初始化数据
├── docs/
│   ├── AI使用记录.md                      # 提示词摘要 + 人工校验与修正（评分维度三）
│   ├── 问题与排查记录.md                   # ≥2 个真实问题及解决过程（评分维度二）
│   └── 人工审查清单.md                     # AI 生成代码的逐项审查结论
└── src
    ├── main
    │   ├── java/com/lab
    │   │   ├── entity/Emp.java            # 实体（含 MP 注解）
    │   │   ├── mapper/EmpMapper.java      # 手写 XML 映射的 Mapper
    │   │   ├── mapper/EmpPlusMapper.java  # 继承 BaseMapper 的 MP Mapper
    │   │   ├── util/MyBatisUtil.java      # SqlSessionFactory 单例 + 分页插件注册
    │   │   └── demo/                      # 可直接 run 的演示程序
    │   └── resources
    │       ├── mybatis-config.xml
    │       ├── log4j.properties
    │       ├── jdbc.properties.example    # 连接配置模板（真实配置已 gitignore）
    │       └── com/lab/mapper/EmpMapper.xml
    └── test/java/com/lab/test             # 三组单元测试
```

## 四、快速开始

```bash
# 1) 建库建表
mysql -uroot -p < sql/init.sql

# 2) 复制数据库配置模板并填写本机口令
cp src/main/resources/jdbc.properties.example src/main/resources/jdbc.properties

# 3) 编译 + 运行全部单元测试（控制台可见 SQL 日志）
mvn clean test

# 4) 运行演示程序
mvn -q exec:java -Dexec.mainClass=com.lab.demo.MyBatisDemo
```

> **安全说明**：`jdbc.properties` 保存数据库连接四要素，已被 `.gitignore` 排除，
> 仓库中只保留 `jdbc.properties.example` 模板。这符合指导书「严禁在代码中硬编码
> 真实账号口令」的要求（违反将导致评分维度三计 0 分）。

## 五、实验结论

- 手写 XML 映射 + `resultMap` 可精确控制列与属性的映射关系，适合复杂 SQL；
- 动态 SQL 的 `<where>` / `<set>` / `<trim>` 能自动处理多余 AND 与逗号，
  但 `<foreach>` 多参数场景必须配合 `@Param` 显式命名；
- MyBatis-Plus 的 `BaseMapper` 让单表 CRUD 零 SQL 化，`LambdaQueryWrapper`
  以方法引用代替字符串字段名，能在编译期发现字段写错；
- 分页依赖 `PaginationInnerInterceptor` 显式注册，未注册时表现为「分页失效、
  返回全量数据」——详见 `docs/问题与排查记录.md`。
