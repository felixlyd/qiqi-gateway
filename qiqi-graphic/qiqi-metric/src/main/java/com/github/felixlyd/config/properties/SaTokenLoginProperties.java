package com.github.felixlyd.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * class SaTokenLoginProperties: do something
 *
 * @author : liuyaodong
 * @date 2023/5/17
 */
@ConfigurationProperties("qiqi-gateway.sa-token.login")
@Configuration
@Getter
@Setter
public class SaTokenLoginProperties {
    private String loginUrl;
    private String logoutUrl;
    private String indexUrl;
    private String method;
    private String username;
    private String password;
    private String loginId;
    private String usernameField;
    private String passwordField;
}
