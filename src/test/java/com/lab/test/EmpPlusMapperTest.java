package com.lab.test;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.entity.Emp;
import com.lab.mapper.EmpPlusMapper;
import com.lab.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * MyBatis-Plus 通用 CRUD、条件构造器与分页单元测试（实验一 任务 4）。
 *
 * @author 文一博
 */
public class EmpPlusMapperTest {

    /** 步骤 4.3：BaseMapper 零 SQL 通用 CRUD */
    @Test
    public void testBaseMapperCrud() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EmpPlusMapper mapper = session.getMapper(EmpPlusMapper.class);

            // selectList
            List<Emp> all = mapper.selectList(null);
            Assert.assertTrue(all.size() >= 4);

            // selectById
            Emp one = mapper.selectById(2);
            Assert.assertNotNull(one);
            Assert.assertEquals("李娜", one.getEmpName());

            // insert
            Emp emp = new Emp("MP新增", "男", "研发部", "后端工程师",
                    new BigDecimal("11000"), new Date(), 1);
            Assert.assertEquals(1, mapper.insert(emp));
            session.commit();
            Assert.assertNotNull("MP 应回填自增主键", emp.getEmpId());

            // updateById
            emp.setSalary(new BigDecimal("11500"));
            Assert.assertEquals(1, mapper.updateById(emp));
            session.commit();
            Assert.assertTrue(mapper.selectById(emp.getEmpId())
                    .getSalary().compareTo(new BigDecimal("11500")) == 0);

            // deleteById
            Assert.assertEquals(1, mapper.deleteById(emp.getEmpId()));
            session.commit();
            Assert.assertNull(mapper.selectById(emp.getEmpId()));
        }
    }

    /** 步骤 4.4：QueryWrapper 多条件 + 范围 + 排序 */
    @Test
    public void testQueryWrapper() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EmpPlusMapper mapper = session.getMapper(EmpPlusMapper.class);

            // 注意：岗位关键词存在于 post 列（如 "Java工程师"、"前端工程师"），
            //      而不是 emp_name 列（姓名只有"张伟/李娜"）——
            //      人工校验时踩过一次坑：条件列写错不会报错，只会静默返回空结果。
            String keyword = "工程师";
            QueryWrapper<Emp> wrapper = new QueryWrapper<>();
            wrapper.like(keyword != null && !keyword.isEmpty(), "post", keyword)
                    .eq("status", 1)
                    .ge("salary", new BigDecimal("9000"))
                    .orderByDesc("salary");

            List<Emp> list = mapper.selectList(wrapper);
            Assert.assertFalse("岗位含“工程师”且在职、薪资>=9000 的记录不应为空", list.isEmpty());
            Assert.assertTrue(list.stream().allMatch(e -> e.getPost().contains(keyword)));
            Assert.assertTrue(list.stream().allMatch(e -> e.getSalary().compareTo(new BigDecimal("9000")) >= 0));

            // 排序生效校验
            for (int i = 1; i < list.size(); i++) {
                Assert.assertTrue("应按薪资倒序",
                        list.get(i - 1).getSalary().compareTo(list.get(i).getSalary()) >= 0);
            }
        }
    }

    /** 步骤 4.4：LambdaQueryWrapper（推荐，防字段名写错） */
    @Test
    public void testLambdaQueryWrapper() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EmpPlusMapper mapper = session.getMapper(EmpPlusMapper.class);

            LambdaQueryWrapper<Emp> lw = new LambdaQueryWrapper<>();
            lw.like(Emp::getEmpName, "李")
                    .eq(Emp::getStatus, 1)
                    .orderByDesc(Emp::getSalary);

            List<Emp> list = mapper.selectList(lw);
            Assert.assertFalse(list.isEmpty());
            Assert.assertTrue(list.stream().allMatch(e -> e.getEmpName().contains("李")));
        }
    }

    /** 步骤 4.5：分页插件生效校验 */
    @Test
    public void testSelectPage() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EmpPlusMapper mapper = session.getMapper(EmpPlusMapper.class);

            Page<Emp> page = new Page<>(1, 3);
            Page<Emp> result = mapper.selectPage(page, new QueryWrapper<Emp>().orderByAsc("emp_id"));

            System.out.println("总记录数 = " + result.getTotal());
            System.out.println("总页数   = " + result.getPages());

            Assert.assertTrue("总记录数应不少于 4", result.getTotal() >= 4);
            Assert.assertEquals("第 1 页每页 3 条，records 应为 3 条", 3, result.getRecords().size());
            Assert.assertTrue("分页插件未生效（返回了全量数据）", result.getRecords().size() < result.getTotal());
        }
    }

    /** 步骤 4.5：分页边界 —— 超出尾页应返回空列表但 total 正确 */
    @Test
    public void testSelectPageOutOfRange() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EmpPlusMapper mapper = session.getMapper(EmpPlusMapper.class);

            Page<Emp> result = mapper.selectPage(new Page<>(999, 10), null);
            Assert.assertTrue(result.getTotal() >= 4);
            Assert.assertTrue("超尾页应返回空列表", result.getRecords().isEmpty());
        }
    }

    /** selectCount 统计 */
    @Test
    public void testSelectCount() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EmpPlusMapper mapper = session.getMapper(EmpPlusMapper.class);
            Long total = mapper.selectCount(null);
            Assert.assertNotNull(total);
            Assert.assertTrue(total >= 4);
            System.out.println("员工总数 = " + total);
        }
    }
}
