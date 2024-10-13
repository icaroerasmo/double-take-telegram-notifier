package com.icaroerasmo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.*;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class DtTMqttCallBack implements MqttCallbackExtended {

    private final IMqttClient client;
    private final List<String> topics;

    @Override
    public void connectComplete(boolean b, String s) {
        topics.forEach(t -> {
            try {
                this.client.subscribe(t, 0);
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        log.info(String.format("[%s] %s", s, new String(mqttMessage.getPayload())));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
