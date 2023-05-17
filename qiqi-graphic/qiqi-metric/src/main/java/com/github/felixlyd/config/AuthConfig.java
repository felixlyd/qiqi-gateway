package com.github.felixlyd.config;

import com.github.felixlyd.config.properties.SaTokenLoginProperties;
import com.github.felixlyd.config.properties.SaTokenProperties;
import com.github.felixlyd.filter.LoginFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * class AuthConfig: do something
 *
 * @author : liuyaodong
 * @date 2023/5/17
 */
@Configuration
public class AuthConfig {
    @Bean
    public LoginFilter loginFilter(SaTokenProperties saTokenProperties, SaTokenLoginProperties saTokenLoginProperties){
        return new LoginFilter(saTokenProperties, saTokenLoginProperties);
    }
}
