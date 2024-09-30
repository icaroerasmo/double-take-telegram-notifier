package com.icaroerasmo.config;

import com.icaroerasmo.listeners.AbstractListener;
import com.icaroerasmo.listeners.CamListener;
import com.icaroerasmo.listeners.MatchListener;
import com.icaroerasmo.properties.ListenersProperties;
import com.icaroerasmo.properties.MqttProperties;
import com.icaroerasmo.utils.MqttUtil;
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

import java.util.List;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class MqttConfig {

    private final MqttUtil mqttUtil;
    private final MqttProperties mqttProperties;
    private final ApplicationContext applicationContext;
    private final ListenersProperties listenersProperties;

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
                addGenericArgumentValue(mqttUtil.matchesCallback());
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
                addGenericArgumentValue(mqttUtil.camCallback());
        registry.registerBeanDefinition(beanName, bd);
    }
}
