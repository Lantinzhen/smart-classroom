package com.oh.oh;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.oh")
@MapperScan("com.oh.oh.mapper")
public class OhApplication {

	public static void main(String[] args) {
		SpringApplication.run(OhApplication.class, args);
	}

}
