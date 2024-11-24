package kd.cosmic;


import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.list.IListView;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;
import net.sf.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 描述: 在列表推送资产收货通知单
 * 开发者: 易佳伟
 * 创建日期: 1期完成
 * 关键客户：无
 * 已部署正式：false
 * 备注：无
 */
public class pushreceiptnotice extends AbstractListPlugin implements Plugin {

    private final static String KEY_BARITEM = "ezob_pushinvoice";

    private static Log log = LogFactory.getLog(pushreceiptnotice.class);
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);

        this.addItemClickListeners(KEY_BARITEM);
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        GY gy = new GY();
        int successCount = 0;
        int failureCount = 0;
        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {
            ListSelectedRowCollection selectedRows = ((IListView) this.getView()).getSelectedRows();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
            String outputTimeFormat = "yyyy-MM-dd HH:mm:ss";
            String url = gy.url+"api/v2/app/kd/synReceiptM";
            // 定义header对象4
            HttpHeaders headers = new HttpHeaders();
            //header请求参数
            String appSecret = "GME";
            String appId = gy.appid;
            headers.set("Content-Type","application/json");
            headers.set("MV-Div",appSecret);
            headers.set("MV-AppId",appId);
            Map<String, Object> paramBody = new HashMap<String, Object>();

            try {
                    String remark = "";
                    for (ListSelectedRow list1 : selectedRows) {
                        String billNo1 = list1.getBillNo();
                        DynamicObject aAps = BusinessDataServiceHelper.loadSingle(list1.getPrimaryKeyValue(),"pm_receiptnotice");
                        String vendorDn = aAps.getString("billNo");   //单据编号
                        String acceptDate = aAps.getString("biztime").substring(0,19);//验收时间
                        String suppCode = "";
                        if(aAps.get("providersupplier")!=null){
                            suppCode = ((DynamicObject) aAps.get("providersupplier")).get("ezob_mvjbm").toString();//供货商编号
                        }

                        String recdUser ="";
                        if(aAps.get("creator")!=null) {
                            recdUser = ((DynamicObject) aAps.get("creator")).get("number").toString();//制单人工号
                        }
                        if (aAps.getString("comment") != null) {
                            remark = aAps.getString("comment");
                        }//备注
                        String poNumber = aAps.getString("ezob_pobh");//po编号

                        String divCode="";
                        if(aAps.get("ezob_mverpzt")!=null){
                            divCode = ((DynamicObject) aAps.get("ezob_mverpzt")).getString("number"); //meERP编码
                        }

                        DynamicObjectCollection cols = aAps.getDynamicObjectCollection("billentry");

                        int length = cols.size();
                        List<Object> itemLists = new ArrayList<>();//itemLists 列表明细
                        try {
                            for (int i = 0; i < length; i++) {
                                Map<String, Object> itemMap = new HashMap<>();
                                DynamicObject col = cols.get(i);
                                String itemCode = ((DynamicObject) ((DynamicObject) col.get("material")).get("masterid")).get("number").toString();//物料编码
                                String itemName = col.getString("ezob_ms");//物料描述
                                String recdQty = col.getString("qty");
                                //deliverdate
                                String srcbillentity = col.getString("srcbillentity");
                                String srcbillid =col.getString("srcbillid");
                                DynamicObject aCGDD = BusinessDataServiceHelper.loadSingle(srcbillid,srcbillentity);
                                DynamicObjectCollection aDatetime = aCGDD.getDynamicObjectCollection("billentry");
                                String planDate =  aDatetime.get(i).getString("deliverdate").substring(0,10);
                                //添加到ltemList
                                itemMap.put("itemCode ", itemCode );
                                itemMap.put("itemName", itemName);
                                itemMap.put("poNumber", poNumber);
                                itemMap.put("recdQty", recdQty);
                                itemMap.put("planDate", planDate);
                                itemLists.add(itemMap);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            this.getView().showMessage(e.getMessage());
                        }

                        paramBody.put("vendorDn",vendorDn);
                        paramBody.put("acceptDate",acceptDate);
                        paramBody.put("suppCode",suppCode);
                        paramBody.put("recdUser",recdUser);
                        paramBody.put("remark",remark);
                        paramBody.put("divCode",divCode);
                        paramBody.put("itemLists",itemLists);

                        JSONObject json = new JSONObject() ;//获取json对象
                        json=JSONObject.fromObject(paramBody);//
                        JSONObject aJ2=new JSONObject();
                        //JSONArray Jry=new JSONArray();
                        //Jry.add(json);
                        aJ2.put("data",json);
                        aJ2.toString();
                        String cs = aJ2.toString();
                        System.out.println(cs);
                        HttpEntity<String> entity = new HttpEntity<String>(cs,headers);

                        // 发送请求
                        RestTemplate template = new RestTemplate(RestTemplateConfiguration.generateHttpRequestFactory());
                        ResponseEntity<String> exchange = template.exchange(url, HttpMethod.POST, entity, String.class);

                        if(exchange.getStatusCodeValue()==200)
                        {
                            ObjectMapper aMapperTS = new ObjectMapper();
                            JsonNode rootNodeTS = aMapperTS.readTree(exchange.getBody().toString());
                            String aCodeV = rootNodeTS.path("code").asText();
                            if(aCodeV.equals("200")) {
                                aAps.set("ezob_pushstatus", "推送成功");
                                SaveServiceHelper.update(aAps);
                                successCount++;

                            }
                            else{
                                aAps.set("ezob_pushstatus", "推送错误情况:"+rootNodeTS.path("data").path("failResult").toString());
                                SaveServiceHelper.update(aAps);
                                log.error(String.valueOf(exchange.getStatusCode())+"pushreceiptnotice");
                                failureCount++;
                            }
                        }
                        else{
                            aAps.set("ezob_pushstatus","请求异常："+exchange.getStatusCode());
                            SaveServiceHelper.update(aAps);
                            failureCount++;
                        }
                        this.getView().showMessage("同步采购收货单"+selectedRows.size()+"条,推送成功"+successCount+",推送失败"+failureCount);
                        this.getView().invokeOperation("refresh");
                    }
            }catch (Exception e){
                e.printStackTrace();
                this.getView().showMessage("推送成功"+successCount+"条,失败"+failureCount+"条,推送时异常"+e.getMessage());

        }
    }
}}
