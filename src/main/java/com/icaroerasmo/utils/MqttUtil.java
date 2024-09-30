package com.icaroerasmo.utils;

import com.icaroerasmo.enums.QueueType;
import com.icaroerasmo.properties.ListenersProperties;
import com.icaroerasmo.properties.TelegramProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendPhoto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

@Log4j2
@Component
@RequiredArgsConstructor
public class MqttUtil {

    private static final String PARAMETER_WILDCARD_PATTERN = "\\{\\{\\s*%s\\s*\\}\\}";
    private static final String DEFAULT_MESSAGE = "{{camName}}: {{personName}} detected";

    private final TelegramBot telegramBot;
    private final TelegramProperties telegramProperties;
    private final ListenersProperties listenersProperties;

    public BiConsumer<String, Map<String, Object>> matchesCallback() {
        return (camName, detectionData) -> {
            for(String name : detectionData.keySet()) {
                String base64Image = (String) detectionData.get(name);
                byte[] image = Base64.getDecoder().decode(base64Image);
                SendPhoto request = new SendPhoto(telegramProperties.getChatId(), image);

                Map<String, String> parameters = new HashMap<>();
                parameters.put("camName", camName);
                parameters.put("personName", name);

                final String mainMessage = resolveMessage(QueueType.MATCHES, parameters);

                log.warn("{}. Image: {}", mainMessage, base64Image);

                request.caption(mainMessage);
                telegramBot.execute(request);
            }
        };
    }

    public  BiConsumer<String, Map<String, Object>> camCallback() {
        return (camName, detectionData) -> {
            for(String name : detectionData.keySet()) {

                String base64Image = (String) detectionData.get(name);
                byte[] image = Base64.getDecoder().decode(base64Image);
                SendPhoto request = new SendPhoto(telegramProperties.getChatId(), image);

                Map<String, String> parameters = new HashMap<>();
                parameters.put("camName", camName);
                parameters.put("personName", name);

                final String mainMessage = resolveMessage(QueueType.CAMERAS, parameters);

                log.info("{}. Image: {}", mainMessage, base64Image);

                request.caption(mainMessage);
                telegramBot.execute(request);
            }
        };
    }

    private String resolveMessage(QueueType type, Map<String, String> parameters) {

        String messageTemplate = null;

        String queueName = null;

        ListenersProperties.QueueTypeProperties properties = null;

        switch (type) {
            case CAMERAS -> {
                queueName = parameters.get("camName");
                properties = listenersProperties.getCameras();
            }
            case MATCHES -> {
                queueName = parameters.get("personName");
                properties = listenersProperties.getMatches();
            }
        }

        final String finalQueueName = queueName;

        Optional<ListenersProperties.QueueProperties> queue =
                properties.getQueues().stream().
                        filter(q -> finalQueueName.equals(q.getName()))
                        .findFirst();

        if(queue.isPresent() && queue.get().getMessage() != null) {
            messageTemplate = queue.get().getMessage();
        } else {
            messageTemplate = DEFAULT_MESSAGE;
        }

        for(String parameter : parameters.keySet()) {
            messageTemplate = messageTemplate.
                    replaceAll(PARAMETER_WILDCARD_PATTERN.
                            formatted(parameter), parameters.get(parameter));
        }

        return messageTemplate;
    }
}
