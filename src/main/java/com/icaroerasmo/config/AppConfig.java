package com.icaroerasmo.config;

import com.icaroerasmo.listeners.AbstractListener;
import com.icaroerasmo.properties.TelegramProperties;
import com.pengrad.telegrambot.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.*;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final TelegramProperties telegramProperties;

    @Bean
    public TelegramBot bot() {
        return new TelegramBot(telegramProperties.getBotToken());
    }
}
