package com.github.felixlyd.filter;

import cn.dev33.satoken.exception.BackResultException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.StopMatchException;
import cn.dev33.satoken.filter.SaFilterAuthStrategy;
import cn.dev33.satoken.filter.SaFilterErrorStrategy;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaSessionCustomUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaTokenConsts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * class SaServletCustomFilter: do something
 *
 * @author : liuyaodong
 * @date 2023/5/16
 */

@Slf4j
@Order(SaTokenConsts.ASSEMBLY_ORDER)
public class SaServletCustomFilter extends SaServletFilter {

    /**
     * 添加 [拦截路由]
     * @param paths 路由
     * @return 对象自身
     */
    public SaServletCustomFilter addInclude(String... paths) {
        super.getIncludeList().addAll(Arrays.asList(paths));
        return this;
    }

    /**
     * 添加 [放行路由]
     * @param paths 路由
     * @return 对象自身
     */
    public SaServletCustomFilter addExclude(String... paths) {
        super.getExcludeList().addAll(Arrays.asList(paths));
        return this;
    }

    /**
     * 写入[认证函数]: 每次请求执行
     * @param auth see note
     * @return 对象自身
     */
    public SaServletCustomFilter setAuth(SaFilterAuthStrategy auth) {
        this.auth = auth;
        return this;
    }

    /**
     * 写入[异常处理函数]：每次[认证函数]发生异常时执行此函数
     * @param error see note
     * @return 对象自身
     */
    public SaServletCustomFilter setError(SaFilterErrorStrategy error) {
        this.error = error;
        return this;
    }

    /**
     * 写入[前置函数]：在每次[认证函数]之前执行
     * @param beforeAuth see note
     * @return 对象自身
     */
    public SaServletCustomFilter setBeforeAuth(SaFilterAuthStrategy beforeAuth) {
        this.beforeAuth = beforeAuth;
        return this;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            // 执行全局过滤器
            SaRouter.match(super.getIncludeList()).notMatch(super.getExcludeList()).check(r -> {
                log.info("请求路径："+httpServletRequest.getRequestURI());
                beforeAuth.run(null);
                auth.run(null);
            });

        } catch (StopMatchException e) {
            log.info(e.getMessage());
        } catch (NotLoginException e) {
            log.info(e.getMessage());
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            SaSession saSession = SaSessionCustomUtil.getSessionById("origin-urls");
            saSession.set("originUrl", httpServletRequest.getRequestURI());
            httpServletResponse.sendRedirect("/login");
        } catch (Throwable e) {
            // 1. 获取异常处理策略结果
            String result = (e instanceof BackResultException) ? e.getMessage() : String.valueOf(error.run(e));

            // 2. 写入输出流
            if(response.getContentType() == null) {
                response.setContentType("text/plain; charset=utf-8");
            }
            response.getWriter().print(result);
            return;
        }

        // 执行
        chain.doFilter(request, response);
    }
}
