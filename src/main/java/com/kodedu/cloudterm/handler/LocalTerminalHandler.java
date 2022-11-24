package com.kodedu.cloudterm.handler;

import com.kodedu.cloudterm.helper.IOHelper;
import com.kodedu.cloudterm.helper.ThreadHelper;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class LocalTerminalHandler extends AbstractTerminalHandler {

    private String[] termCommand;

    private PtyProcess process;
    private Integer columns = 20;
    private Integer rows = 10;
    private BufferedReader inputReader;
    private BufferedReader errorReader;
    private BufferedWriter processOutputWriter;

    @SneakyThrows
    @Override
    public void onTerminalReady() {
        if (isReady()) return;
        String userHome = System.getProperty("user.home");
        Path dataDir = Paths.get(userHome).resolve(".terminalfx");
        IOHelper.copyLibPty(dataDir);

        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("win")) {
            this.termCommand = "cmd.exe".split("\\s+");
        } else {
            this.termCommand = "/bin/sh -i".split("\\s+");
        }

        Map<String, String> envs = new HashMap<>(System.getenv());
        envs.put("TERM", "xterm");

        System.setProperty("PTY_LIB_FOLDER", dataDir.resolve("libpty").toString());

        this.process = PtyProcess.exec(termCommand, envs, userHome);

        process.setWinSize(new WinSize(columns, rows));
        this.inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        this.processOutputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        ThreadHelper.start(() -> {
            printReader(inputReader);
        });

        ThreadHelper.start(() -> {
            printReader(errorReader);
        });
        setReady(true);
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

    @SneakyThrows
    @Override
    public void onCommand(String command) {

        getCommandQueue().put(command);

        ThreadHelper.start(() -> {
            try {
                processOutputWriter.write(getCommandQueue().poll());
                processOutputWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onTerminalResize(String columns, String rows) {
        if (Objects.nonNull(columns) && Objects.nonNull(rows)) {
            this.columns = Integer.valueOf(columns);
            this.rows = Integer.valueOf(rows);

            if (Objects.nonNull(process)) {
                process.setWinSize(new WinSize(this.columns, this.rows));
            }

        }
    }

    @Override
    public void onTerminalClose() {
        if (null != process && process.isAlive()) {
            process.destroy();
        }
    }
}
