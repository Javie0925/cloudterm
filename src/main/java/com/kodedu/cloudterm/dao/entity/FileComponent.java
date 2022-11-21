package com.kodedu.cloudterm.dao.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FileComponent {
    private String name;
    private String absolutePath;
    private String parent;
    private boolean isDirectory;
    private long freeSpace;
    private long totalSpace;
    private long usableSpace;
    private long lastModified;
    List<FileComponent> children;
}
