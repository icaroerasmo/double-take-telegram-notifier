package com.icaroerasmo.config;

import com.icaroerasmo.enums.QueueType;
import com.icaroerasmo.listeners.AbstractListener;
import com.icaroerasmo.listeners.CamListener;
import com.icaroerasmo.listeners.MatchListener;
import com.icaroerasmo.properties.ListenersProperties;
import com.icaroerasmo.properties.MqttProperties;
import com.icaroerasmo.properties.TelegramProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendPhoto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.*;
import java.util.function.BiConsumer;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class Beans {

    private static final String PARAMETER_WILDCARD_PATTERN = "\\{\\{\\s*%s\\s*\\}\\}";
    private static final String DEFAULT_MESSAGE = "{{camName}}: {{personName}} detected";

    private final ApplicationContext applicationContext;
    private final MqttProperties mqttProperties;
    private final TelegramProperties telegramProperties;
    private final ListenersProperties listenersProperties;

    @Bean
    public TelegramBot bot() {
        return new TelegramBot(telegramProperties.getBotToken());
    }

    @Bean
    public IMqttClient mqttClient() throws MqttException {

        final String clientId = "mqttListener";
        final String connectionString = "%s://%s:%s".
                formatted(mqttProperties.getProtocol().getProtocolShort(),
                        mqttProperties.getHost(), mqttProperties.getPort());


        IMqttClient client = new MqttClient(connectionString, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(mqttProperties.getUsername());
        options.setPassword(mqttProperties.getPassword().toCharArray());
        options.setAutomaticReconnect(mqttProperties.getAutomaticReconnect());
        options.setCleanSession(mqttProperties.getCleanSession());
        options.setConnectionTimeout(mqttProperties.getConnectionTimeout());
        client.connect(options);

        return client;
    }

    @Bean
    @DependsOn("registerListeners")
    public Void listen(List<AbstractListener> listeners) {
        listeners.forEach(listener -> listener.listen());
        return null;
    }

    @Bean
    public Void registerListeners() {
        listenersProperties.getCameras().getQueues().forEach(cam -> {
            try {
                registerCams(cam.getName());
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        });

        listenersProperties.getMatches().getQueues().forEach(person -> {
            try {
                registerMatches(person.getName());
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        });

        return null;
    }

    private void registerMatches(String personName) throws MqttException {

        AutowireCapableBeanFactory factory =
                applicationContext.getAutowireCapableBeanFactory();
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory;

        BeanDefinition bd = new RootBeanDefinition(MatchListener.class);
        bd.getConstructorArgumentValues().
                addGenericArgumentValue(mqttClient());
        bd.getConstructorArgumentValues()
                .addGenericArgumentValue(personName);
        bd.getConstructorArgumentValues().
                addGenericArgumentValue(matchesCallback());
        registry.registerBeanDefinition(personName, bd);
    }

    private void registerCams(String beanName) throws MqttException {

        AutowireCapableBeanFactory factory =
                applicationContext.getAutowireCapableBeanFactory();
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory;

        BeanDefinition bd = new RootBeanDefinition(CamListener.class);
        bd.getConstructorArgumentValues().
                addGenericArgumentValue(mqttClient());
        bd.getConstructorArgumentValues()
                .addGenericArgumentValue(beanName);
        bd.getConstructorArgumentValues().
                addGenericArgumentValue(camCallback());
        registry.registerBeanDefinition(beanName, bd);
    }

    private BiConsumer<String, Map<String, Object>> matchesCallback() {
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
                bot().execute(request);
            }
        };
    }

    private  BiConsumer<String, Map<String, Object>> camCallback() {
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
                bot().execute(request);
            }
        };
    }

    private String resolveMessage(QueueType type, Map<String, String> parameters) {

        String messageTemplate = null;

        final String personName = parameters.get("personName");

        ListenersProperties.QueueTypeProperties properties = null;

        switch (type) {
            case CAMERAS -> properties = listenersProperties.getCameras();
            case MATCHES -> properties = listenersProperties.getMatches();
        }

        Optional<ListenersProperties.QueueProperties> queue =
                properties.getQueues().stream().
                filter(q -> personName.equals(q.getName()))
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
