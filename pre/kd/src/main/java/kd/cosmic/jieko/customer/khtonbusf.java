package kd.cosmic.jieko.customer;

import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.OrmLocaleValue;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import net.sf.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Collectors;


/**
 * 描述: 思方客户同步
 * 开发者: 钟有吉
 * 关键客户：keller
 * 已部署正式：false
 * 备注：目前只在测试环境
 */
public class khtonbusf extends AbstractOperationServicePlugIn {

    private static Log log = LogFactory.getLog(khtonbusf.class);
    @Override
    public void endOperationTransaction(EndOperationTransactionArgs e) {
        String msg = "";
        int n = 0;
//        try {
//        进到循环，把数据循环一遍
        for (DynamicObject customer : e.getDataEntities()) {
            int successCount = 0;
            int failCount = 0;
            StringBuilder msgFail = new StringBuilder();

//            if (StringUtils.equals(evt.getItemKey(), KEY_BUTTON)) {
//                ListSelectedRowCollection selectedRows = ((IListView) this.getView()).getSelectedRows();
//                if (CollectionUtil.isNullOrEmpty(selectedRows)){
//                    this.getView().showMessage("请至少选择一条单据!");
//                }
                //先判断所有单据的状态
//                for (ListSelectedRow selectedRow : selectedRows) {

                    //获取单据编号
                    String fnumber = null;
                    try {
                        if (!customer.getString("status").equals("C")) {
                            msg = "推送的单据状态需要已审批！";
//                            this.getView().showMessage("推送的单据状态需要已审批！");
                            return;
                        }
                        //定义请求参数
                        HashMap<String, Object> paramBody = new HashMap<>();
                        //查询客户信息
//                        DynamicObject customer = BusinessDataServiceHelper.loadSingle(selectedRow.getPrimaryKeyValue(), "bd_customer");
                        Object pkValueCustomer = customer.getPkValue().toString();
                        fnumber = customer.getString("number");
                        if (StringUtils.isEmpty(fnumber)) {
                            msg = "客户编码为空";
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
                            msg = "客户简称不能为空！";
                            throw new RuntimeException("客户简称不能为空！");
                        }
                        //客户地址中/英名称
                        OrmLocaleValue customeraddressValue =(OrmLocaleValue) customer.getDynamicObject("delivercustomerid").get("bizpartner_address");
                        if (customeraddressValue ==null){
                            msg = "详细地址不能为空！";
                            throw new RuntimeException("详细地址不能为空！");
                        }
                        String customeraddress = customeraddressValue.getLocaleValue_zh_CN();
                        String customeraddressen = customeraddressValue.getLocaleValue_en();

                        String group = customer.getDynamicObject("group").getString("number");


                        String settlementcyid = customer.getString("settlementcyid.number");//交易币别
                        int saleType = 0;
                        if (StringUtils.isEmpty(settlementcyid)) {
                            msg = "货币代码不能为空！";
                            throw new RuntimeException("货币代码不能为空！");
                        } else if ("CNY".equals(settlementcyid) || "RMB".equals(settlementcyid)) {
                            settlementcyid = "RMB";
                            saleType = 0;
                        } else {
                            saleType = 2;
                        }
                        String fkEzobXyedmx = customer.getString("ezob_xyedmx");
                        if (StringUtils.isEmpty(fkEzobXyedmx)){
                            msg = "信用额度不能为空！";
                            throw new RuntimeException("信用额度不能为空！");
                        }
                        if (!"RMB".equals(settlementcyid)) {
                            BigDecimal huiLv = this.getHuiLv();
                            BigDecimal xyed = new BigDecimal(fkEzobXyedmx);
//                            BigDecimal huiLv2 = new BigDecimal(huiLv);
                            BigDecimal xyedxhl = xyed.multiply(huiLv);
                            fkEzobXyedmx = xyedxhl.toString();
                        }
                        String settlementcyName = customer.getDynamicObject("settlementcyid").getString("name");
                        if (StringUtils.isEmpty(settlementcyName)) {
                            msg = "货币名称不能为空！";
                            throw new RuntimeException("货币名称不能为空！");
                        }
                        String settlementtypeidName = customer.getDynamicObject("settlementtypeid").getString("name");
                        if (StringUtils.isEmpty(settlementtypeidName)) {
                            msg = "付款方式不能为空！";
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
                        SimpleDateFormat formatIn = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH); // 输入日期格式
                        SimpleDateFormat formatOut = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 输出日期格式
                        String formattedDate = null;
                        try {
                            Date date = formatIn.parse(createtime);
                            formattedDate = formatOut.format(date);
                            System.out.println(formattedDate); // 输出结果: "2024-10-25 14:25:08"
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        String approvedate2 = customer.getString("approvedate");//审核时间
                        SimpleDateFormat formatIn2 = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH); // 输入日期格式
                        SimpleDateFormat formatOut2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 输出日期格式
                        String approvedate = null;
                        try {
                            Date date = formatIn2.parse(approvedate2);
                            approvedate = formatOut2.format(date);
                            System.out.println(approvedate); // 输出结果: "2024-10-25 14:25:08"
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        String creator = customer.getString("modifier.name");//创建人
//                    String ezobJtkhdm = customer.getDynamicObject("ezob_jtkhdm").getString("number");//集团客户代码
                        String ezobJtkhdm = customer.getString("ezob_jtkhdm.number");//集团客户代码
//                    String ezobjskhdmx = customer.getDynamicObject("ezob_jskhdmx").getString("number");//结算客户代码
                        String ezobjskhdmx = customer.getString("ezob_jskhdmx");
                        String ezobyxzx = customer.get("ezob_yxzx.number").toString();//营销中心编码
                        //查询销售代表



                        //查询客户地址信息
//                        QFilter qFilterAddress = new QFilter("customerid", QCP.in, pkValueCustomer);
                        ArrayList<HashMap> addressList = new ArrayList<>();
//                        DynamicObject[] addresses = BusinessDataServiceHelper.load("bd_address", "name,detailaddress,ezob_ywydm,ezob_zzdsh,admindivision,phone", qFilterAddress.toArray());
                        DynamicObjectCollection entryAddress = customer.getDynamicObjectCollection("entry_address");
                        for (DynamicObject address : entryAddress) {
                            HashMap<String, Object> addressMap = new HashMap<>();
                            String addreName = address.getString("ezob_shddmc");
                            String locationName = address.getString("addfulladdress");
                            //客户送货具体地址
                            String phoneAddress = address.getString("addphone");
                            String salesCode = customer.getString("ezob_zzdsh.name");
                            String rateConverter = taxRateConverter(salesCode);
                            if (StringUtils.isEmpty(rateConverter) || rateConverter == null) {
                                msg = "增值代税不存在，请重新选择！";
                                throw new RuntimeException("增值代税不存在，请重新选择！");
                            }
                            String admindivision = address.getString("addadmindivision");
                            addressMap.put("Location", addreName);
                            addressMap.put("LocationName", locationName);
                            addressMap.put("StateCode", rateConverter);
                            addressMap.put("SREPCode1", "xs");
                            addressMap.put("SHIP_TO_PHONE",phoneAddress);
                            addressMap.put("STATE_SHIP_TAX_FLAG", "Y");
                            addressMap.put("CountryName", country);
                            addressList.add(addressMap);
                        }
                        if (entryAddress.size() == 0) {
                            HashMap<String, Object> addressMap = new HashMap<>();
                            addressMap.put("Location", "");
                            addressMap.put("LocationName", "");
                            addressMap.put("StateCode", "");
                            addressMap.put("SREPCode1", "xs");
                            addressMap.put("SHIP_TO_PHONE","");
                            addressMap.put("STATE_SHIP_TAX_FLAG", "Y");
                            addressMap.put("CountryName", "");
                            addressList.add(addressMap);
                        }

                        //构建请求对象
                        //判断是否是新增/修改类型
//                        String ezobSfkhtszt = customer.getString("ezob_sfkhtszt");
//                        //1 新增 2 修改
//                        int addFlag;
//                        if (!StringUtils.isEmpty(ezobSfkhtszt) && ezobSfkhtszt != null) {
//                            addFlag = 2;
//                        } else {
//                            addFlag = 1;
//                        }
                        String tbbs = customer.getString("ezob_tbbs");
                        if (tbbs == "") {
                            tbbs = "1";
                        }
                        int addFlag;
                        if (tbbs.equals("1")) {
                            addFlag = 1;
                        } else {
                            addFlag = 2;
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
                        paramBody.put("BILLING_ADDRESS_2",customeraddressen);
                        paramBody.put("ZIP", "");
                        paramBody.put("D0010audited_date", approvedate);
                        paramBody.put("D0010last_modified_date", approvedate);
                        paramBody.put("Customer_label_flag", 0);
                        paramBody.put("PAYMENT_TERMS_FLAG", 0);
                        paramBody.put("CountryName", country);
                        paramBody.put("FED_TAX_ID_NO", settlementtypeidName);
                        System.out.println(formattedDate);
                        paramBody.put("CreateDate", formattedDate);
//                        paramBody.put("UserId", creator);
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
//                            正式
//                            String url = "http://10.101.238.243:8072/api/api/CustomerProject";
//                            测试
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
                                String dmsg = jsonNode.path("Message").asText("");
                                if ("true".equals(code)) {
                                    customer.set("ezob_sfkhtszt", "推送时间" + getCurrTime() + "推送成功！");
                                    customer.set("ezob_tbbs","2");
                                    this.operationResult.setShowMessage(true);

                                    this.operationResult.setSuccess(true);

                                    this.operationResult.setMessage("推送成功！");
                                    log.info("同步思方客户成功：" + response);
                                    successCount++;
                                } else {
                                    customer.set("ezob_sfkhtszt", "推送时间" + getCurrTime() + "推送失败:" + msg);
                                    failCount++;
                                    this.operationResult.setShowMessage(true);

                                    this.operationResult.setSuccess(false);

                                    this.operationResult.setMessage("推送失败:" + dmsg);
//                                log.info("客户推送失败：" + fnumber + msg);
                                    log.info("同步思方客户失败：" + response);

                                }
                                SaveServiceHelper.update(customer);
                                OperationResult opResult = OperationServiceHelper.executeOperate("save","bd_customer", new DynamicObject[]{customer});
                            }
                        } catch (Exception e1) {
                            failCount++;
                            this.getOperationResult().setMessage("同步思方推送不成功，原因：数据有异常：" + e1.getMessage());
                            this.getOperationResult().setShowMessage(true);
//                        log.info("客户推送失败：" + fnumber + e.getMessage());

                        }
                    } catch (Exception e2) {
                        msgFail.append(String.format("%s编码推送不成功，原因：数据有异常,%s\r\n", fnumber, e2.getMessage()));

//                    log.info("客户推送失败：" + fnumber + e.getMessage());
                        this.getOperationResult().setMessage("同步思方推送不成功，原因：数据有异常：" + e2.getMessage());
                        this.getOperationResult().setShowMessage(true);
                        failCount++;
//                        msgFail.append(String.format("%s编码推送不成功，原因：数据有异常,%s\r\n", fnumber, e.getMessage()));

                    }finally {
                        if (!msg.equals("")){
                            this.operationResult.setShowMessage(true);

                            this.operationResult.setSuccess(false);

                            this.operationResult.setMessage(msg);
                        }
                    }

                }
//                this.getView().showMessage("客户推送成功:" + successCount + "条!失败：" + failCount + "条!\r\n"  + msgFail);

            }







//        }catch (Exception e1){
//            this.operationResult.setMessage(e1.getMessage());
//            return;

//        }
//    }




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
    public BigDecimal getHuiLv(){
        String selsql = "/*dialect*/select FEffectDate,FEXRATE from t_bd_exrate " +
                "WHERE FOrgCurID = 6 and FCurID = 1 ORDER BY FEFFECTDATE desc limit 1";

        DataSet selDs = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata,selsql);
        ORM orm = ORM.create();

        DynamicObjectCollection rows = orm.toPlainDynamicObjectCollection(selDs);
        BigDecimal hl = null;
        for (DynamicObject row : rows) {
            hl = row.getBigDecimal("FEXRATE");
        }
        return hl;
    }
}
