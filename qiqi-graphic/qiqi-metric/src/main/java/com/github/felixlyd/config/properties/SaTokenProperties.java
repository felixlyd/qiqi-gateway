package com.github.felixlyd.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * class SaTokenProperties: do something
 *
 * @author : liuyaodong
 * @date 2023/5/17
 */
@ConfigurationProperties("qiqi-gateway.sa-token")
@Configuration
@Getter
@Setter
public class SaTokenProperties {

    /**
     * 拦截路由
     */
    private List<String> includeList;

    /**
     * 放行路由
     */
    private List<String> excludeList;

    private String originUrlSession;

}
