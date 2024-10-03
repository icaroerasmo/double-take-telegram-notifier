package com.icaroerasmo.listeners;

import com.google.gson.Gson;
import com.icaroerasmo.enums.MatchTypeEnum;
import com.icaroerasmo.enums.QueueType;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class CamListener extends AbstractListener {

    private IMqttMessageListener listener = (topic, msg) -> {

        byte[] payload = msg.getPayload();

        Map<String, Object> data = new Gson().fromJson(
                new String(payload, StandardCharsets.UTF_8), Map.class);

        List<Map<String, Object>> matches = (List) data.get("matches");

        Map<String, Object> detected = new HashMap<>();

        for(Map<String, Object> match : matches) {

            String type = (String) match.get("type");

            if(MatchTypeEnum.SNAPSHOT.equals(
                    MatchTypeEnum.valueOf(type.toUpperCase()))) {
                continue;
            }

            String name = (String) match.get("name");
            final String base64Image = (String) match.get("base64");

            Optional<String> key = detected.keySet().stream().
                    filter(person -> detected.get(person).
                            equals(base64Image)).findFirst();

            if(key.isPresent()) {
                final String oldName = key.get();
                detected.remove(oldName);
                name = name+", "+oldName;
            }

            detected.put(name, base64Image);
        }

        var callback = getCallback();

        if(callback != null) {
            callback.accept(getName(), detected);
        }
    };

    public CamListener(IMqttClient client, QueueType queueType, String name, BiConsumer<String, Map<String, Object>> callback) {
        super(client, queueType, name, callback);
    }

    @Override
    public void listen() {
        super.listen(listener);
    }
}
