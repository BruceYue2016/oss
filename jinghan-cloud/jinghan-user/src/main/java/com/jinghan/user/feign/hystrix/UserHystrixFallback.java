package com.jinghan.user.feign.hystrix;

import com.jinghan.core.domain.user.User;
import com.jinghan.user.feign.UserFeignLocalClient;
import org.springframework.stereotype.Component;

/**
 * @author Bruce
 * @date 2018/6/25
 */
@Component
public class UserHystrixFallback implements UserFeignLocalClient {

    @Override
    public User save(String userName) {
        return new User("UserHystrixServiceImpl sorry," + userName);
    }

    @Override
    public User insert(User user) {
        return new User("UserHystrixServiceImpl sorry," + user.getNickName());
    }

    @Override
    public User findById(Integer userId) {
        return new User("UserHystrixServiceImpl sorry," + userId);
    }
}
