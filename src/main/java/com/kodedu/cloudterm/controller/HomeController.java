package com.kodedu.cloudterm.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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

    @RequestMapping("/serverList")
    @ResponseBody
    public Object serverList() throws IOException {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("serverList.json");
        byte[] bytes = new byte[resourceAsStream.available()];
        resourceAsStream.read(bytes);
        JSONArray jsonArray = JSON.parseArray(new String(bytes));
        JSONObject obj = new JSONObject();
        return obj.fluentPut("code",0).fluentPut("data",jsonArray);
    }
}
