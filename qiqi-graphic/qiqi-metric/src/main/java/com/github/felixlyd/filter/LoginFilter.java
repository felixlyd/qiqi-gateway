package com.github.felixlyd.filter;


import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaSessionCustomUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.undertow.util.HttpString;
import org.springframework.core.annotation.Order;

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
@Order(1)
public class LoginFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if(StrUtil.equals(request.getMethod(), "POST")&&StrUtil.equals(request.getRequestURI(),"/login")){
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            if(StrUtil.equals(username,"1")&&StrUtil.equals(password,"2")){
                StpUtil.login(1);
                SaSession saSession = SaSessionCustomUtil.getSessionById("origin-urls");
                String originUrl = String.valueOf(saSession.get("originUrl"));
                response.sendRedirect(originUrl);
            }else {
                throw new NotLoginException("用户名密码不正确！","","");
            }
        }else {
            filterChain.doFilter(request,response);
        }
    }
}
