package kd.cosmic;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.BeforeOperationArgs;
import kd.cosmic.synUser.MyTaskUser;
import kd.fi.ar.util.UUID;
import kd.sdk.plugin.Plugin;

import java.io.IOException;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述: 单据操作自定义管控验证接口kd.cosmic.verificationE
 * 开发者: 易佳伟
 * 创建日期:
 * 关键客户：
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
public class verificationE extends AbstractOperationServicePlugIn implements Plugin {


    private static final Log log = LogFactory.getLog(MyTaskUser.class);


    //一定要预加载字段，否则审核后提示报错提示没有自定义字段实体类型tripentry中不存在名为ezob_kssj的属性
    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        List<String> fieldKeys = e.getFieldKeys();
//        fieldKeys.add(TripreqBill.HEAD_ISLOAN);
//        fieldKeys.add(TripreqBill.HEAD_REIMBURSETIME);
//        fieldKeys.add(ErCoreBaseBill.HEAD_BILLSTATUS);
        fieldKeys.add("ezob_kssj");
        fieldKeys.add("ezob_jssj");
        fieldKeys.add("ischange");
        fieldKeys.add("creator");
        fieldKeys.add("istravelers");
        fieldKeys.add("tripentry");
        fieldKeys.add("tripentry.travelers");
        fieldKeys.add("std_costcenter");
    }


    @Override
    public void beforeExecuteOperationTransaction(BeforeOperationArgs e) {

        super.beforeExecuteOperationTransaction(e);


        String code = dateAndOrganization(e);
        if(code.equals("500"))
        {
            this.operationResult.setMessage("单据提交成功，请注意检查行程时间");
            return;
        }else {

        }

        //行程变更：江伟维修改此处代码，一旦点击行程变更，就先删除myHR的公干记录###################################
        if (StringUtils.equals(e.getOperationKey(), "tripchange")) {//变更通过后
            DynamicObject[] tripList = e.getDataEntities();
            for (DynamicObject entrydata : tripList) {
                try {
                    String aREQUESTID = UUID.randomUUID().toLowerCase();
                    String aSIGN = md5(aREQUESTID);
                    String actioncode =tripList[0].get("ischange").toString();
//                    if(action_code.equals("false")){
//                        action_code="1";
//                    }else {
                        actioncode="3";    //江伟维：由于改为行程变更时就推送删除数据过去，所以此处统统为3
//                    }

                    String url = "https://elb-test.zenhr.cn/PUB/IF/WS.FWK?METHOD=INTERFACE_MAINTAIN_LVINFO&TOKEN=57678627&CO_CODE=1220617001&INTERFACE_ID=100097&REQUEST_ID=" + aREQUESTID + "&LANGUAGE=zh-cn&SIGN=" + aSIGN+"&KEY=1250549469";
                    DynamicObjectCollection tripentryList = entrydata.getDynamicObjectCollection("tripentry");
                    String jsonT = "[";

                    // String description=entrydata.get("description").toString();
                    Boolean bl = entrydata.getBoolean("istravelers");


                    for (DynamicObject triE : tripentryList) {
                        if(bl)
                        {

                            //多出差人集合
                            DynamicObjectCollection dynamicObjects = triE.getDynamicObjectCollection("travelers");

                            for (DynamicObject dynamicObjectE : dynamicObjects) {

                                DynamicObject userNumber = dynamicObjectE.getDynamicObject("fbasedataid");
                                String number = userNumber.getString("number");
                            JSONObject jsonObject = new JSONObject();
                            // 添加属性到JSON对象
                            jsonObject.set("veri_type", "5");
                            DynamicObject userE = (DynamicObject) entrydata.get("creator");
                            jsonObject.set("user_id", number);
                            jsonObject.set("action_code", actioncode);            //是否变更，1为不变更，2为变更
                            // 生成随机的UU
                            //String uuid = UUID.randomUUID();
                            //jsonObject.set("key_id", uuid);
                            //江伟维：不能用随机的UU，因为变更后要再传递一次相同的ID过去，以便那边判断
                            String uuid =String.format("1000000000000%19d",triE.getPkValue());
                            String entiryId =  triE.getPkValue().toString();
                            String keyid =entiryId+number;
                            jsonObject.set("key_id", keyid);
                            //需要获取时间

                            String inputTime = "";
                            if(triE.get("ezob_kssj")!=null) {
                                inputTime=  " " +formatSecondsToHHmmss(Integer.parseInt(triE.get("ezob_kssj").toString()));
                            }
                            String inputTimeE = "";
                            if(triE.get("ezob_jssj")!=null) {
                                inputTimeE= " " +formatSecondsToHHmmss(Integer.parseInt(triE.get("ezob_jssj").toString()));
                            }
                            String rstartdateT="";
                            if(triE.get("startdate").toString().contains("-")||triE.get("startdate").toString().contains("/"))
                            {
                                rstartdateT =triE.get("startdate").toString().split(" ")[0] + " " + inputTime;
                            }
                            else{
                                rstartdateT =recommendE.gettime(triE.get("startdate").toString()) + " " + inputTime;
                            }
                            String renddateT ="";
                            if(triE.get("enddate").toString().contains("-")||triE.get("enddate").toString().contains("/"))
                            {
                                renddateT =triE.get("enddate").toString().split(" ")[0] + " " + inputTimeE;
                            }
                            else{
                                renddateT = recommendE.gettime(triE.get("enddate").toString()) + " " + inputTimeE;

                            }

                            jsonObject.set("start_datetime", rstartdateT);

                            //jsonObject.set("end_datetime", renddateT);

                            String stress = verficationHR.btnE(number, rstartdateT, renddateT);
                            if (stress.equals("400")) {
                                this.operationResult.setMessage("获取推荐时数异常");
                                e.setCancel(true);
                                return;

                            }
                            ObjectMapper aMapperSS = new ObjectMapper();
                            JsonNode rootNodeSS = aMapperSS.readTree(stress);
                            String codeV = rootNodeSS.path("MSG_CODE").asText();
                            String codeV1 = rootNodeSS.path("msg_code").asText();
                            if (codeV.equals("1") || codeV1.equals("1")) {

                                String plvhours = rootNodeSS.path("DATA1").get(0).path("p_lvhours").asText();
                                ObjectMapper objectMapper = new ObjectMapper();

                                jsonObject.set("end_datetime", renddateT);
                                jsonObject.set("biz_code", "L24");
                                jsonObject.set("hrs", plvhours);
                                jsonObject.set("remarks", "");
                                jsonT += jsonObject.toString() + ",";
                            } else {

                                this.operationResult.setMessage("获取推荐时数异常");

                                e.setCancel(true);
                                return;
                            }
                                //同步结果
                                result(e,jsonT,url);
                        }

                        }else {
                            JSONObject jsonObject = new JSONObject();
                            // 添加属性到JSON对象
                            jsonObject.set("veri_type", "5");
                            DynamicObject userE = (DynamicObject) entrydata.get("creator");
                            jsonObject.set("user_id", userE.get("number"));
                            jsonObject.set("action_code", actioncode);            //是否变更，1为不变更，2为变更
                            // 生成随机的UU
                            //String uuid = UUID.randomUUID();
                            //jsonObject.set("key_id", uuid);
                            //江伟维：不能用随机的UU，因为变更后要再传递一次相同的ID过去，以便那边判断
                            String uuid =String.format("1000000000000%19d",triE.getPkValue());
                            jsonObject.set("key_id", triE.getPkValue());
                            //需要获取时间

                            String inputTime = "";
                            if(triE.get("ezob_kssj")!=null) {
                                inputTime=  " " +formatSecondsToHHmmss(Integer.parseInt(triE.get("ezob_kssj").toString()));
                            }
                            String inputTimeE = "";
                            if(triE.get("ezob_jssj")!=null) {
                                inputTimeE= " " +formatSecondsToHHmmss(Integer.parseInt(triE.get("ezob_jssj").toString()));
                            }
                            String rstartdateT="";
                            if(triE.get("startdate").toString().contains("-")||triE.get("startdate").toString().contains("/"))
                            {
                                rstartdateT =triE.get("startdate").toString().split(" ")[0] + " " + inputTime;
                            }
                            else{
                                rstartdateT =recommendE.gettime(triE.get("startdate").toString()) + " " + inputTime;
                            }
                            String renddateT ="";
                            if(triE.get("enddate").toString().contains("-")||triE.get("enddate").toString().contains("/"))
                            {
                                renddateT =triE.get("enddate").toString().split(" ")[0] + " " + inputTimeE;
                            }
                            else{
                                renddateT = recommendE.gettime(triE.get("enddate").toString()) + " " + inputTimeE;

                            }

                            jsonObject.set("start_datetime", rstartdateT);

                            //jsonObject.set("end_datetime", renddateT);

                            String stress = verficationHR.btnE(userE.get("number").toString(), rstartdateT, renddateT);
                            if (stress.equals("400")) {
                                this.operationResult.setMessage("获取推荐时数异常");
                                e.setCancel(true);
                                return;

                            }
                            ObjectMapper aMapperSS = new ObjectMapper();
                            JsonNode rootNodeSS = aMapperSS.readTree(stress);
                            String codeV = rootNodeSS.path("MSG_CODE").asText();
                            String codeV1 = rootNodeSS.path("msg_code").asText();
                            if (codeV.equals("1") || codeV1.equals("1")) {

                                String plvhours = rootNodeSS.path("DATA1").get(0).path("p_lvhours").asText();
                                ObjectMapper objectMapper = new ObjectMapper();

                                jsonObject.set("end_datetime", renddateT);
                                jsonObject.set("biz_code", "L24");
                                jsonObject.set("hrs", plvhours);
                                jsonObject.set("remarks", "");
                                jsonT += jsonObject.toString() + ",";
                            } else {

                                this.operationResult.setMessage("获取推荐时数异常");

                                e.setCancel(true);
                                return;
                            }
                            //同步结果
                            result(e,jsonT,url);
                        }


                    }
                    /* this.operationResult.setMessage(bodys); */

                } catch (Exception ex) {
                    //ex.printStackTrace();
                    this.operationResult.setMessage(ex.getMessage());
                    e.setCancel(true);
                    return;
                }

            }
            return;
        }

        String verJG = getverificatione(e);
        //String verJG = "200";

        if (verJG.equals("200")) {

            System.out.println("审核测试" + e.getOperationKey());

            if (StringUtils.equals(e.getOperationKey(), "audit")) {//审核通过后
                DynamicObject[] tripList = e.getDataEntities();
                for (DynamicObject entrydata : tripList) { //循环出差申请单
                    try {
                        String aREQUESTID = UUID.randomUUID().toLowerCase();
                        String aSIGN = md5(aREQUESTID);
                        String actioncode =tripList[0].get("ischange").toString();
//                        if(action_code.equals("false")){
                            actioncode="1";    //江伟维：由于改为行程变更时就推送删除数据过去，所以审核成功后的action_code统统为1
//                        }else {
//                            action_code="2";
//                        }

                        String url = "https://elb-test.zenhr.cn/PUB/IF/WS.FWK?METHOD=INTERFACE_MAINTAIN_LVINFO&TOKEN=57678627&CO_CODE=1220617001&INTERFACE_ID=100097&REQUEST_ID=" + aREQUESTID + "&LANGUAGE=zh-cn&SIGN=" + aSIGN+"&KEY=1250549469";
                        DynamicObjectCollection tripentryList = entrydata.getDynamicObjectCollection("tripentry");
                        String jsonT = "[";

                        // String description=entrydata.get("description").toString();
                        Boolean bl = entrydata.getBoolean("istravelers");//判断是否是多出差人
                        for (DynamicObject triE : tripentryList) { //循环明细
                            if(bl) //多
                            {

                                //多出差人集合
                                DynamicObjectCollection dynamicObjects = triE.getDynamicObjectCollection("travelers");

                                for (DynamicObject dynamicObjectE : dynamicObjects) { //循环多申请人

                                    DynamicObject userNumber = dynamicObjectE.getDynamicObject("fbasedataid");
                                    String number = userNumber.getString("number");

                                    JSONObject jsonObject = new JSONObject();
                                    // 添加属性到JSON对象
                                    jsonObject.set("veri_type", "5");
                                    DynamicObject userE = (DynamicObject) entrydata.get("creator");
                                    jsonObject.set("user_id", number);
                                    jsonObject.set("action_code", actioncode);            //是否变更，1为不变更，2为变更
                                    // 生成随机的UU
                                    //String uuid = UUID.randomUUID();
                                    //jsonObject.set("key_id", uuid);
                                    //江伟维：不能用随机的UU，因为变更后要再传递一次相同的ID过去，以便那边判断
                                    String uuid = String.format("1000000000000%19d", triE.getPkValue());
                                    String entiryId =  triE.getPkValue().toString();
                                    String keyid =entiryId+number; //实体ID加工号组成 key_id
                                    jsonObject.set("key_id", keyid);

                                    //需要获取时间
                                    String inputTime = "";
                                    if (triE.get("ezob_kssj") != null) {
                                        inputTime = " " + formatSecondsToHHmmss(Integer.parseInt(triE.get("ezob_kssj").toString()));
                                    }
                                    String inputTimeE = "";
                                    if (triE.get("ezob_jssj") != null) {
                                        inputTimeE = " " + formatSecondsToHHmmss(Integer.parseInt(triE.get("ezob_jssj").toString()));
                                    }
                                    String rstartdateT = "";
                                    if (triE.get("startdate").toString().contains("-") || triE.get("startdate").toString().contains("/")) {
                                        rstartdateT = triE.get("startdate").toString().split(" ")[0] + " " + inputTime;
                                    } else {
                                        rstartdateT = recommendE.gettime(triE.get("startdate").toString()) + " " + inputTime;
                                    }
                                    String renddateT = "";
                                    if (triE.get("enddate").toString().contains("-") || triE.get("enddate").toString().contains("/")) {
                                        renddateT = triE.get("enddate").toString().split(" ")[0] + " " + inputTimeE;
                                    } else {
                                        renddateT = recommendE.gettime(triE.get("enddate").toString()) + " " + inputTimeE;

                                    }

                                    jsonObject.set("start_datetime", rstartdateT);

                                    //jsonObject.set("end_datetime", renddateT);

                                    String stress = verficationHR.btnE(number, rstartdateT, renddateT);
                                    if (stress.equals("400")) {
                                        this.operationResult.setMessage("获取推荐时数异常");
                                        e.setCancel(true);
                                        return;

                                    }
                                    ObjectMapper aMapperSS = new ObjectMapper();
                                    JsonNode rootNodeSS = aMapperSS.readTree(stress);
                                    String codeV = rootNodeSS.path("MSG_CODE").asText();
                                    String codeV1 = rootNodeSS.path("msg_code").asText();
                                    if (codeV.equals("1") || codeV1.equals("1")) {

                                        String plvhours = rootNodeSS.path("DATA1").get(0).path("p_lvhours").asText();
                                        ObjectMapper objectMapper = new ObjectMapper();

                                        jsonObject.set("end_datetime", renddateT);
                                        jsonObject.set("biz_code", "L24");
                                        jsonObject.set("hrs", plvhours);
                                        jsonObject.set("remarks", "");
                                        jsonT += jsonObject.toString() + ",";
                                    } else {

                                        this.operationResult.setMessage("获取推荐时数异常");

                                        e.setCancel(true);
                                        return;
                                    }
                                    //同步结果
                                    result(e,jsonT,url);
                                }

                            }else {  //单
                                JSONObject jsonObject = new JSONObject();
                                // 添加属性到JSON对象
                                jsonObject.set("veri_type", "5");
                                DynamicObject userE = (DynamicObject) entrydata.get("creator");
                                jsonObject.set("user_id", userE.get("number"));
                                jsonObject.set("action_code", actioncode);            //是否变更，1为不变更，2为变更
                                // 生成随机的UU
                                //String uuid = UUID.randomUUID();
                                //jsonObject.set("key_id", uuid);
                                //江伟维：不能用随机的UU，因为变更后要再传递一次相同的ID过去，以便那边判断
                                String uuid =String.format("1000000000000%19d",triE.getPkValue());
                                jsonObject.set("key_id", triE.getPkValue());
                                //需要获取时间

                                String inputTime = "";
                                if(triE.get("ezob_kssj")!=null) {
                                    inputTime=  " " +formatSecondsToHHmmss(Integer.parseInt(triE.get("ezob_kssj").toString()));
                                }
                                String inputTimeE = "";
                                if(triE.get("ezob_jssj")!=null) {
                                    inputTimeE= " " +formatSecondsToHHmmss(Integer.parseInt(triE.get("ezob_jssj").toString()));
                                }
                                String rstartdateT="";
                                if(triE.get("startdate").toString().contains("-")||triE.get("startdate").toString().contains("/"))
                                {
                                    rstartdateT =triE.get("startdate").toString().split(" ")[0] + " " + inputTime;
                                }
                                else{
                                    rstartdateT =recommendE.gettime(triE.get("startdate").toString()) + " " + inputTime;
                                }
                                String renddateT ="";
                                if(triE.get("enddate").toString().contains("-")||triE.get("enddate").toString().contains("/"))
                                {
                                    renddateT =triE.get("enddate").toString().split(" ")[0] + " " + inputTimeE;
                                }
                                else{
                                    renddateT = recommendE.gettime(triE.get("enddate").toString()) + " " + inputTimeE;

                                }

                                jsonObject.set("start_datetime", rstartdateT);

                                //jsonObject.set("end_datetime", renddateT);

                                String stress = verficationHR.btnE(userE.get("number").toString(), rstartdateT, renddateT);
                                if (stress.equals("400")) {
                                    this.operationResult.setMessage("获取推荐时数异常");
                                    e.setCancel(true);
                                    return;

                                }
                                ObjectMapper aMapperSS = new ObjectMapper();
                                JsonNode rootNodeSS = aMapperSS.readTree(stress);
                                String codeV = rootNodeSS.path("MSG_CODE").asText();
                                String codeV1 = rootNodeSS.path("msg_code").asText();
                                if (codeV.equals("1") || codeV1.equals("1")) {

                                    String plvhours = rootNodeSS.path("DATA1").get(0).path("p_lvhours").asText();
                                    ObjectMapper objectMapper = new ObjectMapper();

                                    jsonObject.set("end_datetime", renddateT);
                                    jsonObject.set("biz_code", "L24");
                                    jsonObject.set("hrs", plvhours);
                                    jsonObject.set("remarks", "");
                                    jsonT += jsonObject.toString() + ",";
                                } else {

                                    this.operationResult.setMessage("获取推荐时数异常");

                                    e.setCancel(true);
                                    return;
                                }
                                result(e,jsonT,url);
                            }//单出差人尾部


                        }
//                        if (jsonT.endsWith(",")) {
//                            // 去掉最后一个字符
//                            jsonT = jsonT.substring(0, jsonT.length() - 1);
//                        }
//                        jsonT += "]";
//                        if(!e.cancel) {//未取消过时，推送数据
//                            Map<String, Object> map = new HashMap<>();
//                            map.put("Content-Type", "application/json");
//                            HttpRequest request = HttpRequest.post(url).form(map).body(jsonT);
//                            String bodys = request.execute().body();
//                            //String aa = bodys;
//                            ObjectMapper MapperTS = new ObjectMapper();
//                            JsonNode rootNodeTS = MapperTS.readTree(bodys);
//                            String CodeV = rootNodeTS.path("MSG_CODE").asText();
//                            String CodeV1 = rootNodeTS.path("msg_code").asText();
//                            if (CodeV.equals("1") || CodeV1.equals("1")) {
//
//                            }
//                            else{
//                                String msg_desc = rootNodeTS.path("MSG_DESC").asText();
//                                String msg_desc1 = rootNodeTS.path("msg_desc").asText();
//                                this.operationResult.setMessage("同步数据异常"+msg_desc+msg_desc1);
//                                e.setCancel(true);
//                                return;
//                            }
//
//
//
//                        }
                        /* this.operationResult.setMessage(bodys); */

                    } catch (Exception ex) {
                        //ex.printStackTrace();
                        this.operationResult.setMessage(ex.getMessage());
                        e.setCancel(true);
                        return;
                    }

                }
            }

        } else {

            this.operationResult.setMessage(verJG);
            e.setCancel(true);
            return;
        }

    }
    //判断时间和组织
    public String dateAndOrganization(BeforeOperationArgs e)
    {
        DynamicObject[] tripList = e.getDataEntities();
        for (DynamicObject entrydata : tripList) {
            try {
                DynamicObjectCollection tripentryList = entrydata.getDynamicObjectCollection("tripentry");
                for (DynamicObject triE : tripentryList) {
                    JSONObject jsonObject = new JSONObject();

                    String inputTime = "";
                    if (triE.get("ezob_kssj") != null)
                        inputTime = " " + formatSecondsToHHmmss(Integer.parseInt(triE.get("ezob_kssj").toString()));
                    String inputTimeE = "";
                    if (triE.get("ezob_jssj") != null)
                        inputTimeE = " " + formatSecondsToHHmmss(Integer.parseInt(triE.get("ezob_jssj").toString()));
                    String rstartdateT = "";
                    if (triE.get("startdate").toString().contains("-") || triE.get("startdate").toString().contains("/")) {
                        rstartdateT = triE.get("startdate").toString().split(" ")[0] + " " + inputTime;
                    } else {
                        rstartdateT = recommendE.gettime(triE.get("startdate").toString()) + " " + inputTime;
                    }
                    String renddateT = "";
                    if (triE.get("enddate").toString().contains("-") || triE.get("enddate").toString().contains("/")) {
                        renddateT = triE.get("enddate").toString().split(" ")[0] + " " + inputTimeE;
                    } else {
                        renddateT = recommendE.gettime(triE.get("enddate").toString()) + " " + inputTimeE;
                    }
                    jsonObject.set("start_datetime", rstartdateT);
                    String stress = "";
                    String fyNumber = entrydata.getDynamicObject("costcompany") .getString("number");
                    String cbzx = entrydata.getDynamicObject("std_costcenter") .getString("name");


                    if (rstartdateT.compareTo("2023-09- 01 00:00:00 ") > 0 && renddateT.compareTo("2023-09-01 00:00:00 ") > 0&&!fyNumber.equals("LE0011")&&!fyNumber.equals("LE0012")&&!fyNumber.equals("LE0016")&&!cbzx.equals("管委会")&&!fyNumber.equals("LE0013")&&!fyNumber.equals("LE0014")&&!fyNumber.equals("LE0015")&&!fyNumber.equals("LE0018")&&!fyNumber.equals("LE0089")) {
                        return "250";
                    } else {


                        return "500";

                    }


                }


            } catch (Exception ex) {
                this.operationResult.setMessage(ex.getMessage());
                e.setCancel(true);
                return "error";
            }
        }
                return "000";
    }

    public void result(BeforeOperationArgs e,String jsonT,String url) throws IOException {
        if (jsonT.endsWith(",")) {
            // 去掉最后一个字符
            jsonT = jsonT.substring(0, jsonT.length() - 1);
        }
        jsonT += "]";
        if(!e.cancel) {//未取消过时，推送数据
            Map<String, Object> map = new HashMap<>();
            map.put("Content-Type", "application/json");
            HttpRequest request = HttpRequest.post(url).form(map).body(jsonT);
            String bodys = request.execute().body();
            System.out.println("公干同步结果:"+ bodys);
            log.info("jsonT"+jsonT);
            log.info("公干同步结果:"+bodys);
            //String aa = bodys;
            ObjectMapper aMapperTS = new ObjectMapper();
            JsonNode rootNodeTS = aMapperTS.readTree(bodys);
            String codeV = rootNodeTS.path("MSG_CODE").asText();
            String codeV1 = rootNodeTS.path("msg_code").asText();
            if (codeV.equals("1") || codeV1.equals("1")) {

            }
            else{
                String msgdesc = rootNodeTS.path("MSG_DESC").asText();
                String msgdesc1 = rootNodeTS.path("msg_desc").asText();
                this.operationResult.setMessage("同步数据异常"+msgdesc+msgdesc1);
                e.setCancel(true);
                return;
            }



        }
    }
    public String md5(String guid) {
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

    public String getverificatione(BeforeOperationArgs e) {
        String verJG = "3";
        try {
            DynamicObject[] tripList = e.getDataEntities();
            DynamicObjectCollection tripentryList = tripList[0].getDynamicObjectCollection("tripentry");
            for (DynamicObject triE : tripentryList) {  //循环表体

                DynamicObject userE = (DynamicObject) tripList[0].get("creator");
                String aUSERID = userE.get("number").toString();
                String actioncode =tripList[0].get("ischange").toString();
//                if(action_code.equals("false")){
                actioncode="1";        //江伟维：由于改为行程变更时就推送删除数据过去，所以审核成功后的action_code统统为1
//                }else {
//                    action_code="2";
//                }

                String inputTime = formatSecondsToHHmmss(Integer.parseInt(triE.get("ezob_kssj").toString()));
                String inputTimeE = formatSecondsToHHmmss(Integer.parseInt(triE.get("ezob_jssj").toString()));
                String aSTime="";
                if(triE.get("startdate").toString().contains("-")||triE.get("startdate").toString().contains("/"))
                {
                    aSTime =triE.get("startdate").toString().split(" ")[0] + " " + inputTime;
                }
                else{
                    aSTime =recommendE.gettime(triE.get("startdate").toString()) + " " + inputTime;
                }
                String aETime ="";
                if(triE.get("enddate").toString().contains("-")||triE.get("enddate").toString().contains("/"))
                {
                    aETime =triE.get("enddate").toString().split(" ")[0] + " " + inputTimeE;
                }
                else{
                    aETime = recommendE.gettime(triE.get("enddate").toString()) + " " + inputTimeE;

                }
                String keyedT = triE.get(0).toString();
                //判断是否是多出差人
                Boolean bl = tripList[0].getBoolean("istravelers");

                DynamicObjectCollection dynamicObjects=  triE.getDynamicObjectCollection("travelers");
//               List<String> numbers = dynamicName.stream().map(su ->su.getString("fbasedataid")).collect(Collectors.toList());
                if(bl)
                {
                    String number="";
                    for (DynamicObject dynamicObjectE:dynamicObjects)
                    { //循环表体出差人
                        DynamicObject userNumber = dynamicObjectE.getDynamicObject("fbasedataid");
                        number =  userNumber.getString("number");
                        verJG = verficationHR.post1(number, aSTime, aETime, keyedT,actioncode);
                        if(!StringUtils.equals(verJG, "200"))//不通过时直接返回
                        {
                            return verJG;
                        }
                    }







                }else {
                    verJG = verficationHR.post1(aUSERID, aSTime, aETime, keyedT,actioncode);
                    if(!StringUtils.equals(verJG, "200"))//不通过时直接返回
                    {
                        return verJG;
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return ex.getMessage();
        }
        return verJG;



    }
    private  String formatSecondsToHHmmss(int seconds) {
        int hours = seconds / 3600; // 计算小时数
        int minutes = (seconds % 3600) / 60; // 计算分钟数
        int remainingSeconds = seconds % 60; // 计算剩余的秒数

        // 使用String.format()方法将时间格式化为HH:mm:ss格式
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }
    public String settime(String time){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDate date = LocalDate.parse(time, formatter);
        String outputDate = date.toString();
        return outputDate;
    }





}