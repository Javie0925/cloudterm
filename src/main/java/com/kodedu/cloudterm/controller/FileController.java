package com.kodedu.cloudterm.controller;

import com.kodedu.cloudterm.helper.Result;
import com.kodedu.cloudterm.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.IOException;

@Controller
@RequestMapping("/file")
public class FileController {

    @Resource(name = "localFileService")
    private FileService localFileService;

    @Resource(name = "remoteFileService")
    private FileService remoteFileService;

    @GetMapping("/listRoots")
    @ResponseBody
    public Result listRoots(@RequestParam(required = false) String sessionId) {
        if (StringUtils.hasText(sessionId)) {
            return Result.success(remoteFileService.listRoots(sessionId));
        } else {
            return Result.success(localFileService.listRoots(null));
        }
    }

    @GetMapping("/listFilesByPath")
    @ResponseBody
    public Result listFilesByPath(@RequestParam(required = false) String sessionId, String path) {
        if (StringUtils.hasText(sessionId)) {
            return Result.success(remoteFileService.listFilesByPath(sessionId, path));
        } else {
            return Result.success(localFileService.listFilesByPath(null, path));
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
}
