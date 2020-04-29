package com.zebrunner.agent.core.appender.log4j;

import com.zebrunner.agent.core.appender.Log;
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

    private final LogsBuffer<LoggingEvent> logsBuffer;

    public ReportingAppender() {
        this.logsBuffer = new LogsBuffer<>(CONVERTER);
    }

    @Override
    protected void append(LoggingEvent event) {
        logsBuffer.put(event);
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

}
