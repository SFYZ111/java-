package com.lab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 员工实体类（实验一 步骤 2.2 / 步骤 4.2）。
 *
 * <p>说明：实体同时服务于 MyBatis 的 XML 映射（任务 2、3）与
 * MyBatis-Plus 的注解映射（任务 4），因此同时使用 {@code resultMap}
 * 与之对应的 MP 表注解。</p>
 *
 * @author 文一博
 */
@TableName("emp")
public class Emp implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 员工编号（自增主键） */
    @TableId(value = "emp_id", type = IdType.AUTO)
    private Integer empId;

    /** 姓名 */
    @TableField("emp_name")
    private String empName;

    /** 性别 */
    private String gender;

    /** 部门 */
    private String dept;

    /** 岗位 */
    private String post;

    /** 薪资 */
    private BigDecimal salary;

    /** 入职时间 */
    @TableField("hire_date")
    private Date hireDate;

    /**
     * 状态：1 在职 / 0 离职。
     *
     * <p><b>人工校验记录：</b>指导书步骤 4.2 的示例写法在本字段上加了
     * {@code @TableLogic}。经人工核对，{@code @TableLogic} 的语义是
     * <i>逻辑删除标记</i>，加上后 MP 的 {@code deleteById} 会被改写为
     * {@code UPDATE emp SET status = 0 ...}，而本实验中 status 表示
     * "在职/离职"业务状态，与"已删除"不是同一语义，两者混用会导致
     * 删除接口行为与实验要求不符。故此处<b>不采用</b> {@code @TableLogic}，
     * 如需演示逻辑删除，应另建一个独立字段（如 {@code deleted}）。</p>
     */
    private Integer status;

    public Emp() {
    }

    public Emp(String empName, String gender, String dept, String post,
               BigDecimal salary, Date hireDate, Integer status) {
        this.empName = empName;
        this.gender = gender;
        this.dept = dept;
        this.post = post;
        this.salary = salary;
        this.hireDate = hireDate;
        this.status = status;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Emp{" +
                "empId=" + empId +
                ", empName='" + empName + '\'' +
                ", gender='" + gender + '\'' +
                ", dept='" + dept + '\'' +
                ", post='" + post + '\'' +
                ", salary=" + salary +
                ", hireDate=" + hireDate +
                ", status=" + status +
                '}';
    }
}
