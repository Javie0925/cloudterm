package com.kodedu.cloudterm.handler;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
public enum TerminalHandlerFactory {

    DEFAULT(LocalTerminalHandler.class),
    JSCH(JschTerminalHandler.class),
    CLOUD(CloudTerminalHandler.class),

    ;

    private Class<? extends TerminalHandler> handler;

    @SneakyThrows
    public TerminalHandler getHandler() {
        return this.handler.getConstructor().newInstance();
    }

    public static TerminalHandler getHandler(String type) {
        TerminalHandlerFactory target =
                valueOf(
                        Optional.ofNullable(type)
                                .map(t -> t.toUpperCase(Locale.ROOT))
                                .filter(t -> Arrays.stream(values()).anyMatch(t1 -> t1.name().equals(t)))
                                .orElse("DEFAULT")
                );
        if (Objects.nonNull(target)) {
            return target.getHandler();
        } else {
            return DEFAULT.getHandler();
        }
    }
}
