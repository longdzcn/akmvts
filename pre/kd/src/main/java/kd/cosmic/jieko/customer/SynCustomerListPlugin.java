package kd.cosmic.jieko.customer;

import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.ais.util.CollectionUtil;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.OrmLocaleValue;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.list.IListView;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;
import net.sf.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 描述: 思方客户同步
 * 开发者: 钟有吉
 * 关键客户：keller
 * 已部署正式：false
 * 备注：目前已经放弃，改为操作插件khtonbusf
 */

public class SynCustomerListPlugin extends AbstractListPlugin implements Plugin {

    private final static String KEY_BUTTON = "bar_audit";
//    private static Log log = LogFactory.getLog(TBSFOperationPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        int successCount = 0;
        int failCount = 0;
        StringBuilder msgFail = new StringBuilder();
        if (StringUtils.equals(evt.getItemKey(), KEY_BUTTON)) {
            ListSelectedRowCollection selectedRows = ((IListView) this.getView()).getSelectedRows();
            if (CollectionUtil.isNullOrEmpty(selectedRows)){
                this.getView().showMessage("请至少选择一条单据!");
            }
            //先判断所有单据的状态
            for (ListSelectedRow selectedRow : selectedRows) {
                String fnumber = null;
                try {
                    if (!selectedRow.getBillStatus().equals("C")) {
                        this.getView().showMessage("推送的单据状态需要已审批！");
                        return;
                    }
                    //定义请求参数
                    HashMap<String, Object> paramBody = new HashMap<>();
                    //查询客户信息
                    DynamicObject customer = BusinessDataServiceHelper.loadSingle(selectedRow.getPrimaryKeyValue(), "bd_customer");
                    Object pkValueCustomer = customer.getPkValue().toString();
                    fnumber = customer.getString("number");
                    if (StringUtils.isEmpty(fnumber)) {
                        throw new RuntimeException("客户编码为空！");
                    }
                    //客户名称
                    String fname ="";
                    OrmLocaleValue customerName = (OrmLocaleValue) customer.get("name");
                    fname = customerName.getLocaleValue_zh_CN();
                    if (customerName.getLocaleValue_zh_CN()=="" && customerName.getLocaleValue_en() !=""){
                        fname = customerName.getLocaleValue_en();
                    }
                    String simpleName = customer.getString("SimpleName");
                    if (StringUtils.isEmpty(simpleName)) {
                        throw new RuntimeException("客户简称不能为空！");
                    }
                    //客户地址中/英名称
                    OrmLocaleValue customeraddressValue =(OrmLocaleValue) customer.getDynamicObject("delivercustomerid").get("bizpartner_address");
                    if (customeraddressValue ==null){
                        throw new RuntimeException("详细地址不能为空！");
                    }
                    String customeraddress = customeraddressValue.getLocaleValue_zh_CN();
                    String customeraddressEn = customeraddressValue.getLocaleValue_en();

                    String group = customer.getDynamicObject("group").getString("number");
                    String fkEzobXyedmx = customer.getString("ezob_xyedmx");
                    if (StringUtils.isEmpty(fkEzobXyedmx)){
                        throw new RuntimeException("信用额度不能为空！");
                    }

                    String settlementcyid = customer.getDynamicObject("settlementcyid").getString("number");//交易币别
                    int saleType = 0;
                    if (StringUtils.isEmpty(settlementcyid)) {
                        throw new RuntimeException("货币代码不能为空！");
                    } else if ("CNY".equals(settlementcyid) || "RMB".equals(settlementcyid)) {
                        settlementcyid = "RMB";
                        saleType = 0;
                    } else {
                        saleType = 2;
                    }
                    String settlementcyName = customer.getDynamicObject("settlementcyid").getString("name");
                    if (StringUtils.isEmpty(settlementcyName)) {
                        throw new RuntimeException("货币名称不能为空！");
                    }
                    String settlementtypeidName = customer.getDynamicObject("settlementtypeid").getString("name");
                    if (StringUtils.isEmpty(settlementtypeidName)) {
                        throw new RuntimeException("付款方式不能为空");
                    }
                    String country = customer.getDynamicObject("country").getString("name");


                    String phone = customer.getString("bizpartner_phone");

                    String ffax = customer.getString("bizpartner_fax");//传真

//                    净付款天数
//                    DynamicObject recconditionId = customer.getDynamicObject("receivingcondid");
//                    //查询收款条件
//                    DynamicObject fkts = BusinessDataServiceHelper.loadSingle(recconditionId.get("ID"),"bd_reccondition");
//                    DynamicObjectCollection entry1 = fkts.getDynamicObjectCollection("entry");
//                    for (DynamicObject dynamicObject : entry1) {
//                        String odday = dynamicObject.getString("DETAIL.odday");
//                    }
//                    String a = recconditionId.getString("description");
//                    log.info("付款条件："+recconditionId);
//                    QFilter qFilterReccondition = new QFilter("customerid", QCP.equals, recconditionId);
//                    DynamicObject bdReccondition = BusinessDataServiceHelper.loadSingle("bd_payconditionentry","fconfirmtypedesc",qFilterReccondition.toArray());
//                    String entry = bdReccondition.getString("name");
//                    log.info("付款条件："+entry);
                    String postalCode = customer.getString("postal_code");//电子邮件
                    String createtime = customer.getString("createtime");//创建时间
                    String approvedate = customer.getString("approvedate");//审核时间
                    String creator = customer.getString("modifier.name");//创建人
//                    String ezobJtkhdm = customer.getDynamicObject("ezob_jtkhdm").getString("number");//集团客户代码
                    String ezobJtkhdm = customer.getString("ezob_jtkhdm.number");//集团客户代码
//                    String ezobjskhdmx = customer.getDynamicObject("ezob_jskhdmx").getString("number");//结算客户代码
                    String ezobjskhdmx = customer.getString("ezob_jskhdmx");
                    String ezobyxzx = customer.get("ezob_yxzx.number").toString();//营销中心编码
                    //查询销售代表



                    //查询客户地址信息
                    QFilter qFilterAddress = new QFilter("customerid", QCP.in, pkValueCustomer);
                    ArrayList<HashMap> addressList = new ArrayList<>();
                    DynamicObject[] addresses = BusinessDataServiceHelper.load("bd_address", "detailaddress,ezob_ywydm,ezob_zzdsh,admindivision,phone", qFilterAddress.toArray());
                    for (DynamicObject address : addresses) {
                        HashMap<String, Object> addressMap = new HashMap<>();
                        String name = address.getString("name").toString();
                        String locationName = address.getString("detailaddress").toString();//客户送货具体地址
                        String phoneAddress = address.getString("phone");
                        String salesCode = address.getDynamicObject("ezob_zzdsh").getString("name");
                        String rateConverter = taxRateConverter(salesCode);
                        if (StringUtils.isEmpty(rateConverter) || rateConverter == null) {
                            throw new RuntimeException("增值代税不存在，请重新选择！");
                        }
                        String admindivision = address.getString("admindivision");
                        addressMap.put("Location", name);
                        addressMap.put("LocationName", locationName);
                        addressMap.put("StateCode", rateConverter);
                        addressMap.put("SREPCode1", "xs");
                        addressMap.put("SHIP_TO_PHONE",phoneAddress);
                        addressMap.put("STATE_SHIP_TAX_FLAG", "Y");
                        addressMap.put("CountryName", country);
                        addressList.add(addressMap);
                    }


                    //构建请求对象
                    //判断是否是新增/修改类型
                    String ezobSfkhtszt = customer.getString("ezob_sfkhtszt");
                    //1 新增 2 修改
                    int addFlag;
                    if (!StringUtils.isEmpty(ezobSfkhtszt) && ezobSfkhtszt != null) {
                        addFlag = 2;
                    } else {
                        addFlag = 1;
                    }
                    paramBody.put("CUST_GROUP_DESC",ezobyxzx);//营销中心编码->客户分组
                    paramBody.put("AddFlag", addFlag);
                    paramBody.put("CustomerCode", fnumber);
                    paramBody.put("CustomerName", fname);
                    paramBody.put("AbbrName", simpleName);
                    paramBody.put("BillingAddress", customeraddress);
                    paramBody.put("CustomerType", 1);
                    paramBody.put("CurrencyCode", settlementcyid);
                    paramBody.put("SalesCode", "ZYM");
                    paramBody.put("CREDIT_RATING", 1);
                    paramBody.put("CREDIT_LIMIT", fkEzobXyedmx);
                    paramBody.put("Phone", phone);
                    paramBody.put("Tax", ffax);
                    paramBody.put("EDI_FLAG_FOR_SOACK", saleType);
                    paramBody.put("BILLING_ADDRESS_2",customeraddressEn);
                    paramBody.put("ZIP", "");
                    paramBody.put("D0010audited_date", approvedate);
                    paramBody.put("D0010last_modified_date", approvedate);
                    paramBody.put("Customer_label_flag", 0);
                    paramBody.put("PAYMENT_TERMS_FLAG", 0);
                    paramBody.put("CountryName", country);
                    paramBody.put("FED_TAX_ID_NO", settlementtypeidName);
                    paramBody.put("CreateDate", createtime);
                    paramBody.put("UserId", "admin");
                    paramBody.put("Deblocking_falg", getCurrTime());
                    paramBody.put("D0010last_modified_date", getCurrTime());
                    paramBody.put("Deblocking_begin_date", getCurrTime());
                    paramBody.put("Deblocking_end_date", getCurrTime());
                    paramBody.put("Deblocking_empl_code", "admin");
                    paramBody.put("ParentCustcode",ezobjskhdmx);
                    paramBody.put("GroupCustcode",ezobJtkhdm);

                    //客户地址
                    paramBody.put("AddressData", addressList);
//
                    JSONObject jsonObject = JSONObject.fromObject(paramBody);
                    String jsonString = jsonObject.toString();
//                    log.info("请求参数信息为：" + JsonUtils.toJson(jsonObject));

//                    getView().showMessage(jsonString);
                    ResponseEntity<String> response = null;

                    try {
                        String url = "http://10.101.23.218:8489/api/api/CustomerProject";
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Content-Type", "application/json;charset=UTF-8");
                        HttpEntity<String> entity = new HttpEntity<>(jsonString, headers);
//                    log.info(JSONUtils.toJSONString(entity));
                        RestTemplate restTemplate = new RestTemplate();
                        response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//                    log.info(JSONUtils.toJSONString(response));
                        if (response.getStatusCodeValue() == 200) {
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode jsonNode = objectMapper.readTree(response.getBody().toString());
                            String code = jsonNode.path("Success").asText("");
                            String msg = jsonNode.path("Message").asText("");
                            if ("true".equals(code)) {
                                customer.set("ezob_sfkhtszt", "推送时间" + getCurrTime() + "推送成功！");
                                SaveServiceHelper.update(customer);
                                successCount++;
                            } else {
                                customer.set("ezob_sfkhtszt", "推送时间" + getCurrTime() + "推送失败:" + msg);
                                failCount++;
                                msgFail.append(String.format("%s编码推送不成功，原因：%s\r\n", fnumber, msg));
//                                log.info("客户推送失败：" + fnumber + msg);
                            }


                        }
                    } catch (Exception e) {
                        failCount++;
                        msgFail.append(String.format("%s编码推送不成功，原因：%s\r\n", fnumber, e.getMessage()));
//                        log.info("客户推送失败：" + fnumber + e.getMessage());

                    }
                } catch (Exception e) {
//                    log.info("客户推送失败：" + fnumber + e.getMessage());
                    failCount++;
                    msgFail.append(String.format("%s编码推送不成功，原因：数据有异常,%s\r\n", fnumber, e.getMessage()));

                }

            }
            this.getView().showMessage("客户推送成功:" + successCount + "条!失败：" + failCount + "条!\r\n"  + msgFail);

        }
    }


    //时间格式转换 yyyy-MM-dd HH:mm:ss
    public static String getTime(Date dateTimeString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(dateTimeString);
        return date;
    }

    //转换税率
    public String taxRateConverter(String salesCode) {
        if (StringUtils.isEmpty(salesCode)) {
            throw new RuntimeException("增值税代码不能为空！");
        }
        //示例税率数据
        HashMap<String, String> taxRate = new HashMap<>();
        taxRate.put("NOTAX", "增值税0%");
        taxRate.put("TAX01", "增值税3%");
        taxRate.put("TAX04", "增值税6%");
        taxRate.put("TAX05", "增值税13%");
        taxRate.put("TAX06", "增值税9%");
        taxRate.put("TAX07", "增值税7%");
        taxRate.put("TAX08", "增值税11%");
        taxRate.put("TAX09", "增值税4%");
        taxRate.put("TAX10", "增值税19%");
        taxRate.put("TAX11", "增值税10%");
        taxRate.put("TAX12", "增值税1%");

        String keyCode = taxRate.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getValue(), e -> e.getKey()))
                .get(salesCode);
        return keyCode;

    }

    public static String getCurrTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currTime = dateFormat.format(date);
        return currTime;
    }
}