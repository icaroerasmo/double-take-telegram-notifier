package com.icaroerasmo.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "telegram")
public class TelegramProperties implements ConfigYaml {
    // -1000000000000
    private String chatId;
    // 7556187858:AAFAe98-yuof8daJYptVZVVV4MzV-w7WgMV5
    private String botToken;
}
