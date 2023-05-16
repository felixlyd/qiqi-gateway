package com.github.felixlyd.config;

import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.github.felixlyd.filter.LoginFilter;
import com.github.felixlyd.filter.SaServletCustomFilter;
import de.codecentric.boot.admin.server.config.AdminServerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * class AuthConfig: do something
 *
 * @author : liuyaodong
 * @date 2023/5/16
 */
@Configuration
@Slf4j
public class AuthConfig {

    private final AdminServerProperties adminServer;

    public AuthConfig(AdminServerProperties adminServer) {
        this.adminServer = adminServer;
    }

    /**
     * 注册 [Sa-Token全局过滤器]
     */
    @Bean
    public SaServletCustomFilter getSaServletFilter() {
        return new SaServletCustomFilter()
                // 指定 拦截路由 与 放行路由
                .addInclude(this.adminServer.path("/**"))
                .addExclude(this.adminServer.path("/assets/**"),
                        this.adminServer.path("/actuator/**"),
                        this.adminServer.path("/instances/**"),
                        this.adminServer.path("/favicon.ico")
                )
                // 认证函数: 每次请求执行
                .setAuth(obj -> {
                    log.info("---------- 进入Sa-Token全局认证 -----------");

                    // 登录认证 -- 拦截所有路由，并排除/user/doLogin 用于开放登录
                    SaRouter.match(this.adminServer.path("/**"))
                            .notMatch(this.adminServer.path("/login"))
                            .check(r -> StpUtil.checkLogin());
                    // 更多拦截处理方式，请参考“路由拦截式鉴权”章节 */
                })
                // 异常处理函数：每次认证函数发生异常时执行此函数
                .setError(e -> {
                    log.info("---------- 进入Sa-Token异常处理 -----------");
                    return SaResult.error(e.getMessage());
                })
                ;
    }

    @Bean
    public LoginFilter loginFilter(){
        return new LoginFilter();
    }
}
