package com.jinghan.user.feign.hystrix;

import com.jinghan.core.domain.user.User;
import com.jinghan.user.feign.UserFeignLocalClient;
import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Bruce
 * @date 2018/6/28
 */
@Component
public class UserHystrixFallbackFactory implements FallbackFactory<UserFeignLocalClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserHystrixFallbackFactory.class);

    @Override
    public UserFeignLocalClient create(Throwable e) {

        LOGGER.info("fallback; reason was: {}", e.getMessage());

        return new UserFeignLocalClient(){

            @Override
            public User save(String userName) {
                return new User("UserHystrixFallbackFactory sorry," + userName);
            }

            @Override
            public User insert(User user) {
                return new User("UserHystrixFallbackFactory sorry," + user.getNickName());
            }

            @Override
            public User findById(Integer userId) {
                return new User("UserHystrixFallbackFactory sorry," + userId);
            }
        };
    }

}