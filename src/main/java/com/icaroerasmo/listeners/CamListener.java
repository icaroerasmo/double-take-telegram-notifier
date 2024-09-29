package com.icaroerasmo.listeners;

import com.google.gson.Gson;
import com.pengrad.telegrambot.request.SendPhoto;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CamListener extends AbstractListener {

    private String camName;

    private IMqttMessageListener listener = (topic, msg) -> {

        byte[] payload = msg.getPayload();

        Map<String, Object> data = new Gson().fromJson(
                new String(payload, StandardCharsets.UTF_8), Map.class);

        List<Map<String, Object>> matches = (List) data.get("matches");

        Map<String, Object> detected = new HashMap<>();

        for(Map<String, Object> match : matches) {

            final String name = (String) match.get("name");
            final String base64Image = (String) match.get("base64");

            detected.put(name, base64Image);
        }

        var callback = getCallback();

        if(callback != null) {
            callback.accept(camName, detected);
        }
    };

    public CamListener(IMqttClient client, String camName, BiConsumer<String, Map<String, Object>> callback) {
        super(client, "double-take/cameras/%s".formatted(camName), callback);
        this.camName = camName;
    }

    @Override
    public void listen() {
        super.listen(listener);
    }
}
