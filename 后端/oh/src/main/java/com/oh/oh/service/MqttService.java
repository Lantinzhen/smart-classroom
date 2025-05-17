package com.oh.oh.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class MqttService {

    @Autowired
    private MqttPahoMessageHandler mqttOutbound;

    public void sendMessage(String message) {
        Message<String> mqttMessage = MessageBuilder.withPayload(message).build();
        mqttOutbound.handleMessage(mqttMessage);
    }
}