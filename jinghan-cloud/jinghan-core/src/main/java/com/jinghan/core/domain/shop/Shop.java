package com.jinghan.core.domain.shop;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Bruce
 * @date 2018/6/25
 */
@Entity
@Table(name = "t_shop")
@Data
public class Shop {

    @Id
    @GeneratedValue
    private Integer id;

    private Integer userId;

    private String unionId;

    private Integer authId;

    private String shopName;

    private String province;

    private String address;

    private String head;

    private String mobile;

    private String logo;

    private String avatar;

    private String license;

    private Integer sampleClassifyId; // 店铺套用的样板模板行业id

    private Integer classifyId; // 行业分类id

    private String classify;   // 行业分类

    private Integer carriageSetup; // 物流配送运费设置类型   0-未设置  98-通用  99-餐饮

    private Double longitude;  // 经度

    private Double latitude;   // 纬度

    private String bgImage ;   // 背景图片

    private Byte status;

    private Date publishExpire; // 店铺发布有效期

    private String label;

    private Integer redbagId ;   // 红包id

    private Integer spreadId;    // 分享记录Id

    private Byte delFlag;

    private Date createTime;

    private Date updateTime;

    public Shop() {
    }

    public Shop(String shopName) {
        this.shopName = shopName;
    }
}
