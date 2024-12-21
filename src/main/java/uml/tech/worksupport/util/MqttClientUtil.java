package uml.tech.worksupport.util;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttClientPersistence;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uml.tech.worksupport.common.MqttCallbackImpl;

/**
 * 创建mqtt链接工具类
 *
 * @author SunYu
 */
@Component
public class MqttClientUtil {
    private final Log log = LogFactory.get();

    @Value("${mqtt.broker}")
    private String broker;
    @Value("${mqtt.username}")
    private String username;
    @Value("${mqtt.password}")
    private String password;
    @Value("${mqtt.keep-alive}")
    private Integer keepAliveInterval;
    @Value("${mqtt.connection-timeout}")
    private Integer connectionTimeout;
    @Value("${mqtt.automatic-reconnect}")
    private Boolean automaticReconnect;
    @Value("${mqtt.clean-start}")
    private Boolean cleanStart;

    /**
     * 创建一个链接
     *
     * @param clientId
     * @return
     */
    public IMqttClient getClient(String clientId) {
        IMqttClient client = null;
        try {
            MqttClientPersistence persistence = new MemoryPersistence();
            client = new MqttClient(broker, clientId, persistence);
            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setAutomaticReconnect(automaticReconnect);
            options.setCleanStart(cleanStart);
            options.setUserName(username);
            options.setPassword(StrUtil.bytes(password));
            options.setKeepAliveInterval(keepAliveInterval);
            options.setConnectionTimeout(connectionTimeout);
            client.connect(options);
            log.info("[链接Broker成功] [{}] {}", clientId, JSONUtil.toJsonStr(options));
        } catch (Exception e) {
            log.error("[创建MQTT链接异常] [{}] {}", clientId, ExceptionUtil.stacktraceToString(e));
        }
        return client;
    }

    /**
     * 获取一个能自动重连的链接
     *
     * @param clientId
     * @return
     */
    public IMqttClient getAutoReconnectClient(String clientId) {
        IMqttClient client = null;
        try {
            client = getClient(clientId);
            client.setCallback(new MqttCallbackImpl(client));//这里是为了获取自己实现的自动重连功能
            log.info("[设置自动重连] [{}] 成功", clientId);
        } catch (Exception e) {
            log.error("[设置自动重连] [{}] 失败 {}", clientId, ExceptionUtil.stacktraceToString(e));
        }
        return client;
    }
}
