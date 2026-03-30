package com.fundtogether;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.fundtogether.mapper")
@EnableScheduling
public class FundTogetherApplication {

	public static void main(String[] args) {
		SpringApplication.run(FundTogetherApplication.class, args);
	}

}
