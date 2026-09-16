# 实验一 AI 应用记录

> 对应实验指导书「实验报告要求」第 4 项与评分维度三（AI 工具规范化应用，20 分）：
> **使用 AI 工具生成的关键内容、提示词摘要、人工校验与修正过程**。
>
> 归档要求：本记录须与代码提交历史（Git commit）一一对应，
> 体现"AI 生成 → 人工逐行校验 → 修正 → 提交"的完整过程。

使用的 AI 工具：**CodeGeeX（IDEA 插件）** + 通用对话式 AI。
使用边界（遵循实验守则）：AI 仅用于生成基础配置、通用代码片段与接口模板；
复杂逻辑、排错、优化与重构由本人自主完成，**未整段复制任何 AI 生成的完整项目**。

---

## 记录 1：依赖清单生成与版本兼容性核对

| 项目 | 内容 |
|---|---|
| 提示词摘要 | "请为一个 SSM 学习项目生成 Maven 依赖清单：MyBatis 3.5.13 + MyBatis-Plus 3.5.3.1 + MySQL 8 驱动 + Log4j 1.2.17 + JUnit 4.13.2，JDK 8，并说明各依赖的版本兼容性" |
| AI 生成内容 | 给出上述依赖块，`packaging` 建议 jar，未提示任何额外依赖 |
| **人工校验** | ① 核对 MP 3.5.3.1 与 MyBatis 3.5.13 的兼容性 → MP 3.5.3.1 内部依赖 mybatis 3.5.13，版本一致无冲突，保留两个依赖；② 检查是否存在重复引入 mybatis 核心的情况 → `mvn dependency:tree` 确认无重复；③ **发现 AI 漏了一个必需依赖**（见修正） |
| **人工修正** | 运行测试时报 `NoClassDefFoundError: org/springframework/core/GenericTypeResolver`；经排查（见《问题与排查记录》问题一）确认 MP 在非 Spring 工程下仍需 `spring-core`，遂补充 `org.springframework:spring-core:5.3.31`，并在 pom 中写清原因注释 |
| 结论 | AI 给出的依赖清单"能编译"，但**漏了一个运行期隐性依赖**——依赖清单必须落实验证，不能只看编译是否通过 |

## 记录 2：实体类生成与注解语义审查

| 项目 | 内容 |
|---|---|
| 提示词摘要 | "根据 emp 表结构（emp_id, emp_name, gender, dept, post, salary, hire_date, status）生成 MyBatis-Plus 实体类，使用 @TableName/@TableId/@TableField 注解，status 字段用于在职/离职" |
| AI 生成内容 | 实体类骨架正确，但对 `status` 字段追加了 `@TableLogic`（与指导书示例一致） |
| **人工校验** | 逐行核对注解语义：`@TableLogic` = 逻辑删除标记；本项目 `status` = 在职(1)/离职(0) 业务状态。**两者语义不一致**——加上后 `deleteById` 会被 MP 改写为 `UPDATE emp SET status = 0 ...`，且所有查询会自动附加 `status = 1`，"查离职员工"将永远查不到 |
| **人工修正** | 删除 `@TableLogic`，并在字段注释中写明取舍理由；同时在实验报告问题记录中留档（问题五） |
| 结论 | 这是本次实验**最有价值的一处人工修正**：AI 代码"跑得通"≠"语义正确" |

## 记录 3：MyBatis 构建器代码生成与故障定位

| 项目 | 内容 |
|---|---|
| 提示词摘要 | "按指导书步骤 2.4 生成 SqlSessionFactory 工具类，封装成 MyBatisUtil 避免重复创建" |
| AI 生成内容 | `new SqlSessionFactoryBuilder().build(in)` + `openSession()`，实现无误 |
| **人工校验** | 进入步骤 4.3 引入 `BaseMapper` 后，该写法抛出 `BindingException: Invalid bound statement (not found): com.lab.mapper.EmpPlusMapper.selectList` |
| **人工修正** | 定位根因为「原生构建器不走 MP 的 `MybatisMapperAnnotationBuilder`，BaseMapper 方法未被注入」；改为 `new MybatisSqlSessionFactoryBuilder().build(in)`，并保留 `buildByPlainBuilder()` 作为反面示例用于复现 |
| 结论 | AI 生成的片段在其被生成的上下文里是正确的；**引入新框架后必须重新回归验证既有代码** |

