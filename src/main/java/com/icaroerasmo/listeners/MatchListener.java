package com.icaroerasmo.listeners;

import com.google.gson.Gson;
import com.pengrad.telegrambot.request.SendPhoto;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MatchListener extends AbstractListener {
    private String name;
    private IMqttMessageListener listener = (topic, msg) -> {
        byte[] payload = msg.getPayload();
        Map<String, Object> data = new Gson().fromJson(new String(payload, StandardCharsets.UTF_8), Map.class);
        String camera = (String) data.get("camera");
        String base64Image = (String) ((Map)data.get("unknown")).get("base64");

        Map<String, Object> detected = new HashMap<>();
        detected.put(name, base64Image);

        var callback = getCallback();

        if(callback != null) {
            callback.accept(camera, detected);
        }
    };

    public MatchListener(IMqttClient client, String name, BiConsumer<String, Map<String, Object>> callback) {
        super(client, "double-take/matches/%s".formatted(name), callback);
        this.name = name;
    }

    @Override
    public void listen() {
        super.listen(listener);
    }
}
