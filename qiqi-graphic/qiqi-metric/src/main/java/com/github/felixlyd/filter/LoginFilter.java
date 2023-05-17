package com.github.felixlyd.filter;


import cn.dev33.satoken.exception.BackResultException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.StopMatchException;
import cn.dev33.satoken.filter.SaFilterErrorStrategy;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaSessionCustomUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.dev33.satoken.util.SaTokenConsts;
import cn.hutool.core.util.StrUtil;
import com.github.felixlyd.config.properties.SaTokenLoginProperties;
import com.github.felixlyd.config.properties.SaTokenProperties;
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

    private final SaTokenProperties saTokenProperties;
    private final SaTokenLoginProperties saTokenLoginProperties;


    /**
     * 异常处理函数：每次[认证函数]发生异常时执行此函数
     */
    public SaFilterErrorStrategy error = e -> SaResult.error(e.getMessage());

    public LoginFilter(SaTokenProperties saTokenProperties, SaTokenLoginProperties saTokenLoginProperties) {
        this.saTokenProperties = saTokenProperties;
        this.saTokenLoginProperties = saTokenLoginProperties;
    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;

            if (StrUtil.equals(request.getMethod(), saTokenLoginProperties.getMethod())
                    && StrUtil.equals(request.getRequestURI(), saTokenLoginProperties.getLoginUrl())) {
                String username = request.getParameter(saTokenLoginProperties.getUsernameField());
                String password = request.getParameter(saTokenLoginProperties.getPasswordField());
                if (StrUtil.equals(username, saTokenLoginProperties.getUsername())
                        && StrUtil.equals(password, saTokenLoginProperties.getPassword())) {
                    StpUtil.login(saTokenLoginProperties.getLoginId());
                    SaSession saSession = SaSessionCustomUtil.getSessionById(saTokenProperties.getOriginUrlSession());
                    String originUrl = String.valueOf(saSession.get(saTokenProperties.getOriginUrlSession()));
                    response.sendRedirect(StrUtil.equals(originUrl, saTokenLoginProperties.getLoginUrl())?saTokenLoginProperties.getIndexUrl():originUrl);
                    return;
                } else {
                    throw new NotLoginException("用户名密码不正确！", StpUtil.getLoginType(), "-2");
                }
            }
            // 执行全局过滤器
            SaRouter.match(this.saTokenProperties.getIncludeList())
                    .notMatch(this.saTokenProperties.getExcludeList())
                    .check(r -> {
                        StpUtil.checkLogin();
                    });
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (StopMatchException e) {
            log.info(e.getMessage());
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (NotLoginException e) {
            log.info(e.getMessage());
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            SaSession saSession = SaSessionCustomUtil.getSessionById(saTokenProperties.getOriginUrlSession());
            saSession.set(saTokenProperties.getOriginUrlSession(), httpServletRequest.getRequestURI());
            httpServletResponse.sendRedirect(saTokenLoginProperties.getLoginUrl());
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
