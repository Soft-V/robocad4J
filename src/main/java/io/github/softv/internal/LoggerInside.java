package io.github.softv.internal;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class LoggerInside {
    protected final org.apache.logging.log4j.Logger logger;

    public LoggerInside(String path) {
        ConfigurationBuilder<BuiltConfiguration> builder
                = ConfigurationBuilderFactory.newConfigurationBuilder();
        AppenderComponentBuilder file
                = builder.newAppender("log", "File");
        file.addAttribute("fileName", path);
        file.addAttribute("append", false);
        LayoutComponentBuilder standard
                = builder.newLayout("PatternLayout");
        standard.addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable");
        file.add(standard);
        builder.add(file);

        RootLoggerComponentBuilder rootLogger
                = builder.newRootLogger(Level.ALL);
        rootLogger.add(builder.newAppenderRef("log"));

        builder.add(rootLogger);
        LoggerContext c = Configurator.initialize(builder.build());

        this.logger = c.getLogger("root");
    }

    public synchronized void log(String s) {
        this.logger.log(Level.INFO, s);
    }
}
