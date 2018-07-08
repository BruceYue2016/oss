package com.jinghan.dist.repository;

import com.jinghan.core.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Bruce
 * @date 2018/6/25
 */
public interface UserRepository extends JpaRepository<User, Integer> {
}
