package com.kodedu.cloudterm.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.kodedu.cloudterm.dao.entity.SessionEntity;
import com.kodedu.cloudterm.helper.DesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class SessionListDao {

    private static final String filePath = System.getProperty("sessionList.filepath");

    private Map<String, SessionEntity> sessionMap = new ConcurrentHashMap<>();

    private boolean hasFile = true;

    private File sessionListFile;

    private String desPassword = System.getProperty("des.password", "123456");

    @PostConstruct
    public void init() {
        try {
            if (!StringUtils.hasLength(filePath)) {
                hasFile = false;
                return;
            }
            sessionListFile = new File(filePath);
            if (!sessionListFile.exists()) {
                sessionListFile.createNewFile();
            }
            FileInputStream fileInputStream = new FileInputStream(sessionListFile);
            if (fileInputStream.available() > 0) {
                byte[] bytes = new byte[fileInputStream.available()];
                fileInputStream.read(bytes);
                byte[] decryptBytes = DesUtil.decrypt(bytes, desPassword);
                if (null == decryptBytes || decryptBytes.length == 0) {
                    throw new RuntimeException("The DES password may be wrong, please check it before restarting! ");
                }
                List<SessionEntity> sessionEntityList = JSON.parseObject(new String(DesUtil.decrypt(bytes, desPassword), StandardCharsets.UTF_8), new TypeReference<List<SessionEntity>>() {
                });
                if (!CollectionUtils.isEmpty(sessionEntityList)) {
                    sessionEntityList.stream().forEach(s -> sessionMap.put(s.getId(), s));
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage());
            hasFile = false;
        }
    }

    public SessionEntity findById(String id) {
        if (sessionMap.containsKey(id)) {
            return sessionMap.get(id);
        }
        return null;
    }

    public List<SessionEntity> getServerList() {
        if (!sessionMap.isEmpty()) {
            return sessionMap.entrySet().stream().map(en -> en.getValue()).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public void upsert(SessionEntity sessionEntity) {
        if (StringUtils.hasLength(sessionEntity.getId())) {
            if (StringUtils.hasLength(sessionEntity.getPasswd())) {
                sessionMap.put(sessionEntity.getId(), sessionEntity);
            } else {
                sessionEntity.setPasswd(sessionMap.get(sessionEntity.getId()).getPasswd());
                sessionMap.put(sessionEntity.getId(), sessionEntity);
            }
        } else {
            Assert.hasLength(sessionEntity.getPasswd(), "password can not be empty!");
            sessionEntity.setId(UUID.randomUUID().toString().replaceAll("-", ""));
            if (sessionEntity.getPort() == 0) sessionEntity.setPort(22);
            sessionMap.put(sessionEntity.getId(), sessionEntity);
        }
    }


    private synchronized void writeToFile() {
        if (!hasFile) return;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(sessionListFile);
            List<SessionEntity> list = sessionMap.entrySet().stream().map(en -> en.getValue()).collect(Collectors.toList());
            fileOutputStream.write(DesUtil.encrypt(JSON.toJSONString(list).getBytes(StandardCharsets.UTF_8), desPassword));
            fileOutputStream.flush();
        } catch (IOException e) {
            log.error("writeToFile fail: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void preDestroy() {
        writeToFile();
    }


    public void delete(List<String> idList) {
        idList.stream().forEach(id -> sessionMap.remove(id));
    }
}
