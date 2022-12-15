package net.nonswag.tnl.mappings.v1_19_R3.api.logger;

import lombok.Getter;
import net.nonswag.tnl.listener.api.logger.LogManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.filter.Filterable;

import javax.annotation.Nonnull;

public class NMSLogManager extends LogManager {

    @Override
    public void initialize() {
        try {
            ((Logger) org.apache.logging.log4j.LogManager.getRootLogger()).getAppenders().values().forEach(appender -> {
                if (!(appender instanceof Filterable filter)) return;
                filter.addFilter(LogRewriter.getInstance());
                filter.addFilter(Filter.getInstance());
            });
        } catch (Throwable t) {
            net.nonswag.core.api.logger.Logger.error.println("Failed to override loggers", t);
        }
    }

    private static final class LogRewriter extends AbstractFilter {

        @Getter
        @Nonnull
        private static final LogRewriter instance = new LogRewriter();

        @Override
        public Result filter(@Nonnull LogEvent event) {
            String message = event.getMessage().getFormattedMessage();
            net.nonswag.core.api.logger.Logger logger;
            if (event.getLevel().equals(Level.OFF)) return Result.DENY;
            else if (event.getLevel().equals(Level.FATAL)) logger = net.nonswag.core.api.logger.Logger.error;
            else if (event.getLevel().equals(Level.ERROR)) logger = net.nonswag.core.api.logger.Logger.error;
            else if (event.getLevel().equals(Level.WARN)) logger = net.nonswag.core.api.logger.Logger.warn;
            else if (event.getLevel().equals(Level.DEBUG)) logger = net.nonswag.core.api.logger.Logger.debug;
            else logger = net.nonswag.core.api.logger.Logger.info;
            logger.println(message);
            return Result.DENY;
        }
    }

    public static final class Filter extends AbstractFilter {

        @Getter
        @Nonnull
        private static final Filter instance = new Filter();

        private Filter() {
        }

        @Override
        public Result filter(@Nonnull LogEvent event) {
            String message = event.getMessage().getFormattedMessage();
            return LOG_4_SHELL.matcher(message).find() ? Result.DENY : super.filter(event);
        }
    }
}
