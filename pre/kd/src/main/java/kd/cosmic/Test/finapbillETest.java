package kd.cosmic.Test;

import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 应付推送测试环境
 * 标准单据列表 kd.cosmic.Test.finapbillETest
 */
public class finapbillETest extends AbstractListPlugin implements Plugin {

    private final static String KEY_BARITEM = "ezob_baritemap";

    private static Log log = LogFactory.getLog(finapbillETest.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(KEY_BARITEM);
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {


        super.beforeItemClick(evt);

        String msg ="";
        Control source = (Control) evt.getSource();
        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {
            try{


                // 定义请求接口URL
                String url = "http://10.22.10.220:8020/api/v2/app/kd/synApInfo";

                // 定义header对象4
                HttpHeaders headers = new HttpHeaders();
                //header请求参数

                String appSecret = "MV";

                String appId = "S3YMUYp13bpDDyxLO0DodcHSR3ld5brt";

                headers.set("Content-Type", "application/json");

                headers.set("MV-Div", appSecret);

                headers.set("MV-AppId", appId);

                //获取当前选中的记录数，并循环推送，推送后保存状态成功
                //出错标识后继续推下一条记录

                ListSelectedRowCollection selectedRows = ((IListView)this.getView()).getSelectedRows();

                for (ListSelectedRow list1:selectedRows) {
                    try {
                        String billNo = list1.getBillNo();

                        // 定义请求参数Map
                        Map<String, Object> paramBody = new HashMap<String, Object>();
                        //S为标准采购订单(ApFin_pur_BT_S)，其他都设置为M

                        DynamicObject finE=     BusinessDataServiceHelper.loadSingle(list1.getPrimaryKeyValue(),"ap_finapbill");
                        //发票类型实体
                        DynamicObject billtypeE= (DynamicObject)finE.get("billtypeid");//.get("number");
                        //发票类型
                        String  invoiceType=null;
                        if(billtypeE.get("number").equals("ApFin_pur_BT_S"))
                        {
                            invoiceType="S";
                        }
                        else{
                            invoiceType="M";
                        }
                        // 发票号码/预付单号invoiceQty
                        String invoiceNo=null;
                        if(finE.get("ezob_fphm")!=null)
                        {
                            invoiceNo = finE.get("ezob_fphm").toString();
                        }

                        //供应商实体
                        DynamicObject basstactE= (DynamicObject)finE.get("receivingsupplierid");
                        // 结算供应商.代码
                        String supplierCode=null;
                        if(basstactE.get("ezob_mvjbm")!=null)
                        {
                            supplierCode=basstactE.get("ezob_mvjbm").toString();
                        }
                        // 发票日期/预付日期	财务应付单表头发票日期
                        String invoiceDate=null;

                        // 支付日期	财务应付单表头到期日
                        String payDate="";
                        // GL日期	财务应付单表头到期日
                        String glDate="";
                        if(finE.get("bizdate")!=null){
                            payDate=finE.get("bizdate").toString();
                            glDate=finE.get("bizdate").toString();
                        }


                        // GL日期	到期日	财务应付单表头到期日
                        String dueDate=finE.get("duedate").toString();
                        // 付款条件	财务应付单表头的付款条件代码

                        String  paytermCode="";
                        if(finE.get("paycond")!=null) {
                            paytermCode= ((DynamicObject)finE.get("paycond")).get("number").toString();
                        }

                        // 总金额	财务应付单表头中的金额 ->后改为应付金额
                        BigDecimal invoicTotal=null;
                        if(finE.get("pricetaxtotal")!=null) {
                            invoicTotal= new  BigDecimal(finE.get("pricetaxtotal").toString());
                        }
                        // 税金总额	财务应付单表头中的税额
                        BigDecimal taxesTotalY=null;
                        if(finE.get("tax")!=null) {

                            taxesTotalY= new  BigDecimal(finE.get("tax").toString());
                        }
                        // 折扣金额(含税)	财务应付单表头的折扣额
                        BigDecimal discountTotal=null;
                        if(finE.get("ezob_discountamount")!=null) {
                            discountTotal= new  BigDecimal(finE.get("ezob_discountamount").toString());
                        }
                        //操作人实体
                        DynamicObject creatorE= (DynamicObject)finE.get("creator");
                        // 制单人(工号)	制单人.工号
                        String producerNo=creatorE.get("number").toString();
                        // 制单日期(YYYY-MM-DD HH:MI:SS)	创建日期
                        String produceDate=finE.get("createtime").toString();
                        // 备注	表头备注
                        String remark="";
                        if(finE.get("remark")!=null) {
                            remark= finE.get("remark").toString();
                        }
                        //工厂实体
                        DynamicObject assisE= (DynamicObject)finE.get("ezob_mverpzt");
                        String mvAccount = "AKMMV";
                        if(finE.get("ezob_mverpzt")!=null) {
                            //工厂代号
                            mvAccount= assisE.get("number").toString();
                        }
                        //currency
                        //币种实体
                        // DynamicObject currencyE= (DynamicObject)finE.get("currency");
                        String serialnumber=null;
                        String cgbillNo="";
                        String keyID="";
                        String tabelID="";

                        paramBody.put("invoiceType",invoiceType);
                        paramBody.put("invoiceNo",invoiceNo);
                        paramBody.put("invoiceDate",invoiceDate);
                        paramBody.put("producerNo",producerNo);
                        paramBody.put("supplierCode",supplierCode);
                        paramBody.put("produceDate",produceDate);
                        paramBody.put("payDate",payDate);//你改成获取创建日期
                        paramBody.put("dueDate",dueDate);
                        paramBody.put("glDate",glDate);
                        //paramBody.put("costConter",""); 成本中心不传
                        paramBody.put("poTotal",invoicTotal);
                        paramBody.put("newTotal",discountTotal);
                        paramBody.put("taxesTotal",taxesTotalY);
                        //paramBody.put("discountTotal",""); 只有三个值
                        paramBody.put("divCode", mvAccount);//工厂代号
                        paramBody.put("paytermCode",paytermCode);//付款条件




                        // 明细列表
                        List<Object> itemLists = new ArrayList<>();//itemLists
                        DynamicObjectCollection detailentryList = finE.getDynamicObjectCollection("detailentry");
                        Integer lineNo=0;
                        Integer rKNO=-1;
                        int countmx =0;
                        try {
                            for (DynamicObject dataE : detailentryList) {

                                lineNo++;
                                rKNO++;
                                Map<String, Object> itemMap = new HashMap<>();
                                //财务应付单表体明细行号
                                itemMap.put("lineNo", lineNo);
                                //只区分S和M，根据财务应付单单据类型
                                itemMap.put("lineType", invoiceType);
                                //根据关联信息上查到采购入库单，取到采购入库单的单据编号,暂时无值

                                if (invoiceType.equals("M")) {
                                    if (cgbillNo == "") {    //查暂估预付的源单编码
                                        String id = finE.getString("sourcebillid");
                                        String type = finE.getString("sourcebilltype");
                                        DynamicObject apfin = BusinessDataServiceHelper.loadSingle(id, type);
                                        cgbillNo = apfin.getString("sourcebillno");
                                    } else {
                                    }
                                } else {
                                }


                                //根据关联信息上查到采购入库单，取到采购入库单表体中的mvERP序号同步过去

                                //物料编码	财务应付单表体物料编码
                                //物料相关
                                DynamicObject materialE = (DynamicObject) dataE.get("material");
                                itemMap.put("itemCode", materialE.get("number"));


                                //财务应付单表体是否存在‘mvERP序号’字段
                                //判断是否为M单的serialnumber        取表体
                                if (invoiceType.equals("M")) {
                                    if (dataE.get("ezob_mverpxh") != null) {
                                        serialnumber = dataE.get("ezob_mverpxh").toString();
                                    } else {
                                    }
                                } else {
                                }

                                //判断是否为S单的serialnumber  //取上游
                                if (invoiceType.equals("S")) {
                                    DynamicObjectCollection billentryList = new DynamicObjectCollection();
                                    if (finE.get("sourcebillid") != null) {
                                        keyID = finE.get("sourcebillid").toString();
                                    }
                                    if (finE.get("sourcebilltype") != null) {
                                        tabelID = finE.get("sourcebilltype").toString();
                                    }

                                    if (!"".equals(keyID) && !"".equals(tabelID)) {
                                        // QFilter  qFilter = new QFilter("id", QCP.equals, Long.parseLong(KeyID));
                                        //预估应付单信息
                                        // DynamicObject ygE = BusinessDataServiceHelper.loadSingle(TabelID, "id,sourcebillid,sourcebilltype", new QFilter[]{qFilter});
                                        DynamicObject ygE = BusinessDataServiceHelper.loadSingle(keyID, tabelID);

                                        if (ygE != null && ygE.get("sourcebilltype") != null && !"".equals(ygE.get("sourcebilltype"))) {
                                            keyID = "";
                                            tabelID = "";
                                            if (finE.get("sourcebillid") != null) {
                                                keyID = ygE.get("sourcebillid").toString();
                                            }
                                            if (finE.get("sourcebilltype") != null) {
                                                tabelID = ygE.get("sourcebilltype").toString();
                                            }
                                            //如果TabelID为im_purinbill，为入库单取值
                                            if (tabelID.toLowerCase().equals("im_purinbill")) {
                                                // qFilter = new QFilter("id", QCP.equals,  Long.parseLong(KeyID));
                                                //入库单信息
                                                // DynamicObject rkE = BusinessDataServiceHelper.loadSingle(TabelID, "id,billNo,billentry,im_purinbillentry", new QFilter[]{qFilter});
                                                DynamicObject rkE = BusinessDataServiceHelper.loadSingle(keyID, tabelID);
                                                if (rkE != null && rkE.get("billNo") != null && !"".equals(rkE.get("billNo"))) {
                                                    cgbillNo = rkE.get("billNo").toString();
                                                    billentryList = rkE.getDynamicObjectCollection("billentry");
                                                    if (!billentryList.get(countmx).get("ezob_mverpxh").equals("")&&billentryList.get(countmx).get("ezob_mverpxh")!=null) {
                                                        serialnumber = billentryList.get(countmx).get("ezob_mverpxh").toString();
                                                        countmx++;
                                                    } else {
                                                    }
                                                }
                                            } else {
                                                //没有入库单不处理
                                            }
                                        }
                                    }

                                } else {
                                }

                                //S为取入库单号，M为暂估预付源单
                                itemMap.put("billNo", cgbillNo);
                                //S取上游入库单erp序号 ，M为本地erp序号

                                itemMap.put("serialnumber", serialnumber);
                                //财务应付单表体数量
                                if(dataE.get("quantity")!=null)
                                {
                                    itemMap.put("invoiceQty", dataE.get("quantity"));

                                }else{
                                    itemMap.put("invoiceQty", null);

                                }
                                //财务应付单表体单价
                                if(dataE.get("pricetax")!=null)
                                {
                                    itemMap.put("invoicePrice", dataE.get("pricetax"));
                                }else{
                                    itemMap.put("invoicePrice", null);
                                }
                                //财务科目代码,不传
                                // itemMap.put("subjectCode","");
                                itemLists.add(itemMap);
                            }
                        }catch (Exception e){
                            log.error(e);//报错用这个
                        }


                        paramBody.put("itemLists",itemLists);//添加明细列表

                        JSONObject json = new JSONObject() ;
                        json=JSONObject.fromObject(paramBody);
                        JSONObject j2=new JSONObject();
                        JSONArray jry=new JSONArray();
                        jry.add(json);
                        j2.put("data",jry);
                        j2.toString();
                        String cs = j2.toString();
                        System.out.println(cs);
                        log.info("应付推送Json记录："+cs);
                        HttpEntity<String> entity = new HttpEntity<String>(cs,headers);
                        // 发送请求
                        RestTemplate template = new RestTemplate();
                        ResponseEntity<String> exchange = template.exchange(url, HttpMethod.POST, entity, String.class);
                        ObjectMapper mapperTS = new ObjectMapper();
                        JsonNode rootNodeTS = mapperTS.readTree(exchange.getBody().toString());
                        String codeV = rootNodeTS.path("code").asText();
                        msg = rootNodeTS.path("data").path("failResult").toString();
                        //标识记录为推送成功或异常
                        if(exchange.getStatusCodeValue()==200)
                        {


                            if(codeV.equals("200")) {
                                this.getView().showMessage("推送成功！推送时间"+getCurrTime()+"msg:"+msg);
                                finE.set("ezob_pushstatus", "推送成功！推送时间"+getCurrTime()+"msg:"+msg);
                                SaveServiceHelper.update(finE);

                            }
                            else{
                                this.getView().showMessage("推送失败！推送时间"+getCurrTime()+"msg:"+msg);
                                finE.set("ezob_pushstatus", "推送失败！推送时间"+getCurrTime()+"msg:"+msg);
                                SaveServiceHelper.update(finE);

                            }
                        }
                        else{
                            this.getView().showMessage("请求异常"+exchange.getStatusCode());
                            finE.set("ezob_pushstatus","请求异常："+exchange.getStatusCode());
                            SaveServiceHelper.update(finE);


                        }

                    } catch (Exception e) {
                        //标识当条记录的推送异常
                        e.printStackTrace();
                        this.getView().showMessage("代码异常推送失败"+msg);
                        StackTraceElement stackTranceElment=  e.getStackTrace()[0];
                        int lineNumber = stackTranceElment.getLineNumber();
                        log.error("mverp应收推送报错在"+lineNumber+"行","报错原因"+e.getMessage());

                    }
                    break;
                }

                this.getView().invokeOperation("refresh");

            }
            catch (Exception e) {
                e.printStackTrace();
                this.getView().showMessage("代码异常推送失败"+msg);
                e.printStackTrace();
                StackTraceElement stackTranceElment=  e.getStackTrace()[0];
                int lineNumber = stackTranceElment.getLineNumber();
                log.error("mverp应收推送报错在"+lineNumber+"行","报错原因"+e.getMessage());

            }

        }
    }
    public static String getCurrTime()
    {
        Date date = new Date(); SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd :HH:mm:ss");
        String currTime = dateFormat.format(date);
        return currTime;
    }
}