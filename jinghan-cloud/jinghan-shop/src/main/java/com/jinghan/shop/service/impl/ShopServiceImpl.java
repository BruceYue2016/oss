package com.jinghan.shop.service.impl;

import com.jinghan.core.client.ShopFeignClient;
import com.jinghan.core.client.UserFeignClient;
import com.jinghan.core.domain.shop.Shop;
import com.jinghan.core.domain.user.User;
import com.jinghan.shop.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Bruce
 * @date 2018/6/27
 */
@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private ShopFeignClient shopFeignClient;

    @Override
    public Shop findById(Integer userId, Integer shopId) {

        // 如果userFeignClient的方法找不到，注意检查jinghan-dist中的controller中的restful请求方法是否创建
        User user = userFeignClient.findById(userId);

        Shop shop = shopFeignClient.findById(shopId);

        shop.setUserId(user.getUserId());

        return shop;
    }
}
