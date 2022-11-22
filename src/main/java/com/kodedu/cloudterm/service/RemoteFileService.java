package com.kodedu.cloudterm.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.kodedu.cloudterm.dao.SessionListDao;
import com.kodedu.cloudterm.dao.entity.FileComponent;
import com.kodedu.cloudterm.dao.entity.SessionEntity;
import com.kodedu.cloudterm.helper.JschHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RemoteFileService implements FileService {

    @Resource
    private SessionListDao sessionListDao;

    private static final List<FileComponent> EMPTY_LIST = new ArrayList<>();
    private static final File[] EMPTY_FILE_ARRAY = new File[0];

    @Override
    public List<FileComponent> listRoots(String sessionId) {
        Assert.hasText(sessionId, "session id can't be empty");
        SessionEntity sessionEntity = sessionListDao.findById(sessionId);
        Assert.notNull(sessionEntity, "session does not exist!");
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = JschHelper.getSession(sessionEntity);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(5 * 1000);
            Vector<ChannelSftp.LsEntry> ls = channel.ls("/");
            return toFileComponent(ls, "");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (Objects.nonNull(channel) && channel.isConnected()) {
                channel.disconnect();
            }
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private List<FileComponent> toFileComponent(Vector<ChannelSftp.LsEntry> ls, String path) {
        return ls.stream()
                .filter(en -> {
                    if (en.getFilename().equals(".") || en.getFilename().equals("..") || en.getAttrs().isLink()) {
                        return false;
                    }
                    return true;
                })
                .map(entity -> FileComponent.builder()
                        .name(entity.getFilename())
                        .absolutePath(path + "/" + entity.getFilename())
                        .size(entity.getAttrs().getSize())
                        .isDirectory(entity.getAttrs().isDir())
                        .lastModified(entity.getAttrs().getATime())
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
    public List<FileComponent> listFilesByPath(String sessionId, String path) {
        Assert.hasText(sessionId, "session id can't be empty");
        Assert.hasText(path, "path can't be empty");
        SessionEntity sessionEntity = sessionListDao.findById(sessionId);
        Assert.notNull(sessionEntity, "session does not exist!");
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = JschHelper.getSession(sessionEntity);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(5 * 1000);
            Vector<ChannelSftp.LsEntry> ls = channel.ls(path);
            return toFileComponent(ls, path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (Objects.nonNull(channel) && channel.isConnected()) {
                channel.disconnect();
            }
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    @Override
    public byte[] downloadFile(String sessionId, String path) throws FileNotFoundException {
        Assert.hasText(sessionId, "session id can't be empty");
        Assert.hasText(path, "path can't be empty");
        SessionEntity sessionEntity = sessionListDao.findById(sessionId);
        Assert.notNull(sessionEntity, "session does not exist!");
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = JschHelper.getSession(sessionEntity);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(5 * 1000);
            return channel.get(path).readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (Objects.nonNull(channel) && channel.isConnected()) {
                channel.disconnect();
            }
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
        }
    }
}
