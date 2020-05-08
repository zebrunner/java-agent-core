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

    private static volatile LogsBuffer<ILoggingEvent> logsBuffer;

    @Override
    protected void append(ILoggingEvent event) {
        getBuffer().put(event);
    }

    private static LogsBuffer<ILoggingEvent> getBuffer() {
        if (logsBuffer == null) {
            synchronized (ReportingAppender.class) {
                if (logsBuffer == null) {
                    logsBuffer = LogsBuffer.create(CONVERTER);
                }
            }
        }
        return logsBuffer;
    }

}
