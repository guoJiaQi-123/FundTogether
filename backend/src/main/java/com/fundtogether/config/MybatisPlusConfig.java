package com.fundtogether.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * <p>
 * 用于配置 MyBatis-Plus 的核心插件，当前主要配置了分页插件，
 * 使 MyBatis-Plus 的分页查询功能（Page 对象）能够正常工作。
 * 后续如需添加其他插件（如乐观锁插件、防全表更新插件等），可在此类中统一管理。
 * </p>
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 创建 MyBatis-Plus 拦截器 Bean
     * <p>
     * 注册 MyBatis-Plus 核心拦截器，并添加分页插件：
     * <ul>
     * <li>{@link PaginationInnerInterceptor}：分页插件，拦截分页查询SQL并自动拼接分页语句</li>
     * <li>指定数据库类型为 MySQL，以生成对应方言的分页语句（LIMIT）</li>
     * </ul>
     * 使用该插件后，业务层可通过 {@code mapper.selectPage(page, wrapper)} 等方法实现自动分页。
     * </p>
     *
     * @return 配置了分页插件的 MyBatis-Plus 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 添加MySQL分页插件
        return interceptor;
    }
}
