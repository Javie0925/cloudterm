package com.kodedu.cloudterm.service;

import com.kodedu.cloudterm.dao.entity.FileComponent;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {

    List<FileComponent> listRoots(String sessionId, boolean dirOnly);

    List<FileComponent> listFilesByPath(String sessionId, String path, boolean dirOnly);

    byte[] downloadFile(String sessionId, String path) throws IOException;

    void upload(String sessionId, String dest, MultipartFile file);
}
