package com.oh.oh.mqtt.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oh.oh.constant.SignalConstant;
import com.oh.oh.dto.MqttDto;
import com.oh.oh.entity.OhosRecord;
import com.oh.oh.service.OhosRecordService;
import com.oh.oh.service.WebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Configuration
@Slf4j
public class MqttConfig {

    @Value("${spring.mqtt.url}")
    private  String mqttUrl;
    @Value("${spring.mqtt.client.id}")
    private  String clientId;
    @Value("${spring.mqtt.username}")
    private  String username;
    @Value("${spring.mqtt.password}")
    private  String password;
    @Value("${spring.mqtt.default-topic}")
    private  String defaultTopic;

    @Autowired
    WebSocketHandler webSocketHandler;

    public static final String subTopic = "pubTopic";//订阅的主题

    public static final String pubTopic = "subTopic";  //发布的主题

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    OhosRecordService recordService;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{mqttUrl});
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        factory.setConnectionOptions(options);
        options.setAutomaticReconnect(true);  // **启用自动重连**
        options.setConnectionTimeout(10); // 连接超时时间（秒）
        options.setKeepAliveInterval(20); // 心跳包间隔（秒）
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MqttPahoMessageHandler mqttPahoMessageHandler() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(clientId + "-outbound", mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(pubTopic);  // 确保默认主题已设置
        return messageHandler;
    }

    @Bean
    //订阅主题
    public MqttPahoMessageDrivenChannelAdapter inbound(MqttPahoClientFactory clientFactory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId + "-inbound", clientFactory, subTopic);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    //TODO  这里是订阅回调，这里需要使用websocket把数据返回给app
    public MessageHandler handler() {
        return message -> {
            String payload = (String) message.getPayload();
            log.info("Received message: {} ", payload);

            try {
                // 解析 MQTT JSON 数据
                MqttDto mqttDto = objectMapper.readValue(payload, MqttDto.class);
                // 根据信号 ID 获取对应的 SignalEnum
                int signal = mqttDto.getSignal();
//                    signal = SignalConstant.OTHER;
                OhosRecord record = new OhosRecord();
                // 使用 switch 处理不同信号
                switch (signal) {
                    case SignalConstant.OPEN_DOOR:
                        log.info("处理开门请求");
                        break;
                    case SignalConstant.CLOSE_DOOR:
                        log.info("处理关门请求");
                        break;
                    case SignalConstant.HUMAN_COME:
                        log.info("处理人来了请求");
                        break;
                    case SignalConstant.HUMAN_LEAVE:
                        log.info("处理人走了请求");
                        break;
                    case SignalConstant.LIGHT_ON:
                        log.info("处理天黑亮红灯请求");
                        break;
                    case SignalConstant.LIGHT_OFF:
                        log.info("处理天亮请求");
                        break;

                    case SignalConstant.GET_TEMPERATURE:
                        log.info("处理获取温度请求");
                        record.setOhosTemperature(mqttDto.getValue());
                        recordService.saveRecord(record);
                        break;
                    case SignalConstant.GET_HUMIDITY:
                        log.info("处理发送温度请求");
                        record.setOhosHumidity(mqttDto.getValue());
                        recordService.saveRecord(record);
                        break;
                    default:
                        log.warn("未处理的信号: {}", signal);
                        break;
                }

                // 发送 WebSocket 消息到前端
                WebSocketSession session = webSocketHandler.getSession("test");
                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage(payload));
                    log.info("Sent message: {} to APP", mqttDto);
                } else {
                    log.warn("WebSocket session 不存在或已关闭");
                }

            } catch (IOException e) {
                log.error("解析或发送消息失败", e);
                throw new RuntimeException(e);
            }
        };
    }


    @Bean
    @ServiceActivator(inputChannel = "mqttOutputChannel")
    public MessageHandler mqttOutbound(MqttPahoClientFactory clientFactory) {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(clientId + "-outbound", clientFactory);
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(pubTopic);
        return messageHandler;
    }
}

