package com.icaroerasmo.properties;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Log4j2
@Configuration
@ConfigurationProperties(prefix = "listeners")
public class ListenersProperties {
    // matches queues
    private QueueTypeProperties matches;
    // cameras queues
    private QueueTypeProperties cameras;

    @Data
    public static class QueueTypeProperties {
        private List<QueueProperties> queues;
    }

    @Data
    public static class QueueProperties {
        // Jhon Doe
        private String name;
        // {{camName}}: {{personName}} detected
        private String message;
    }
}
