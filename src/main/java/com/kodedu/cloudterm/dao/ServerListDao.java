package com.kodedu.cloudterm.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.kodedu.cloudterm.dao.entity.Server;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ServerListDao {

    private static final String filePath = System.getProperty("serverList.filepath");

    private Map<String, Server> serverMap = new ConcurrentHashMap<>();

    private boolean hasFile = true;

    private File serverListFile;

    private String desPassword = System.getProperty("des.password","123456");

    @PostConstruct
    public void init() {
        try {
            if (!StringUtils.hasLength(filePath)) {
                hasFile = false;
                return;
            }
            serverListFile = new File(filePath);
            if (!serverListFile.exists()) {
                serverListFile.createNewFile();
            }
            FileInputStream fileInputStream = new FileInputStream(serverListFile);
            if (fileInputStream.available() > 0) {
                byte[] bytes = new byte[fileInputStream.available()];
                fileInputStream.read(bytes);
                List<Server> serverList = JSON.parseObject(new String(DesUtil.decrypt(bytes, desPassword), StandardCharsets.UTF_8), new TypeReference<List<Server>>() {
                });
                if (!CollectionUtils.isEmpty(serverList)) {
                    serverList.stream().forEach(s -> serverMap.put(s.getId(), s));
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage());
            hasFile = false;
        }
    }

    public Server findById(String id) {
        if (serverMap.containsKey(id)) {
            return serverMap.get(id);
        }
        return null;
    }

    public List<Server> getServerList() {
        if (!serverMap.isEmpty()) {
            return serverMap.entrySet().stream().map(en -> en.getValue()).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public void upsert(Server server) {
        if (StringUtils.hasLength(server.getId())) {
            if (StringUtils.hasLength(server.getPasswd())) {
                serverMap.put(server.getId(), server);
            } else {
                server.setPasswd(serverMap.get(server.getId()).getPasswd());
                serverMap.put(server.getId(), server);
            }
        } else {
            Assert.hasLength(server.getPasswd(),"password can not be empty!");
            server.setId(UUID.randomUUID().toString().replaceAll("-", ""));
            if (server.getPort() == 0) server.setPort(22);
            serverMap.put(server.getId(), server);
        }
    }


    private synchronized void writeToFile() {
        if (!hasFile) return;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(serverListFile);
            List<Server> list = serverMap.entrySet().stream().map(en -> en.getValue()).collect(Collectors.toList());
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
        idList.stream().forEach(id -> serverMap.remove(id));
    }
}
