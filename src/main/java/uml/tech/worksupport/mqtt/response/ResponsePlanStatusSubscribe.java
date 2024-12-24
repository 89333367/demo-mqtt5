package uml.tech.worksupport.mqtt.response;

import cn.hutool.core.util.IdUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import uml.tech.worksupport.common.MqttSubscribeCallback;
import uml.tech.worksupport.util.MessageUtil;
import uml.tech.worksupport.util.MqttClientUtil;

import javax.annotation.Resource;

@Component
public class ResponsePlanStatusSubscribe implements ApplicationRunner {
    private final Log log = LogFactory.get();
    private final String clientId = this.getClass().getSimpleName() + "_" + IdUtil.getSnowflakeNextId();

    private String did = "did1";

    @Resource
    private MessageUtil messageUtil;
    @Resource
    private MqttClientUtil mqttClientUtil;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        IMqttClient client = mqttClientUtil.getClient(clientId);
        client.setCallback(new MqttSubscribeCallback(client, "response/plan/" + did + "/+", 1) {
            @Override
            public void doMessageArrived(String topic, MqttMessage message) {
                log.info("[收到升级计划状态] {}", message);
            }
        });
    }
}
