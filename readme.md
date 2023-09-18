- [ ] 网关：spring-cloud-gateway
- [ ] 安全性：防重放、数据签名、验签
- [ ] 授权：sa-token
- [ ] 限流：sentinel
- [x] 监控：spring-boot-admin
# 柒柒监控
## 监控实现：spring-boot-admin
[GitHub - codecentric/spring-boot-admin: Admin UI for administration of spring boot applications](https://github.com/codecentric/spring-boot-admin)
## 监控安全：sa-token或spring-security
### 公共配置类
`SecureLoginProperties`类
```java
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
    private String loginId;
    private String usernameField;
    private String passwordField;
}

```

`SecureProperties`类
```java
package com.github.felixlyd.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * class SecureProperties: do something
 *
 * @author : liuyaodong
 * @date 2023/5/17
 */
@ConfigurationProperties("qiqi-gateway.secure")
@Configuration
@Getter
@Setter
public class SecureProperties {

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

```
### sa-token``
sa-token通过参考sa-token全局过滤器实现[Sa-Token 全局过滤器](https://sa-token.cc/doc.html#/up/global-filter)

1. pom文件
```xml
<!--  方案1：引入sa token-->
<dependency>
  <groupId>cn.dev33</groupId>
  <artifactId>sa-token-spring-boot-starter</artifactId>
</dependency>
```

2. filter类和config配置

`LoginFilter`类
```java
package com.github.felixlyd.filter;


import cn.dev33.satoken.exception.BackResultException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.filter.SaFilterErrorStrategy;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaSessionCustomUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.dev33.satoken.util.SaTokenConsts;
import cn.hutool.core.util.StrUtil;
import com.github.felixlyd.config.properties.SecureLoginProperties;
import com.github.felixlyd.config.properties.SecureProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * class LoginFilter: do something
 *
 * @author : liuyaodong
 * @date 2023/5/16
 */

@Slf4j
@Order(SaTokenConsts.ASSEMBLY_ORDER)
public class LoginFilter implements Filter {

    private final SecureProperties secureProperties;
    private final SecureLoginProperties secureLoginProperties;


    /**
     * 异常处理函数：每次[认证函数]发生异常时执行此函数
     */
    public SaFilterErrorStrategy error = e -> SaResult.error(e.getMessage());

    public LoginFilter(SecureProperties secureProperties, SecureLoginProperties secureLoginProperties) {
        this.secureProperties = secureProperties;
        this.secureLoginProperties = secureLoginProperties;
    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;

            // 拦截登录逻辑
            if (StrUtil.equals(request.getMethod(), secureLoginProperties.getMethod())
                    && StrUtil.equals(request.getRequestURI(), secureLoginProperties.getLoginUrl())) {
                String username = request.getParameter(secureLoginProperties.getUsernameField());
                String password = request.getParameter(secureLoginProperties.getPasswordField());
                if (StrUtil.equals(username, secureLoginProperties.getUsername())
                        && StrUtil.equals(password, secureLoginProperties.getPassword())) {
                    StpUtil.login(secureLoginProperties.getLoginId());
                    SaSession saSession = SaSessionCustomUtil.getSessionById(secureProperties.getOriginUrlSession());
                    String originUrl = String.valueOf(saSession.get(secureProperties.getOriginUrlSession()));
                    response.sendRedirect(StrUtil.equals(originUrl, secureLoginProperties.getLoginUrl())? secureLoginProperties.getIndexUrl():originUrl);
                    return;
                } else {
                    throw new NotLoginException("用户名密码不正确！", StpUtil.getLoginType(), "-2");
                }
            }

            // 拦截注销逻辑
            if(StrUtil.equals(request.getRequestURI(), secureLoginProperties.getLogoutUrl())){
                StpUtil.logout(secureLoginProperties.getLoginId());
                response.sendRedirect(secureLoginProperties.getLoginUrl());
                return;
            }

            // 全局拦截，校验是否登录
            SaRouter.match(this.secureProperties.getIncludeList())
                    .notMatch(this.secureProperties.getExcludeList())
                    .check(r -> {
                        StpUtil.checkLogin();
                    });

            filterChain.doFilter(servletRequest, servletResponse);
        } catch (NotLoginException e) {
            // 未登录逻辑
            log.info(e.getMessage());
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            SaSession saSession = SaSessionCustomUtil.getSessionById(secureProperties.getOriginUrlSession());
            saSession.set(secureProperties.getOriginUrlSession(), httpServletRequest.getRequestURI());
            httpServletResponse.sendRedirect(secureLoginProperties.getLoginUrl());
        } catch (Throwable e) {
            // 1. 获取异常处理策略结果
            String result = (e instanceof BackResultException) ? e.getMessage() : String.valueOf(error.run(e));

            // 2. 写入输出流
            if (servletResponse.getContentType() == null) {
                servletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            }
            servletResponse.getWriter().print(result);
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}

```
`SaTokenSecureConfig`类
```java
package com.github.felixlyd.config;

import com.github.felixlyd.config.properties.SecureLoginProperties;
import com.github.felixlyd.config.properties.SecureProperties;
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
     * @param secureProperties saToken配置
     * @param secureLoginProperties saToken登录配置
     * @return 登录过滤器
     */
    @Bean
    public LoginFilter loginFilter(SecureProperties secureProperties, SecureLoginProperties secureLoginProperties){
        return new LoginFilter(secureProperties, secureLoginProperties);
    }
}

```

3. 配置文件
```yaml
# spring配置
spring:
  boot:
    admin:
      ui:
        external-views:
          - label: "注销"
            url: /logout
            order: 2000
```
### spring-security
spring-security参考spring-boot-admin官方文档实现[Spring Boot Admin Reference Guide](http://docs.spring-boot-admin.com/2.7.10/)

1. pom文件
```xml
<!--  方案2：引入spring security-->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

2. config配置

`SecuritySecureConfig`类
```java
package com.github.felixlyd.config;

import cn.hutool.core.lang.UUID;
import com.github.felixlyd.config.properties.SecureLoginProperties;
import com.github.felixlyd.config.properties.SecureProperties;
import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
* class SecuritySecureConfig: do something
*
* @author : liuyaodong
* @date 2023/5/17
*/
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecuritySecureConfig extends WebSecurityConfigurerAdapter {
   private final AdminServerProperties adminServer;
   private final SecureProperties secureProperties;
   private final SecureLoginProperties secureLoginProperties;

   public SecuritySecureConfig(AdminServerProperties adminServer, SecureProperties secureProperties, SecureLoginProperties secureLoginProperties) {
       this.adminServer = adminServer;
       this.secureProperties = secureProperties;
       this.secureLoginProperties = secureLoginProperties;
   }

   @Override
   protected void configure(HttpSecurity http) throws Exception {
       SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
       successHandler.setTargetUrlParameter("redirectTo");
       successHandler.setDefaultTargetUrl(secureLoginProperties.getIndexUrl());

       http.authorizeRequests(
                       (authorizeRequests) -> authorizeRequests.antMatchers(secureProperties.getExcludeList().get(0)).permitAll()
                               .antMatchers(secureProperties.getExcludeList().get(1)).permitAll()
                               .antMatchers(secureProperties.getExcludeList().get(2)).permitAll()
                               .antMatchers(secureProperties.getExcludeList().get(3)).permitAll()
                               .antMatchers(secureProperties.getExcludeList().get(4)).permitAll().anyRequest().authenticated()
               ).formLogin(
                       (formLogin) -> formLogin.loginPage(secureLoginProperties.getLoginUrl()).successHandler(successHandler).and()
               ).logout((logout) -> logout.logoutUrl(secureLoginProperties.getLogoutUrl())).httpBasic(Customizer.withDefaults())
               .csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                       .ignoringRequestMatchers(
                               new AntPathRequestMatcher(this.adminServer.path("/instances"),
                                       HttpMethod.POST.toString()),
                               new AntPathRequestMatcher(this.adminServer.path("/instances/*"),
                                       HttpMethod.DELETE.toString()),
                               new AntPathRequestMatcher(this.adminServer.path("/actuator/**"))
                       ))
               .rememberMe((rememberMe) -> rememberMe.key(UUID.randomUUID().toString()).tokenValiditySeconds(1209600));
   }

   // Required to provide UserDetailsService for "remember functionality"
   @Override
   protected void configure(AuthenticationManagerBuilder auth) throws Exception {
       auth.inMemoryAuthentication().withUser(secureLoginProperties.getUsername())
               .password("{noop}" + secureLoginProperties.getPassword()).roles("USER");
   }


}

```
