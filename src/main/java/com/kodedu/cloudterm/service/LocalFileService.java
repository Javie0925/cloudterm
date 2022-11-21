package com.kodedu.cloudterm.service;

import com.kodedu.cloudterm.dao.entity.FileComponent;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocalFileService {

    private static final List<FileComponent> EMPTY_LIST = new ArrayList<>();

    /**
     * list root files and their child files
     *
     * @return
     */
    public List<FileComponent> listRoots() {
        return toFileComponents(File.listRoots());
    }

    public List<FileComponent> listFilesByPath(String path) {
        File file = new File(path);
        Assert.isTrue(file.exists(), "file path does not exists.");
        if (file.isFile()) return EMPTY_LIST;
        return toFileComponents(file.listFiles());
    }

    private List<FileComponent> toFileComponents(File[] files) {
        Assert.isTrue(Objects.nonNull(files) && files.length > 0, "files can not be null!");
        return Arrays.stream(files)
                .map(f ->
                        FileComponent.builder().absolutePath(f.getAbsolutePath())
                                .name(f.getName())
                                .freeSpace(f.getFreeSpace())
                                .usableSpace(f.getUsableSpace())
                                .totalSpace(f.getTotalSpace())
                                .isDirectory(f.isDirectory())
                                .absolutePath(f.getAbsolutePath())
                                .lastModified(f.lastModified())
                                .children(
                                        Arrays.stream(f.listFiles()).map(_f ->
                                                FileComponent.builder().absolutePath(_f.getAbsolutePath())
                                                        .name(_f.getName())
                                                        .freeSpace(_f.getFreeSpace())
                                                        .usableSpace(_f.getUsableSpace())
                                                        .totalSpace(_f.getTotalSpace())
                                                        .isDirectory(_f.isDirectory())
                                                        .absolutePath(_f.getAbsolutePath())
                                                        .lastModified(_f.lastModified())
                                                        .build()
                                        ).collect(Collectors.toList())
                                ).build()
                )
                .sorted(Comparator.comparing(FileComponent::getAbsolutePath))
                .collect(Collectors.toList());
    }
}
