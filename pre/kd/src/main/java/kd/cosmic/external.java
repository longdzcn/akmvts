package kd.cosmic;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述: 与myhr同步
 * 开发者: 易佳伟
 * 创建日期: 1期完成
 * 关键客户：马衍浩
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

public class external {

    public static void main(String[] args) {
        //external a = new external();
       // a.FOJobFunction();


    }
    //获取级别编码名称
    public void custjobfamilygroup(){
        String url = "https://api15.sapsf.cn/odata/v2/cust_JobFamilyGroup";
        Map<String,Object> map = new HashMap<>();
        map.put("$format","json");
        map.put("$select","externalCode,externalName_zh_CN");
        map.put("$filter","lastModifiedDateTime ge datetime'2021-08-15T13:50:41'");
        HttpRequest request = HttpRequest.get(url).form(map).header("Authorization", "Basic SmluRGllQVBJQGFrbW1lYWR2aWxEOjEyMzQ1Ng==");
        String bodys = request.execute().body();
        JSONObject jsonObject = JSONUtil.parseObj(bodys);
        ObjectMapper objectMapper = new ObjectMapper();
        try{
        JsonNode rootNode = objectMapper.readTree(bodys);
        JsonNode resultsNode = rootNode.path("d").path("results");
        int length = resultsNode.size();
            for(int i=0;i<length;i++) {
                //编码
                String externalCode = resultsNode.get(i).path("externalCode").asText();
                //名称
                String externalNamezhCN = resultsNode.get(i).path("externalName_zh_CN").asText();

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    //职位群
    public void fojobfunction(){
        String url = "https://api15.sapsf.cn/odata/v2/FOJobFunction";
        Map<String,Object> map = new HashMap<>();
        map.put("$format","json");
        map.put("$select","externalCode,name_zh_CN,cust_JobFamilyGroup");
        map.put("$filter","lastModifiedDateTime ge datetime'2021-08-15T13:50:41'");
        HttpRequest request = HttpRequest.get(url).form(map).header("Authorization", "Basic SmluRGllQVBJQGFrbW1lYWR2aWxEOjEyMzQ1Ng==");
        String bodys = request.execute().body();
        JSONObject jsonObject = JSONUtil.parseObj(bodys);
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            JsonNode rootNode = objectMapper.readTree(bodys);
            JsonNode resultsNode = rootNode.path("d").path("results");
            int length = resultsNode.size();
            for(int i=0;i<length;i++) {
                //代码
                String externalCode = resultsNode.get(i).path("externalCode").asText();
                //所属职群
                String custJobFamilyGroup = resultsNode.get(i).path("cust_JobFamilyGroup").path("__deferred").path("uri").asText();
                //名称
                String namezhCN = resultsNode.get(i).path("name_zh_CN").asText();

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //职位代码
 public void fojobcode(){
        String url = "https://api15.sapsf.cn/odata/v2/FOJobCode";
        Map<String,Object> map = new HashMap<>();
        map.put("$format","json");
        map.put("$select","externalCode,name_zh_CN,jobFunction");
        map.put("$filter","lastModifiedDateTime ge datetime'2021-08-15T13:50:41'");
        HttpRequest request = HttpRequest.get(url).form(map).header("Authorization", "Basic SmluRGllQVBJQGFrbW1lYWR2aWxEOjEyMzQ1Ng==");
        String bodys = request.execute().body();
        JSONObject jsonObject = JSONUtil.parseObj(bodys);
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            JsonNode rootNode = objectMapper.readTree(bodys);
            JsonNode resultsNode = rootNode.path("d").path("results");
            int length = resultsNode.size();
            for(int i=0;i<length;i++) {
                //代码
                String externalCode = resultsNode.get(i).path("externalCode").asText();
                //名称
                String namezhCN = resultsNode.get(i).path("name_zh_CN").asText();
                //职位群
                String jobFunction = resultsNode.get(i).path("jobFunction").asText();

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    //L1、L2部门出报表用
    public void custDepartment(){
        String url = "https://api15.sapsf.cn/odata/v2/cust_Department";
        Map<String,Object> map = new HashMap<>();
        map.put("$format","json");
        map.put("$select","externalCode,externalName_zh_CN");
        map.put("$filter","lastModifiedDateTime ge datetime'2021-08-15T13:50:41'");
        HttpRequest request = HttpRequest.get(url).form(map).header("Authorization", "Basic SmluRGllQVBJQGFrbW1lYWR2aWxEOjEyMzQ1Ng==");
        String bodys = request.execute().body();
        JSONObject jsonObject = JSONUtil.parseObj(bodys);
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            JsonNode rootNode = objectMapper.readTree(bodys);
            JsonNode resultsNode = rootNode.path("d").path("results");
            int length = resultsNode.size();
            for(int i=0;i<length;i++) {
                //编码
                String externalCode = resultsNode.get(i).path("externalCode").asText();
                //名称
                String externalNamezhCN = resultsNode.get(i).path("externalName_zh_CN").asText();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    //地点
    public void folocation(){
        String url = "https://api15.sapsf.cn/odata/v2/FOLocation";
        Map<String,Object> map = new HashMap<>();
        map.put("$format","json");
        map.put("$select","externalCode,name");
        map.put("$filter","lastModifiedDateTime ge datetime'2021-08-15T13:50:41'");
        HttpRequest request = HttpRequest.get(url).form(map).header("Authorization", "Basic SmluRGllQVBJQGFrbW1lYWR2aWxEOjEyMzQ1Ng==");
        String bodys = request.execute().body();
        JSONObject jsonObject = JSONUtil.parseObj(bodys);
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            JsonNode rootNode = objectMapper.readTree(bodys);
            JsonNode resultsNode = rootNode.path("d").path("results");
            int length = resultsNode.size();
            for(int i=0;i<length;i++) {
                //代码
                String externalCode = resultsNode.get(i).path("externalCode").asText();
                //名称
                String namezhCN = resultsNode.get(i).path("name").asText();

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

   //成本中心
    public void focostcenter(){
        String url = "https://api15.sapsf.cn/odata/v2/FOCostCenter";
        Map<String,Object> map = new HashMap<>();
        map.put("$format","json");
        map.put("$select","externalCode,name_zh_CN");
        map.put("$filter","lastModifiedDateTime ge datetime'2021-08-15T13:50:41'");
        HttpRequest request = HttpRequest.get(url).form(map).header("Authorization", "Basic SmluRGllQVBJQGFrbW1lYWR2aWxEOjEyMzQ1Ng==");
        String bodys = request.execute().body();
        JSONObject jsonObject = JSONUtil.parseObj(bodys);
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            JsonNode rootNode = objectMapper.readTree(bodys);
            JsonNode resultsNode = rootNode.path("d").path("results");
            int length = resultsNode.size();
            for(int i=0;i<length;i++) {
                //代码
                String externalCode = resultsNode.get(i).path("externalCode").asText();
                //名称
                String namezhCN = resultsNode.get(i).path("name_zh_CN").asText();

                System.out.print(namezhCN);


            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }





}
