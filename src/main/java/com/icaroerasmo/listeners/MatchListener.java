package com.icaroerasmo.listeners;

import com.google.gson.Gson;
import com.icaroerasmo.enums.QueueType;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MatchListener extends AbstractListener {

    private IMqttMessageListener listener = (topic, msg) -> {
        byte[] payload = msg.getPayload();
        Map<String, Object> data = new Gson().fromJson(new String(payload, StandardCharsets.UTF_8), Map.class);
        String camera = (String) data.get("camera");
        String base64Image = (String) ((Map)data.get("unknown")).get("base64");

        Map<String, Object> detected = new HashMap<>();
        detected.put(getName(), base64Image);

        var callback = getCallback();

        if(callback != null) {
            callback.accept(camera, detected);
        }
    };

    public MatchListener(IMqttClient client, QueueType queueType, String name, BiConsumer<String, Map<String, Object>> callback) {
        super(client, queueType, name, callback);
    }

    @Override
    public void listen() {
        super.listen(listener);
    }
}
