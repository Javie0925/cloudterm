package com.kodedu.cloudterm.dao.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileComponent {
    private String name;
    private String absolutePath;
    private String parentName;
    private String parentAbsolutePath;
    private boolean isDirectory;
    private long size;
    private long freeSpace;
    private long totalSpace;
    private long usableSpace;
    private long lastModified;
    private int childNum;
}
