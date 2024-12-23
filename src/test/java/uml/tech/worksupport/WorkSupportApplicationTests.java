package uml.tech.worksupport;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uml.tech.worksupport.util.MessageUtil;
import uml.tech.worksupport.util.MqttClientUtil;

import javax.annotation.Resource;

@SpringBootTest
class WorkSupportApplicationTests {
    Log log = LogFactory.get();

    @Resource
    private MqttClientUtil mqttClientUtil;
    @Resource
    private MessageUtil messageUtil;

    String did = "did1";

    @Test
    void 上报电控单元信息() {
        // upload/info/设备编号
        /**
         * [
         *     {
         *         "id": "1234567890123456789",
         *         "fwVer": "1.0",
         *         "hwVer": "1.1",
         *         "swVer": "1.8"
         *     }
         * ]
         */
        JSONArray o = new JSONArray();
        JSONObject jo = new JSONObject();
        jo.set("id", "1234567890123456789");
        jo.set("fwVer", "1.0");
        jo.set("hwVer", "1.1");
        jo.set("swVer", "1.8");
        o.add(jo);
        messageUtil.publish("upload/info/" + did, new MqttMessage(o.toString().getBytes()));
    }

    @Test
    void 请求升级计划() {
        // request/plan/设备编号
        messageUtil.publish("request/plan/" + did, new MqttMessage());

        ThreadUtil.sleep(1000 * 10);
    }

    @Test
    void 上报各种状态() {
        // upload/status/设备编号
        /**
         * {
         *     "planId": 1,
         *     "status": 2
         * }
         */
        JSONObject jo = new JSONObject();
        jo.set("planId", 1);
        jo.set("status", 2);
        messageUtil.publish("upload/status/" + did, new MqttMessage(jo.toString().getBytes()));
    }


    @Test
    void 获取升级包数据() {
        // request/file/设备编号
        /**
         * {
         *     "fileId": 1,
         *     "blockNum": 1,
         *     "chunk": 4096
         * }
         */
        JSONObject jo = new JSONObject();
        jo.set("fileId", 1);
        jo.set("blockNum", 1);
        jo.set("chunk", 4096);
        messageUtil.publish("request/file/" + did, new MqttMessage(jo.toString().getBytes()));

        ThreadUtil.sleep(1000 * 10);
    }

}
