package com.github.felixlyd.config;

import com.github.felixlyd.config.properties.SaTokenLoginProperties;
import com.github.felixlyd.config.properties.SaTokenProperties;
import com.github.felixlyd.filter.LoginFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * class SaTokenSecureConfig: do something
 *
 * @author : liuyaodong
 * @date 2023/5/17
 */
@Configuration
public class SaTokenSecureConfig {

    /**
     * 方案1：sa-token
     * 引入spring security后，该代码需要注释
     * @param saTokenProperties saToken配置
     * @param saTokenLoginProperties saToken登录配置
     * @return 登录过滤器
     */
    @Bean
    public LoginFilter loginFilter(SaTokenProperties saTokenProperties, SaTokenLoginProperties saTokenLoginProperties){
        return new LoginFilter(saTokenProperties, saTokenLoginProperties);
    }
}
