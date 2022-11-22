package com.kodedu.cloudterm.helper;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.kodedu.cloudterm.dao.entity.SessionEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

@Slf4j
public class JschHelper {

    public static Session getSession(SessionEntity sessionEntity) throws JSchException {
        Assert.notNull(sessionEntity, "sessionEntity is null");
        JSch jsch = new JSch();
        Session jschSession = jsch.getSession(sessionEntity.getUser(), sessionEntity.getHost(), sessionEntity.getPort());
        jschSession.setPassword(sessionEntity.getPasswd());
        jschSession.setConfig("StrictHostKeyChecking", "no");
        UserInfo userInfo = new UserInfo() {
            @Override
            public String getPassphrase() {
                return null;
            }

            @Override
            public String getPassword() {
                return sessionEntity.getPasswd();
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
                log.info("jcsh show message:{}", s);
            }
        };
        jschSession.setUserInfo(userInfo);
        jschSession.connect(5000);
        return jschSession;
    }
}
