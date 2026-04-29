package com.fundtogether;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * FundTogether应用启动类
 * <p>
 * 众筹平台后端服务的入口类，负责启动Spring Boot应用。
 * 通过注解配置了以下核心功能：
 * <ul>
 *   <li>@SpringBootApplication：启用Spring Boot自动配置和组件扫描</li>
 *   <li>@MapperScan：扫描MyBatis-Plus的Mapper接口所在包，自动注册Mapper Bean</li>
 *   <li>@EnableScheduling：启用Spring定时任务调度，支持@Scheduled注解驱动的定时任务</li>
 * </ul>
 * </p>
 */
@SpringBootApplication
@MapperScan("com.fundtogether.mapper")
@EnableScheduling
public class FundTogetherApplication {

	/**
	 * 应用程序入口方法
	 *
	 * @param args 命令行参数
	 */
	public static void main(String[] args) {
		SpringApplication.run(FundTogetherApplication.class, args);
	}

}
