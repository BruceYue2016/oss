package com.jinghan.shop.service;

import com.jinghan.core.domain.shop.Shop;

/**
 * @author Bruce
 * @date 2018/6/27
 */
public interface ShopService {

    Shop findById(Integer userId, Integer shopId);

}
