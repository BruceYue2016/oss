package com.jinghan.core.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 由于hibernate一对多延迟加载，在json化时也需要忽略
 * @ManyToOne(fetch = FetchType.LAZY)
 * @JsonIgnoreProperties(ignoreUnknown = true, value = {"items"})
 *
 * @author Bruce
 * @date 2018/6/25
 */
@Entity
@Table(name = "t_user")
@Data
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler", "fieldHandler"})
public class User implements Serializable {

    @Id
    @GeneratedValue
    private Integer id;

    private Integer userId; // t_user id

    private Integer appId;  // j_app id

    private String openId;

    private String unionId;

    private String nickName;

    private Byte sex;

    private String headImgUrl;

    private String city;

    private String province;

    private String country;

    private Byte channel;

    private Date createTime;

    private Date updateTime;

    private Byte delFlag;

    private String shortUnionId; // unionId短码

    public User() {
    }

    public User(String nickName) {
        this.nickName = nickName;
    }
}
