package com.jinghan.dist.controller;

import com.jinghan.core.domain.shop.Shop;
import com.jinghan.core.domain.user.User;
import com.jinghan.dist.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * @author Bruce
 * @date 2018/6/27
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户管理")
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${server.port}")
    private String port;

    /**
     * 测试spring-data-redis
     * @param userName
     * @return
     */
    @GetMapping("/save")
    public User save(@RequestParam String userName) {
        return userService.save(userName);
    }

    @PostMapping("/insert")
    @ApiOperation(value = "查询账户", httpMethod = "POST", response = User.class)
    public User insert(@RequestBody User user){
        user.setAppId(Integer.parseInt(port));
        return userService.insert(user);
    }

    @GetMapping("/findById")
    public User findById(@RequestParam(value = "userId") Integer userId){
        return userService.findById(userId);
    }

}
