package com.kodedu.cloudterm.handler;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.UserInfo;
import com.kodedu.cloudterm.dao.entity.SessionEntity;
import com.kodedu.cloudterm.helper.ThreadHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.*;

@Slf4j
public class JschTerminalHandler extends AbstractTerminalHandler {

    private BufferedReader inputReader;
    private BufferedWriter outputWriter;
    private Channel jschChannel;
    private com.jcraft.jsch.Session jschSession;

    private void printReader(BufferedReader bufferedReader) {
        try {
            int nRead;
            char[] data = new char[1 * 1024];

            while ((nRead = bufferedReader.read(data, 0, data.length)) != -1) {
                StringBuilder builder = new StringBuilder(nRead);
                builder.append(data, 0, nRead);
                print(builder.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    @Override
    public void onTerminalReady() {
        if (isReady()) return;
        SessionEntity sessionEntity = getSessionEntity();
        Assert.notNull(sessionEntity, String.format("can not find any session info!"));
        JSch jsch = new JSch();
        jschSession = jsch.getSession(sessionEntity.getUser(), sessionEntity.getHost(), sessionEntity.getPort());
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
                log.info("jsch show message:{}", s);
                print(s);
            }
        };
        jschSession.setUserInfo(userInfo);
        jschSession.connect(5000);
        jschChannel = jschSession.openChannel("shell");
        jschChannel.connect(5 * 1000);
        this.inputReader = new BufferedReader(new InputStreamReader(jschChannel.getInputStream()));
        this.outputWriter = new BufferedWriter(new OutputStreamWriter(jschChannel.getOutputStream()));
        ThreadHelper.start(() -> {
            printReader(inputReader);
        });
        setReady(true);
    }

    @SneakyThrows
    @Override
    public void onCommand(String command) {

        getCommandQueue().put(command);
        ThreadHelper.start(() -> {
            try {
                outputWriter.write(getCommandQueue().poll());
                outputWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public void onTerminalClose() {
        if (null != jschChannel && !jschChannel.isClosed()) {
            jschChannel.disconnect();
        }
        if (null != jschSession && !jschSession.isConnected()) {
            jschSession.disconnect();
        }
    }
}
