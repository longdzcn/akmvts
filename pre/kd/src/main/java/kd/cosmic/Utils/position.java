package kd.cosmic.Utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 描述: 与myhr同步用的类
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：马衍浩
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

public class position {


    public static boolean isValidPhoneNumber(String phoneNumber) {
        // 使用正则表达式定义手机号的格式
        String pattern = "^1[0-9]{10}$"; // 以1开头，后跟10个数字
        // 创建Pattern对象，并使用手机号的格式构建正则表达式
        Pattern regex = Pattern.compile(pattern);
        // 创建Matcher对象，并使用手机号进行匹配
        Matcher matcher = regex.matcher(phoneNumber);
        // 返回匹配结果
        return matcher.matches();
    }



public static void main(String[] args) {
        //String phoneNumber = " 1234567890"; // 替换为要验证的手机号

        //if (isValidPhoneNumber(phoneNumber)) {
            //System.out.println(isValidPhoneNumber(phoneNumber));
      //  } else {
           // System.out.println(isValidPhoneNumber(phoneNumber));
       // }
      //a.Picklist();
      }

    //获取级别编码名称
    public void picklist() {


        String url ="https://api15.sapsf.cn/odata/v2/Picklist('JobLevel')?$expand=picklistOptions&$format=JSON";
        Map<String, Object> map = new HashMap<>();
        map.put("$expand","picklistOptions");
        map.put("$format","JSON");
        HttpRequest request = HttpRequest.get(url).form(map).header("Authorization", "Basic SmluRGllQVBJQGFrbW1lYWR2aWxEOjEyMzQ1Ng==");

        //发起请求
        String bodys = request.execute().body();
        //将json字符串转成JSONOBJECT对象，方便迭代对象判断人员是否已纯在
        JSONObject jsonObject = JSONUtil.parseObj(bodys);
        // System.out.println(jsonObject);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(bodys);
            JsonNode resultsNode = rootNode.path("d").path("picklistOptions").path("results");
            int len = resultsNode.size();
            for (int i = 0; i < len; i++) {
                String id = resultsNode.get(i).path("id").asText();

                String externalCode=resultsNode.get(i).path("externalCode").asText();

                String uri = resultsNode.get(i).path("picklistLabels").path("__deferred").path("uri").asText();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
       // this.getView().showMessage("同步完成");


    }




}
