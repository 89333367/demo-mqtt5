package uml.tech.worksupport.common;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.common.MqttMessage;

/**
 * 抽象回调类
 * 目的是接收消息时，自动try-catch记录异常
 *
 * @author SunYu
 */
public abstract class MqttSubscribeCallback extends MqttCallbackImpl {
    private final Log log = LogFactory.get();

    /**
     * 初始化
     *
     * @param client
     */
    public MqttSubscribeCallback(IMqttClient client) {
        super(client);
    }

    /**
     * 初始化，链接断开自动重连，并且重新监听
     *
     * @param client
     * @param topic
     * @param qos
     */
    public MqttSubscribeCallback(IMqttClient client, String topic, int qos) {
        super(client, topic, qos);
    }

    /**
     * 初始化，链接断开自动重连，并且重新监听
     *
     * @param client
     * @param topics
     * @param qos
     */
    public MqttSubscribeCallback(IMqttClient client, String[] topics, int[] qos) {
        super(client, topics, qos);
    }


    /**
     * 当从服务器接收到消息时调用此方法
     *
     * @param topic   name of the topic on the message was published to
     * @param message the actual message.
     * @throws Exception
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {
            doMessageArrived(topic, message);
        } catch (Exception e) {
            log.error("[消息处理异常] [{}] {}", client.getClientId(), ExceptionUtil.stacktraceToString(e));
        }
    }

    /**
     * 抽象方法，让子类实现具体的业务逻辑，自动try-catch记录异常
     *
     * @param topic
     * @param message
     */
    protected abstract void doMessageArrived(String topic, MqttMessage message);
}