## 记录 4：分页插件配置

| 项目 | 内容 |
|---|---|
| 提示词摘要 | "MyBatis-Plus 分页插件怎么在 mybatis-config.xml 里注册？" |
| AI 生成内容 | 给出 XML `<plugins>` 写法（在 `<property>` 中写 MP 内部类全限定名） |
| **人工校验** | 该写法类名冗长、易因版本差异静默失效；实测时选择改为 Java 配置以降低出错面 |
| **人工修正** | 保留 XML 方案作为参考，实际改为在 `MyBatisUtil` 中用 `MybatisPlusInterceptor#addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL))` 注册；补 `testSelectPage` 用例断言 `records.size() < total`，防止"插件没生效但测试仍通过" |
| 结论 | 对同一个问题 AI 会给出多种方案，**需结合可验证性（能否用测试兜住）来选** |

## 记录 5：单元测试用例生成与断言缺陷修正

| 项目 | 内容 |
|---|---|
| 提示词摘要 | "为 QueryWrapper 多条件查询生成 JUnit 4 测试用例，覆盖模糊查询、范围查询与排序" |
| AI 生成内容 | 用例结构完整（构造 wrapper → 查询 → 断言非空 → 断言条件命中 → 断言排序），但检索列写成了 `emp_name` |
| **人工校验** | 用例失败，日志显示 `Parameters: %工程师%(String), 1(Integer), 9000(BigDecimal)` 且 `Total: 0`——SQL 与参数均正常，**说明是条件列选错**；核对表结构后确认"工程师"在 `post` 列而非 `emp_name` 列 |
| **人工修正** | 改为 `like("post", keyword)`，并为断言补充中文说明信息；同时把该踩坑过程写入问题记录（问题四） |
| 结论 | AI 生成的"看起来合理"的测试断言可能存在语义错误；**失败的断言要追到 SQL 日志层去核实**，而不是改断言迁就结果 |

## 记录 6：Log4j 配置

| 项目 | 内容 |
|---|---|
| 提示词摘要 | "生成 log4j.properties，让控制台能看到 SQL 与参数绑定" |
| AI 生成内容 | `rootLogger=DEBUG, console` 与 `log4j.logger.com.lab.mapper=DEBUG, console` |
| **人工校验** | 运行时每条日志打印两次 |
| **人工修正** | 去掉 logger 上重复的 `console` appender（rootLogger 已挂载，logger 自动继承） |
| 结论 | 配置类文件的问题往往"不报错、只表现异常"，需要观察输出而非只看程序是否启动 |

---

## 人工审查清单（步骤 5.3）落实情况

指导书步骤 5.3 要求对 AI 生成的依赖配置、映射文件与实体类逐项审查，结论如下：

| 审查项 | 结论 |
|---|---|
| **版本统一性** | MP 3.5.3.1 与 MyBatis 3.5.13 版本一致（MP 内部即依赖该版本）；MySQL 驱动 8.0.33 与数据库 8.0.46 兼容；❌ 原清单缺 spring-core（已补） |
| **SQL 正确性** | 表名 `emp`、列名与 `sql/init.sql` 完全一致；全部使用 `#{}` 预编译占位符，❌ 未发现 `${}` 拼接（符合防注入要求） |
| **映射完整性** | `resultMap` 覆盖全部 8 个字段；`mapUnderscoreToCamelCase=true` 已开启，`emp_name → empName`、`hire_date → hireDate` 双保险 |
| **代码规范** | 统一 4 空格缩进、包名全小写、类名大驼峰；无未使用的 import；公共 SQL 抽为 `<sql id="allColumns">` 避免重复 |

## 隐私与合规自查

- ✅ 数据库账号口令**未硬编码**在 Java 代码或 XML 中，统一放在 `jdbc.properties`；
- ✅ `jdbc.properties` 已写入 `.gitignore`，仓库只提交 `jdbc.properties.example` 模板；
- ✅ 未向 AI 工具提交任何真实账号、口令或个人隐私数据；
- ✅ 全部提交历史保留，可从 commit 轨迹还原开发过程。

---

> ⚠️ **提交前请自行复核**：本记录是对本次开发过程的客观归档。
> 正式提交实验报告前，请逐条回顾以上 6 条记录与审查清单，
> 确认你已经理解每一处校验与修正的技术原因（这也是答辩时最可能被追问的点）。
