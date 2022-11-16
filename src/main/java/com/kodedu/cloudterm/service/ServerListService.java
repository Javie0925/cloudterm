package com.kodedu.cloudterm.service;

import com.jcraft.jsch.*;
import com.kodedu.cloudterm.controller.vo.ServerVO;
import com.kodedu.cloudterm.dao.ServerListDao;
import com.kodedu.cloudterm.dao.entity.Server;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ServerListService {

    @Resource
    private ServerListDao serverListDao;

    public List<Server> getServerList() {
        List<Server> serverList = serverListDao.getServerList();
        List<Server> result = serverList.stream()
                .sorted(Comparator.comparing(Server::getName))
                .collect(Collectors.toList());
        return result;
    }

    public ServerVO getById(String id) {
        Server server = serverListDao.findById(id);
        ServerVO serverVO = new ServerVO();
        BeanUtils.copyProperties(server, serverVO);
        return serverVO;
    }

    public void upsert(Server server) {
        serverListDao.upsert(server);
    }

    public void delete(List<String> idList) {
        serverListDao.delete(idList);
    }

    public void testConnection(String id){
        Server server = serverListDao.findById(id);
        Assert.notNull(server,"server doesn't exist!");
        Session session = null;
        Channel channel = null;
        try {
            session = new JSch().getSession(server.getUser(), server.getHost(), server.getPort());
            session.setPassword(server.getPasswd());
            session.setConfig("StrictHostKeyChecking", "no");
            UserInfo userInfo = new UserInfo() {
                @Override
                public String getPassphrase() {
                    return null;
                }

                @Override
                public String getPassword() {
                    return server.getPasswd();
                }

                @Override
                public boolean promptPassword(String s) {
                    return false;
                }

                @Override
                public boolean promptPassphrase(String s) {
                    return false;
                }

                @Override
                public boolean promptYesNo(String s) {
                    return false;//notice here!
                }

                @SneakyThrows
                @Override
                public void showMessage(String s) {
                    log.info("test connection show message:{}", s);
                }
            };
            session.setUserInfo(userInfo);
            session.connect(5000);
            channel = session.openChannel("shell");
            channel.connect(5000);
        } catch (JSchException e) {
            log.error("test connection fail: ", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }finally {
            if (Objects.nonNull(channel) && !channel.isClosed()){
                channel.disconnect();
            }
            if (Objects.nonNull(session) && session.isConnected()){
                session.disconnect();
            }
        }
    }
}
