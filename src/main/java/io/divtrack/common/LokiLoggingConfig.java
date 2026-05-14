package io.divtrack.common;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.LoggerContext;
import com.github.loki4j.logback.AbstractHttpSender;
import com.github.loki4j.logback.AbstractLoki4jEncoder;
import com.github.loki4j.logback.JavaHttpSender;
import com.github.loki4j.logback.JsonEncoder;
import com.github.loki4j.logback.Loki4jAppender;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "app.observability")
@Setter
public class LokiLoggingConfig {

    private String lokiUrl = "";
    private String lokiUser = "";
    private String lokiToken = "";

    @PostConstruct
    void init() {
        if (lokiUrl.isBlank() || lokiToken.isBlank()) {
            log.info("Loki not configured — skipping Loki appender");
            return;
        }

        var context = (LoggerContext) LoggerFactory.getILoggerFactory();
        var root = context.getLogger(Logger.ROOT_LOGGER_NAME);

        var auth = new AbstractHttpSender.BasicAuth();
        auth.setUsername(lokiUser);
        auth.setPassword(lokiToken);

        var sender = new JavaHttpSender();
        sender.setUrl(lokiUrl);
        sender.setAuth(auth);
        sender.setConnectionTimeoutMs(5000);
        sender.setRequestTimeoutMs(5000);

        var label = new AbstractLoki4jEncoder.LabelCfg();
        label.setPattern("app=divtrack,host=" + System.getenv().getOrDefault("HOSTNAME", "unknown"));

        var encoder = new JsonEncoder();
        encoder.setContext(context);
        encoder.setLabel(label);
        encoder.start();

        var appender = new Loki4jAppender();
        appender.setContext(context);
        appender.setHttp(sender);
        appender.setFormat(encoder);
        appender.setBatchMaxItems(100);
        appender.setBatchTimeoutMs(5000);

        var filter = new ThresholdFilter();
        filter.setLevel("INFO");
        filter.start();
        appender.addFilter(filter);

        appender.start();
        root.addAppender(appender);

        log.info("Loki appender configured: {}", lokiUrl);
    }
}
