package com.jinghan.shop.controller;

import com.jinghan.core.client.ShopFeignClient;
import com.jinghan.core.domain.shop.Shop;
import com.jinghan.shop.service.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Bruce
 * @date 2018/6/25
 */
@RestController
@RequestMapping("/api/shop")
public class ShopController {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ShopFeignClient shopFeignClient;

    @Autowired
    private ShopService shopService;

    /**
     * 测试spring-cache
     * @param shopName
     * @param shopName
     * @return
     */
    @GetMapping("/save")
    public Shop save(@RequestParam(value = "shopName") String shopName) {
        Shop shop = shopFeignClient.save(shopName);

//        //shopFeignService.delete(1);
        Shop shop1 = shopFeignClient.findById(6);
        LOGGER.info("shop1:" + shop1.getShopName());

        Shop shop2 = shopFeignClient.findById(6);
        LOGGER.info("cache find shop2:" + shop2.getShopName());

        Shop shop3 = shopFeignClient.findById(6);
        LOGGER.info("cache find shop3:" + (shop3 == null ? "" : shop3.getShopName()));

        return shop;
    }

    /**
     * 测试feign
     * @param shopId
     * @return
     */
    @GetMapping("/find")
    public Shop find(@RequestParam(value = "userId") Integer userId, @RequestParam(value = "shopId") Integer shopId) {

        return shopService.findById(userId, shopId);
    }

}
