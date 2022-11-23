package com.kodedu.cloudterm.controller;

import com.kodedu.cloudterm.helper.Result;
import com.kodedu.cloudterm.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Objects;

@Controller
@RequestMapping("/file")
public class FileController {

    @Resource(name = "localFileService")
    private FileService localFileService;

    @Resource(name = "remoteFileService")
    private FileService remoteFileService;

    @GetMapping("/listRoots")
    @ResponseBody
    public Result listRoots(@RequestParam(required = false) String sessionId, boolean dirOnly) {
        if (StringUtils.hasText(sessionId)) {
            return Result.success(remoteFileService.listRoots(sessionId, dirOnly));
        } else {
            return Result.success(localFileService.listRoots(null, dirOnly));
        }
    }

    @GetMapping("/listFilesByPath")
    @ResponseBody
    public Result listFilesByPath(@RequestParam(required = false) String sessionId, String path, boolean dirOnly) {
        if (StringUtils.hasText(sessionId)) {
            return Result.success(remoteFileService.listFilesByPath(sessionId, path, dirOnly));
        } else {
            return Result.success(localFileService.listFilesByPath(null, path, dirOnly));
        }
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<byte[]> download(@RequestParam(required = false) String sessionId, String path, String filename) throws IOException {
        byte[] fileBytes = null;
        if (StringUtils.hasText(sessionId)) {
            fileBytes = remoteFileService.downloadFile(sessionId, path);
        } else {
            fileBytes = localFileService.downloadFile(sessionId, path);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment;filename=" + filename);
        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/upload")
    @ResponseBody
    public Result upload(@RequestParam(required = false) String sessionId, String dest, MultipartFile file) throws IOException {
        Assert.hasText(dest, "File target path can not be empty!");
        Assert.isTrue(Objects.nonNull(file) || !file.isEmpty() || file.getSize() != 0, "File is empty!");
        if (StringUtils.hasText(sessionId)) {
            remoteFileService.upload(sessionId, dest, file);
        } else {
            localFileService.upload(sessionId, dest, file);
        }
        return Result.success();
    }
}
