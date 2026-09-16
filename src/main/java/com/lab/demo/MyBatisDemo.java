package com.lab.demo;

import com.lab.entity.Emp;
import com.lab.mapper.EmpMapper;
import com.lab.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * 第一个 MyBatis 程序（实验一 步骤 2.4）。
 *
 * <p>执行链路：读取配置 → 构建 SqlSessionFactory → 开启 SqlSession →
 * 获取 Mapper 代理 → 执行 SQL → 结果映射 → 返回 Java 对象。</p>
 *
 * @author 文一博
 */
public class MyBatisDemo {

    public static void main(String[] args) {
        // 1. 拿到全局唯一的 SqlSessionFactory（由 MyBatisUtil 负责构建）
        // 2. 开启 SqlSession（用完必须关闭）
        try (SqlSession session = MyBatisUtil.openSession()) {
            // 3. 获取 Mapper 动态代理对象
            EmpMapper mapper = session.getMapper(EmpMapper.class);

            System.out.println("========== 1) selectAll ==========");
            List<Emp> list = mapper.selectAll();
            list.forEach(System.out::println);

            System.out.println("========== 2) selectById(1) ==========");
            System.out.println(mapper.selectById(1));
        }
    }
}
