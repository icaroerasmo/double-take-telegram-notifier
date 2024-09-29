package com.icaroerasmo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MqttListenerApplication {
    public static void main(String[] args) {
        System.setProperty("spring.config.name", "config");
        SpringApplication.run(MqttListenerApplication.class, args);
    }
}
