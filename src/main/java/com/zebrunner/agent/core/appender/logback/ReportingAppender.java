package com.zebrunner.agent.core.appender.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.zebrunner.agent.core.appender.Log;
import com.zebrunner.agent.core.registrar.LogsBuffer;

import java.util.function.Function;

public class ReportingAppender extends AppenderBase<ILoggingEvent> {

    private static final Function<ILoggingEvent, Log> CONVERTER = event -> Log.builder()
                                                                              .message(event.getFormattedMessage())
                                                                              .level(event.getLevel().toString())
                                                                              .timestamp(event.getTimeStamp())
                                                                              .build();

    private final LogsBuffer<ILoggingEvent> logsBuffer;

    public ReportingAppender() {
        this.logsBuffer = new LogsBuffer<>(CONVERTER);
    }

    @Override
    protected void append(ILoggingEvent event) {
        logsBuffer.put(event);
    }

}
