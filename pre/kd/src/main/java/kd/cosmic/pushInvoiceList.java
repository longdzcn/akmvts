package kd.cosmic;


import com.alibaba.druid.util.StringUtils;
import com.cloudera.impala.jdbc41.internal.fasterxml.jackson.databind.JsonNode;
import com.cloudera.impala.jdbc41.internal.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
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
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 描述: 在列表推送应收发票，按钮在发票查询
 * 开发者: 易佳伟
 * 创建日期: 1期完成
 * 关键客户：关敏婷
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class pushInvoiceList extends AbstractListPlugin implements Plugin {

    /**
     *  问题处理 1：将divcode传值改为结算组织
     *         2：将bank银行名称加上if判断（不加上判断银行为空时，会导致取值报错d）
     */
    private final static String KEY_BARITEM = "ezob_pushinvoice";

    private static Log log = LogFactory.getLog(ListPushSupplier.class);

    private static  String msg ="";
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);

        this.addItemClickListeners(KEY_BARITEM);
    }
        //vatCode 明细dono,明细factory
    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        int successCount = 0;
        int failureCount = 0;

        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {
            ListSelectedRowCollection selectedRows = ((IListView)this.getView()).getSelectedRows();

            DynamicObject aAps =null;
            GY gy = new GY();
            try {

                String url = gy.url+"api/v2/app/kd/synARInvInfo";

                // 定义header对象4
                HttpHeaders headers = new HttpHeaders();
                //header请求参数
                String appSecret = "GME";
                String appId = gy.appid;

                headers.set("Content-Type","application/json");
                headers.set("MV-Div",appSecret);
                headers.set("MV-AppId",appId);
                Map<String, Object> paramBody = new HashMap<String, Object>();



                // 销售出库->暂估应收单->财务应收->开票申请单->发票查询
                for (ListSelectedRow list1 : selectedRows) {
                    //查找发票查询，取源单id
                    DynamicObject aApsss = BusinessDataServiceHelper.loadSingle(list1.getPrimaryKeyValue(), "sim_vatinvoice", "items,items.billsourceid");
                    DynamicObjectCollection item = aApsss.getDynamicObjectCollection("items");
                    long id = 0;
                    for (DynamicObject dynamicObject : item) {
                        id = dynamicObject.getLong("billsourceid");
                    }
                    //sim_original_bill
                    QFilter qFilter = new QFilter("sim_original_bill_item.billsourceid", QCP.equals, id);

                    //开票申请单
                    aAps = BusinessDataServiceHelper.loadSingle("sim_original_bill", new QFilter[]{qFilter});

                    //条件，判断是否等于明细中的sourcebillid
                    QFilter qFilter2 = new QFilter("id", QCP.equals, id);
                    //查询应收单
                    DynamicObject[] fin = BusinessDataServiceHelper.load("ar_finarbill", "org,invoiceno,asstact,invoicedate,exratedate,creator,remark,sourcebillid,sourcebilltype,ezob_mverpzt,currency,entry,e_taxrate,e_corebillno,e_material,e_unitprice,e_taxunitprice,e_amount,e_recamount,e_quantity,e_tax,ezob_ydbm", new QFilter[]{qFilter2});

                    for (int y = 0; y < fin.length; y++) {

                    //表头
                    String divCode = "";
                    if (fin[y].getString("org") != null) {
                        divCode = ((DynamicObject) fin[y].get("org")).getString("number");
                        if (divCode.equals("LE0001")) {
                            divCode = "XMC";
                        } else if (divCode.equals("LE0006")) {
                            divCode = "GME";
                        } else if (divCode.equals("LE0002")) {
                            divCode = "MV";
                        } else if (divCode.equals("LE0016")) {
                            divCode = "MV";
                        }
                    }  //分厂代码

                    String factory = null;  //工厂代码 取销售出库单
                    String invoiceCode = fin[y].getString("invoiceno"); //发票编号
                    String invoiceNo = fin[y].getString("invoiceno");  //发票号码
                    String purchaser = "";//购买人名称
                    String shipadd = "";//购买方地址
                    String desttel = "";//电话
                    String bank = "";//购买方开户行
                    String acNo = "";//购买方账号
                    //DynamicObject asstactDynamic = fin[y].getDynamicObject("asstact");
                    QFilter qFilterss = new QFilter("id", QCP.equals, fin[y].getDynamicObject("asstact").get("id"));
                    DynamicObject asstactDynamic = BusinessDataServiceHelper.loadSingleFromCache("bd_customer", new QFilter[]{qFilterss});
                    //1755116351631971328  bd_customer
                    purchaser = asstactDynamic.getString("name");
                    shipadd = asstactDynamic.getString("bizpartner_address");
                    desttel = asstactDynamic.getString("bizpartner_phone");

                    DynamicObjectCollection bankList = asstactDynamic.getDynamicObjectCollection("entry_bank");

                    for (DynamicObject banks : bankList) {
                        if (banks.getDynamicObject("bank") != null) {
                            bank = banks.getDynamicObject("bank").getString("name");

                        }
                        acNo = banks.getString("bankaccount");
                        break;
                    }

                    String invoiceDate1 = fin[y].getString("invoicedate");//
                    String invoiceDate = invoiceDate1.substring(0, 10);//发票日期


                    String tddate = fin[y].getString("exratedate");
                    String tDate = tddate.substring(0, 10);//汇率日期

                    String invoiceByEmpno = "";
                    if (aAps.getString("creator") != null) {
                        invoiceByEmpno = ((DynamicObject) fin[y].get("creator")).get("number").toString();//制单人
                    }
                    String vatNo = fin[y].getString("invoiceno");//增值税号码
                    String[] values = new String[0];
                    if(vatNo.contains(","))
                    {
                       values = vatNo.split(",");
                    }
                    String vatCode = "239420000000";
                    //String vatCode = fin[y].getString("invoicecode");//增值税代码
                    String remark = fin[y].getString("remark");  //备注
                    String invoicetype = "";//发票类型
                    String doNo = "";


                    String sourcebillid2 = fin[y].getString("sourcebillid");
                    String sourcebilltype2 = fin[y].getString("sourcebilltype");                 //上查暂估应收单

                    DynamicObject fen3 = BusinessDataServiceHelper.loadSingle(sourcebillid2, sourcebilltype2);//查暂估应收
                    String sourcebillid3 = fen3.getString("sourcebillid");
                    String sourcebilltype3 = fen3.getString("sourcebilltype");                   //上查销售出库
                    doNo = fen3.getString("billno");


                    //DynamicObject fen4 = BusinessDataServiceHelper.loadSingle(sourcebillid3, sourcebilltype3);
                    //doNo = fen4.getString("billno");
                    //doNo ="GME-DN233931";


                   /* if(fen4.getString("ezob_mverpzt")!=null){
                            factory = ((DynamicObject)fen4.get("ezob_mverpzt")).getString("number");
                        }*/
                    DynamicObject dynamicObjectMverp = fin[y].getDynamicObject("ezob_mverpzt");
                    if (dynamicObjectMverp != null) {
                            factory = dynamicObjectMverp.getString("number");

                    }


//                    if(invoicetype.equals("ELE")||invoicetype.equals("GE")){     //判断发票类型 普通发票为out，电子+纸质普通
//                        invoicetype="out";
//                    }
//                    else {
//                        invoicetype="vat";
//                    }

                    if (((DynamicObject) fin[y].get("currency")).getString("number").equals("CNY")) {   //按币别判断发票类型，人民币为vat
                        invoicetype = "vat";
                    } else {
                        invoicetype = "out";     //按币别判断发票类型，其他币别为海外发票
                    }


                    //表体
                    DynamicObjectCollection cols = fin[y].getDynamicObjectCollection("entry");
                    int length = cols.size();
                    List<Object> itemLists = new ArrayList<>();//itemLists
                    Double vatRate = 0.0;

                    try {
                        for (int i = 0; i < length; i++) {
                            Map<String, Object> itemMap = new HashMap<>();
                            DynamicObject col = cols.get(i);
                            String vatRate2 = cols.get(0).getString("e_taxrate");//税率
                            vatRate = Double.parseDouble(vatRate2) / 100.0;
                            //String salesOrder = "860436=004";
                            String salesOrder = col.getString("e_corebillno");  //核心单据号
                            String customerPartNumber = "";
                            if (col.getString("e_material") != null) {
                                customerPartNumber = ((DynamicObject) col.get("e_material")).getString("name");//物料编码 生产型号
                            }

                            String price = col.getString("e_unitprice");  //单价
                            String tPrice = col.getString("e_taxunitprice");  //含税单价
                            String amount = col.getString("e_amount");//总金额
                            String tAmount = col.getString("e_recamount");//含税总金额  价税合计
                            String vatQty = col.getString("e_quantity");//增值税数量 表体数量
                            String tax = col.getString("e_tax");//税额
                            doNo = col.getString("ezob_ydbm");
                            if (invoicetype.equals("vat")) {
                                itemMap.put("doNo", doNo);
                                itemMap.put("salesOrder", salesOrder);
                                itemMap.put("customerPartNumber", customerPartNumber);
                                itemMap.put("price", price);
                                itemMap.put("tPrice", tPrice);
                                itemMap.put("amount", amount);
                                itemMap.put("tAmount", tAmount);
                                itemMap.put("vatQty", vatQty);
                                itemMap.put("tax", tax);
                                itemMap.put("factory", factory);
                                itemMap.put("clientPort", "XMC");
                                itemLists.add(itemMap);
                            }
                            if (invoicetype.equals("out")) {
                                itemMap.put("doNo", doNo);
                                itemMap.put("salesOrder", salesOrder);
                                itemLists.add(itemMap);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.getView().showMessage(e.getMessage());
                    }


                    if (invoicetype.equals("vat")) {                     //增值税发票示例
                        //paramBody.put("invoiceNo",invoiceNo);
                        paramBody.put("tDate", tDate);
                        paramBody.put("vatNo", vatNo);
                        paramBody.put("vatCode", vatCode);
                        paramBody.put("vatRate", vatRate);
                        paramBody.put("remark", remark);
                        paramBody.put("divCode", divCode);
                        paramBody.put("invType", invoicetype);
                        paramBody.put("itemList", itemLists);
                    }
                    if (invoicetype.equals("out")) {

                        /*paramBody.put("invoiceCode",invoiceCode);*/
                        //paramBody.put("invoiceNo",invoiceNo);
                        paramBody.put("purchaser", purchaser);
                        paramBody.put("shipadd", shipadd);
                        paramBody.put("desttel", desttel);
                        paramBody.put("bank", bank);
                        paramBody.put("acNo", acNo);
                        paramBody.put("invoiceDate", invoiceDate);
                        paramBody.put("invoiceByEmpno", invoiceByEmpno);
                        paramBody.put("remark", remark);
                        paramBody.put("divCode", divCode);
                        paramBody.put("invType", invoicetype);
                        paramBody.put("itemList", itemLists);
                    }




                        ResponseEntity<String> exchange = null;

                    if(values.length>0)
                    {
                        for (int  i=0;i<values.length;i++)
                        {
                            paramBody.put("vatNo",values[i].trim());
                            JSONObject json = new JSONObject();
                            json = JSONObject.fromObject(paramBody);
                            JSONObject aJ2 = new JSONObject();
                            JSONArray aJry = new JSONArray();
                            aJry.add(json);
                            aJ2.put("data", aJry);
                            aJ2.toString();
                            String cs = aJ2.toString();
                            System.out.println(cs);
                            log.info("应收推送Json记录：" + cs);
                            exchange  = getApi(url,cs,headers);
                        }

                    }else {
                        JSONObject json = new JSONObject();
                        json = JSONObject.fromObject(paramBody);
                        JSONObject aJ2 = new JSONObject();
                        JSONArray aJry = new JSONArray();
                        aJry.add(json);
                        aJ2.put("data", aJry);
                        aJ2.toString();
                        String cs = aJ2.toString();
                        System.out.println(cs);
                        log.info("应收推送Json记录：" + cs);
                         exchange = getApi(url,cs,headers);
                    }


                        // 定义请求接口URL

                    msg = "";
                    ObjectMapper aMapperTS = new ObjectMapper();
                    JsonNode rootNodeTS = aMapperTS.readTree(exchange.getBody().toString());
                    String aCodeV = rootNodeTS.path("code").asText();
                    msg = rootNodeTS.path("msg").asText() + String.valueOf(rootNodeTS.path("data"));
                    try {
                        if (exchange.getStatusCodeValue() == 200) {

                            if (aCodeV.equals("200")) {
                                aAps.set("ezob_pushstatus", "推送时间" + getCurrTime() + "推送成功" + msg);
                                log.info("开票推送成功:" + msg + "发票号码" + invoiceNo);
                                SaveServiceHelper.update(aAps);

                            } else {
                                this.getView().showMessage("推送失败！推送时间" + getCurrTime() + "msg" + msg);
                                aAps.set("ezob_pushstatus", "推送时间" + getCurrTime() + "推送失败" + msg);
                                log.info("开票推送失败:" + msg + "发票号码" + invoiceNo);
                                SaveServiceHelper.update(aAps);

                            }
                        } else {
                            this.getView().showMessage("请求异常：" + exchange.getStatusCode());
                            aAps.set("ezob_pushstatus", "请求异常：" + exchange.getStatusCode());
                            log.info("开票推送异常:" + msg + "发票号码" + invoiceNo);
                            SaveServiceHelper.update(aAps);
                            failureCount++;
                        }
                    } catch (Exception e) {
                        StackTraceElement stackTranceElment = e.getStackTrace()[0];
                        int lineNumber = stackTranceElment.getLineNumber();
                        this.getView().showMessage("代码异常：" + e.getMessage() + "推送时间" + getCurrTime() + "推送失败" + e.getMessage() + "在代码" + lineNumber + "行");
                        aAps.set("ezob_pushstatus", "代码异常：" + e.getMessage() + "推送时间" + getCurrTime() + "推送失败" + msg);
                        e.printStackTrace();
                        log.error("mverp应收推送报错在" + lineNumber + "行", "报错原因" + e.getMessage());
                    }
                }
                }//for循环结束
                this.getView().showMessage("推送时间" + getCurrTime() + "推送成功" + msg);
                this.getView().invokeOperation("refresh");

            }catch (Exception e){
                StackTraceElement stackTranceElment=  e.getStackTrace()[0];
                int lineNumber = stackTranceElment.getLineNumber();
                log.error("mverp应收推送报错在"+lineNumber+"行","报错原因"+e.getMessage());
                this.getView().showMessage("代码异常："+e.getMessage()+ "推送时间"+getCurrTime()+"推送失败"+e.getMessage()+"在代码"+lineNumber+"行");
                aAps.set("ezob_pushstatus", "代码异常："+e.getMessage()+ "推送时间"+getCurrTime()+"推送失败"+msg);
                e.printStackTrace();


            }
        }


    }
    public static String getCurrTime()
    {
        Date date = new Date(); SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd :HH:mm:ss");
        String currTime = dateFormat.format(date);
        return currTime;
    }

    public ResponseEntity<String> getApi(String url,String cs,HttpHeaders headers) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        HttpEntity<String> entity = new HttpEntity<String>(cs, headers);
        RestTemplate template = new RestTemplate(RestTemplateConfiguration.generateHttpRequestFactory());
        ResponseEntity<String> exchange = template.exchange(url, HttpMethod.POST, entity, String.class);
        return  exchange;
    }
}
