package com.jinghan.core.client;

import com.jinghan.core.domain.user.User;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 定义一个feign接口，通过 @FeignClient（“服务名”），来指定调用哪个服务
 *
 * @author Bruce
 * @date 2018/6/25
 */
@FeignClient(value = "jinghan-dist")
public interface UserFeignClient {

    /**
     * value = "userName"必须指定，否则没法接收参数值,报错：Request method 'POST' not supported
     * @param userName
     * @return
     */
    @RequestMapping(value = "/user/save", method = RequestMethod.GET)
    User save(@RequestParam(value = "userName") String userName);

    @PostMapping(value = "/user/insert")
    User insert(@RequestBody User user);

    @RequestMapping(value = "/user/findById", method = RequestMethod.GET)
    User findById(@RequestParam(value = "userId") Integer userId);

}
