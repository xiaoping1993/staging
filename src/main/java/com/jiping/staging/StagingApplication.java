package com.jiping.staging;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackages = {"com.jiping.staging"})
@MapperScan("com.jiping.staging.dal")
@EnableAutoConfiguration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class StagingApplication {
	public static void main(String[] args) {
		SpringApplication.run(StagingApplication.class, args);
	}

}
