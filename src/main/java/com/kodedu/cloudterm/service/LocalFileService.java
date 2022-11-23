package com.kodedu.cloudterm.service;

import com.kodedu.cloudterm.dao.entity.FileComponent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LocalFileService implements FileService {

    private static final List<FileComponent> EMPTY_LIST = new ArrayList<>();
    private static final File[] EMPTY_FILE_ARRAY = new File[0];

    /**
     * list root files and their child files
     *
     * @return
     */
    @Override
    public List<FileComponent> listRoots(String sessionId, boolean dirOnly) {
        return toFileComponents(File.listRoots(), dirOnly);
    }

    @Override
    public List<FileComponent> listFilesByPath(String sessionId, String path, boolean dirOnly) {
        File file = new File(path);
        Assert.isTrue(Objects.nonNull(file) && file.exists(), "file path does not exists.");
        if (file.isFile() || Optional.ofNullable(file.listFiles()).orElse(EMPTY_FILE_ARRAY).length == 0)
            return EMPTY_LIST;
        return toFileComponents(file.listFiles(), dirOnly);
    }

    private List<FileComponent> toFileComponents(File[] files, boolean dirOnly) {
        Assert.isTrue(Objects.nonNull(files) && files.length > 0, "files can not be null!");
        return Arrays.stream(files)
                .filter(f -> dirOnly ? f.isDirectory() : true)
                .map(f ->
                        FileComponent.builder().absolutePath(f.getAbsolutePath())
                                .name(f.getName())
                                .size(f.length())
                                .isDirectory(f.isDirectory())
                                .absolutePath(f.getAbsolutePath())
                                .lastModified(f.lastModified())
                                .parentName(Objects.nonNull(f.getParentFile()) ? f.getParentFile().getName() : null)
                                .parentAbsolutePath(Objects.nonNull(f.getParentFile()) ? f.getParentFile().getAbsolutePath() : null)
                                .childNum(Optional.ofNullable(f.listFiles()).orElse(new File[0]).length)
                                .build()
                )
                .sorted((f1, f2) -> {
                    if (f1.isDirectory() && f2.isDirectory()) {
                        return f1.getName().compareTo(f2.getName());
                    } else if (!f1.isDirectory() && !f2.isDirectory()) {
                        return f1.getName().compareTo(f2.getName());
                    } else if (f1.isDirectory()) {
                        return -1;
                    } else {
                        return 1;
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public byte[] downloadFile(String sessionId, String path) throws IOException {
        File file = new File(path);
        Assert.isTrue(Objects.nonNull(file) && file.exists(), String.format("file [%s] does not exist!", path));
        return new FileInputStream(file).readAllBytes();
    }

    @SneakyThrows
    @Override
    public void upload(String sessionId, String dest, MultipartFile multipart) {
        FileOutputStream fileOutputStream = null;
        try {
            File file = new File(dest, multipart.getOriginalFilename());
            if (!file.exists()) {
                file.createNewFile();
            } else {
                throw new RuntimeException(String.format("File [%s] already existed!", dest));
            }
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(multipart.getBytes());
        } finally {
            if (Objects.nonNull(fileOutputStream))
                fileOutputStream.close();
        }
    }
}
