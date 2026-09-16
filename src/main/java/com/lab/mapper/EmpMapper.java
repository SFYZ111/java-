package com.lab.mapper;

import com.lab.entity.Emp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工持久层接口（实验一 步骤 2.3 / 任务 3）。
 *
 * <p>由 MyBatis 通过 JDK 动态代理生成实现类，与
 * {@code resources/com/lab/mapper/EmpMapper.xml} 一一对应。</p>
 *
 * @author 文一博
 */
public interface EmpMapper {

    // ---------------- 任务 2：基础 CRUD ----------------

    /** 查询全部员工 */
    List<Emp> selectAll();

    /** 按主键查询单个员工 */
    Emp selectById(Integer empId);

    /** 新增员工，并回填自增主键到 emp.empId */
    int insert(Emp emp);

    /** 按主键整行更新 */
    int update(Emp emp);

    /** 按主键删除 */
    int deleteById(Integer empId);

    // ---------------- 任务 3：动态 SQL 与批量操作 ----------------

    /** 多条件模糊查询（if / where） */
    List<Emp> selectByCondition(Emp emp);

    /** 多条件模糊查询的 trim 等价实现 */
    List<Emp> selectByConditionTrim(Emp emp);

    /** 动态更新：只更新非 null 字段（set 标签） */
    int updateDynamic(Emp emp);

    /**
     * 批量删除。
     *
     * <p>多参数场景必须用 {@link Param} 显式命名，否则 XML 中按名称取值会抛
     * {@code BindingException: Parameter 'ids' not found}。</p>
     */
    int deleteBatch(@Param("ids") List<Integer> ids);

    /** 批量插入 */
    int insertBatch(@Param("list") List<Emp> emps);

    /** 分支选择查询（choose / when / otherwise） */
    List<Emp> selectByChoose(Emp emp);
}
