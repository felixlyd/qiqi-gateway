package com.github.felixlyd.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.github.felixlyd.config.properties.SaTokenLoginProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * class AuthController: do something
 *
 * @author : liuyaodong
 * @date 2023/5/16
 */
@Controller
public class AuthController {

    @Autowired
    private SaTokenLoginProperties saTokenLoginProperties;

    @GetMapping("/logout")
    public String logout(){
        StpUtil.logout(saTokenLoginProperties.getLoginId());
        return "redirect:/login";
    }
}
