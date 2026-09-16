package com.lab.test;

import com.lab.entity.Emp;
import com.lab.mapper.EmpMapper;
import com.lab.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * MyBatis 基础 CRUD 单元测试（实验一 步骤 2.6）。
 *
 * <p>注意：增删改必须显式 {@code session.commit()}，否则事务回滚。</p>
 *
 * @author 文一博
 */
public class EmpMapperTest {

    /** 查询全部：初始脚本插入 4 条数据，断言不少于 4 条 */
    @Test
    public void testSelectAll() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            List<Emp> list = session.getMapper(EmpMapper.class).selectAll();
            list.forEach(System.out::println);
            Assert.assertTrue("员工列表不应少于初始的 4 条", list.size() >= 4);
        }
    }

    /** 按主键查询：验证驼峰映射与 resultMap 是否生效 */
    @Test
    public void testSelectById() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            Emp emp = session.getMapper(EmpMapper.class).selectById(1);
            Assert.assertNotNull("emp_id = 1 的记录应存在", emp);
            Assert.assertEquals("张伟", emp.getEmpName());
            Assert.assertNotNull("emp_name 应映射到 empName（驼峰映射生效）", emp.getEmpName());
            Assert.assertNotNull("hire_date 应映射到 hireDate", emp.getHireDate());
            Assert.assertTrue(emp.getSalary().compareTo(new BigDecimal("12000")) == 0);
        }
    }

    /** 新增：验证 useGeneratedKeys 回填自增主键，并在断言后清理数据 */
    @Test
    public void testInsertAndBackfillKey() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);
            Emp emp = new Emp("陈晨", "女", "研发部", "测试工程师",
                    new BigDecimal("9000"), new Date(), 1);

            int rows = mapper.insert(emp);
            session.commit();

            Assert.assertEquals(1, rows);
            Assert.assertNotNull("useGeneratedKeys 应回填自增主键 empId", emp.getEmpId());
            System.out.println("回填主键 empId = " + emp.getEmpId());

            // 清理：避免影响其它测试用例的数据基线
            Assert.assertEquals(1, mapper.deleteById(emp.getEmpId()));
            session.commit();
        }
    }

    /** 修改：改完再查一次，验证修改落库，最后还原 */
    @Test
    public void testUpdate() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);

            Emp emp = mapper.selectById(1);
            BigDecimal origin = emp.getSalary();

            emp.setSalary(new BigDecimal("13000"));
            Assert.assertEquals(1, mapper.update(emp));
            session.commit();

            Assert.assertTrue("薪资应已更新为 13000",
                    mapper.selectById(1).getSalary().compareTo(new BigDecimal("13000")) == 0);

            // 还原
            emp.setSalary(origin);
            mapper.update(emp);
            session.commit();
        }
    }

    /** 删除：新增一条临时数据后删除，验证受影响行数 */
    @Test
    public void testDelete() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);

            Emp tmp = new Emp("临时员工", "男", "测试部", "临时岗",
                    new BigDecimal("1000"), new Date(), 1);
            mapper.insert(tmp);
            session.commit();
            Assert.assertNotNull(tmp.getEmpId());

            int rows = mapper.deleteById(tmp.getEmpId());
            session.commit();
            Assert.assertEquals(1, rows);
            Assert.assertNull("删除后应查询不到", mapper.selectById(tmp.getEmpId()));
        }
    }
}
