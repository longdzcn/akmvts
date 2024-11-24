package kd.cosmic.Test;


import cfca.sadk.util.Base64;
import com.cloudera.impala.jdbc41.internal.fasterxml.jackson.databind.JsonNode;
import com.cloudera.impala.jdbc41.internal.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.workflow.engine.msg.ctx.MessageContext;
import kd.bos.workflow.engine.msg.info.ToDoInfo;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 *  author：易佳伟
 *  createDate : 2023/05/07
 *  description: 蓝凌OA测试环境插件，接口调用写在里面，被LanglingServiceHandler调用 （测试环境使用）
 *
 */
public   class LanglingOATaskUtilsTest {
//    public static void main(String[] args) {
//        pushTodo();
//    }

    private static Log logger = LogFactory.getLog(LanglingOATaskUtilsTest.class);

    public static String getUserNo(long userId){

        //ezob_bos_user_ext 测试账套

        QFilter qFilter = new QFilter("id","=", userId);

        DynamicObject obj = BusinessDataServiceHelper.loadSingle(userId, "bos_user");

        String userNo = (String) obj.get("number");



        return userNo;
    }

    public  static  void pushTodo(ToDoInfo toDoInfo, MessageContext ctx) throws JsonProcessingException, com.cloudera.impala.jdbc41.internal.fasterxml.jackson.core.JsonProcessingException {

        List<Long> userList =toDoInfo.getUserIds();


        for (Long userIds:userList) {

            String userNo =null;

            if(toDoInfo.getUserIds().size()>0)
            {
                userNo = getUserNo(userIds);
            }

            // 定义请求接口URL
            String url = "https://oatest.akmmv.com/api/sys-notify/sysNotifyTodoRestService/sendTodo";

            // 定义header对象
            HttpHeaders headers = new HttpHeaders();

            // 如果EKP对该接口启用了Basic认证，那么客户端需要加入认证header信息
            String accountID = "oatest"; // 认证用户名
            String accountPassword = "oatest"; // 认证密码
            addAuth(headers,accountID+":"+accountPassword);
            Date date = ctx.getCreateDate();

            String format = date.toString();
            String createTime = getTime(format);
            // 定义请求参数Map
            Map<String,Object> paramBody = new HashMap<String,Object>();
            paramBody.put("targets", "{\"PersonNo\":\""+userNo+"\"}");
            paramBody.put("type", 1);
            paramBody.put("modelName",ctx.getEntityName());
            paramBody.put("appName", "LangLingApp");
            paramBody.put("modelId",toDoInfo.getTaskId());
            paramBody.put("subject","[金蝶]"+toDoInfo.getContent());
            paramBody.put("link",toDoInfo.getUrl()+"&userNumber="+userNo);
            paramBody.put("mobileLink",toDoInfo.getUrl()+"&userNumber="+userNo);
            paramBody.put("padLink",toDoInfo.getUrl()+"&userNumber="+userNo);
            paramBody.put("createTime",createTime);

            // 定义http请求实体对象
            HttpEntity<Map<String,Object>> entity = new HttpEntity<Map<String,Object>>(paramBody,headers);


            // 发送请求
            RestTemplate template = new RestTemplate();
            ResponseEntity<Map> exchange = template.exchange(url, HttpMethod.POST, entity, Map.class);
            ObjectMapper aMapperTS = new ObjectMapper();
            JsonNode rootNodeTS = aMapperTS.readTree(exchange.getBody().toString());
            logger.info("蓝凌测试:"+exchange.getBody().toString());

        }


    }



