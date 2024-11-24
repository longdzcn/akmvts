package kd.cosmic.connector;

import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.MulBasedataDynamicObjectCollection;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.list.IListView;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.cosmic.RestTemplateConfiguration;
import kd.sdk.plugin.Plugin;
import kd.taxc.common.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.EventObject;

/**
 * 标准单据列表插件
 */
public class KhSynchronisedMV extends AbstractListPlugin implements Plugin {
    //同步按钮
    private final static String KEY_BARITEM = "ezob_tbmv";

    private static Log log = LogFactory.getLog(KhSynchronisedMV.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(KEY_BARITEM);
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        String msg = "";
        String code = "";
        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {
//            正式
//            String url = "https://mverp.webapi.meadvilletech.com/api/v2/app/kd/syncCustomerInfo";
//            测试
            String url = "http://10.22.10.249:8020/api/v2/app/kd/syncCustomerInfo";
            HttpHeaders headers = new HttpHeaders();
//            分厂代码
            String appSecret = "GME";
//            AppId账号
//            String appId = "m5ULwr1y6613qLyxsrpKgFNyuFGZfHay";
            String appId = "L8ruVx0ZQPL8YL5zYO2q9BXfdHbw8u0E";
//            时间戳
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            String dTimestamp = "1670309475";
//            设置请求头
            headers.set("Content-Type", "application/json");
            headers.set("MV-Div", appSecret);
            headers.set("MV-AppId", appId);
            headers.set("MV-Timestamp", timestamp);
//            外层data
            JSONObject root = new JSONObject();
//            data数组
            JSONArray data = new JSONArray();

            JSONObject paramBody = new JSONObject();
//          获取在列表上勾选的客户信息
            ListSelectedRowCollection selectedRows = ((IListView) this.getView()).getSelectedRows();
//            遍历每个客户信息
            for (ListSelectedRow selectedRow : selectedRows) {
                DynamicObject customer = BusinessDataServiceHelper.loadSingle(selectedRow.getPrimaryKeyValue(), "bd_customer");
//                获取当前同步人
                RequestContext requestContext = RequestContext.get();
                long currUserId = requestContext.getCurrUserId();
                DynamicObject bosuser = BusinessDataServiceHelper.loadSingle(currUserId, "bos_user", "name,ezob_mverpzt3,hihn_tradeterms");
                //MV账套
                String ezobMverpzt3 = bosuser.getString("ezob_mverpzt3");
                if (bosuser!=null){
                    String str = StringUtil.strip(ezobMverpzt3, ",");
                    String[] sp = str.split(",");
                    if ( !str.isEmpty()){
//                        根据工厂循环
                        for (String s : sp) {
                            paramBody.put("DIV_CODE",s);
                        }
                    }else {
                        this.getView().getControl("请维护mvERP账套！");
                    }
                }else {
                    this.getView().getControl("请维护mvERP账套！");
                }                //                当前联系人分录
                DynamicObjectCollection entryLinkman = customer.getDynamicObjectCollection("entry_linkman");
                int j = 0;
                if (entryLinkman != null){
                    for (DynamicObject dynamicObject : entryLinkman) {
                        j++;
//                        邮箱
//                        String email = dynamicObject.getString("email");
//                        if (email != null) {
//                            paramBody.put("EMAIL", email);                        }
//
//                        联系人名称（多文本语言，可能要加.toString）
                        String lxrmz = dynamicObject.getString("contactperson");
                        paramBody.put("CONTACT_NAME_" + j, lxrmz);
//                        联系人职称（多文本语言，可能要加.toString）
                        String lxrzc = dynamicObject.getString("contactpersonpost");
                        paramBody.put("TITLE_FOR_CONTACT" + j, lxrzc);
//                        联系人电话
                        String phone = dynamicObject.getString("phone");
                        paramBody.put("CONT_PHONE_" + j,phone);
//                        联系人邮箱
                        String email = dynamicObject.getString("email");
                        paramBody.put("EMAIL_FOR_CONTACT" + j,email);
                    }
                }
//                客户编码
                String number = customer.getString("number");
                paramBody.put("CUST_CODE",number);
//                客户名称
                String khmc = customer.getString("name");
                paramBody.put("CUSTOMER_NAME",khmc);
//                客户简称
                String khjc = customer.getString("simplename");
                paramBody.put("ABBR_NAME", khjc);
//                集线器发票流程（无则默认0）
                paramBody.put("HUB_INVOICE_FLOW",0);
//                传真
                String cz = customer.getString("bizpartner_fax");
                paramBody.put("FAX",cz);
//                联系电话
                String phone = customer.getString("bizpartner_phone");
                paramBody.put("PHONE",phone);
                //地区
                paramBody.put("REGION","N/A");
//                国家代码
                String gjdm = customer.getString("country.ezob_mverp");
//                String guonei = customer.getString("country.number");
                paramBody.put("COUNTRY_CODE", gjdm);
//                判断是否国内
                String guonei = customer.getString("country.name");
                if (guonei != null && (guonei.contains("中国") || guonei.contains("中国香港") || guonei.contains("中国澳门") || guonei.contains("中国台湾"))){
                    paramBody.put("DOMESTIC","Y");
                }else {
                    paramBody.put("DOMESTIC","N");
                }
//                收款地址1
                String skdz = customer.getString("bizpartner_address");
                paramBody.put("BILLING_ADDRESS_1", skdz);
//                客户状态
                String khzt = customer.getString("customerstatus.name");
                if(khzt.equals("正常")) {
                    paramBody.put("CUST_STATUS", 1);
                } else if (khzt.contains("处理")) {
                    paramBody.put("CUST_STATUS",2);
                } else{
                    paramBody.put("CUST_STATUS",3);
                }
//                获取终端客户分录信息
                DynamicObjectCollection ezobKhxx = customer.getDynamicObjectCollection("ezob_entry_khxx");
                JSONArray endCustList = new JSONArray();
                String zdkhbm = null;
                String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                int index = 0;
                if (ezobKhxx.size() > 0) {
                    for (DynamicObject dynamicObject : ezobKhxx) {
                        char currentChar = abc.charAt(index);
                        zdkhbm = dynamicObject.getString("ezob_zdkh.number");
                        JSONObject endCustListJson = new JSONObject();
                        endCustListJson.put("END_CUST_CODE",zdkhbm);
                        endCustListJson.put("RESTRICT_CUST","N");
                        String s = String.valueOf(currentChar);
                        endCustListJson.put("END_CUST_NUM",s);
                        index++;
                        endCustList.put(endCustListJson);
                    }
                }else {
                    msg = "终端客户不存在，取消推送";
                    this.getView().showMessage(msg);
                    return;
                }
                QFilter q4 = new QFilter("number",QCP.equals,zdkhbm);
                DynamicObject dynamicObject2 = BusinessDataServiceHelper.loadSingle("bd_customer", "number,ezob_dyxsmc", new QFilter[]{q4});
                String oEMxsdbdm = dynamicObject2.getString("ezob_dyxsmc.number");
                paramBody.put("OEM_SALES_REP_CODE", oEMxsdbdm);
//                OEM最终客户代码列表
                paramBody.put("END_CUST_LIST",endCustList);
//                OEM销售代表代码(需与mvERP匹配)
//                paramBody.put("OEM_SALES_REP_CODE", "1475");//测试
//                OEM最终客户国家代码(需与mvERP匹配)
                paramBody.put("END_CUSTOMER_COUNTRY_CODE","CHINA");
//                市场部
                String jskhdmx = customer.getString("ezob_jskhdmx");
//                paramBody.put("FTY_CODE", jskhdmx);
                if ("01.01.0052".equals(jskhdmx)) {
                    String scbdm = "GME";
                    paramBody.put("FTY_CODE", scbdm);
                } else if ("01.01.0188".equals(jskhdmx)){
                    String scbdm = "XMC";
                    paramBody.put("FTY_CODE", scbdm);
                } else if ("04.0009".equals(jskhdmx)){
                    String scbdm = "MV";
                    paramBody.put("FTY_CODE", scbdm);
                } else if ("04.01.0010".equals(jskhdmx)){
                    String scbdm = "AKMI";
                    paramBody.put("FTY_CODE", scbdm);
                } else if ("01.01.0001".equals(jskhdmx)){
                    String scbdm = "AKMGZ";
                    paramBody.put("FTY_CODE", scbdm);
                } else if ("01.01.0051".equals(jskhdmx)){
                    msg = "同步MVERP失败，不存在接单公司为广州安博新能源科技有限公司";
                    this.getView().showMessage(msg);
                    return;
                }
//                获取货币代码
                String hbdm = customer.getString("settlementcyid.number");
                if (hbdm != null) {
                    if (hbdm.equals("CNY")){
                        paramBody.put("CURRENCY_CODE", "RMB");
                    } else {
                        paramBody.put("CURRENCY_CODE",hbdm);
                    }
                }
                if ("02.05.0019".equals(jskhdmx) && "CNY".equals(hbdm)){
                    String scbdm = "AKMSZ";
                    paramBody.put("FTY_CODE", scbdm);
                } else if ("02.05.0019".equals(jskhdmx)){
                    String scbdm = "SZUSD";
                    paramBody.put("FTY_CODE", scbdm);
                }
//                获取销售代表基础资料代码
                String dywxsdb = customer.getString("ezob_dyxsmc.number");
                paramBody.put("SALES_REP_CODE",dywxsdb);//测试
//                String xsdbnumber = "1475";
//                查询对应销售代表数据
//                QFilter q2 = new QFilter("number",QCP.equals,xsdbnumber);
//                DynamicObject dynamicObject1 = BusinessDataServiceHelper.loadSingle("ezob_xsdb", "name,ezob_yxzxfzr,ezob_xsdbdss,ezob_yxzx", new QFilter[]{q2});
//                String xsdbss = dynamicObject1.getString("ezob_xsdbdss.name");
//                String yxzx = dynamicObject1.getString("ezob_yxzx.name");
                QFilter q2 = new QFilter("number", QCP.equals, dywxsdb);
                DynamicObject dynamicObject1 = BusinessDataServiceHelper.loadSingle("bos_user", "number,ezob_superior_number,entryentity,orgstructure,dpt", new QFilter[]{q2});
                String xsdbsj = dynamicObject1.getString("ezob_superior_number");
                paramBody.put("SALES_REP_CODE1", xsdbsj);//测试
//                DynamicObjectCollection entryentity = dynamicObject1.getDynamicObjectCollection("entryentity");
//                for (DynamicObject dynamicObject : entryentity) {
//                    String yxzxfzrbm = dynamicObject1.getString("ezob_superior_number");
//                }
                    paramBody.put("SALES_REP_CODE2", xsdbsj);//测试



//                是否含税
                String sfhs = customer.getString("ezob_sfhs");
                if (sfhs.equals("true")){
                    paramBody.put("INCLUDE_TAX","Y");
                }else {
                    paramBody.put("INCLUDE_TAX","N");
                }
//                有无打印重量
                paramBody.put("PRINTWEIGHTS","Y");
//                IPT标志(是: 1，否: 2 , 默认2)
                paramBody.put("IPT_FLAG",2);
//                付款条件
                String ezobfktjdm = customer.getString("receivingcondid.ezob_mverpdydm");
                paramBody.put("PAYTERM_CODE",ezobfktjdm);//付款条件
//                CIA级别
                String fktjName = customer.getString("receivingcondid.name");//待沟通
                if (fktjName.contains("CIA")||fktjName.contains("COD")){
                    paramBody.put("CIA_LEVEL",2);
                }else {
                    paramBody.put("CIA_LEVEL",0);
                }
//                客户建档日期(格式：YYYY-MM-DD HH24:mi:ss)
                paramBody.put("CUST_ENT_DATE",getCurrTime());
//                语言标志(E代表English，C代表Chinese)
                paramBody.put("LANGUAGE_FLAG","C");
//                最长保留天数
                String vmizcblts = customer.getString("ezob_zcblts");
                paramBody.put("MAX_HOLD_DAYS",vmizcblts);
//                运输时间
                String vmiyssj = customer.getString("ezob_yssj");
                paramBody.put("IN_TRANSIT_TIME",vmiyssj);
//                非自动订单确认标志（是：Y，否：N）
                String fzdddqrbz = customer.getString("ezob_fzdddqrbz");
                if (fzdddqrbz.equals("true")){
                    paramBody.put("NONAUTO_ORDER_CONFIRM_FLAG","Y");
                }else {
                    paramBody.put("NONAUTO_ORDER_CONFIRM_FLAG","N");
                }
                //CS
                MulBasedataDynamicObjectCollection csfzr = (MulBasedataDynamicObjectCollection) customer.get("ezob_csfzrdx");
                if (csfzr.size() > 0) {
                    for (int i = 0; i < csfzr.size(); i++) {
                        if (i == 0) {
                            DynamicObject dynamicObject = csfzr.get(i).getDynamicObject("fbasedataid");
                            String cSnumber = dynamicObject.getString("number");
                            paramBody.put("CS_REPRES",cSnumber);
                        }else {
                            DynamicObject dynamicObject = csfzr.get(i).getDynamicObject("fbasedataid");
                            String cSnumber = dynamicObject.getString("number");
                            paramBody.put("CS_REPRES" + i,cSnumber);
                        }
                    }
                }
                System.out.println(csfzr);
                JSONArray shipAddressList = new JSONArray();
                QFilter q = new QFilter("customer.number", QCP.equals,number);
                int c = 0;


                DynamicObject[] load = BusinessDataServiceHelper.load("bd_address", "ezob_svmif,default,detailaddress,ezob_shddmc,addemail,ezob_buy_sell,name,phone,zipcode,ezob_svmif,hihn_tradeterms,addnumber,number,hihn_clearanceco,ezob_ysfs", new QFilter[]{q});
                for (DynamicObject dynamicObject : load) {
                    JSONObject xsdqxx = new JSONObject();
//                    贸易术语
                    String aDefault = dynamicObject.getString("default");
                    String hihnTradeterms = dynamicObject.getString("hihn_tradeterms.name");
                    xsdqxx.put("SHIPPING_MARK", hihnTradeterms);
//                    详细地址
                    String flexpanelap = dynamicObject.getString("detailaddress");
//                       paramBody.put("STATE",addfulladdress);//州/省
                    xsdqxx.put("LOCATION", flexpanelap);//送货
                    //是否Buy_sell
                    String buySell = dynamicObject.getString("ezob_buy_sell");
                    if (buySell.equals("true")) {
                        xsdqxx.put("BUY_SELL_MARK", "Y");
                    } else {
                        xsdqxx.put("BUY_SELL_MARK", "N");
                    }
//                    送款方式
                    String shfs = dynamicObject.getString("ezob_ysfs.name");
                    xsdqxx.put("SHIP_SHIPPING_METHOD", shfs);
//                    送货天数
//                    String shts = customer.getString("ezob_yssj");
                    String shts = customer.getString("ezob_yssj");
                    System.out.println(Integer.parseInt(shts));
                    xsdqxx.put("TRANSIT_DAYS", shts);//
//                    送货联系人
                    String name = dynamicObject.getString("name");
                    xsdqxx.put("SHIP_TO_CONTACT", name);//送货联系人
//                    联系电话
                    String addresphone = dynamicObject.getString("phone");
                    xsdqxx.put("SHIP_TO_PHONE", addresphone);//联系电话
                    String email = dynamicObject.getString("addemail");
                    xsdqxx.put("EMAIL_FOR_CONTACT", email);//联系人电子邮箱
                    String yzbm = dynamicObject.getString("zipcode");
                    xsdqxx.put("ZIP", yzbm);//邮政编码
                    String ezobSvmif = dynamicObject.getString("ezob_svmif");//是否VIM
                    if (ezobSvmif.equals("true")) {
                        xsdqxx.put("JIT", "Y");
                    } else {
                        xsdqxx.put("JIT", "N");
                    }
                    //                    送货地点名称
                    String ezobShddmc = dynamicObject.getString("ezob_shddmc");
                    if (ezobShddmc != null) {
                        xsdqxx.put("SHIP_TO_ADDRESS_1", ezobShddmc);
                    } else {
                        xsdqxx.put("SHIP_TO_ADDRESS_1", "送货地址为空");
                    }
                    shipAddressList.put(xsdqxx);
                }
                paramBody.put("SHIP_ADDRESS_LIST",shipAddressList);
                data.put(paramBody);
                root.put("data",data);
                String requestBody = root.toString();
                String token = requestBody + appId + timestamp;
                try {
                    String dToken = sign(token,appId);
                    headers.set("MV-Token",dToken);
                } catch (Exception e) {
                    msg = "Token处理有误！";
                    this.getView().showMessage(msg);
                    throw new RuntimeException(e);
                }
                JsonNode rootNodeTs = null;
                try {
                    rootNodeTs = getUrl(requestBody,url,headers);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                boolean success = rootNodeTs.path("success").asBoolean();
                if (success==true) {
                    this.getView().showMessage("同步成功！");
                }else {
                    msg = rootNodeTs.path("msg").toString();
                    this.getView().showMessage(msg);
                    log.info("同步mv失败：" + rootNodeTs);
                }
            }
        }
    }
    public static String getCurrTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }
    public static JsonNode getUrl(String data, String url, HttpHeaders headers) throws IOException {
        try {
            // 发送请求
            HttpEntity<String> entity = new HttpEntity<String>(data, headers);
            RestTemplate template = new RestTemplate(RestTemplateConfiguration.generateHttpRequestFactory());
            ResponseEntity<String> exchange = template.exchange(url, HttpMethod.POST, entity, String.class);
            ObjectMapper mapperTS = new ObjectMapper();
            if (exchange.getStatusCode().is2xxSuccessful()){
            }
            JsonNode rootNodeTS = null;
            rootNodeTS = mapperTS.readTree(exchange.getBody());
            return rootNodeTS;
        } catch (JsonProcessingException | NoSuchAlgorithmException | KeyManagementException |
                 KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }
    private String sign(String token, String appId) throws Exception {
        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretkey = new SecretKeySpec(appId.getBytes(), "HmacSHA256");
        sha256HMAC.init(secretkey);

        // 执行签名操作
        byte[] hash = sha256HMAC.doFinal(token.getBytes());

        // 将签名结果转换为Base64字符串
        return Base64.getEncoder().encodeToString(hash);
    }
}