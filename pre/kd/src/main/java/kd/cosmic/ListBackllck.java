package kd.cosmic;

import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.list.IListView;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述: 在列表推送供应商反写
 * 开发者: 易佳伟
 * 创建日期: 1期完成
 * 关键客户：黄小清
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class ListBackllck extends AbstractListPlugin implements Plugin {
    private final static String KEY_BARITEM = "ezob_gbmv";

    private static Log log = LogFactory.getLog(ListPushSupplier.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(KEY_BARITEM);
    }

    public void beforeItemClick(BeforeItemClickEvent evt) {
        GY gy = new GY();
        super.beforeItemClick(evt);
        String as = "";
        int successCount = 0;
        int failureCount = 0;
        Control source = (Control) evt.getSource();
        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {
            try{
                log.info("");

                // 定义请求接口URL
                String url = gy.url+"api/v2/app/kd/synMatCostInfo";
                // 定义header对象4
                HttpHeaders headers = new HttpHeaders();
                //header请求参数
                String appSecret = "GME";
                String appId = gy.appid;
                headers.set("Content-Type","application/json");
                headers.set("MV-Div",appSecret);
                headers.set("MV-AppId",appId);

                //获取当前选中的记录数，并循环推送，推送后保存状态成功
                //出错标识后继续推下一条记录

                ListSelectedRowCollection selectedRows = ((IListView)this.getView()).getSelectedRows();

                for (ListSelectedRow list1:selectedRows) {
                    try {

                        // 定义请求参数Map
                        Map<String, Object> paramBody = new HashMap<String, Object>();
                        //S为标准采购订单(ApFin_pur_BT_S)，其他都设置为M
                        DynamicObject sup =     BusinessDataServiceHelper.loadSingle(list1.getPrimaryKeyValue(),"bd_supplier");


                        JSONObject json = new JSONObject() ;
                        json=JSONObject.fromObject(paramBody);
                        JSONObject aJ2=new JSONObject();
                        JSONArray aJry=new JSONArray();
                        aJry.add(json);
                        aJ2.put("data",aJry);
                        aJ2.toString();
                        String cs = aJ2.toString();

                        as = cs;
                        HttpEntity<String> entity = new HttpEntity<String>(cs,headers);
                        // 发送请求
                        RestTemplate template = new RestTemplate(RestTemplateConfiguration.generateHttpRequestFactory());
                        ResponseEntity<String> exchange = template.exchange(url, HttpMethod.POST, entity, String.class);
                        ObjectMapper aMapperTS = new ObjectMapper();
                        JsonNode rootNodeTS = aMapperTS.readTree(exchange.getBody().toString());
                        String aCodeV = rootNodeTS.path("code").asText();
                        String msg = null;
                        //标识记录为推送成功或异常
                        if(exchange.getStatusCodeValue()==200)
                        {

                            msg   = rootNodeTS.path("msg").asText();
                            if(aCodeV.equals("200")) {
                                sup.set("ezob_pushstatus", "推送成功");
                                SaveServiceHelper.update(sup);
                                successCount++;
                            }
                            else{
                                sup.set("ezob_pushstatus","推送失败"+msg);
                                log.error("供应商推送失败"+msg);
                                SaveServiceHelper.update(sup);
                                failureCount++;
                                String[] cookings;
                            }
                        }
                        else{
                            sup.set("ezob_pushstatus","供应商请求异常："+msg);
                            SaveServiceHelper.update(sup);
                            failureCount++;
                        }

                    } catch (Exception e) {
                        //标识当条记录的推送异常
                        e.printStackTrace();
                        log.error(e);
                        this.getView().showMessage("供应商反写成功"+successCount+"条,失败"+failureCount+"条,推送时异常"+e.getMessage());
                    }

                }
                this.getView().showMessage("供应商"+selectedRows.size()+"条，推送成功"+successCount+"推送失败"+failureCount);
                this.getView().invokeOperation("refresh");

            }
            catch (Exception e) {
                log.error(e);
                this.getView().showMessage("供应商推送成功"+successCount+"失败"+failureCount+"代码异常:"+e.getMessage());
                e.printStackTrace();


            }

        }
    }

    public static  String  getTime(Date dateTimeString)
    {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date =simpleDateFormat.format(dateTimeString);
        return date;
    }
}
