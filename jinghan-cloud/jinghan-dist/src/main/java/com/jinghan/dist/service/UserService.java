package com.jinghan.dist.service;

import com.jinghan.core.domain.user.User;

/**
 * @author Bruce
 * @date 2018/6/25
 */
public interface UserService {

    User save(String userName);

    User insert(User user);

    User findById(Integer userId);
}
