package com.jinghan.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

/**
 * 开启EurekaClient
 */
@EnableEurekaClient

/**
 * 开启feign
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.jinghan.core.client" /*,"com.jinghan.user.feign"*/})

@SpringBootApplication
public class JinghanUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(JinghanUserApplication.class, args);
	}
}
