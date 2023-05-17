package com.github.felixlyd.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * class SecureLoginProperties: do something
 *
 * @author : liuyaodong
 * @date 2023/5/17
 */
@ConfigurationProperties("qiqi-gateway.secure.login")
@Configuration
@Getter
@Setter
public class SecureLoginProperties {
    private String loginUrl;
    private String logoutUrl;
    private String indexUrl;
    private String method;
    private String username;
    private String password;
    private int loginId;
    private String usernameField;
    private String passwordField;

    private String rememberMeField;

    private int validSeconds;
}
