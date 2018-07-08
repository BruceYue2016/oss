package com.jinghan.dist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * 当JPA的实体类不在默认的目录结构时，需用@EntityScan指明实体类位置
 */
@EntityScan("com.jinghan.core.domain.*")
@EnableEurekaClient
@SpringBootApplication
public class JinghanDistApplication {

	public static void main(String[] args) {
		SpringApplication.run(JinghanDistApplication.class, args);
	}
}
