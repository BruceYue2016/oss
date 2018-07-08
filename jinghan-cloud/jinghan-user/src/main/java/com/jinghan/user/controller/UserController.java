package com.jinghan.user.controller;

import com.jinghan.core.client.UserFeignClient;
import com.jinghan.core.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;

/**
 * @author Bruce
 * @date 2018/6/25
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserFeignClient userFeignClient;

    /**
     * 测试spring-data-redis
     * @param userName
     * @return
     */
    @GetMapping("/save")
    public User save(@RequestParam String userName) {

        return userFeignClient.save(userName);
    }

    @GetMapping("/insert")
    public User insert(@RequestParam String userName) {
        Date now = new Date();
        User user = new User();
        user.setAppId(13);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setDelFlag((byte)0);
        user.setUnionId(UUID.randomUUID().toString());
        user.setOpenId(user.getUnionId());
        user.setShortUnionId("");
        user.setHeadImgUrl("");
        user.setNickName("用户昵称:".concat(userName));
        user.setSex((byte)1);

        return userFeignClient.insert(user);
    }

}
