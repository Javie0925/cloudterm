package com.kodedu.cloudterm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import com.kodedu.cloudterm.dao.SessionListDao;
import com.kodedu.cloudterm.dao.entity.SessionEntity;
import com.kodedu.cloudterm.helper.IOHelper;
import com.kodedu.cloudterm.helper.ThreadHelper;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
@Scope("prototype")
public class TerminalService {

    @Value("${shell:#{null}}")
    private String shellStarter;

    @Resource
    private SessionListDao serverListDao;

    private boolean isReady;
    private String[] termCommand;
    private PtyProcess process;
    private Integer columns = 20;
    private Integer rows = 10;
    private BufferedReader inputReader;
    private BufferedReader errorReader;
    private BufferedWriter outputWriter;
    private WebSocketSession webSocketSession;
    private String sessionId;
    private Channel jschChannel;
    private com.jcraft.jsch.Session jschSession;

    private LinkedBlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();

    public void onTerminalInit() {

    }

    public void onTerminalReady() throws Exception {
        initializeProcess();
    }

    private void initializeProcess() throws Exception {
        if (isReady) {
            return;
        }
        if (StringUtils.hasLength(sessionId)) {
            try {
                initializeJschProcess(sessionId);
                return;
            } catch (Exception e) {
                print(e.getMessage() + System.lineSeparator());
                print("back to home ... " + System.lineSeparator());
                print(System.lineSeparator());
                print(System.lineSeparator());
                log.error("", e);
            }
        }
        String userHome = System.getProperty("user.home");
        Path dataDir = Paths.get(userHome).resolve(".terminalfx");
        IOHelper.copyLibPty(dataDir);

        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("win")) {
            this.termCommand = "cmd.exe".split("\\s+");
        } else {
            this.termCommand = "/bin/sh -i".split("\\s+");
        }

        if (Objects.nonNull(shellStarter)) {
            this.termCommand = shellStarter.split("\\s+");
        }

        Map<String, String> envs = new HashMap<>(System.getenv());
        envs.put("TERM", "xterm");

        System.setProperty("PTY_LIB_FOLDER", dataDir.resolve("libpty").toString());

        this.process = PtyProcess.exec(termCommand, envs, userHome);

        process.setWinSize(new WinSize(columns, rows));
        this.inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        this.outputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        ThreadHelper.start(() -> {
            printReader(inputReader);
        });

        ThreadHelper.start(() -> {
            printReader(errorReader);
        });
        this.isReady = true;

    }

    private void initializeJschProcess(String sessionId) throws JSchException, IOException {
        SessionEntity sessionEntity = serverListDao.findById(sessionId);
        Assert.notNull(sessionEntity, "session is null!");
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
                log.info("jcsh show message:{}", s);
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

        this.isReady = true;
    }

    public void print(String text) throws IOException {

        Map<String, String> map = new HashMap<>();
        map.put("type", "TERMINAL_PRINT");
        map.put("text", text);

        String message = new ObjectMapper().writeValueAsString(map);

        webSocketSession.sendMessage(new TextMessage(message));

    }

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

    public void onCommand(String command) throws InterruptedException {

        if (Objects.isNull(command)) {
            return;
        }

        commandQueue.put(command);
        ThreadHelper.start(() -> {
            try {
                outputWriter.write(commandQueue.poll());
                outputWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public void onTerminalResize(String columns, String rows) {
        if (Objects.nonNull(columns) && Objects.nonNull(rows)) {
            this.columns = Integer.valueOf(columns);
            this.rows = Integer.valueOf(rows);

            if (Objects.nonNull(process)) {
                process.setWinSize(new WinSize(this.columns, this.rows));
            }

        }
    }

    public void onTerminalClose() {
        if (null != process && process.isAlive()) {
            process.destroy();
        }
        if (null != jschChannel && !jschChannel.isClosed()) {
            jschChannel.disconnect();
        }
        if (null != jschSession && !jschSession.isConnected()) {
            jschSession.disconnect();
        }
    }

    public void setWebSocketSession(WebSocketSession webSocketSession, String sessionId) {
        this.webSocketSession = webSocketSession;
        if ("undefined".equals(sessionId)) {
            sessionId = null;
        }
        this.sessionId = sessionId;
    }

    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }
}
