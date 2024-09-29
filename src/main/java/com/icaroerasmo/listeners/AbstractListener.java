package com.icaroerasmo.listeners;

import lombok.Getter;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Map;
import java.util.function.BiConsumer;

public abstract class AbstractListener {

    private IMqttClient client;
    private String topic;

    @Getter
    private BiConsumer<String, Map<String, Object>> callback;

    public AbstractListener(IMqttClient client, String topic, BiConsumer<String, Map<String, Object>> callback) {
        this.client = client;
        this.topic = topic;
        this.callback = callback;
    }

    public abstract void listen();

    protected void listen(IMqttMessageListener listener) {
        try {
            this.client.subscribe(topic, listener);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}
