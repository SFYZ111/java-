package com.lab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.entity.Emp;

/**
 * MyBatis-Plus 通用 Mapper（实验一 步骤 4.3）。
 *
 * <p>继承 {@link BaseMapper} 后即免费获得
 * {@code insert / deleteById / deleteBatchIds / updateById /
 * selectById / selectList / selectPage / selectCount} 等方法，
 * 单表 CRUD 零 SQL。</p>
 *
 * <p>与手写 XML 的 {@link EmpMapper} 共存：两者互不影响，
 * 复杂 SQL 仍可继续用 XML 实现。</p>
 *
 * @author 文一博
 */
public interface EmpPlusMapper extends BaseMapper<Emp> {
    // 无需声明任何方法即可拥有通用 CRUD 能力
}
