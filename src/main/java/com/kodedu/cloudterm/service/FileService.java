package com.kodedu.cloudterm.service;

import com.kodedu.cloudterm.dao.entity.FileComponent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface FileService {

    List<FileComponent> listRoots(String sessionId);

    List<FileComponent> listFilesByPath(String sessionId, String path);

    byte[] downloadFile(String sessionId, String path) throws IOException;
}
