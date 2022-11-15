package com.kodedu.cloudterm.controller;

import com.kodedu.cloudterm.controller.vo.ServerVO;
import com.kodedu.cloudterm.dao.entity.Server;
import com.kodedu.cloudterm.helper.Result;
import com.kodedu.cloudterm.service.ServerListService;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/server")
@ResponseBody
public class ServerListController {

    private static final String ipPattern = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";

    @Resource
    private ServerListService serverListService;


    @GetMapping("/list")
    public Result serverList() {
        List<ServerVO> serverList = serverListService.getServerList();
        return Result.success(serverList);
    }


    public static void main(String[] args) {
        System.out.println(Pattern.matches(ipPattern, "10.10.1.1"));
    }

    @PostMapping
    public Result upsert(@Validated Server server) {
        serverListService.upsert(server);
        Assert.isTrue(Pattern.matches(ipPattern, server.getHost()), "please enter correct ip host! ");
        Assert.isTrue(Arrays.stream(server.getHost().split("\\.")).filter(s -> {
            int i = Integer.parseInt(s);
            return i >= 0 && i <= 255;
        }).count() == 4, "please enter correct ip host! ");
        return Result.success();
    }

    @GetMapping
    public Result getById(String id) {
        ServerVO serverVO = serverListService.getById(id);
        return Result.success(serverVO);
    }


}
