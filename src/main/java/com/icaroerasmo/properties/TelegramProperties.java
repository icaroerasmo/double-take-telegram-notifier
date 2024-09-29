package com.icaroerasmo.properties;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Log4j2
@Configuration
@ConfigurationProperties(prefix = "telegram")
public class TelegramProperties {
    // -1000000000000
    private String chatId;
    // 7556187858:AAFAe98-yuof8daJYptVZVVV4MzV-w7WgMV5
    private String botToken;
}
