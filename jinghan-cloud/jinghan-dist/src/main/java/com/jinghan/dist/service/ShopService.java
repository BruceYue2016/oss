package com.jinghan.dist.service;

import com.jinghan.core.domain.shop.Shop;

/**
 * @author Bruce
 * @date 2018/6/27
 */
public interface ShopService {

    Shop save(Shop shop);

    void delete(Integer id);

    Shop findById(Integer id);

}
