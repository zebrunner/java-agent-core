package com.zebrunner.agent.core.appender.log4j2;

import com.zebrunner.agent.core.appender.Log;
import com.zebrunner.agent.core.registrar.LogsBuffer;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.util.function.Function;

@Plugin(
    name = "ReportingAppender",
    category = Core.CATEGORY_NAME,
    elementType = Appender.ELEMENT_TYPE
)
public final class ReportingAppender extends AbstractAppender {

    private static final Function<LogEvent, Log> CONVERTER = e -> Log.builder()
                                                                     .message(e.getMessage().getFormattedMessage())
                                                                     .level(e.getLevel().toString())
                                                                     .timestamp(e.getTimeMillis())
                                                                     .build();

    private static volatile LogsBuffer<LogEvent> logsBuffer;

    protected ReportingAppender(String name,
                                Filter filter,
                                Layout<? extends Serializable> layout,
                                boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
    }

    @PluginFactory
    public static ReportingAppender create(@PluginAttribute("name") String name,
                                           @PluginElement("Layout") Layout<? extends Serializable> layout,
                                           @PluginElement("Filter") Filter filter) {

        if (name == null) {
            LOGGER.error("No name provided for TestLoggerAppender");
            return null;
        }

        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        return new ReportingAppender(name, filter, layout, true);
    }

    @Override
    public void append(LogEvent event) {
        getBuffer().put(event);
    }

    private static LogsBuffer<LogEvent> getBuffer() {
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
