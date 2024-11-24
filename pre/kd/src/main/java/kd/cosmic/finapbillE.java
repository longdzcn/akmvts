package kd.cosmic;

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
 * 描述: 在应付单列表批量推送应付单信息到mverp
 * 开发者: 易佳伟
 * 创建日期: 1期完成
 * 关键客户：王诗婷
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class finapbillE extends AbstractListPlugin implements Plugin {

    private final static String KEY_BARITEM = "ezob_baritemap";

    private static Log log = LogFactory.getLog(finapbillE.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(KEY_BARITEM);
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        GY gy = new GY();
        super.beforeItemClick(evt);

        Control source = (Control) evt.getSource();
        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {
            try{
                // 定义请求接口URL
//                String url = "http://10.22.10.220:8022/api/v2/app/kd/synApInfo";
                String url = gy.url+"api/v2/app/kd/synApInfo";
                // 定义header对象4
                HttpHeaders headers = new HttpHeaders();
                //header请求参数
                String appSecret = "MV";
//                String appId = "L8ruVx0ZQPL8YL5zYO2q9BXfdHbw8u0E";  //测试
                String appId =gy.appid;  // 正式
                headers.set("Content-Type", "application/json");
                headers.set("MV-Div", appSecret);
                headers.set("MV-AppId", appId);

                //获取当前选中的记录数，并循环推送，推送后保存状态成功
                //出错标识后继续推下一条记录

                ListSelectedRowCollection selectedRows = ((IListView)this.getView()).getSelectedRows();
                if(selectedRows.size()==0)
                {
                    this.getView().showMessage("请选择任意一行再进行本操作！");
                    return;
                }

                // 收集并去重PrimaryKeyValue
                HashSet<Object> uniquePrimaryKeys = new HashSet<>();
                for (ListSelectedRow list1 : selectedRows) {
                    uniquePrimaryKeys.add(list1.getPrimaryKeyValue());
                }

                // 使用增强的 for 循环遍历 HashSet
                for (Object primaryKey : uniquePrimaryKeys) {
                        // 定义请求参数Map
                        Map<String, Object> paramBody = new HashMap<String, Object>();
                        //S为标准采购订单(ApFin_pur_BT_S)，其他都设置为M

                        DynamicObject finE=BusinessDataServiceHelper.loadSingle(primaryKey,"ap_finapbill");
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
//                    财务应付单单头 “发票号”字段    传到MVERP 时候从后面截断只保留前50个字符。9-3邮件确认
                        String invoiceNo=null;
                        if(finE.get("ezob_fphm")!=null)
                        {
                             invoiceNo =  finE.get("ezob_fphm").toString();
                            if(invoiceNo.length()>50){
                                invoiceNo = invoiceNo.substring(0,50);
                            }

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
                        if(finE.get("ezob_datefield1")!=null) {
                            invoiceDate= finE.get("ezob_datefield1").toString();
                        }
                        paramBody.put("invoiceDate",invoiceDate);//发票日期

                        // GL日期	财务应付单表头到期日——9.5确认取单据日期
                        String glDate="";
                        if(finE.get("bizdate")!=null){
                            glDate=finE.get("bizdate").toString();
                        }

                        paramBody.put("glDate",glDate);//到期日
                        // 支付日期	财务应付单表头到期日
                        String payDate="";
                        if(finE.get("duedate")!=null){
                            payDate=finE.get("duedate").toString();
                        }
                        //mv反馈日期弄反了，本次调换 24-9-4 -次日再次说反了，又换回去
                        paramBody.put("payDate", payDate);//到期日
                        // GL日期	到期日	财务应付单表头到期日
                        String dueDate=finE.get("duedate").toString();
                        // 付款条件	财务应付单表头的付款条件代码
                        paramBody.put("dueDate", dueDate);//到期日
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
                        paramBody.put("produceDate",produceDate);//创建日期  制单日期

                        //单据日期
                        String  voucherDate = finE.get("bizdate").toString();
                        //mv反馈单据日期没有传输， 凭证日期：voucherDate 对应单据日期 24-9-5
                        paramBody.put("voucherDate",voucherDate);//单据日期


//                        paramBody.put("payDate", payDate);//你改成获取创建日期
//                        paramBody.put("dueDate", dueDate);





                        // 备注	表头备注
                        String remark="";
                        if(finE.get("remark")!=null) {
                            remark= finE.get("remark").toString();
                        }
                        //工厂实体 -如果MVerp账套为空则默认AKMMV
                        DynamicObject assisE= (DynamicObject)finE.get("ezob_mverpzt");
                        String org = finE.get("org.number").toString();
                        String mvAccount = "AKMMV";
                        if(finE.get("ezob_mverpzt")!=null) {
                            //工厂代号
                            mvAccount= assisE.get("number").toString();
                        }

//                    先发现用财务应付 表头MVERP字段（会存在跨组织） 传到MVERP  组织字段（导致现在传其他组织）。 现改成 法人组织默认对方的组织
//
//                    LE0003  上海美维电子有限公司       默认SME
//                    LE0004  上海美维科技有限公司       默认SP
//                    LE0005  上海凯思尔电子有限公司    默认SKE
//
//                    请丁经理和玉兰确认一下。
                        if(org.equals("LE0003" +
                                "")){
                            mvAccount = "SME";
                        }
                        if(org.equals("LE0004")){
                            mvAccount = "SP";
                        }
                        if(org.equals("LE0005")){
                            mvAccount = "SKE";
                        }
                        //currency
                        //币种实体
                        // DynamicObject currencyE= (DynamicObject)finE.get("currency");
                        String serialnumber=null;
                        String cgbillNo="";
                        //新增取值汇率 -9-5
                        String exRate = finE.get("exchangerate").toString();
                        paramBody.put("exRate",exRate);
                        //9-5 日期字段进行全部重新调整
                        paramBody.put("invoiceType",invoiceType);
                        paramBody.put("invoiceNo",invoiceNo);
                        paramBody.put("producerNo",producerNo);
                        paramBody.put("supplierCode",supplierCode);
                        //paramBody.put("costConter",""); 成本中心不传j
                        paramBody.put("poTotal",invoicTotal);
                        paramBody.put("newTotal",discountTotal);
                        paramBody.put("taxesTotal",taxesTotalY);
                        //paramBody.put("discountTotal",""); 只有三个值
                        paramBody.put("divCode", mvAccount);//工厂代号
                        paramBody.put("paytermCode",paytermCode);//付款条件

                        // 明细列表
                        List<Object> itemLists = new ArrayList<>();//itemLists
                        DynamicObjectCollection detailentryList = finE.getDynamicObjectCollection("detailentry");
                        Integer lineNo=0;  //行号
                        try {
                            for (DynamicObject dataE : detailentryList) {

                                lineNo++;   //行号+1
                                Map<String, Object> itemMap = new HashMap<>();
                                //财务应付单表体明细行号
                                itemMap.put("lineNo", lineNo);
                                //只区分S和M，根据财务应付单单据类型
                                itemMap.put("lineType", invoiceType);
                                //根据关联信息上查到采购入库单，取到采购入库单的单据编号,暂时无值

                                //物料编码	财务应付单表体物料编码
                                //物料相关
                                DynamicObject materialE = (DynamicObject) dataE.get("material");
                                String bm = materialE.get("number").toString();
                                String wlnumber = bm;
                                if(bm.substring(0,3).equals("SME")){
                                    wlnumber = bm.substring(3);
                                }
                                if(bm.substring(0,2).equals("SP")){
                                    wlnumber = bm.substring(2);
                                }
                                if(bm.substring(0,3).equals("SKE")){
                                    wlnumber = bm.substring(3);
                                }
                                if(bm.substring(0,3).equals("GME")){
                                    wlnumber = bm.substring(3);
                                }
                                if(bm.substring(0,3).equals("FPC")){
                                    wlnumber = bm.substring(3);
                                }

                                itemMap.put("itemCode", wlnumber);

                                //S为取入库单号，M为暂估预付源单
                                //【ZOC】物料类采购：采购入库单单号
                                //【ZOC】费用类采购：采购验收单单号
                                //【ZOC】资产类采购：采购收货单单号
                                //【ZOC】以上无论来源是什么，都可以取暂估应付单源单单号，当然最好的办法，应该是从以上三种单据携带下来到财务应付单表体
                                String bmbillno = dataE.get("ezob_zgyfdd").toString();
                                //财务应付明细 “源单编号  ”字段  传到MVERP 去掉最末尾带3位，以杠开始
                                if(bmbillno.substring(bmbillno.length()-3,bmbillno.length()).contains("-")){
                                    bmbillno = cgbillNo.substring(0,cgbillNo.length()-3);

                                }



                                if(bmbillno.substring(0,3).equals("SME")){
                                    bmbillno = bmbillno.substring(4);
                                }
                                if(bmbillno.substring(0,2).equals("SP")){
                                    bmbillno = bmbillno.substring(3);
                                }
                                if(bmbillno.substring(0,3).equals("SKE")){
                                    bmbillno = bmbillno.substring(4);
                                }
                                if(bmbillno.substring(0,3).equals("GME")){
                                    bmbillno = bmbillno.substring(4);
                                }
                                if(bmbillno.substring(0,3).equals("FPC")){
                                    bmbillno = bmbillno.substring(4);
                                }




                                itemMap.put("billNo", bmbillno);
                                //S取上游入库单erp序号 ，M为本地erp序号
                                //【ZOC】以上为易佳伟的原本描述，但是后来咨询用户后发现这个ezob_mverpxh是从采购入库单一路携带到财务应付单的，它们是一摸一样的，那应直接获取财务应付单上的即可
                                serialnumber = dataE.get("ezob_mverpxh").toString();
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

                                //新增取值 税率 taxrateid
                                String taxrate = dataE.get("taxrateid.taxrate").toString();
                                itemMap.put("fedTax", taxrate);
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
                        //System.out.println(cs);
                        log.info("应付推送Json记录："+cs);
                        HttpEntity<String> entity = new HttpEntity<String>(cs,headers);
                        // 发送请求
                        RestTemplate template = new RestTemplate(RestTemplateConfiguration.generateHttpRequestFactory());
                        ResponseEntity<String> exchange = template.exchange(url, HttpMethod.POST, entity, String.class);
                        ObjectMapper mapperTS = new ObjectMapper();
                        JsonNode rootNodeTS = mapperTS.readTree(exchange.getBody().toString());
                        String jsonString = rootNodeTS.toString();
                        String toCheck = "\"success\":true";
                        boolean containsSuccessTrue = jsonString.contains(toCheck);
                        if(containsSuccessTrue==true) {
                            this.getView().showMessage("同步mvERP应付发票成功！您可以在mvERP查看这些发票了！具体返回结果：" + jsonString);
                        }
                        else{
                            this.getView().showMessage("同步mvERP应付发票失败，您可以根据具体的返回结果分析错误原因并解决：" + jsonString);
                        }
                        finE.set("ezob_pushstatus", jsonString.substring(0, Math.min(jsonString.length(), 200)));  //只截取前200个字符，以防太大超出字段限制
                        SaveServiceHelper.update(finE);
                }
                this.getView().invokeOperation("refresh");
            }
            catch (Exception e) {
                StackTraceElement stackTraceElement=  e.getStackTrace()[0];
                this.getView().showMessage("异常发生在: " + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + " - " + stackTraceElement.getMethodName());
                System.out.println();
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