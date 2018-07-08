package com.jinghan.core.client;

import com.jinghan.core.domain.shop.Shop;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Bruce
 * @date 2018/6/27
 */
@FeignClient(value = "jinghan-dist")
public interface ShopFeignClient {

    @RequestMapping(value = "/shop/save", method = RequestMethod.GET)
    Shop save(@RequestParam(value = "shopName") String shopName);

    @RequestMapping(value = "/shop/findById", method = RequestMethod.GET)
    Shop findById(@RequestParam(value = "shopId") Integer shopId);
}
