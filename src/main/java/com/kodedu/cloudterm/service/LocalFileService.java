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
    private static final File[] EMPTY_FILE_ARRAY = new File[0];

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
        if (file.isFile() || Optional.ofNullable(file.listFiles()).orElse(EMPTY_FILE_ARRAY).length == 0)
            return EMPTY_LIST;
        return toFileComponents(file.listFiles());
    }

    private List<FileComponent> toFileComponents(File[] files) {
        Assert.isTrue(Objects.nonNull(files) && files.length > 0, "files can not be null!");
        return Arrays.stream(files)
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
}
