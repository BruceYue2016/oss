package com.jinghan.zuul;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Zuul是路由控制服务器，ribbon，feign实现反向代理及负载均衡
 * 以/jinghan-user/开头的请求都转发给jinghan-user服务；
 * 以/jinghan-shop/开头的请求都转发给jinghan-shop服务；
 */
@EnableDiscoveryClient
@EnableZuulProxy
@SpringBootApplication
public class RoutingZuulApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoutingZuulApplication.class, args);
	}

	/**
	 * 以下为测试ribbon专用
	 * @return
	 */
	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Autowired
	private RestTemplate restTemplate;

	public String ribbonService(String name) {
		return restTemplate.getForObject("http://localhost:8080/jinghan-user/api/user/save?userName=" + name, String.class);
	}

	@HystrixCommand(fallbackMethod = "hiError")
	public String ribbonServiceHystrix(String name) {
		return restTemplate.getForObject("http://localhost:8080/jinghan-user/api/user/save?userName=" + name, String.class);
	}

	public String hiError(String name) {
		return "hi,"+name+",sorry,error!";
	}
}
