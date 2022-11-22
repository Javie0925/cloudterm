package com.kodedu.cloudterm.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.kodedu.cloudterm.controller.vo.SessionVO;
import com.kodedu.cloudterm.dao.entity.SessionEntity;
import com.kodedu.cloudterm.helper.Result;
import com.kodedu.cloudterm.service.SessionListService;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/server")
@ResponseBody
public class SessionListController {

    private static final String ipPattern = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";

    @Resource
    private SessionListService sessionListService;


    @GetMapping("/list")
    public Result serverList() {
        List<SessionVO> serverList = sessionListService.getServerList();
        return Result.success(serverList);
    }


    @PostMapping
    public Result upsert(@Validated SessionEntity sessionEntity) {
        Assert.isTrue(Pattern.matches(ipPattern, sessionEntity.getHost()), "please enter correct ip host! ");
        Assert.isTrue(Arrays.stream(sessionEntity.getHost().split("\\.")).filter(s -> {
            int i = Integer.parseInt(s);
            return i >= 0 && i <= 255;
        }).count() == 4, "please enter correct ip host! ");
        Assert.hasLength(sessionEntity.getName(),"name can not be empty!");
        Assert.hasLength(sessionEntity.getUser(),"user can not be empty!");
        Assert.hasLength(sessionEntity.getHost(),"host can not be empty!");
        sessionListService.upsert(sessionEntity);
        return Result.success();
    }

    @GetMapping
    public Result getById(String id) {
        SessionVO sessionVO = sessionListService.getById(id);
        return Result.success(sessionVO);
    }
    @DeleteMapping
    public Result del(String idList) {
        if (!StringUtils.hasLength(idList)) return Result.success();
        sessionListService.delete(JSON.parseObject(idList,new TypeReference<>(){}));
        return Result.success();
    }

    @GetMapping("/test")
    public Result testConnection(String id) {
        sessionListService.testConnection(id);
        return Result.success();
    }


}
