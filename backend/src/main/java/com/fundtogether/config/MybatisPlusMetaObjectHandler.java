package com.fundtogether.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * <p>
 * 实现 {@link MetaObjectHandler} 接口，配合实体类字段上的
 * {@code @TableField(fill = FieldFill.INSERT)} 和 {@code @TableField(fill = FieldFill.UPDATE)}
 * 注解，在执行插入和更新操作时自动填充时间字段，避免在业务代码中手动设置创建时间和更新时间。
 * </p>
 * <p>
 * 填充策略使用 strictInsertFill / strictUpdateFill（严格模式），
 * 仅当实体类对应字段值为 null 时才会填充，不会覆盖已有的非空值。
 * </p>
 */
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作时自动填充
     * <p>
     * 在执行 INSERT 操作时自动填充以下字段：
     * <ul>
     *     <li>createdAt —— 创建时间，填充为当前时间</li>
     *     <li>updatedAt —— 更新时间，填充为当前时间</li>
     * </ul>
     * 新记录创建时，创建时间和更新时间均设置为当前时间。
     * </p>
     *
     * @param metaObject MyBatis 元对象，包含实体类字段信息
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now()); // 自动填充创建时间
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now()); // 自动填充更新时间
    }

    /**
     * 更新操作时自动填充
     * <p>
     * 在执行 UPDATE 操作时自动填充以下字段：
     * <ul>
     *     <li>updatedAt —— 更新时间，填充为当前时间</li>
     * </ul>
     * 每次更新记录时，自动将更新时间刷新为当前时间，无需在业务代码中手动设置。
     * </p>
     *
     * @param metaObject MyBatis 元对象，包含实体类字段信息
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now()); // 自动刷新更新时间
    }
}
