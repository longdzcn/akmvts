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

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 描述: 在列表推送供应商
 * 开发者: 易佳伟
 * 创建日期: 1期完成
 * 关键客户：黄小清
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

// GME-DN233931 , 860436-004
public class ListPushSupplier extends AbstractListPlugin implements Plugin {

    private final static String KEY_BARITEM = "ezob_gbmv";

    private static Log log = LogFactory.getLog(ListPushSupplier.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(KEY_BARITEM);
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        String as = "";
        int successCount = 0;
        int failureCount = 0;
        Control source = (Control) evt.getSource();
        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {
            GY gy = new GY();
            try{
                log.info("");//这里面可以在日志搜到，可以把关键信息写到这里面，你来吧

                // 定义请求接口URL
                String url ="https://ctrsim.webapi.meadvilletech.com/api/v2/app/kd/synSupplier";

                // 定义header对象4
                HttpHeaders headers = new HttpHeaders();
                //header请求参数
                String appSecret = "GME";
                String appId = "L8ruVx0ZQPL8YL5zYO2q9BXfdHbw8u0E";
                headers.set("Content-Type","application/json");
                headers.set("MV-Div",appSecret);
                headers.set("MV-AppId",appId);

                //获取当前选中的记录数，并循环推送，推送后保存状态成功
                //出错标识后继续推下一条记录

                ListSelectedRowCollection selectedRows = ((IListView)this.getView()).getSelectedRows();
                //先判断所有单据的状态
                for (ListSelectedRow statuslist:selectedRows)
                {
                    if(statuslist.getBillStatus().equals("C"))
                    {

                    }else {
                        this.getView().showMessage("推送的单据状态必须是已审核！");
                        return;
                    }
                }
                for (ListSelectedRow list1:selectedRows) {
                    try {


                        // 定义请求参数Map
                        Map<String, Object> paramBody = new HashMap<String, Object>();
                        //S为标准采购订单(ApFin_pur_BT_S)，其他都设置为M
                        DynamicObject sup =     BusinessDataServiceHelper.loadSingle(list1.getPrimaryKeyValue(),"bd_supplier");

                        //国家/地区
                        DynamicObject contry = (DynamicObject) sup.get("country");

                        String country = null;

                        if (contry != null) {
                            country = contry.getString("number");
                        }
                        //第一组
                        String number = sup.getString("number");
                        paramBody.put("codeKing", number);//编码

                        String corpSupCode = sup.getString("ezob_mvjbm");
                        paramBody.put("code",corpSupCode);//MV旧编码
                        paramBody.put("corpSupCode", "");

                        Boolean elecPo = sup.getBoolean("ezob_sfjsdzpo");
                        String elecPo2 = "";
                        if (elecPo) {
                            elecPo2 = "Y";
                        } else {
                            elecPo2 = "N";
                        }
                        paramBody.put("elecPo", elecPo2);//添加【是否接收电子PO】【布尔值】

                        String suppName = sup.getString("name");
                        paramBody.put("suppName", suppName);//中文供应商名称

                        String abbrName = sup.getString("ezob_zwjc");
                        paramBody.put("abbrName", abbrName);//中文简称

                        String suppNameEn = sup.getString("ezob_ywmc");
                        paramBody.put("suppNameEn", suppNameEn);//名称（英文）

                        String abbrNameEn = sup.getString("ezob_ywjc");
                        paramBody.put("abbrNameEn", abbrNameEn);//简称（英文）

                        paramBody.put("countryCode", country);//*国家

                        paramBody.put("state", "");//
                        paramBody.put("city", "");//

                        //第二组
                        String county = sup.getString("admindivision");
                        paramBody.put("county", "");//行政区划

                        String billAddress1 = sup.getString("ezob_scdz1");
                        String billaddress2 = sup.getString("ezob_scdz2");
                        paramBody.put("billAddress1",billAddress1);//生产地址1

                        paramBody.put("billAddress2",billaddress2);//生产地址2

                        String phone = sup.getString("bizpartner_phone");
                        paramBody.put("phone", phone);//联系电话

                        String fax1 = sup.getString("bizpartner_fax");
                        paramBody.put("fax", fax1);//传真

                        paramBody.put("vpFlag", "");//
                        paramBody.put("zip", "");//没指出是哪个字段

                        String suppWebsite = sup.getString("url");
                        paramBody.put("suppWebsite", suppWebsite);//公司网址

                        paramBody.put("langFlag", "");//

                        String busIcence = sup.getString("tx_register_no");
                        paramBody.put("busIcence", busIcence);//纳税人识别号

                        //第三组
                        String icenceEffDate = null;
                        if (sup.getString("ezob_datefield") != null) {
                            icenceEffDate = getTime(sup.getDate("ezob_datefield"));

                        }
                        paramBody.put("icenceEffDate", icenceEffDate); //营业执照最后日期

                        String suppType = sup.getString("ezob_gyslb");
                        paramBody.put("suppType", suppType);//供应商类别

                        paramBody.put("iptFlag", "");//
                        paramBody.put("suppMat", "");//分类
                        paramBody.put("suppCategoryType", "");//供应商 +**

                        String suppStatus = sup.getString("ezob_gyszt");
                        if(suppStatus.equals("6"))//临时供应商显示关闭时间字段
                        {
                            if(sup.getDate("ezob_gbsj")!=null)
                            {
                                String endTime = getTime(sup.getDate("ezob_gbsj"));
                                paramBody.put("expDate",endTime);
                            }

                        }
                        paramBody.put("suppStatus", suppStatus);//供应商状态

                        String opcContact = null;
                        if (sup.getDynamicObject("purchaserid") != null) {
                            opcContact = sup.getDynamicObject("purchaserid").getString("name");
                        }
                        paramBody.put("opcContact", opcContact);//负责人

                        String paymentcurrency = null;
                        if (sup.getDynamicObject("paymentcurrency") != null) {
                            paymentcurrency = sup.getDynamicObject("paymentcurrency").getString("number");
                            if (paymentcurrency.equals("CNY")) {
                                paymentcurrency = "RMB";
                            }
                        }


                        paramBody.put("currCode", paymentcurrency);//*付款币别

                        String paycond = null;
                        if (sup.getDynamicObject("paycond") != null) {
                            paycond = sup.getDynamicObject("paycond").getString("number");
                        }
                        paramBody.put("payTermCode", paycond);//*付款条件
                        paramBody.put("accountnumber", "");//


                        //银行信息集合
                        DynamicObjectCollection entrybank = sup.getDynamicObjectCollection("entry_bank");
                        String bank = "";
                        String name = "";
                        String bankaccount = "";
                        String bankNumber ="";
                        String swiftCode ="";
                        for (DynamicObject entry : entrybank) {
                            if (entry.getDynamicObject("bank") != null) {
                                bank = entry.getDynamicObject("bank").getString("name");
                                bankNumber = entry.getDynamicObject("bank").getString("number");
                            }
                            bankaccount = entry.getString("bankaccount");
                            if(country!="CHINA"||country!="01")
                            {
                                swiftCode =  entry.getString("iban");
                            }
                        }
                        //第四组  过
                        paramBody.put("taxType", "");//

                        String tax1 = null;
                        if (sup.getString("taxrate") != null) {
                            tax1 = sup.getDynamicObject("taxrate").getString("taxrate");
                            if(tax1.equals("0E-10"))
                            {
                                tax1 ="0";
                            }
                        }

                        paramBody.put("tax1", tax1);//默认税率(%)

                        paramBody.put("taxIdNumber", "");//
                        paramBody.put("bankName", bank); //开户银行
                        paramBody.put("bankAcc", bankaccount);//银行账号的名称
                        paramBody.put("paymentMethod", "");//待定
                        paramBody.put("bankAddress", "");//
                        paramBody.put("bankCode", bankNumber);
                        paramBody.put("bankEkspress",swiftCode);//swiftCode
                        paramBody.put("paymentTerms", "");//


                        paramBody.put("discount", "");//
                        paramBody.put("discountDays", "");//
                        paramBody.put("remiEmail", "");//
                        paramBody.put("bankBranchName", "");//
                        paramBody.put("branchNumber", "");//

                        Boolean stdPaymentFlag = sup.getBoolean("ezob_bzfk");
                        String stdPaymentFlag2 = "";
                        if (stdPaymentFlag) {
                            stdPaymentFlag2 = "Y";
                        } else {
                            stdPaymentFlag2 = "N";
                        }
                        String paymentDay = sup.getString("ezob_cwzjapts");

                        String effeDate = null;
                        if (sup.getString("ezob_shrq") != null) {
                            effeDate = getTime(sup.getDate("ezob_shrq"));

                        }
                        Boolean ap = sup.getBoolean("payhold");
                        String ap2 = "";
                        if (ap) {
                            ap2 = "Y";
                        } else {
                            ap2 = "N";
                        }
                        paramBody.put("stdPaymentFlag", stdPaymentFlag2); //标准付款
                        paramBody.put("paymentDay", paymentDay);//财务资金安排天数
                        paramBody.put("effeDate", effeDate); //审核日期
                        paramBody.put("oneItemCheck", "");//
                        paramBody.put("ap", ap2);//付款冻结

                        //第六组
                        paramBody.put("cnsignPur", "");//

                        //取多选下拉框的多个值
                        String mvAccount = sup.getString("ezob_mverpzt2");
                        String[] values = mvAccount.split(",");
                        log.info("values"+values+"长度"+values.length);

                        List<Object> sdivList = new ArrayList<>();
                        for (int i=1;i<values.length;i++)
                        {
                            Map<String, Object> sdivmap = new HashMap<>();
                            log.info("values"+i+":"+values[i]);
                            if(!values[i].equals("AXT")&&!values[i].equals("ANBO-YB"))
                            {
//                            if(!values[i].equals("NED2")&&!values[i].equals("NED1")&&!values[i].equals("AXT")&&!values[i].equals("ANBO-YB"))
//                            {
                                sdivmap.put("divCode",values[i]);//工厂代号
                                sdivList.add(sdivmap);
                            }

                        }
                        paramBody.put("sdivList",sdivList); //mvERP账套 divcode

                        //文件列表集合
                        List<Object> attList = new ArrayList<>();
                        Map<String, Object> attmap = new HashMap<>();
                        attmap.put("attList", "");
                        attmap.put("attachType", "");
                        attmap.put("fileName", "");
                        paramBody.put("attList", attList);

                        // 联系人信息集合
                        List<Object> contactList = new ArrayList<>();

                        DynamicObjectCollection entrylinkman = sup.getDynamicObjectCollection("entry_linkman");

                        for (DynamicObject entry : entrylinkman) {
                            Map<String, Object> contactmap = new HashMap<>();
                            long aId = (long) entry.getPkValue();
                            String titelForContact = entry.getString("ezob_zw");
                            String contactName = entry.getString("contactperson");
                            String phoneForContact = entry.getString("phone");
                            String cellPhone = entry.getString("ezob_sjhm");
                            String fax = entry.getString("fax");
                            String emailForContact = entry.getString("email");
                            contactmap.put("Id", aId);
                            contactmap.put("titleForContact", titelForContact);//职位
                            contactmap.put("contactName", contactName);//姓名
                            contactmap.put("phoneForContact", phoneForContact);//电话
                            contactmap.put("cellPhone", cellPhone);//手机号
                            contactmap.put("faxForContact", fax);//传真
                            contactmap.put("emailForContact", emailForContact);//邮箱

                            String mvALL = "";
                            if (entry.getString("ezob_mverpzt")!=null) {
                                mvALL =  entry.getDynamicObject("ezob_mverpzt").getString("number");
                            }
                            contactmap.put("plant", mvALL);//MV账套

                            Boolean receiveElecPoFlag = entry.getBoolean("ezob_sfjsdzpo1");
                            String receiveElecPoFlag2 = "";
                            if (receiveElecPoFlag) {
                                receiveElecPoFlag2 = "Y";
                            } else {
                                receiveElecPoFlag2 = "N";
                            }
                            contactmap.put("receiveElecPoFlag", receiveElecPoFlag2); //电子PO
                            contactList.add(contactmap);
                        }

                        paramBody.put("contactList", contactList);//联系人 List


                        // 认证或资质证书名称
                        List<Object> certList = new ArrayList<>();//cerList

                        DynamicObjectCollection ezobpztxhjaqjgf = sup.getDynamicObjectCollection("ezob_pztxhjaqjgf");

                        for (DynamicObject entry : ezobpztxhjaqjgf) {
                            Map<String, Object> certMap = new HashMap<>();
                            String scName = entry.getString("ezob_rzhzzzsmc");
                            certMap.put("scName", scName);//认证或资质证书名称

                            String scNumber = entry.getString("ezob_zsbh");
                            certMap.put("scNumber", scNumber);//证书编号

                            String effectDate = null;
                            if (entry.getString("ezob_yxrq") != null) {
                                effectDate = getTime(entry.getDate("ezob_yxrq"));
                            }
                            certMap.put("effectDate", effectDate);//有效日期

                            certList.add(certMap);
                        }

                        paramBody.put("certList", certList);//金蝶星瀚新增页签【品质体系、环境、安全及规范】 证书 List

                        //运送地址集合
                        List<Object> addressList = new ArrayList<>();

                        Map<String, Object> addressmap = new HashMap<>();

                        addressmap.put("factoryLocation", abbrName);
                        addressmap.put("factoryAddress1", "");
                        addressmap.put("factoryAddress2", "");
                        String ezobgsdz = sup.getString("ezob_gsdz");
                        addressmap.put("factoryAddress3",ezobgsdz);

                        addressmap.put("phone", phone);

                        String linkman = sup.getString("linkman");
                        addressmap.put("contact", linkman);
                        addressList.add(addressmap);
                        paramBody.put("shipAddressList", addressList);//



//          paramBody.put("divCode", "AKMMV");//默认AKMMV
                        paramBody.put("suppCategoryName", "");//
                        paramBody.put("managerEmpNo", "");//
                        //新增的字段
                        String taxCreditRating = sup.getString("ezob_nsxydj");
                        paramBody.put("taxCreditRating",taxCreditRating);//纳税信用等级
                        JSONObject json = new JSONObject() ;
                        json=JSONObject.fromObject(paramBody);
                        JSONObject aJ2=new JSONObject();
                        JSONArray aJry=new JSONArray();
                        aJry.add(json);
                        aJ2.put("data",aJry);
                        aJ2.toString();
                        String cs = aJ2.toString();
                        log.info("codeKing"+number+"JSON:"+cs);
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
                                sup.set("ezob_pushstatus", "推送时间"+getCurrTime()+"推送成功"+msg);
                                SaveServiceHelper.update(sup);
                                successCount++;
                            }
                            else{
                                sup.set("ezob_pushstatus","推送时间"+getCurrTime()+"推送失败"+msg);
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
                        this.getView().showMessage("供应商推送成功"+successCount+"条,失败"+failureCount+"条,推送时异常"+e.getMessage());
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


    //时间格式转换 yyyy-MM-dd HH:mm:ss
    public static  String  getTime(Date dateTimeString)
    {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date =simpleDateFormat.format(dateTimeString);
        return date;
    }
    public static String getCurrTime()
    {
        Date date = new Date(); SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd :HH:mm:ss");
        String currTime = dateFormat.format(date);
        return currTime;
    }
}