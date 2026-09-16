package com.lab.test;

import com.lab.entity.Emp;
import com.lab.mapper.EmpMapper;
import com.lab.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 动态 SQL、多条件查询与批量操作单元测试（实验一 任务 3）。
 *
 * @author 文一博
 */
public class EmpDynamicSqlTest {

    /** 步骤 3.1：多条件模糊查询（if / where） */
    @Test
    public void testSelectByCondition() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);

            // 条件 1：只给姓名（模糊）
            Emp q1 = new Emp();
            q1.setEmpName("张");
            List<Emp> r1 = mapper.selectByCondition(q1);
            Assert.assertFalse("姓名含“张”的记录应至少 1 条", r1.isEmpty());
            Assert.assertTrue(r1.stream().allMatch(e -> e.getEmpName().contains("张")));

            // 条件 2：只给部门
            Emp q2 = new Emp();
            q2.setDept("研发部");
            List<Emp> r2 = mapper.selectByCondition(q2);
            Assert.assertFalse(r2.isEmpty());
            Assert.assertTrue(r2.stream().allMatch(e -> "研发部".equals(e.getDept())));

            // 条件 3：部门 + 在职
            Emp q3 = new Emp();
            q3.setDept("研发部");
            q3.setStatus(1);
            List<Emp> r3 = mapper.selectByCondition(q3);
            Assert.assertTrue(r3.stream().allMatch(e -> Integer.valueOf(1).equals(e.getStatus())));

            // 条件 4：全空 -> 等价于查全部（证明 <where> 未拼出多余 AND）
            List<Emp> r4 = mapper.selectByCondition(new Emp());
            Assert.assertTrue(r4.size() >= 4);
        }
    }

    /** 步骤 3.1：<trim> 与 <where> 等效性验证 */
    @Test
    public void testSelectByConditionTrimEquivalentToWhere() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);

            Emp q = new Emp();
            q.setEmpName("李");
            q.setDept("研发部");
            q.setStatus(1);

            List<Emp> where = mapper.selectByCondition(q);
            List<Emp> trim = mapper.selectByConditionTrim(q);
            Assert.assertEquals("trim 实现结果应与 where 完全一致", where.size(), trim.size());
        }
    }

    /** 步骤 3.2：动态更新，未传的字段不应被覆盖 */
    @Test
    public void testUpdateDynamic() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);

            Emp before = mapper.selectById(1);
            String originPost = before.getPost();

            Emp patch = new Emp();
            patch.setEmpId(1);
            patch.setPost("高级Java工程师");
            Assert.assertEquals(1, mapper.updateDynamic(patch));
            session.commit();

            Emp after = mapper.selectById(1);
            Assert.assertEquals("岗位应被更新", "高级Java工程师", after.getPost());
            Assert.assertEquals("未传的姓名不应被覆盖", before.getEmpName(), after.getEmpName());

            // 还原
            patch.setPost(originPost);
            mapper.updateDynamic(patch);
            session.commit();
        }
    }

    /** 步骤 3.3：批量插入 + 批量删除（foreach） */
    @Test
    public void testInsertBatchAndDeleteBatch() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);

            List<Emp> batch = new ArrayList<>();
            batch.add(new Emp("批量甲", "男", "测试部", "工程师", new BigDecimal("6000"), new Date(), 1));
            batch.add(new Emp("批量乙", "女", "测试部", "工程师", new BigDecimal("6500"), new Date(), 1));

            Assert.assertEquals(2, mapper.insertBatch(batch));
            session.commit();

            List<Integer> ids = new ArrayList<>();
            for (Emp e : mapper.selectByCondition(new Emp())) {
                if (e.getEmpName().startsWith("批量")) {
                    ids.add(e.getEmpId());
                }
            }
            Assert.assertEquals("应能查到刚批量插入的 2 条", 2, ids.size());

            Assert.assertEquals(2, mapper.deleteBatch(ids));
            session.commit();

            Assert.assertTrue("批量删除后应查不到",
                    mapper.selectByCondition(new Emp()).stream()
                            .noneMatch(e -> e.getEmpName().startsWith("批量")));
        }
    }

    /** 步骤 3.3 反例：多参数未加 @Param 会抛 BindingException（此处验证正确用法） */
    @Test
    public void testDeleteBatchWithParamAnnotation() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);

            Emp tmp = new Emp("待删丙", "男", "测试部", "临时岗", new BigDecimal("3000"), new Date(), 1);
            mapper.insert(tmp);
            session.commit();

            int rows = mapper.deleteBatch(Arrays.asList(tmp.getEmpId()));
            session.commit();
            Assert.assertEquals(1, rows);
        }
    }

    /** 步骤 3.4：choose / when / otherwise 分支选择 */
    @Test
    public void testSelectByChoose() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);

            // 命中第一个 when：姓名精确
            Emp q1 = new Emp();
            q1.setEmpName("张伟");
            q1.setDept("研发部");
            List<Emp> r1 = mapper.selectByChoose(q1);
            Assert.assertEquals("应只按姓名精确命中 1 条", 1, r1.size());
            Assert.assertEquals("张伟", r1.get(0).getEmpName());

            // 未命中第一个 when，命中第二个 when：按部门
            Emp q2 = new Emp();
            q2.setDept("研发部");
            List<Emp> r2 = mapper.selectByChoose(q2);
            Assert.assertFalse(r2.isEmpty());
            Assert.assertTrue(r2.stream().allMatch(e -> "研发部".equals(e.getDept())));

            // 全部未命中，走 otherwise：在职员工
            List<Emp> r3 = mapper.selectByChoose(new Emp());
            Assert.assertTrue(r3.stream().allMatch(e -> Integer.valueOf(1).equals(e.getStatus())));
        }
    }
}
