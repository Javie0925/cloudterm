package com.kodedu.cloudterm.service;

import com.jcraft.jsch.*;
import com.kodedu.cloudterm.controller.vo.SessionVO;
import com.kodedu.cloudterm.dao.SessionListDao;
import com.kodedu.cloudterm.dao.entity.SessionEntity;
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
public class SessionListService {

    @Resource
    private SessionListDao serverListDao;

    public List<SessionVO> getServerList() {
        List<SessionEntity> sessionEntityList = serverListDao.getServerList();
        List<SessionVO> result = sessionEntityList.stream()
                .map(s->{
                    SessionVO sessionVO = new SessionVO();
                    BeanUtils.copyProperties(s, sessionVO);
                    return sessionVO;
                })
                .sorted(Comparator.comparing(SessionVO::getName))
                .collect(Collectors.toList());
        return result;
    }

    public SessionVO getById(String id) {
        SessionEntity sessionEntity = serverListDao.findById(id);
        SessionVO sessionVO = new SessionVO();
        BeanUtils.copyProperties(sessionEntity, sessionVO);
        return sessionVO;
    }

    public void upsert(SessionEntity sessionEntity) {
        serverListDao.upsert(sessionEntity);
    }

    public void delete(List<String> idList) {
        serverListDao.delete(idList);
    }

    public void testConnection(String id){
        SessionEntity server = serverListDao.findById(id);
        Assert.notNull(server,"server doesn't exist!");
        com.jcraft.jsch.Session session = null;
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
