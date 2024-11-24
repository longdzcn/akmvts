package kd.cosmic.tripreq;

import cn.hutool.http.HttpRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.fi.ar.util.UUID;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws IOException {

        String aREQUESTID = UUID.randomUUID().toLowerCase();
        String aSIGN = md5(aREQUESTID);

        String url = "https://elb-prd.zenhr.cn/PUB/IF/WS.FWK?METHOD=INTERFACE_MAINTAIN_LVINFO&TOKEN=287930172&CO_CODE=2104250001&INTERFACE_ID=100097&REQUEST_ID=" + aREQUESTID + "&LANGUAGE=zh-cn&SIGN=" + aSIGN+"&KEY=1250549469";

        String jsonT ="[{\"veri_type\":\"5\",\"user_id\":\"E0000026\",\"action_code\":\"1\",\"key_id\":1814477454476052481,\"start_datetime\":\"2023-11-13  08:00:00\",\"end_datetime\":\"2023-11-15  17:30:00\",\"biz_code\":\"L24\",\"hrs\":\"24.00\",\"remarks\":\"\"}]";
        result(jsonT,url);
    }
    public static void result(String jsonT, String url) throws IOException {
        if (jsonT.endsWith(",")) {
            // 去掉最后一个字符
            jsonT = jsonT.substring(0, jsonT.length() - 1);
        }

        System.out.println("jsonT:"+jsonT);
            Map<String, Object> map = new HashMap<>();
            map.put("Content-Type", "application/json");
            HttpRequest request = HttpRequest.post(url).form(map).body(jsonT);
            String bodys = request.execute().body();
            System.out.println("公干同步结果:"+ bodys);
            //String aa = bodys;
            ObjectMapper mapperTS = new ObjectMapper();
            JsonNode rootNodeTS = mapperTS.readTree(bodys);
            String codeV = rootNodeTS.path("MSG_CODE").asText();
            String codeV1 = rootNodeTS.path("msg_code").asText();
            if (codeV.equals("1") || codeV1.equals("1")) {






        }
    }


    public static String md5(String guid) {
        String input = "287930172|2104250001|100097|" + guid + "|zh-cn|1250549469";

        try {
            // 获取MD5消息摘要实例
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 将输入转换为字节数组
            byte[] inputBytes = input.getBytes();
            // 计算MD5摘要
            byte[] digestBytes = md.digest(inputBytes);
            // 将字节数组转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : digestBytes) {
                sb.append(String.format("%02x", b));
            }
            String md5Hash = sb.toString();
            return md5Hash;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
