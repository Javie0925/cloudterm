package com.kodedu.cloudterm.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.kodedu.cloudterm.dao.entity.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ServerListDao {

    private URL serverListUrl;
    private FileOutputStream serverListFileOutputStream;
    private Map<String, Server> serverMap = new ConcurrentHashMap<>();
    private File serverListFile;

    @PostConstruct
    public void init() throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("serverList.json");
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        serverListUrl = this.getClass().getClassLoader().getResource("serverList.json");
        serverListFile = new File(serverListUrl.getPath());
        serverListFileOutputStream = new FileOutputStream(serverListFile);
        List<Server> serverList = JSON.parseObject(new String(bytes), new TypeReference<>() {
        });
        if (!CollectionUtils.isEmpty(serverList)) {
            Map<String, Server> map = serverList.stream().collect(Collectors.toMap(s -> s.getId(), s -> s));
            serverMap = new ConcurrentHashMap<>(map);
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
        if (StringUtils.hasLength(server.getId())){
            serverMap.put(server.getId(),server);
        }
        serverMap.put(UUID.randomUUID().toString(), server);
        writeToFile();
    }


    private synchronized void writeToFile() {
        try {
            List<Server> list = serverMap.entrySet().stream().map(en -> en.getValue()).collect(Collectors.toList());
            serverListFileOutputStream.write(JSON.toJSONString(list).getBytes(StandardCharsets.UTF_8));
            serverListFileOutputStream.flush();
        } catch (IOException e) {
            log.error("writeToFile fail: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void preDestroy() throws IOException {
        writeToFile();
        serverListFileOutputStream.close();
    }


}
