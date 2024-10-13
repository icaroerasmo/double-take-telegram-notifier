package com.icaroerasmo.listeners;

import com.icaroerasmo.enums.QueueType;
import lombok.Getter;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Map;
import java.util.function.BiConsumer;

public abstract class AbstractListener {

    private QueueType queueType;
    private IMqttClient client;

    @Getter
    private String name;

    @Getter
    private String topic;

    @Getter
    private BiConsumer<String, Map<String, Object>> callback;

    public AbstractListener(IMqttClient client, QueueType queueType, String name, BiConsumer<String, Map<String, Object>> callback) {
        this.client = client;
        this.queueType = queueType;
        this.name = name;
        this.callback = callback;
    }

    public abstract void listen();

    protected void listen(IMqttMessageListener listener) {
        try {
            this.topic = "double-take/%s/%s".formatted(queueType.name().toLowerCase(), name);
            this.client.subscribe(topic, listener);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}
