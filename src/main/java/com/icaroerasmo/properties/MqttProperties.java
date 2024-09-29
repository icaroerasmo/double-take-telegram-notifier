package com.icaroerasmo.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Log4j2
@Configuration
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {
    // localhost
    private String host = "localhost";
    // 1883
    private String port = "1883";
    // TCP or WEBSOCKETS
    private ProtocolEnum protocol = ProtocolEnum.TCP;
    // mqtt_user
    private String username = "mqtt_user";
    // password
    private String password = "password";
    // true
    private Boolean automaticReconnect = true;
    //true
    private Boolean cleanSession = true;
    // 10
    private Integer connectionTimeout = 10;

    @Getter
    @AllArgsConstructor
    public enum ProtocolEnum {
        TCP("tcp"), WEBSOCKETS("ws");
        private String protocolShort;
    }
}
