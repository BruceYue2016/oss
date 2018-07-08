package com.jinghan.dist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Bruce
 * @date 2018/6/27
 */
@Configuration
@EnableSwagger2
@ComponentScan(basePackages = {"com.jinghan.dist.controller"})
public class SwaggerConfig {

    @Bean
    public Docket buildDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(buildApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.jinghan.dist.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo buildApiInfo() {
        return new ApiInfoBuilder()
                .title("Jinghan-Cloud API")
                .termsOfServiceUrl("http://localhost:8000/swagger-ui.html")
                .description("晶汉微服务")
                .version("1.0")
                .build();
    }

}
