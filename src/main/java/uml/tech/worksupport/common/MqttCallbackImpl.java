package uml.tech.worksupport.common;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

/**
 * MqttCallback默认实现
 * <p>
 * 连接断开时，自动重连并且重新订阅
 *
 * @author SunYu
 */
public class MqttCallbackImpl implements MqttCallback {
    private final Log log = LogFactory.get();

    protected IMqttClient client;
    private String[] topics;
    private int[] qos;

    /**
     * 初始化，链接断开自动重连，并且重新监听
     *
     * @param client
     * @param topics
     * @param qos
     */
    private void init(IMqttClient client, String[] topics, int[] qos) {
        this.client = client;
        this.topics = topics;
        this.qos = qos;
        if (topics.length != qos.length) {
            log.error("[监听初始化异常] [{}] topics和qos长度不一致 {} {}", client.getClientId(), topics, qos);
        } else {
            try {
                client.subscribe(topics, qos);
                log.info("[订阅成功] [{}] {} {}", client.getClientId(), topics, qos);
            } catch (Exception e) {
                log.error("[订阅失败] [{}] {} {} {}", client.getClientId(), topics, qos, ExceptionUtil.stacktraceToString(e));
            }
        }
    }

    /**
     * 初始化
     *
     * @param client
     */
    public MqttCallbackImpl(IMqttClient client) {
        this.client = client;
    }

    /**
     * 初始化，链接断开自动重连，并且重新监听
     *
     * @param client
     * @param topics
     * @param qos
     */
    public MqttCallbackImpl(IMqttClient client, String[] topics, int[] qos) {
        init(client, topics, qos);
    }

    /**
     * 初始化，链接断开自动重连，并且重新监听
     *
     * @param client
     * @param topic
     * @param qos
     */
    public MqttCallbackImpl(IMqttClient client, String topic, int qos) {
        init(client, new String[]{topic}, new int[]{qos});
    }

    /**
     * 当服务器通过发送断开连接包优雅地与客户端断开连接时，
     * 或者当 TCP 连接由于网络问题丢失时，
     * 或者当客户端遇到错误时，调用此方法
     *
     * @param disconnectResponse a {@link MqttDisconnectResponse} containing relevant properties
     *                           related to the cause of the disconnection.
     */
    @Override
    public void disconnected(MqttDisconnectResponse disconnectResponse) {
        log.info("[与Broker断开] [{}] {}", client.getClientId(), disconnectResponse);
        while (!client.isConnected()) {
            try {
                log.trace("[断开重连] [{}]", client.getClientId());
                client.reconnect();
            } catch (Exception e) {
                if (e instanceof MqttException && ((MqttException) e).getReasonCode() == 32110) {
                    // 遇到32110错误码，表示连接操作正在进行中
                    log.trace("[正在重连中] [{}]", client.getClientId());
                } else {
                    log.error("[断开重连失败] [{}] {}", client.getClientId(), ExceptionUtil.stacktraceToString(e));
                }
            }
            ThreadUtil.sleep(1000);
        }
    }

    /**
     * 当 MQTT 客户端内部发生异常时调用此方法。异常的原因可能多种多样，
     * 包括格式错误的数据包、协议错误，甚至是 MQTT 客户端本身的错误。
     * 这个回调方法将这些错误通知给应用程序，以便它可以决定如何最好地处理它们
     *
     * @param exception - The exception thrown causing the error.
     */
    @Override
    public void mqttErrorOccurred(MqttException exception) {
        log.error("[内部异常] [{}] {}", client.getClientId(), exception);
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
        log.info("[收到消息] [{}] [{}] {}", client.getClientId(), topic, message);
    }

    /**
     * 当消息的传递完成并且所有确认都已收到时调用此方法。
     * 对于 QoS 0 消息，它在消息被交给网络进行传递后立即调用。
     * 对于 QoS 1 消息，它在收到 PUBACK 时调用，
     * 对于 QoS 2 消息，它在收到 PUBCOMP 时调用
     *
     * @param token the delivery token associated with the message.
     */
    @Override
    public void deliveryComplete(IMqttToken token) {
        if (token.isComplete()) {
            log.info("[消息发送成功] [{}] {}", client.getClientId(), token.getTopics());
        } else {
            log.error("[消息发送失败] [{}] {} {}", client.getClientId(), token.getTopics(), token.getException());
        }
    }

    /**
     * 当与服务器的连接成功完成时调用此方法
     *
     * @param reconnect If true, the connection was the result of automatic reconnect.
     * @param serverURI The server URI that the connection was made to.
     */
    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("[与Broker连接成功] [{}] reconnect:{} {}", client.getClientId(), reconnect, serverURI);
        // 如果 topics 不为空，重新订阅
        if (topics != null && qos != null) {
            try {
                client.subscribe(topics, qos);
                log.info("[重新订阅] [{}] {} {} 成功", client.getClientId(), topics, qos);
            } catch (Exception e) {
                log.error("[重新订阅] [{}] {} {} 失败 {}", client.getClientId(), topics, qos, ExceptionUtil.stacktraceToString(e));
            }
        }
    }

    /**
     * 当客户端接收到 AUTH 数据包时调用此方法
     *
     * @param reasonCode The Reason code, can be Success (0), Continue authentication (24)
     *                   or Re-authenticate (25).
     * @param properties The {@link MqttProperties} to be sent, containing the
     *                   Authentication Method, Authentication Data and any required User
     *                   Defined Properties.
     */
    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {
        log.info("[收到AUTH数据包] [{}] reasonCode:{} {}", client.getClientId(), reasonCode, properties);
    }
}
