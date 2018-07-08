package com.jinghan.dist.service.impl;

import com.jinghan.core.domain.user.User;
import com.jinghan.dist.repository.UserRepository;
import com.jinghan.dist.service.RedisService;
import com.jinghan.dist.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * @author Bruce
 * @date 2018/6/26
 */
@Service
public class UserServiceImpl implements UserService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisService<Integer, User> redisService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public User save(String userName) {
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

        userRepository.save(user);

        User user1 = this.findById(10);
        LOGGER.info("user1:" + user1.getNickName());

        User user2 = this.findById(10);
        LOGGER.info("cache find user2:" + user2.getNickName());

        User user3 = this.findById(10);
        LOGGER.info("cache find user3:" + (user3 == null ? "" : user3.getNickName()));

        return user;
    }

    @Override
    public User insert(User user) {
        userRepository.save(user);

        return user;
    }

    @Override
    public User findById(Integer userId) {
        User user = redisService.getByKey(RedisServiceImpl.PREFIX_USER, userId);
        if(user != null){
            return user;
        }

        // 从数据库取，存回缓存，并设置缓存时间
        user = userRepository.getOne(userId);
        if(user != null){
            redisService.setByKey(RedisServiceImpl.PREFIX_USER, userId, user);
        }

        return user;
    }
}
