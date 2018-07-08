package com.jinghan.dist.service.impl;

import com.jinghan.dist.service.ShopService;
import com.jinghan.core.domain.shop.Shop;
import com.jinghan.dist.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

/**
 * 开启Spring-Cache（默认由ehcache管理，ehcache配置文件，磁盘存储），不能设置有效期
 * 当配置了SpringData-Redis，cacheManager则交给了Redis管理
 *
 * @author Bruce
 * @date 2018/6/25
 */
@Service
@EnableCaching
@CacheConfig(cacheNames = "shop") // ehcache name
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopRepository shopRepository;

    @CachePut(key = "'shop_' + #shop.id", value = "shop", condition = "#shop.shopName eq 'shopName'")
    @Override
    public Shop save(Shop shop) {
        shopRepository.save(shop);

        return shop;
    }

    @CacheEvict(key = "'shop_' + #id")
    @Override
    public void delete(Integer id) {
        shopRepository.delete(id);
    }

    @Cacheable(key = "'shop_' + #id")
    @Override
    public Shop findById(Integer id) {
        return shopRepository.findOne(id);
    }
}
