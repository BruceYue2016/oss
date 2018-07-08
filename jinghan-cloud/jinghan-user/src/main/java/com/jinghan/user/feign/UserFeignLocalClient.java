package com.jinghan.user.feign;

import com.jinghan.core.domain.user.User;
import com.jinghan.user.feign.hystrix.UserHystrixFallbackFactory;
import com.jinghan.user.feign.hystrix.UserHystrixFallback;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 定义一个feign接口，通过 @FeignClient（“服务名”），来指定调用哪个服务
 * fallback,fallbackFactory 指定断路处理规则(二者同时指定时，只有fallback生效)
 *
 * 问题：UserFeignClient，UserHystrixFallback，UserHystrixFallbackFactory,位于jinghan-user同一工程中时能正常处理断路；
 *      同时位于jinghan-core(在jinghan-user中指明包路径@EnableFeignClients(basePackages = {"com.jinghan.core.client"}))中时无法访问，404
 *
 * @author Bruce
 * @date 2018/6/25
 */
@FeignClient(value = "jinghan-dist", fallback = UserHystrixFallback.class, fallbackFactory = UserHystrixFallbackFactory.class)
public interface UserFeignLocalClient {

    /**
     * value = "userName"必须指定，否则没法接收参数值
     * @param userName
     * @return
     */
    @RequestMapping(value = "/user/save", method = RequestMethod.GET)
    User save(@RequestParam(value = "userName") String userName);

    @PostMapping(value = "/user/insert")
    User insert(@RequestBody User user);

    @RequestMapping(value = "/user/findById", method = RequestMethod.GET)
    User findById(Integer userId);

}
