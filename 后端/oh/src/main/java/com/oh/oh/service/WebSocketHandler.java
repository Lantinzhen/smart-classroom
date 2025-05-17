package com.oh.oh.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oh.oh.dto.UserDto;
import com.oh.oh.mqtt.config.MqttConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        log.info("有用户连接");
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("收到消息：{}", message.getPayload());
        if(sessions.get("test") == null) {
            sessions.put("test", session);
            session.sendMessage(new TextMessage("连接成功"));
        }
        String payload = message.getPayload();
        //TODO这里把数据发送到mqtt
        Message<String> mqttMessage = MessageBuilder.withPayload(payload).build();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.values().remove(session);
        System.out.println("连接关闭：" + session.getId());
    }

    public  WebSocketSession getSession(String userName) {
        return sessions.get(userName);
    }

}
