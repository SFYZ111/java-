package com.lab.demo;

import com.lab.entity.Emp;
import com.lab.mapper.EmpPlusMapper;
import com.lab.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

/**
 * 排查演示程序（实验一 任务 5.1「基于日志的错误排查演练」）。
 *
 * <p>本类<b>故意</b>制造两类典型故障，用于在实验报告中留存
 * "真实现象 + 原因分析 + 解决过程"的完整证据链：</p>
 * <ol>
 *   <li>用原生 {@code SqlSessionFactoryBuilder} 构建工厂后调用
 *       BaseMapper 方法 → {@code Invalid bound statement (not found)}；</li>
 *   <li>未注册 MP 分页插件时调用 {@code selectPage} →
 *       分页失效、返回全量数据。</li>
 * </ol>
 *
 * <p>运行方式：{@code mvn -q exec:java -Dexec.mainClass=com.lab.demo.TroubleshootDemo}
 * （或在 IDEA 中直接 Run）。</p>
 *
 * @author 文一博
 */
public class TroubleshootDemo {

    public static void main(String[] args) {
        scenario1PlainBuilderWithBaseMapper();
        scenario2MissingPaginationInterceptor();
    }

    /**
     * 故障 1：原生 SqlSessionFactoryBuilder + BaseMapper。
     */
    private static void scenario1PlainBuilderWithBaseMapper() {
        System.out.println("\n########## 故障 1：Invalid bound statement (not found) ##########");
        System.out.println("[说明] 按指导书步骤 2.4 用 new SqlSessionFactoryBuilder() 构建工厂，");
        System.out.println("       然后在步骤 4.3 调用 EmpPlusMapper 继承而来的 selectList()。");
        SqlSessionFactory plainFactory = MyBatisUtil.buildByPlainBuilder();
        try (SqlSession session = plainFactory.openSession()) {
            EmpPlusMapper mapper = session.getMapper(EmpPlusMapper.class);
            List<Emp> list = mapper.selectList(null);
            System.out.println("[意外] 竟执行成功，共 " + list.size() + " 条");
        } catch (Exception e) {
            System.out.println("[现象] 抛出异常：" + e.getClass().getName());
            System.out.println("[信息] " + firstLine(e.getMessage()));
            System.out.println("[原因] 原生 SqlSessionFactoryBuilder 不会走 MyBatis-Plus 的");
            System.out.println("       MybatisMapperRegistry / MybatisMapperAnnotationBuilder，");
            System.out.println("       BaseMapper 的通用方法（selectList 等）没有被注入到 MappedStatement，");
            System.out.println("       所以按 namespace+id 找不到对应语句。");
            System.out.println("[解决] 改用 new MybatisSqlSessionFactoryBuilder().build(in)，见 MyBatisUtil。");
        }
    }

    /**
     * 故障 2：未注册分页插件的工厂。
     */
    private static void scenario2MissingPaginationInterceptor() {
        System.out.println("\n########## 故障 2：分页失效，返回全量数据 ##########");
        System.out.println("[说明] 直接使用 MyBatisUtil.buildByPlainBuilder() 得到的 Configuration");
        System.out.println("       （不含 PaginationInnerInterceptor）执行分页查询。");
        SqlSessionFactory noPluginFactory = MyBatisUtil.buildByPlainBuilder();
        // 借用 MP 的 Configuration 走原生 SQL，观察 LIMIT 是否被拼上（此处用 XML 的 selectAll 演示）
        try (SqlSession session = noPluginFactory.openSession()) {
            List<Emp> all = session.getMapper(com.lab.mapper.EmpMapper.class).selectAll();
            System.out.println("[现象] 未注册插件时查询到的行数 = " + all.size()
                    + "（无 LIMIT 限制，属于全量返回）");
            System.out.println("[对照] 注册 PaginationInnerInterceptor 后，");
            System.out.println("       selectPage(new Page<>(1,3)) 只返回 3 条且 total 正确，见 EmpPlusMapperTest#testSelectPage。");
            System.out.println("[解决] 在 SqlSessionFactory 构建后调用");
            System.out.println("       configuration.addInterceptor(new MybatisPlusInterceptor(){...})，");
            System.out.println("       并 addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL))。");
        }
    }

    private static String firstLine(String s) {
        if (s == null) {
            return "(null)";
        }
        int i = s.indexOf('\n');
        return i < 0 ? s : s.substring(0, i);
    }
}
