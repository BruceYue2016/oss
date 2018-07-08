package com.jinghan.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

@EnableEurekaClient

@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.jinghan.core.client"})

@SpringBootApplication
public class JinghanShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JinghanShopApplication.class, args);
	}
}
