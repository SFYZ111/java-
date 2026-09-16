package com.lab.demo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.entity.Emp;
import com.lab.mapper.EmpPlusMapper;
import com.lab.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

import java.math.BigDecimal;
import java.util.List;

/**
 * MyBatis-Plus 快速开发演示（实验一 任务 4）。
 *
 * <p>覆盖：BaseMapper 通用 CRUD、QueryWrapper / LambdaQueryWrapper
 * 条件构造器、分页插件。</p>
 *
 * @author 文一博
 */
public class MyBatisPlusDemo {

    public static void main(String[] args) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EmpPlusMapper mapper = session.getMapper(EmpPlusMapper.class);

            // ---------- 4.3 零 SQL 的通用 CRUD ----------
            System.out.println("========== selectList(null)：查询全部 ==========");
            mapper.selectList(null).forEach(System.out::println);

            System.out.println("========== selectById(2) ==========");
            System.out.println(mapper.selectById(2));

            // ---------- 4.4 条件构造器 ----------
            System.out.println("========== QueryWrapper：研发部 + 薪资 >= 9000，按薪资倒序 ==========");
            QueryWrapper<Emp> wrapper = new QueryWrapper<>();
            wrapper.eq("dept", "研发部")
                    .ge("salary", new BigDecimal("9000"))
                    .orderByDesc("salary");
            mapper.selectList(wrapper).forEach(System.out::println);

            System.out.println("========== LambdaQueryWrapper：姓名含“李” ==========");
            LambdaQueryWrapper<Emp> lw = new LambdaQueryWrapper<>();
            lw.like(Emp::getEmpName, "李")
                    .eq(Emp::getStatus, 1)
                    .orderByDesc(Emp::getSalary);
            mapper.selectList(lw).forEach(System.out::println);

            // ---------- 4.5 分页查询 ----------
            System.out.println("========== selectPage：第 1 页，每页 3 条 ==========");
            Page<Emp> page = new Page<>(1, 3);
            Page<Emp> result = mapper.selectPage(page, new QueryWrapper<Emp>().orderByAsc("emp_id"));
            System.out.println("总记录数 = " + result.getTotal());
            System.out.println("总页数   = " + result.getPages());
            result.getRecords().forEach(System.out::println);
        }
    }
}
