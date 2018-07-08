package com.jinghan.dist.controller;

import com.jinghan.core.domain.shop.Shop;
import com.jinghan.dist.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Bruce
 * @date 2018/6/27
 */
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    /**
     * 测试spring-cache
     * @param shopName
     * @return
     */
    @GetMapping("/save")
    public Shop save(@RequestParam(value = "shopName") String shopName) {

        Shop shop = new Shop();
        shop.setShopName(shopName);

        return shopService.save(shop);
    }

    @GetMapping("/findById")
    public Shop findById(@RequestParam(value = "shopId") Integer shopId){
        return shopService.findById(shopId);
    }
}
