package com.github.felixlyd.filter;


import cn.dev33.satoken.exception.BackResultException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.filter.SaFilterErrorStrategy;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaSessionCustomUtil;
import cn.dev33.satoken.stp.SaLoginModel;
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
                    StpUtil.login(secureLoginProperties.getLoginId(),
                            new SaLoginModel()
                                    .setIsLastingCookie(StrUtil.isNotBlank(request.getParameter(secureLoginProperties.getRememberMeField())))
                                    .setTimeout(secureLoginProperties.getValidSeconds())
                    );
                    SaSession saSession = SaSessionCustomUtil.getSessionById(secureProperties.getOriginUrlSession());
                    String originUrl = String.valueOf(saSession.get(secureProperties.getOriginUrlSession()));
                    response.sendRedirect(StrUtil.equals(originUrl, secureLoginProperties.getLoginUrl()) ? secureLoginProperties.getIndexUrl() : originUrl);
                    return;
                } else {
                    throw new NotLoginException("用户名密码不正确！", StpUtil.getLoginType(), "-2");
                }
            }

            // 拦截注销逻辑
            if (StrUtil.equals(request.getRequestURI(), secureLoginProperties.getLogoutUrl())) {
                StpUtil.logout(secureLoginProperties.getLoginId());
                response.sendRedirect(secureLoginProperties.getLoginUrl());
                return;
            }

            // 全局拦截，校验是否登录
            SaRouter.match(this.secureProperties.getIncludeList())
                    .notMatch(this.secureProperties.getExcludeList())
                    .check(r -> StpUtil.checkLogin());

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
