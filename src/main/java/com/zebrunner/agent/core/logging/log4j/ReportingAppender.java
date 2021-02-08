package com.zebrunner.agent.core.logging.log4j;

import com.zebrunner.agent.core.logging.Log;
import com.zebrunner.agent.core.registrar.LogsBuffer;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.function.Function;

public class ReportingAppender extends AppenderSkeleton {

    private final static Function<LoggingEvent, Log> CONVERTER = e -> Log.builder()
                                                                         .message(e.getRenderedMessage())
                                                                         .level(e.getLevel().toString())
                                                                         .timestamp(e.getTimeStamp())
                                                                         .build();

    private static volatile LogsBuffer<LoggingEvent> logsBuffer;

    @Override
    protected void append(LoggingEvent event) {
        getBuffer().put(event);
    }

    private static LogsBuffer<LoggingEvent> getBuffer() {
        if (logsBuffer == null) {
            synchronized (ReportingAppender.class) {
                if (logsBuffer == null) {
                    logsBuffer = LogsBuffer.create(CONVERTER);
                }
            }
        }
        return logsBuffer;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

}
