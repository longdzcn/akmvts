package kd.cosmic;

import cn.hutool.http.HttpRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述: 与myhr同步成本中心用
 * 开发者: 易佳伟
 * 创建日期: 1期完成
 * 关键客户：马衍浩
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

public class CostCenterUnit {

    public static String postmessagede(String tcuty,String tstuts,String externalCode,String nameLocalized,String descriptionLocalized,String parent ,String time) {
        String url = "https://api15.sapsf.cn/odata/v2/upsert?$format=json";
        Map<String, Object> map = new HashMap<>();
        // JSON string with variables
        String json = "{ \"__metadata\": { \"uri\": \"FOCostCenter(startDate=datetime'"+time+"',externalCode='"+externalCode+"')\" }, \"name_defaultValue\": \"a\", \"description_localized\": \"b\", \"status\": \"c\", \"glStatementCode\": \"d\", \"parent\": \"\", \"costcenterManager\": \"\", \"cust_Type\": \"e\", \"cust_notes\": \"f\" }";
        String mess="";

        // Create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            // Replace variable values in the JsonNode
            ((ObjectNode) jsonNode).put("name_defaultValue", nameLocalized);
            ((ObjectNode) jsonNode).put("description_localized", descriptionLocalized);
            ((ObjectNode) jsonNode).put("status", tstuts);
            ((ObjectNode) jsonNode).put("glStatementCode", "");
            ((ObjectNode) jsonNode).put("cust_Type", tcuty);
            ((ObjectNode) jsonNode).put("costcenterManager", "");
            ((ObjectNode) jsonNode).put("cust_notes", "");
            ((ObjectNode) jsonNode).put("parent", parent);
            String json1 = objectMapper.writeValueAsString(jsonNode);
             System.out.print(json1);

            map.put("Content-Type", "application/json");
            HttpRequest request = HttpRequest.post(url).form(map).header("Authorization", "Basic SmluRGllQVBJQGFrbW1lYWR2aWxEOjEyMzQ1Ng==").body(json1);
            String bodys = request.execute().body();
            ObjectMapper reback = new ObjectMapper();
            JsonNode rootNode = reback.readTree(bodys);
            JsonNode mesa = rootNode.path("d").get(0).get("httpCode");
              mess = mesa.asText();
             System.out.print(bodys);
             System.out.print(mess);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Parse JSON string to JsonNode

        return mess;
    }


    public static String postmessage(String tcuty,String tstuts,String externalCode,String nameLocalized,String descriptionLocalized,String parent ,String time) {
        String url = "https://api15.sapsf.cn/odata/v2/upsert?$format=json";
        Map<String, Object> map = new HashMap<>();
        // JSON string with variables
        String json = "{ \"__metadata\": { \"uri\": \"FOCostCenter(startDate=datetime'"+time+"',externalCode='"+externalCode+"')\" }, \"name_localized\": \"a\", \"description_localized\": \"b\", \"status\": \"c\", \"glStatementCode\": \"d\", \"parent\": \"\", \"costcenterManager\": \"\", \"cust_Type\": \"e\", \"cust_notes\": \"f\" }";
        String mess="";

        // Create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            // Replace variable values in the JsonNode
            ((ObjectNode) jsonNode).put("name_localized", nameLocalized);
            ((ObjectNode) jsonNode).put("description_localized", descriptionLocalized);
            ((ObjectNode) jsonNode).put("status", tstuts);
            ((ObjectNode) jsonNode).put("glStatementCode", "");
            ((ObjectNode) jsonNode).put("cust_Type", tcuty);
            ((ObjectNode) jsonNode).put("costcenterManager", "");
            ((ObjectNode) jsonNode).put("cust_notes", "");
            ((ObjectNode) jsonNode).put("parent", parent);
            String json1 = objectMapper.writeValueAsString(jsonNode);
            System.out.print(json1);

            map.put("Content-Type", "application/json");
            HttpRequest request = HttpRequest.post(url).form(map).header("Authorization", "Basic SmluRGllQVBJQGFrbW1lYWR2aWxEOjEyMzQ1Ng==").body(json1);
            String bodys = request.execute().body();
            ObjectMapper reback = new ObjectMapper();
            JsonNode rootNode = reback.readTree(bodys);
            JsonNode mesa = rootNode.path("d").get(0).get("httpCode");
            mess = mesa.asText();
            System.out.print(bodys);
            System.out.print(mess);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Parse JSON string to JsonNode

        return mess;
    }

}
