package com.oh.oh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oh.oh.dto.MqttDto;
import com.oh.oh.service.MqttService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class MqttController {

    @Autowired
    private MqttService mqttService;
    @PostMapping  ("/send")
    public String sendMessage(@RequestBody String message) {
        mqttService.sendMessage(message);
        log.info("前端向MQTT服务器发送了新的消息: {}",message);
        return "Message sent: " + message;
    }
}
