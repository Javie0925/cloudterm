package com.kodedu.cloudterm.controller;

import com.kodedu.cloudterm.helper.Result;
import com.kodedu.cloudterm.service.LocalFileService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/localFile")
public class LocalFileController {

    @Resource
    private LocalFileService localFileService;

    @GetMapping("/listRoots")
    @ResponseBody
    public Result listRoots() {
        return Result.success(localFileService.listRoots());
    }

    @GetMapping("/listFilesByPath")
    @ResponseBody
    public Result listFilesByPath(String path) {
        return Result.success(localFileService.listFilesByPath(path));
    }
}
