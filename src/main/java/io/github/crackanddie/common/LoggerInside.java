package io.github.crackanddie.common;

import io.github.crackanddie.shufflecad.InfoHolder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class LoggerInside {
    private Logger logger;

    public LoggerInside(){
        PatternLayout.Builder builder = PatternLayout.newBuilder();
        builder.withPattern("%d | %t | %p | %m%n");

        var appBuilder = FileAppender.newBuilder();
        appBuilder.setLayout(builder.build());
        appBuilder.setName("fileAppender");
        appBuilder.withAppend(true);
        if (InfoHolder.onRealRobot)
            appBuilder.withFileName("/home/pi/robocad/logs/cad_main.log");
        else
            appBuilder.withFileName("./cad_main.log");
        appBuilder.setFilter(ThresholdFilter.createFilter(Level.ALL, Filter.Result.ACCEPT, Filter.Result.DENY));

        var conf = new DefaultConfiguration();
        conf.addAppender(appBuilder.build());
        Configurator.reconfigure(conf);

        logger = LogManager.getLogger();
    }

    public synchronized void writeMainLog(String s){
        logger.info(s);
    }
}
