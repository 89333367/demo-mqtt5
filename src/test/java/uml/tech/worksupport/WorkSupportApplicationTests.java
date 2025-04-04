package uml.tech.worksupport;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uml.tech.worksupport.constant.Constants;
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

    private final String did = Constants.did;

    @Test
    void 上报电控单元信息() {
        // upload/info/设备编号
        /**
         * [
         *     {
         *         "id": "1",
         *         "fwVer": "1.0",
         *         "hwVer": "1.1",
         *         "swVer": "1.2"
         *     }
         * ]
         */
        JSONArray o = new JSONArray();
        JSONObject jo = new JSONObject();
        jo.set("id", "1");
        jo.set("fwVer", "1.0");
        jo.set("hwVer", "1.1");
        jo.set("swVer", "1.2");
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
    void 上报整个流程状态() {
        // upload/status/设备编号
        /**
         * {
         *     "planId": 1874632041221660672,
         *     "status": 2,
         *     "terminalId": 1874630443015675904
         * }
         */
        JSONObject jo = new JSONObject();
        jo.set("planId", "1874632041221660672");
        jo.set("terminalId", "1874630443015675904");

        jo.set("status", 2);//收到升级计划
        messageUtil.publish("upload/status/" + did, new MqttMessage(jo.toString().getBytes()));
        ThreadUtil.sleep(1000);

        jo.set("status", 3);//请求升级文件
        messageUtil.publish("upload/status/" + did, new MqttMessage(jo.toString().getBytes()));
        ThreadUtil.sleep(1000);

        jo.set("status", 4);//下载升级文件
        messageUtil.publish("upload/status/" + did, new MqttMessage(jo.toString().getBytes()));
        ThreadUtil.sleep(1000);

        jo.set("status", 6);//下载文件成功
        messageUtil.publish("upload/status/" + did, new MqttMessage(jo.toString().getBytes()));
        ThreadUtil.sleep(1000);

        jo.set("status", 8);//校验文件成功
        messageUtil.publish("upload/status/" + did, new MqttMessage(jo.toString().getBytes()));
        ThreadUtil.sleep(1000);

        jo.set("status", 9);//升级
        messageUtil.publish("upload/status/" + did, new MqttMessage(jo.toString().getBytes()));
        ThreadUtil.sleep(1000);

        jo.set("status", 1);//升级成功
        messageUtil.publish("upload/status/" + did, new MqttMessage(jo.toString().getBytes()));
        ThreadUtil.sleep(1000);
    }

    @Test
    void 请求升级计划状态() {
        // request/plan/设备编号/planId
        Long planId = 1874632041221660672L;
        messageUtil.publish("request/plan/" + did + "/" + planId, new MqttMessage());

        ThreadUtil.sleep(1000 * 10);
    }

    @Test
    void 上报各种状态() {
        // upload/status/设备编号
        /**
         * {
         *     "planId": 1,
         *     "status": 2,
         *     "terminalId": "1871378516328308736"
         * }
         */
        JSONObject jo = new JSONObject();
        jo.set("planId", 1);
        jo.set("status", 5);
        jo.set("terminalId", "1871378516328308736");
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
        jo.set("planId", 1);
        jo.set("terminalId", "1871378516328308736");
        messageUtil.publish("request/file/" + did, new MqttMessage(jo.toString().getBytes()));

        ThreadUtil.sleep(1000 * 10);
    }

}
