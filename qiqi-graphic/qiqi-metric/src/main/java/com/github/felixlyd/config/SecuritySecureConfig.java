//package com.github.felixlyd.config;
//
//import cn.hutool.core.lang.UUID;
//import com.github.felixlyd.config.properties.SecureLoginProperties;
//import com.github.felixlyd.config.properties.SecureProperties;
//import de.codecentric.boot.admin.server.config.AdminServerProperties;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
//import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
//
///**
// * class SecuritySecureConfig: do something
// *
// * @author : liuyaodong
// * @date 2023/5/17
// */
//@Configuration(proxyBeanMethods = false)
//@EnableWebSecurity
//public class SecuritySecureConfig extends WebSecurityConfigurerAdapter {
//    private final AdminServerProperties adminServer;
//    private final SecureProperties secureProperties;
//    private final SecureLoginProperties secureLoginProperties;
//
//    public SecuritySecureConfig(AdminServerProperties adminServer, SecureProperties secureProperties, SecureLoginProperties secureLoginProperties) {
//        this.adminServer = adminServer;
//        this.secureProperties = secureProperties;
//        this.secureLoginProperties = secureLoginProperties;
//    }
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
//        successHandler.setTargetUrlParameter("redirectTo");
//        successHandler.setDefaultTargetUrl(secureLoginProperties.getIndexUrl());
//
//        http.authorizeRequests(
//                        (authorizeRequests) -> authorizeRequests.antMatchers(secureProperties.getExcludeList().get(0)).permitAll()
//                                .antMatchers(secureProperties.getExcludeList().get(1)).permitAll()
//                                .antMatchers(secureProperties.getExcludeList().get(2)).permitAll()
//                                .antMatchers(secureProperties.getExcludeList().get(3)).permitAll()
//                                .antMatchers(secureProperties.getExcludeList().get(4)).permitAll().anyRequest().authenticated()
//                ).formLogin(
//                        (formLogin) -> formLogin.loginPage(secureLoginProperties.getLoginUrl()).successHandler(successHandler).and()
//                ).logout((logout) -> logout.logoutUrl(secureLoginProperties.getLogoutUrl())).httpBasic(Customizer.withDefaults())
//                .csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                        .ignoringRequestMatchers(
//                                new AntPathRequestMatcher(this.adminServer.path("/instances"),
//                                        HttpMethod.POST.toString()),
//                                new AntPathRequestMatcher(this.adminServer.path("/instances/*"),
//                                        HttpMethod.DELETE.toString()),
//                                new AntPathRequestMatcher(this.adminServer.path("/actuator/**"))
//                        ))
//                .rememberMe((rememberMe) -> rememberMe.key(UUID.randomUUID().toString()).tokenValiditySeconds(1209600));
//    }
//
//    // Required to provide UserDetailsService for "remember functionality"
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication().withUser(secureLoginProperties.getUsername())
//                .password("{noop}" + secureLoginProperties.getPassword()).roles("USER");
//    }
//
//
//}
