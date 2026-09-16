package com.lab.util;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * SqlSessionFactory 工具类（实验一 步骤 2.4 的工程化封装）。
 *
 * <p><b>设计要点（人工校验记录）：</b></p>
 * <ol>
 *   <li>指导书步骤 2.4 用 {@code new SqlSessionFactoryBuilder()} 构建工厂，
 *       这在<b>纯 MyBatis</b> 阶段完全正确；但进入步骤 4.3 后
 *       {@code EmpPlusMapper extends BaseMapper<Emp>} 依赖 MyBatis-Plus
 *       在构建期向 Mapper 注入通用方法，必须改用 MP 提供的
 *       {@link MybatisSqlSessionFactoryBuilder}，否则调用
 *       {@code empPlusMapper.selectList(...)} 会抛
 *       {@code BindingException: Invalid bound statement (not found)}。
 *       本类统一采用 MP 的构建器，可同时兼容手写 XML 与 BaseMapper。</li>
 *   <li>分页插件必须显式注册，否则 {@code selectPage} 不会拼接
 *       {@code LIMIT}，表现为「分页不生效、返回全量数据」。</li>
 * </ol>
 *
 * @author 文一博
 */
public final class MyBatisUtil {

    private static final String CONFIG_LOCATION = "mybatis-config.xml";

    /** 全局唯一的会话工厂（SqlSessionFactory 生命周期 = 应用生命周期） */
    private static final SqlSessionFactory FACTORY = buildFactory();

    private MyBatisUtil() {
        // 工具类禁止实例化
    }

    /**
     * 构建全局唯一的 SqlSessionFactory，并注册 MyBatis-Plus 分页插件。
     */
    private static SqlSessionFactory buildFactory() {
        try (InputStream in = Resources.getResourceAsStream(CONFIG_LOCATION)) {
            // ① 用 MP 的构建器代替原生 SqlSessionFactoryBuilder
            SqlSessionFactory factory = new MybatisSqlSessionFactoryBuilder().build(in);

            // ② 注册 MP 分页插件（等价于指导书步骤 4.5 的 <plugins> 配置，
            //    改写为 Java 配置，避免 XML 内写 MP 内部类全限定名易出错）
            MybatisPlusInterceptor mpInterceptor = new MybatisPlusInterceptor();
            mpInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
            factory.getConfiguration().addInterceptor(mpInterceptor);

            return factory;
        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "加载 MyBatis 全局配置文件失败：" + CONFIG_LOCATION + "，原因：" + e.getMessage());
        }
    }

    /** 获取全局唯一的 SqlSessionFactory */
    public static SqlSessionFactory getSqlSessionFactory() {
        return FACTORY;
    }

    /**
     * 开启一个 SqlSession。<b>必须使用 try-with-resources 或手动 close</b>，
     * 因为 SqlSession 不是线程安全的。
     */
    public static SqlSession openSession() {
        return FACTORY.openSession();
    }

    /** 开启一个自动提交的 SqlSession（便于演示与临时排查，生产代码不推荐） */
    public static SqlSession openSession(boolean autoCommit) {
        return FACTORY.openSession(autoCommit);
    }

    /**
     * 「反面示例」：使用原生 {@link SqlSessionFactoryBuilder} 构建工厂。
     *
     * <p>仅用于实验报告「问题与排查」章节复现
     * {@code Invalid bound statement (not found)} 现象，
     * 业务代码请勿调用。</p>
     */
    public static SqlSessionFactory buildByPlainBuilder() {
        try (InputStream in = Resources.getResourceAsStream(CONFIG_LOCATION)) {
            return new SqlSessionFactoryBuilder().build(in);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
