package com.kodedu.cloudterm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    @RequestMapping("/")
    public String home(HttpServletRequest request) {
        String page = request.getParameter("page");
        if (!StringUtils.hasLength(page)) {
            page = "home";
        }
        return page;
    }
}
