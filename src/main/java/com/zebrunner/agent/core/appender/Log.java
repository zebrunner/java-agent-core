package com.zebrunner.agent.core.appender;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Log {

    private String testId;
    private String message;
    private String level;
    private long timestamp;

    @Builder
    public Log(String message, String level, long timestamp) {
        this.message = message;
        this.level = level;
        this.timestamp = timestamp;
    }

}
