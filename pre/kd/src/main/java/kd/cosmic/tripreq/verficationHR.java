package kd.cosmic.tripreq;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.fi.ar.util.UUID;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述: 与myhr同步用的类
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：马衍浩
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

public class verficationHR {


    ///自定义推荐时数接口
    static public String post(String userId,String stime,String etime ) {


        Map<String, String> data = new HashMap<>();

        String token = gettoken.generateJWT(userId);
        //System.out.println(token);
        String aREQUESTID= UUID.randomUUID().toLowerCase();
        String url = "https://elb-prd.zenhr.cn/PUB/IF/WS.FWK";
        String uri = "https://elb-prd.zenhr.cn/PUB/IF/WS.FWK?HANDLERNAME=ifds&METHOD=IF_RTN_DS_QUERY&OP_CODE=1910180001&JWT_OBJ=\" + token + \"&REQUEST_ID=\"+REQUEST_ID+\"&LANGUAGE=zh-cn&KEY=2ff16168e29e11e98745ec0d9a495454&KEY=2ff16168e29e11e98745ec0d9a495454";


        String bod = "{\n" +
                "\"p_att_type\":\"\",\n" +
                "\"p_calendar_type\":\"\",\n" +
                "\"p_lv_startdate\":\""+stime+"\",\n" +
                "\"p_startdate_daytype\":\"4\",\n" +
                "\"p_lv_enddate\":\""+etime+"\",\n" +
                "\"p_enddate_daytype\":\"4\",\n" +
                "\"p_lv_type_code\":\"L24\",\n" +
                "\"p_add_info\":\"\",\n" +
                "\"p_language\":\"zh-cn\"\n" +
                "}";
        HttpRequest request = HttpRequest.post(uri).body(bod);
        String bodys = request.execute().body();
        System.out.print("验证打印");
        System.out.print(bodys);
        JSONObject jsonObject = JSONUtil.parseObj(bodys);
        String returnT="";
        try {



            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(bodys);
            String codeV= rootNode.path("MSG_CODE").asText();
            String codeV1= rootNode.path("msg_code").asText();
            if(codeV.equals("1")||codeV1.equals("1")) {

                String pEndDdatetime = rootNode.path("DATA1").get(0).path("p_end_datetime").asText();
                String pstartDatetime = rootNode.path("DATA1").get(0).path("p_start_datetime").asText();
                String plvhours = rootNode.path("DATA1").get(0).path("p_lvhours").asText();
                // System.out.println(p_end_datetime);
                //  System.out.println(p_start_datetime);
                // System.out.println(p_lvhours);
                returnT += "推荐时数:开始时间：" + pstartDatetime + ";";
                returnT += "结束时间：" + pEndDdatetime + ";";
                returnT += "小时数：" + plvhours + ";";
            }
            else{
                String msgdesc= rootNode.path("MSG_DESC").asText();
                String msgdesc1= rootNode.path("msg_desc").asText();
                returnT += msgdesc+msgdesc1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnT;

        //  System.out.println(a);
    }

    ///自定义管控验证接口
    public static   String post1(String userId, String stime, String etime, String keyed, String actionCode ) {


        try {
            //获取当前用户的推荐时数
            String stress = btnE(userId, stime, etime);
            if(stress.equals("400")) {
                return "获取推荐时数异常";
            }
            ObjectMapper mapperSS = new ObjectMapper();
            JsonNode rootNodeSS = mapperSS.readTree(stress);
            String codeV = rootNodeSS.path("MSG_CODE").asText();
            String codeV1 = rootNodeSS.path("msg_code").asText();
            if (codeV.equals("1") || codeV1.equals("1")) {

                String token = gettoken.generateJWT102(userId);
                // System.out.println(token);
                String plvhours = rootNodeSS.path("DATA1").get(0).path("p_lvhours").asText();
                String uri = "https://elb-prd.zenhr.cn/PUB/IF/WS.FWK?HANDLERNAME=ifds&METHOD=IF_RTN_DS_QUERY&OP_CODE=1910180001&JWT_OBJ=" + token + "&REQUEST_ID=40f79234-0f1b-11ee-ae80-0c42a1b8824e&LANGUAGE=zh-cn&KEY=2ff16168e29e11e98745ec0d9a495454";
                String bod = "{\n" +
                        "    \"^hrwf_apptype\":\"1\",\n" +
                        "    \"^hrwf_action_code\":\""+actionCode+"\",\n" +
                        "    \"lvwf_original_keyid\":\"\",\n" +
                        "    \"^hrwf_wfkeyid\":\"" + keyed + "\",\n" +
                        "    \"lvwf_startdatetime\":\"" + stime + "\",\n" +
                        "    \"lvwf_enddatetime\":\"" + etime + "\",\n" +
                        "    \"lvwf_lvcode\":\"L24\",\n" +
                        "    \"lvwf_lvhrs\":\""+plvhours+"\",\n" +
                        "    \"^hrwf_remarks\":\" \",\n" +
                        "    \"^hrwf_specified_approverid\":\"\",\n" +
                        "    \"p_pageinfo\":\"\",\n" +
                        "    \"^hrwf_language\":\"zh-cn\"\n" +
                        "}";
                HttpRequest request = HttpRequest.post(uri).body(bod);
                System.out.println("aaaa");
                String bodys = request.execute().body();
                System.out.println(bodys);
                String code = "";
                String des = "";
                String msgcode1="";
                String msgdes1="";
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(bodys);
                    String msgcode = rootNode.path("MSG_CODE").asText();
                    msgcode1= rootNode.path("msg_code").asText();
                    String msgdes = rootNode.path("msg_desc").asText();
                    msgdes1= rootNode.path("MSG_DESC").asText();
                    code = msgcode;
                    des = msgdes;

                } catch (Exception e) {
                    return e.getMessage();
                }
                System.out.println("aaaa");
                System.out.println(code);
                // return "200";
                if (code.equals("9999")||msgcode1.equals("9999")) {
                    return "200";
                } else if (code.equals("3")||msgcode1.equals("3")) {
                    return "myHR返回结果：" + des+msgdes1+des;
                } else {
                    return "myHR管控验证返回结果："+code +msgcode1+ des+msgdes1;
                } /**/
            } else {
                String msgdesc = rootNodeSS.path("MSG_DESC").asText();
                String msgdesc1 = rootNodeSS.path("msg_desc").asText();
                return "获取推荐时数异常" + msgdesc + msgdesc1;

            }
        }
        catch   (Exception e) {
            return e.getMessage();
        }
    }


    ///提交时获取推荐时数
    static public String btnE(String userId,String stime,String eTime ) {

        try {
            Map<String, String> data = new HashMap<>();

            String token = gettoken.generateJWT(userId);
            //System.out.println(token);
            String aREQUESTID = UUID.randomUUID().toLowerCase();

            String url = "https://elb-prd.zenhr.cn/PUB/IF/WS.FWK";
            String uri = "https://elb-prd.zenhr.cn/PUB/IF/WS.FWK?HANDLERNAME=ifds&METHOD=IF_RTN_DS_QUERY&OP_CODE=1910180001&JWT_OBJ=" + token + "&REQUEST_ID=" + aREQUESTID + "&LANGUAGE=zh-cn"+"&KEY=2ff16168e29e11e98745ec0d9a495454";


            String bod = "{\n" +
                    "\"p_att_type\":\"\",\n" +
                    "\"p_calendar_type\":\"\",\n" +
                    "\"p_lv_startdate\":\"" + stime + "\",\n" +
                    "\"p_startdate_daytype\":\"4\",\n" +
                    "\"p_lv_enddate\":\"" + eTime + "\",\n" +
                    "\"p_enddate_daytype\":\"4\",\n" +
                    "\"p_lv_type_code\":\"L24\",\n" +
                    "\"p_add_info\":\"\",\n" +
                    "\"p_language\":\"zh-cn\"\n" +
                    "}";
            HttpRequest request = HttpRequest.post(uri).body(bod);
            String bodys = request.execute().body();
            System.out.println("推送结果:"+bodys);
            JSONObject jsonObject = JSONUtil.parseObj(bodys);
            return bodys;
        }
        catch (Exception e) {
            return "400";//推荐时数异常
        }




    }

}


