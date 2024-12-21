package uml.tech.worksupport.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 消息发送工具
 *
 * @author SunYu
 */
@Component
public class MessageUtil {
    private final Log log = LogFactory.get();
    private final String clientId = this.getClass().getSimpleName() + "_" + IdUtil.getSnowflakeNextId();

    @Value("${mqtt.broker}")
    private String broker;
    @Value("${mqtt.username}")
    private String username;
    @Value("${mqtt.password}")
    private String password;

    private IMqttClient client;

    @Resource
    private MqttClientUtil mqttClientUtil;

    @PostConstruct
    private void init() {
        client = mqttClientUtil.getAutoReconnectClient(clientId);
    }

    /**
     * 发布消息到指定主题
     *
     * @param topic   主题
     * @param message 消息内容
     */
    public void publish(String topic, MqttMessage message) {
        try {
            client.publish(topic, message);
        } catch (MqttException e) {
            log.error(e);
        }
    }
}