        public  static  void pushDelTodo(ToDoInfo toDoInfo,MessageContext ctx)
    {
        List<Long> userList = toDoInfo.getUserIds();


        for (Long userIds : userList) {

            String userNo = null;

            if (toDoInfo.getUserIds().size() > 0) {
                userNo = getUserNo(userIds);
            }

        // 定义请求接口URL
        String url = "https://oatest.akmmv.com/api/sys-notify/sysNotifyTodoRestService/deleteTodo";

        // 定义header对象
        HttpHeaders headers = new HttpHeaders();

        // 如果EKP对该接口启用了Basic认证，那么客户端需要加入认证header信息
        String accountID = "oatest"; // 认证用户名
        String accountPassword = "oatest"; // 认证密码
        addAuth(headers,accountID+":"+accountPassword);

        // 定义请求参数Map
        Map<String,Object> paramBody = new HashMap<String,Object>();
        paramBody.put("targets", "{\"PersonNo\":\""+userNo+"\"}");
        paramBody.put("optType", 2);
        paramBody.put("modelName", ctx.getEntityName());
        paramBody.put("modelId", toDoInfo.getTaskId());



        // 定义http请求实体对象
        HttpEntity<Map<String,Object>> entity = new HttpEntity<Map<String,Object>>(paramBody,headers);


        // 发送请求
        RestTemplate template = new RestTemplate();
        ResponseEntity<Map> exchange = template.exchange(url, HttpMethod.POST, entity, Map.class);
        System.out.println(exchange.getBody());
        }
    }
        public  static  void pushTodoDone(ToDoInfo toDoInfo,MessageContext ctx) {

            List<Long> userList = toDoInfo.getUserIds();


            for (Long userIds : userList) {

                String userNo = null;

                if (toDoInfo.getUserIds().size() > 0) {
                    userNo = getUserNo(userIds);
                }

                // 定义请求接口URL
                String url = "https://oatest.akmmv.com/api/sys-notify/sysNotifyTodoRestService/setTodoDone";

                // 定义header对象
                HttpHeaders headers = new HttpHeaders();

                // 如果EKP对该接口启用了Basic认证，那么客户端需要加入认证header信息
                String accountID = "oatest"; // 认证用户名
                String accountPassword = "oatest"; // 认证密码
                addAuth(headers, accountID + ":" + accountPassword);

                // 定义请求参数Map
                Map<String, Object> paramBody = new HashMap<String, Object>();
                paramBody.put("targets", "{\"PersonNo\":\""+userNo+"\"}");
                paramBody.put("optType", 2);
                paramBody.put("modelName", ctx.getEntityName());
                paramBody.put("modelId", toDoInfo.getTaskId());


                // 定义http请求实体对象
                HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(paramBody, headers);


                // 发送请求
                RestTemplate template = new RestTemplate();
                ResponseEntity<Map> exchange = template.exchange(url, HttpMethod.POST, entity, Map.class);
                System.out.println(exchange.getBody());
            }
        }



            //更新待办
            public  static  void updateTodo(ToDoInfo toDoInfo, MessageContext ctx)
            {

        String titel = toDoInfo.getTitle();
        // 定义请求接口URL
        String url = "https://oatest.akmmv.com/api/sys-notify/sysNotifyTodoRestService/updateTodo";

        // 定义header对象
        HttpHeaders headers = new HttpHeaders();

        // 如果EKP对该接口启用了Basic认证，那么客户端需要加入认证header信息
        String accountID = "oatest"; // 认证用户名
        String accountPassword = "oatest"; // 认证密码
        addAuth(headers,accountID+":"+accountPassword);
        Date date = ctx.getCreateDate();

        String format = date.toString();
        String createTime = getTime(format);
        // 定义请求参数Map
        Map<String,Object> paramBody = new HashMap<String,Object>();

        paramBody.put("modelName",ctx.getEntityName());
        paramBody.put("modelId",toDoInfo.getTaskId());
        paramBody.put("subject",toDoInfo.getContent());
        paramBody.put("link",toDoInfo.getUrl());
        paramBody.put("mobileLink","https://www.baidu.com/");
        paramBody.put("padLink","https://www.baidu.com/");
        paramBody.put("type", 1);
        paramBody.put("level",3);


        // 定义http请求实体对象
        HttpEntity<Map<String,Object>> entity = new HttpEntity<Map<String,Object>>(paramBody,headers);


        // 发送请求
        RestTemplate template = new RestTemplate();
        ResponseEntity<Map> exchange = template.exchange(url, HttpMethod.POST, entity, Map.class);
        System.out.println(exchange.getBody());
    }

            //蓝凌调用接口auth用户验证
            private static void addAuth(HttpHeaders headers,String yourEncryptedWorlds){
        byte[] encodedAuth = Base64.encode(yourEncryptedWorlds.getBytes(Charset.forName("UTF-8")));
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader );

    }

            //将金蝶的英语日期转成数字日期 EEE MMM dd HH:mm:ss zzz uuuu 转 yyyy-MM-dd HH:mm:ss
            public static  String  getTime(String dateTimeString)
            {


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz uuuu", Locale.US);

        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString, formatter);

        LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();

        String formattedDateTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println("Formatted Date and Time: " + formattedDateTime);

        return  formattedDateTime;
    }
}

