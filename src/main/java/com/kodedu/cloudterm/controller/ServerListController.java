package com.kodedu.cloudterm.controller;

import com.kodedu.cloudterm.controller.vo.ServerVO;
import com.kodedu.cloudterm.dao.entity.Server;
import com.kodedu.cloudterm.helper.Result;
import com.kodedu.cloudterm.service.ServerListService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping("/server")
@ResponseBody
public class ServerListController {

    @Resource
    private ServerListService serverListService;


    @GetMapping("/list")
    public Result serverList() {
        List<ServerVO> serverList = serverListService.getServerList();
        return Result.success(serverList);
    }


    @PostMapping
    public Result upsert(@RequestBody Server server) {
        serverListService.upsert(server);
        return Result.success();
    }

    @GetMapping
    public Result getById(String id) {
        ServerVO serverVO = serverListService.getById(id);
        return Result.success(serverVO);
    }


}
