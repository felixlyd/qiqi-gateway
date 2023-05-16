package com.github.felixlyd.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * class AuthController: do something
 *
 * @author : liuyaodong
 * @date 2023/5/16
 */
@Controller
public class AuthController {
    @PostMapping("/logout")
    public String logout(){
        StpUtil.logout(1);
        return "redirect:/login";
    }
}
