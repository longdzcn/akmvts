package kd.cosmic.hsj;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
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
import kd.cosmic.RestTemplateConfiguration;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * 描述: 物料同步mv,插件放在物料列表
 * 开发者: 胡思江
 * 创建日期: 2024/05/01
 * 关键客户：郑楷绚
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
//6.18 测试账套部署新增:SME SKE 工厂 新增SME SKE 个性化自定义  增加GME取消来料检查字段传输
//6.24 特殊需求SME字段需要做特殊处理
public class    wltsmvERP extends AbstractListPlugin implements Plugin {

    private final static String KEY_BARITEM = "ezob_tbmv";

    private static Log log = LogFactory.getLog(wltsmvERP.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(KEY_BARITEM);
    }
    //加密
    private static String createSignature(String requestBody, String timestamp, String appId, String secretKey)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String dataToSign = requestBody + timestamp + appId;

        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKeySpec);

        byte[] signatureBytes = sha256Hmac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    //    类别属性封装成{}形式
    private static JSONObject createAttribute(String attrName, String value) {
        JSONObject attribute = new JSONObject();
        try {
            attribute.put("attrName", attrName);
            attribute.put("value", value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return attribute;
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        String msg = "";
        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {
            ListSelectedRowCollection selectedRows = ((IListView) this.getView()).getSelectedRows();

            for (ListSelectedRow list : selectedRows) {
                DynamicObject finE = BusinessDataServiceHelper.loadSingle(list.getPrimaryKeyValue(), "bd_material");
//                根据当前推送人账套推送
                try {
                    RequestContext requestContext = RequestContext.get();
                    long currUserId = requestContext.getCurrUserId();
                    DynamicObject bosuser = BusinessDataServiceHelper.loadSingle(currUserId, "bos_user", "name,ezob_mverpzt3");
//                获取数据状态
                    String status = finE.getString("status");

//                    获取创建人工厂信息
                    String cratorgc=finE.getString("creator.ezob_mverpzt3");

                    //获取当前物料已经同步厂区信息
                    String  ytbcq = finE.getString("ezob_mvzh2");
//                获取当前用户姓名
                    String name = bosuser.getString("name");
                    String ezobMverpzt3 = bosuser.get("ezob_mverpzt3").toString();
                    Boolean bo = false;
                    if (status.equals("C")){
                        String message = "";
                        if (ezobMverpzt3!=""&&ezobMverpzt3!=null&&ezobMverpzt3.length()>1){
                            String stripped = StringUtils.strip(ezobMverpzt3, ",");
                            String [] pz = stripped.split(",");
                            //获取当前账号的工厂 pz
                            StringBuilder msgString =new StringBuilder();
                            //循环结束后统一弹出与改值
                            //成功的工厂
                            String sb="";
                            //失败的工厂
                            String sbto="失败厂区：";
                            for (String ps : pz){
                                JsonNode rootNodeTS = null;
                                rootNodeTS = getForXun(finE,ps,name,msgString);
                                if (rootNodeTS == null) {
                                    //无响应可能进入此处
                                    message = "请检查该物料是否生成对应业务信息";
                                } else {
                                    Boolean success = rootNodeTS.path("saveSuccess").asBoolean();
                                    if (success == false) {
                                        //截取报错信息
                                        String msgto = rootNodeTS.path("msg").toString();
                                        String bcxx =  rootNodeTS.path("errorMsg").toString();
                                        String mg = "errorMsg";
                                        int index = bcxx.indexOf(mg);
                                        if(index!=-1){
                                            bcxx = bcxx.substring(index+mg.length());
                                        }
                                        bcxx = removeAfterLast(bcxx,"}");
                                        message = message + rootNodeTS.path("errorMsg").toString();
                                        sbto+=ps+":"+bcxx+"_msg:"+msgto+"/";
                                    }else {
                                        if (ytbcq.contains(ps)){

                                        }else {
                                            if(ytbcq.length()>=2){
                                                ytbcq+=","+ps;
                                            }else {
                                                ytbcq+=ps;
                                            }
                                        }
                                        sb+=ps+",";
                                    }
                                }

                            }
                            String invPartNumber = (String) finE.get("number");
                            if (message == null||message =="") {

                                msg = msg + invPartNumber + "  物料推送成功！推送时间：" + getCurrTime();
                                finE.set("ezob_pushstatus", msg + "  推送人：" + name);
                                SaveServiceHelper.update(finE);
                                finE.set("ezob_tbmv", 2);
//                              同步成功，追加工厂去掉最后的,号
                                finE.set("ezob_mvzh2",StringUtils.removeEnd(ytbcq, ","));
                                //msgString.append(ps+",");
                                this.getView().showMessage("推送人所属工厂同步成功");

                            } else {
                                //String Message = rootNodeTS.path("errorMsg").toString();
                                if(sbto!=null){
                                    message = sbto;
                                }
                                if(sb!=null&&sb.length()>1){
                                    message = message+"存在部分成功厂区：  "+sb+"("+invPartNumber+")";
                                    //finE.set("ezob_mvzh2",sb);
                                    //如存在部分成功，也应该反写同步标识并且增加同步厂区
                                    //ytbcq+=sb;
                                    finE.set("ezob_mvzh2",StringUtils.removeEnd(ytbcq, ","));
                                    //finE.set("ezob_mvzh2",sb);
                                    finE.set("ezob_tbmv", 2);
                                }

                                log.error("报错原因" + message);
                                //this.getView().showErrMessage("同步失败！", Message);
                                //String errorMsgmsg = rootNodeTS.path("errorMsg").toString();
                                this.getView().showMessage(message);
                                finE.set("ezob_pushstatus", "推送时间" + getCurrTime() + "推送失败"+message+"  推送人：" + name);
                                log.error("物料推送失败" + msg);
                            }
//                        追加已成功工厂
                            //有吉写，暂时不懂，先注释。-6.1
//                            String ezobMvzh2 = finE.getString("ezob_mvzh2");
//                            msgString.append(ezobMvzh2);
//                            String s = msgString.toString().replaceAll(",$", "");
//
//                            String[] parts = s.split(",");
//
//                            // 使用HashSet去除重复项
//                            Set<String> uniqueParts = new HashSet<>(Arrays.asList(parts));
//
//                            // 转换回逗号分隔的字符串
//                            String result = uniqueParts.stream()
//                                    .collect(Collectors.joining(","));
//
//                            finE.set("ezob_mvzh2",result);

//                        页面更新
                            SaveServiceHelper.update(finE);
                        }else {
                            log.error("无法获取MVERP地址，请在人员信息中维护相关MVERP账套！");
                            this.getView().showMessage("无法获取MVERP地址，请在人员信息中维护相关MVERP账套！");
                        }

                    }else {
                        log.error("请查看该物料是否审核");
                        this.getView().showMessage("请查看该物料是否审核");

                    }

                } catch (NullPointerException e) {
                    this.getView().showMessage("物料未审核！");
                    throw new RuntimeException("物料未审核 ！" + e.getMessage(), e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        }
    }


    public static String removeAfterLast(String str, String substr) {
        int lastIndex = str.lastIndexOf(substr);
        if (lastIndex != -1) {
            return str.substring(0,lastIndex);
        }
        return str;
    }

    public static String getCurrTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd :HH:mm:ss");
        return dateFormat.format(date);
    }

    public static String getName(String flnumber,String xhname,String gc) {
        try {
            QFilter qFilter = new QFilter("ezob_wltype", QCP.equals,flnumber ).and("ezob_xhname",QCP.equals,xhname).and("fdescription",QCP.equals,gc);
            DynamicObject numberObject=null;
            numberObject = BusinessDataServiceHelper.loadSingle("bos_assistantdata_detail", "ezob_mvname", qFilter.toArray());

            if (numberObject==null){
                log.error("在对照表中没找到");
                return  null;
            }else {
                String id = numberObject.getString("ezob_mvname");
                return  id;
            }


        }catch (Exception e){
            return  null;
        }

    }
    public static String getNameDw(String flnumber,String xhname) {
        try {

            DynamicObject numberObject=null;
            QFilter qFilter = new QFilter("ezob_wltype", QCP.equals,flnumber ).and("ezob_xhname",QCP.equals,xhname).and("fdescription",QCP.equals,"单位");
            numberObject = BusinessDataServiceHelper.loadSingle("bos_assistantdata_detail", "ezob_mvname", qFilter.toArray());
            if (numberObject==null){
                return  null;
            }else {
                String  id = numberObject.getString("ezob_mvname");
                if (id.length()>0){
                    return  id;
                }else {
                    return  null;
                }

            }



        }catch (NullPointerException e){
            return  null;
        }

    }

    public static JsonNode getUrl(String data,String url, HttpHeaders headers) throws IOException {
        try {
            // 发送请求
            HttpEntity<String> entity = new HttpEntity<String>(data, headers);
            RestTemplate template = new RestTemplate(RestTemplateConfiguration.generateHttpRequestFactory());
            ResponseEntity<String> exchange = template.exchange(url, HttpMethod.POST, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            if (exchange.getStatusCode().is2xxSuccessful()){
            }
            JsonNode rootNodeTS = null;
            rootNodeTS = mapper.readTree(exchange.getBody());
            return rootNodeTS;
        } catch (JsonProcessingException | NoSuchAlgorithmException | KeyManagementException |
                 KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }


    public JsonNode getForXun(DynamicObject finE,String cq,String username,StringBuilder msgString) throws IOException {
        {

            JSONObject data = new JSONObject();
            //                根据mvERP账套字段判断工厂
            Boolean lo = true;
            if (cq.equals("GME")) {
                String matTypeCode = finE.getString("ezob_wlfl.ezob_gmewl.number");
                data.put("matTypeCode", matTypeCode);
                String matTypeCodeDM = finE.getString("ezob_gmescdm");
                if(matTypeCodeDM!=null&&matTypeCodeDM.length()>0){
                    data.put("prodCode", matTypeCodeDM);
                }

            } else if (cq.equals("FPC")) {
                String matTypeCode = finE.getString("ezob_wlfl.ezob_fpcwl.number");
                data.put("matTypeCode", matTypeCode);
                String matTypeCodeDM = finE.getString("ezob_fpcscdm");
                if(matTypeCodeDM!=null&&matTypeCodeDM.length()>0){
                    data.put("prodCode", matTypeCodeDM);
                }
            } else if (cq.equals("SP")){
                String matTypeCode = finE.getString("ezob_wlfl.ezob_spwl.number");
                data.put("matTypeCode", matTypeCode);
                String matTypeCodeDM = finE.getString("ezob_spscdm");
                if(matTypeCodeDM!=null&&matTypeCodeDM.length()>0){
                    data.put("prodCode", matTypeCodeDM);
                }
            } else if (cq.equals("SZII")) {
                String matTypeCode = finE.getString("ezob_wlfl.ezob_szlllw.number");
                data.put("matTypeCode", matTypeCode);
                String matTypeCodeDM = finE.getString("ezob_sziiscdm");
                if(matTypeCodeDM!=null&&matTypeCodeDM.length()>0){
                    data.put("prodCode", matTypeCodeDM);
                }
            } else if (cq.equals("XMT-1")) {
                String matTypeCode = finE.getString("ezob_wlfl.ezob_xmtwl.number");
                data.put("matTypeCode", matTypeCode);
                String matTypeCodeDM = finE.getString("ezob_xmtscdm");
                if(matTypeCodeDM!=null&&matTypeCodeDM.length()>0){
                    data.put("prodCode", matTypeCodeDM);
                }
            }else if(cq.equals("SME")){
                String matTypeCode = finE.getString("ezob_wlfl.ezob_smewl.number");
                data.put("matTypeCode", matTypeCode);
                String matTypeCodeDM = finE.getString("ezob_smescdm");
                if(matTypeCodeDM!=null&&matTypeCodeDM.length()>0){
                    data.put("prodCode", matTypeCodeDM);
                }
            }else if(cq.equals("SKE")){
                String matTypeCode = finE.getString("ezob_wlfl.ezob_skewl.number");
                data.put("matTypeCode", matTypeCode);
                String matTypeCodeDM = finE.getString("ezob_skescdm");
                if(matTypeCodeDM!=null&&matTypeCodeDM.length()>0){
                    data.put("prodCode", matTypeCodeDM);
                }
            }
            else {
                lo = false;
            }

            String invPartNumber = (String) finE.get("number");
            //                    采购代号
            String purchaseUnitCode = "";
            //                    库存单位代号
            String stockUnitCode = "";
            //保质期
            int lifeDays = 0;
           try {
                String sqlcg = "/*dialect*/ select tbm.fnumber as 物料编码,tbmu.FNUMBER\n" +
                        "from akmmv_prd_eip_test.T_BD_Material tbm\n" +
                        "left join akmmv_prd_eip_test.t_bd_materialpurinfo tbmp on tbm.fid = tbmp.FMASTERID\n" +
                        "left join akmmv_prd_eip_test.T_bd_Measureunit tbmu on tbmu.fid = tbmp.fpurchaseunitid \n" +
                        "left join akmmv_prd_eip_test.t_ORG_ORG too on tbm.fcreateorgid = too.FID\n" +
                        "where tbm.fnumber ='" + invPartNumber + "'";
                DataSet cg = DB.queryDataSet(wltbsf.class.getName(), DBRoute.of("eip"), sqlcg);

                if (!cg.isEmpty()) {
                    Row next = cg.next();
                    if (next.get(1) == null) {
                        log.error("推送失败" + invPartNumber + "物料没有生成采购信息!");
                        this.getView().showErrorNotification("推送失败:物料没有生成采购信息!!！");
                        //continue;
                        return  null;
                    }
                    purchaseUnitCode = next.get(1).toString();
                }

                String sqlkc = "/*dialect*/ select tbm.fnumber as 物料编码,tbmu.FNUMBER,tbmix.fshelflife,tbmi.fk_ezob_sfczwl,tbmi.fmininvqty,tbmi.fmaxinvqty\n" +
                        "from akmmv_prd_eip_test.T_BD_Material tbm\n" +
                        "left join akmmv_prd_eip_test.t_bd_materialinvinfo tbmi on tbm.fid = tbmi.FMASTERID\n" +
                        "left join akmmv_prd_eip_test.t_bd_materialinvinfo_x tbmix on tbmix.fid = tbmi.fid \n" +
                        "left join akmmv_prd_eip_test.T_bd_Measureunit tbmu on tbmu.fid = tbmi.finventoryunitid \n" +
                        "left join akmmv_prd_eip_test.t_ORG_ORG too on tbm.fcreateorgid = too.FID\n" +
                        "where tbm.fnumber ='" + invPartNumber + "'";
                DataSet kc = DB.queryDataSet(wltbsf.class.getName(), DBRoute.of("eip"), sqlkc);





                if (!kc.isEmpty()) {
                    Row next = kc.next();
                    if (next.get(1) == null) {
                        log.error("推送失败" + invPartNumber + "物料没有生成库存信息!");

                        this.getView().showErrorNotification("推送失败:物料没有生成库存信息!!！");
                        return  null;
                        //continue;
                    }

                    //如果当前物料库存信息保质期不为0则进入
                    if (next.get(2) != null) {
                        lifeDays = next.getInteger(2);
                    }

                    stockUnitCode = next.get(1).toString();
                }
            }catch (NullPointerException e){
               lo = false;
               return  null;
           }
            lifeDays =  finE.getInt("ezob_bzq");
            HttpHeaders headers = new HttpHeaders();
            //header请求参数




            if (lo) {


//            获取编码以编码做区分

                String msg = "";

                //DynamicObject finE = BusinessDataServiceHelper.loadSingle(list.getPrimaryKeyValue(), "bd_material");
//
//                判断是否是禁用的物料
                String enable = finE.getString("enable");
                if (enable.equals("0")){
                    data.put("stopPurchFlag","Y");
                }else {
                    data.put("stopPurchFlag","N");
                }
                int flag = finE.getInt("ezob_tbmv");
                //状态
                String status = finE.getString("status");
                //编码

                if (!status.equals("C")) {
                    log.error("报错原因:" + invPartNumber + "物料未审核");
                    msg += ("推送失败" + invPartNumber + "物料未审核");
                    this.getView().showErrorNotification("推送失败:物料未审核!!！");
                    //continue;
                }
                //单位重量
                String unitWeight = finE.get("grossweight").toString();

                String mvzh2=finE.getString("ezob_mvzh2");
//                mv同步标识
                String ezobTbmv = finE.getString("ezob_tbmv");

                //用于判断是否是修改
                if (ezobTbmv.equals("2")) {
                    if ( mvzh2.contains(cq)){
                        data.put("oldInvPartNumber",invPartNumber);
                    }else {
                        data.put("invPartNumber", invPartNumber);
                    }
                }else {
                    //如果为修改则不传
                    data.put("invPartNumber", invPartNumber);
                }
                //测试错误情况使用
//                if(cq.equals("GME")){
//                    data.put("invPartNumber", invPartNumber);
//                }
//                if(cq.equals("FPC")){
//                    data.put("invPartNumber", invPartNumber);
//                }
//                if(cq.equals("SP")){
//                    data.put("invPartNumber", invPartNumber);
//                }

//                    invPartDesc     物料描述
                String invPartDesc = (String) finE.get("ezob_wlms");
//                获取创建人信息
                //String creator = finE.get("creator.name").toString();
                String halogenFlag = "N";
                if (finE.getBoolean("ezob_hf")) {
                    halogenFlag = "Y";
                }
                //内销外销 4.29  等于保税就是外销其余内销
                String domesticFlag = finE.getString("ezob_bsykgldbs");
                if (domesticFlag.equals("1")) {
                    domesticFlag = "2";
                } else {
                    domesticFlag = "1";
                }

                //生产代码  4.29设置非必传

                String prodCode = finE.getString("ezob_wlfl");
                //  物料类型代码   物料分类编码
                String matTypeCodeXH = finE.getString("ezob_wlfl.number");
                //查询采购信息是否存在
//                String sqlcg = "/*dialect*/ select tbm.fnumber as 物料编码,tbmu.FNUMBER\n" +
//                        "from akmmv_prd_eip_test.T_BD_Material tbm\n" +
//                        "left join akmmv_prd_eip_test.t_bd_materialpurinfo tbmp on tbm.fid = tbmp.FMASTERID\n" +
//                        "left join akmmv_prd_eip_test.T_bd_Measureunit tbmu on tbmu.fid = tbmp.fpurchaseunitid \n" +
//                        "left join akmmv_prd_eip_test.t_ORG_ORG too on tbm.fcreateorgid = too.FID\n" +
//                        "where tbm.fnumber ='" + invPartNumber + "'";
//                DataSet cg = DB.queryDataSet(wltbsf.class.getName(), DBRoute.of("eip"), sqlcg);
////                    采购代号
//                String purchaseUnitCode = "";
//                if (!cg.isEmpty()) {
//                    Row next = cg.next();
//                    if (next.get(1) == null) {
//                        log.error("推送失败" + invPartNumber + "物料没有生成采购信息!");
//                        msg += ("推送失败" + invPartNumber + "物料没有生成采购信息!");
//                        this.getView().showErrorNotification("推送失败:物料没有生成采购信息!!！");
//                        //continue;
//                    }
//                    purchaseUnitCode = next.get(1).toString();
//                }
//
//                String sqlkc = "/*dialect*/ select tbm.fnumber as 物料编码,tbmu.FNUMBER,tbmix.fshelflife,tbmi.fk_ezob_sfczwl,tbmi.fmininvqty,tbmi.fmaxinvqty\n" +
//                        "from akmmv_prd_eip_test.T_BD_Material tbm\n" +
//                        "left join akmmv_prd_eip_test.t_bd_materialinvinfo tbmi on tbm.fid = tbmi.FMASTERID\n" +
//                        "left join akmmv_prd_eip_test.t_bd_materialinvinfo_x tbmix on tbmix.fid = tbmi.fid \n" +
//                        "left join akmmv_prd_eip_test.T_bd_Measureunit tbmu on tbmu.fid = tbmi.finventoryunitid \n" +
//                        "left join akmmv_prd_eip_test.t_ORG_ORG too on tbm.fcreateorgid = too.FID\n" +
//                        "where tbm.fnumber ='" + invPartNumber + "'";
//                DataSet kc = DB.queryDataSet(wltbsf.class.getName(), DBRoute.of("eip"), sqlkc);
//
////                    库存单位代号
//                String stockUnitCode = "";
//                //保质期
//                int lifeDays = 0;
//
//                if (!kc.isEmpty()) {
//                    Row next = kc.next();
//                    if (next.get(1) == null) {
//                        log.error("推送失败" + invPartNumber + "物料没有生成库存信息!");
//                        msg += ("推送失败" + invPartNumber + "物料没有生成库存信息!");
//                        this.getView().showErrorNotification("推送失败:物料没有生成库存信息!!！");
//                        //continue;
//                    }
//
//                    //如果当前物料库存信息保质期不为0则进入
//                    if (next.get(2) != null) {
//                        lifeDays = next.getInteger(2);
//                    }
//                    lifeDays =  finE.getInt("ezob_bzq");
//                    stockUnitCode = next.get(1).toString();
//                }


                JSONArray attrList = new JSONArray();
//                此处开始添加类别属性
//                DynamicObject KHBILL = BusinessDataServiceHelper.loadSingle(list, "ezob_wlcsk",
//                "group.number,ezob_cskzd.name,name,ezob_tbmvzt,ezob_cskzd,ezob_cskzd.ezob_cskzdm,*");


                try {
                    String ezobSbmc = finE.getString("ezob_sbmc");
                    String names = getName(matTypeCodeXH, "设备名称", cq);
                    if (!Objects.equals(ezobSbmc, "")) {
                        if (names == null || "".equals(names)) {
                            names = "设备名称";
                        }
                        attrList.put(createAttribute(names, ezobSbmc));
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("设备名称", ""));
                }

                //String name = getName(matTypeCodeXH,"宽度(经向)/inch");
                // System.err.println(name+"11111111111111111111111111");
                try {
                    String modelnum = finE.getString("modelnum");
                    String name = getName(matTypeCodeXH, "物料型号", cq);
                    if (!Objects.equals(modelnum, "")) {
                        if (name == "" ||name == null) {
                            name = "物料型号";
                        }//这边家，前面if不用加  就判断返回的字段名是不是Null
//                        if(matTypeCodeXH.equals("M1.01")){
//                            attrList.put(createAttribute("FCCL规格", modelnum));
//                        }
                        attrList.put(createAttribute(name, modelnum));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("设备名称", ""));
                }

                try {
                    String ezobPpmake = finE.getString("ezob_ppmake.name");
                    String name = getName(matTypeCodeXH, "品牌(制造商)", cq);
                    if (!Objects.equals(ezobPpmake, "")&&ezobPpmake!=null) {
                        if (name ==""|| name==null) {
                            name = "品牌(制造商)";
                        }
                        attrList.put(createAttribute(name, ezobPpmake));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("设备名称", ""));
                }


                String ezobPipp = null;
                try {
                    ezobPipp = finE.getString("ezob_pipp.name");
                    String name = getName(matTypeCodeXH, "PI品牌", cq);

                    if (ezobPipp!=null){
                        if (name == null||name=="") {
                            name = "PI品牌";
                        }
                        attrList.put(createAttribute(name, ezobPipp));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("PI品牌", ""));
                }

                try {
                    BigDecimal bigDecimal = (BigDecimal) finE.get("ezob_kdjx");
                    if (bigDecimal != null) {
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String dataformat = format.format(bigDecimal);
                        String names = getName(matTypeCodeXH, "宽度(经向)", cq);
                        //String names1 = getName(matTypeCodeXH,"宽度(经向)");
                        //tring name = getName(matTypeCodeXH,"宽度(经向)单位");
                        if (names == null) {
                            names = "宽度(经向)";
                        }
                        if (!dataformat.equals("0")) {
                            //ezob_kddw
                            String kdjxdw = finE.get("ezob_kddw").toString();
                            attrList.put(createAttribute(names, dataformat));
                            String dw = getNameDw(matTypeCodeXH, "宽度(经向)单位");
                            if (dw == ""|| dw==null) {
                                dw = "宽度(经向)单位";
                            }


                            if(cq.equals("SME")&&(matTypeCodeXH.equals("M1.15")||matTypeCodeXH.equals("M1.01"))){
                                dw = "尺寸(经向)单位";
                            }
                            if(cq.equals("SME")&&matTypeCodeXH.equals("M1.03")){
                                dw ="宽度单位";
                            }
                            attrList.put(createAttribute(dw, kdjxdw));
                        }
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("宽度(经向)", ""));
                }
                //统一数值接受字段，取消不同单位不同字段——6.11
//                try {
//                    BigDecimal ezob_kdwidh = (BigDecimal) finE.get("ezob_kdwidh");
//                    if (ezob_kdwidh != null) {
//                        DecimalFormat format = new DecimalFormat("#.##########");
//                        String dataformat = format.format(ezob_kdwidh);
//                        //attrList.put(createAttribute("宽度(经向)", ezob_kdwidh));
//                        //attrList.put(createAttribute("宽度(经向)单位", "inch"));
//                        String names = getName(matTypeCodeXH, "宽度(经向)/inch", cq);
//                        if (names == null || names =="") {
//                            names = "宽度(经向)/inch";
//                        }
//                        if (!dataformat.equals("0")) {
//                            attrList.put(createAttribute(names, dataformat));
//                            String dw = getNameDw(matTypeCodeXH, "宽度(经向)/inch");
//                            if (dw == null ||dw=="") {
//                                dw = "宽度单位";
//                            }
//                            attrList.put(createAttribute(dw, "inch"));
////                                String  dw = "\"";
////                                String dws = dw.replace("\\","");
////                                attrList.put(createAttribute("宽度(经向)单位", "\""));
//                        }
//                    }
//                } catch (Exception e) {
//                    //attrList.put(createAttribute("宽度(经向)", ""));
//                }

                try {
                    String ezobFcclgg = finE.getString("ezob_fcclgg.name");
                    String names = getName(matTypeCodeXH, "FCCL规格", cq);
                    if (ezobFcclgg!=null) {
                        if (names == null||names =="") {
                            names = "FCCL规格";
                        }
                        attrList.put(createAttribute(names, ezobFcclgg));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("FCCL规格", ""));
                }

                try {
                    String ezobPpgg = finE.getString("ezob_ppgg.name");
                    String names = getName(matTypeCodeXH, "P片规格", cq);
                    if (ezobPpgg !=null){
                        if (names == null||names =="") {
                            names = "P片规格";
                        }
                        attrList.put(createAttribute(names, ezobPpgg));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("P片规格", ""));
                }
                try {
                    String ezobKhxlh = finE.getString("ezob_khxlh");
                    String names = getName(matTypeCodeXH, "客户系列号", cq);
                    if (!Objects.equals(ezobKhxlh, "")) {
                        if (names == null||names =="") {
                            names = "客户系列号";
                        }
                        attrList.put(createAttribute(names, ezobKhxlh));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("客户系列号", ""));
                }

                try {
                    String ezobGcscxh = finE.getString("ezob_gcscxh");
                    String names = getName(matTypeCodeXH, "工厂生产型号", cq);
                    if (!Objects.equals(ezobGcscxh, "")) {
                        if (names == null||names =="") {
                            names = "工厂生产型号";
                        }
                        attrList.put(createAttribute(names, ezobGcscxh));
                    }
                } catch (Exception e) {

                }

                try {
                    String ezobCb = finE.getString("ezob_cb");
                    String names = getName(matTypeCodeXH, "层别(面次)", cq);
                    if (!Objects.equals(ezobCb, "")) {
                        if (names == null||names =="") {
                            names = "层别(面次)";
                        }
                        attrList.put(createAttribute(names, ezobCb));
                    }
                } catch (Exception e) {

                }
                try {
                    String ezobXlh = finE.getString("ezob_xlh");
                    String names = getName(matTypeCodeXH, "图纸号", cq);
                    if (!Objects.equals(ezobXlh, "")) {
                        if (names == null||names =="") {
                            names = "图纸号";
                        }
                        attrList.put(createAttribute(names, ezobXlh));
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("图纸号", null));
                }

                try {
                    String ezobCsdy = finE.getString("ezob_csdy");
                    String names = getName(matTypeCodeXH, "测试单元", cq);
                    if (!Objects.equals(ezobCsdy, "")) {
                        if (names == null||names =="") {
                            names = "测试单元";
                        }
                        attrList.put(createAttribute(names, ezobCsdy));
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("测试单元", null));
                }
                try {
                    String ezobCsunit = finE.getString("ezob_csunit.name");
                    String names = getName(matTypeCodeXH, "纯胶类型", cq);
                    if (!Objects.equals(ezobCsunit, "") && ezobCsunit != null) {
                        if (names == null||names =="") {
                            names = "纯胶类型";
                        }
                        attrList.put(createAttribute(names, ezobCsunit));
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("测试单元", null));
                }

                try {
                    BigDecimal ezobCdwx = (BigDecimal) finE.get("ezob_cdwx");
                    if (ezobCdwx != null) {

                        DecimalFormat format = new DecimalFormat("#.##########");
                        String dataformat = format.format(ezobCdwx);
                        String name = getName(matTypeCodeXH, "长度(纬向)", cq);
                        if (!dataformat.equals("0")) {
                            if (name == null||name =="") {
                                name = "长度(纬向)";
                            }
                            attrList.put(createAttribute(name, dataformat));
//                            String names= getName(matTypeCodeXH,"长度(纬向)单位");
                            String cddw = finE.get("ezob_cddw").toString();
                            String dw = getNameDw(matTypeCodeXH, "长度(纬向)单位");


                            if (dw == ""||dw == null) {
                                dw = "长度(纬向)单位";
                            }
                            if(cq.equals("SME")&&(matTypeCodeXH.equals("M1.15")||matTypeCodeXH.equals("M1.01"))){
                                dw ="板材尺寸(纬向)单位";
                            }
                            if(cq.equals("SME")&&matTypeCodeXH.equals("M1.03")){
                                dw ="长度单位";
                            }
                            attrList.put(createAttribute(dw, cddw));

                        }
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("长度(纬向)", null));
                }
                //统一数值接受字段——取消根据单位拆分字段—6.11
//                try {
//                    BigDecimal ezob_length = (BigDecimal) finE.get("ezob_length");
//                    if (ezob_length != null) {
//                        //attrList.put(createAttribute("长度(纬向)", ezob_length));
//                        //attrList.put(createAttribute("长度(纬向)单位", "inch"));
//                        DecimalFormat format = new DecimalFormat("#.##########");
//                        String dataformat = format.format(ezob_length);
//                        String name = getName(matTypeCodeXH, "长度(纬向)/inch", cq);
//                        if (!dataformat.equals("0")) {
//                            if (name == ""||name== null) {
//                                name = "长度(纬向)/inch";
//                            }
//                            attrList.put(createAttribute(name, dataformat));
//                            String dw = getNameDw(matTypeCodeXH, "长度(纬向)/inch");
//                            if (dw == null||dw =="") {
//                                dw = "长度单位";
//                            }
//                            attrList.put(createAttribute(dw, "inch"));
//                            //attrList.put(createAttribute("长度(纬向)单位", "\""));
//                        }
//                    }
//                } catch (Exception e) {
//                    //attrList.put(createAttribute("长度(纬向)", null));
//                }
                try {
                    String ezobCc = finE.getString("ezob_cc");
                    String name = getName(matTypeCodeXH, "尺寸", cq);
                    if (!Objects.equals(ezobCc, "")) {
                        if (name == null||name =="") {
                            name = "尺寸";
                        }
                        attrList.put(createAttribute(name, ezobCc));
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("尺寸", null));
                }
                try {
                    BigDecimal ezobHd = (BigDecimal) finE.get("ezob_hd");
                    if (ezobHd != null) {
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String dataformat = format.format(ezobHd);
                        String name = getName(matTypeCodeXH, "厚度", cq);
                        if (!dataformat.equals("0")) {
                            if (name == ""||name== null) {
                                name = "厚度";
                            }
                            String ezobHddw = finE.get("ezob_hddw").toString();
                            attrList.put(createAttribute(name, dataformat));
                            String dw = getNameDw(matTypeCodeXH, "厚度单位");
                            if (dw ==""||dw== null) {
                                dw = "厚度单位";
                            }
                            attrList.put(createAttribute(dw, ezobHddw));
                        }
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("厚度", null));
                }
                try {
                    BigDecimal decimal = (BigDecimal) finE.get("ezob_fgm");
                    if (decimal != null) {
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String dataformat = format.format(decimal);
                        String name = getName(matTypeCodeXH, "覆盖膜油厚/um", cq);
                        if (!dataformat.equals("0")) {
                            if (name == null||name =="") {
                                name = "覆盖膜油厚/um";
                            }
                            attrList.put(createAttribute(name, dataformat));
                            String dw = getNameDw(matTypeCodeXH, "覆盖膜油厚/um");
                            if (dw == null ||dw =="") {
                                dw = "覆盖膜单位";
                            }
                            attrList.put(createAttribute(dw, "um"));
                        }
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("厚度", null));
                }
                try {
                    String ezobTncode = finE.getString("ezob_tncode.name");
                    String name = getName(matTypeCodeXH, "厚度代码", cq);
                    if (ezobTncode !=null) {
                        //DecimalFormat format = new DecimalFormat("#.##########");
                        //String dataformat = format.format(ezob_tncode);
                        if (name == ""||name== null) {
                            name = "厚度代码";
                        }
                        attrList.put(createAttribute(name, ezobTncode));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("厚度", null));
                }


                try {
                    String eString = finE.getString("ezob_lx");
                    String name = getName(matTypeCodeXH, "类型", cq);
                    if (!eString.isEmpty()) {
                        if (name == ""||name== null) {
                            name = "类型";
                        }
                        attrList.put(createAttribute(name, eString));
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("类型", null));

                }

                try {
                    String ezobYs = finE.getString("ezob_ys.name");
                    String name = getName(matTypeCodeXH, "颜色", cq);

                    if (ezobYs != null) {
                        if (name == ""||name== null) {
                            name = "颜色";
                        }
                        attrList.put(createAttribute(name, ezobYs));
                    }
                } catch (Exception e) {

                }

                try {
                    String ezobHlnd = finE.getString("ezob_hlnd");
                    String name = getName(matTypeCodeXH, "含量/浓度", cq);
                    if (!Objects.equals(ezobHlnd, "")) {
                        if (name == null||name =="") {
                            name = "含量/浓度";
                        }
                        attrList.put(createAttribute(name, ezobHlnd));
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("含量/浓度", null));
                }
                try {
                    String ezobTncode = finE.getString("ezob_level.name");
                    String name = getName(matTypeCodeXH, "级别", cq);
                    if (ezobTncode != null) {
                        //DecimalFormat format = new DecimalFormat("#.##########");
                        //String dataformat = format.format(ezob_tncode);
                        if (name == null||name =="") {
                            name = "级别";
                        }
                        attrList.put(createAttribute(name, "级别" + ezobTncode));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("厚度", null));
                }


                try {
                    String finEString = finE.getString("ezob_cdu");
                    String name = getName(matTypeCodeXH, "纯度", cq);
                    if (!Objects.equals(finEString, "")) {
                        if (name == null||name =="") {
                            name = "纯度";
                        }
                        attrList.put(createAttribute(name, finEString));
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("纯度", null));
                }
                try {
                    BigDecimal decimal = (BigDecimal) finE.get("ezob_xbhd");
                    if (decimal != null) {
                        //attrList.put(createAttribute("板厚", ezob_xbhd));
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String dataformat = format.format(decimal);
                        String name = getName(matTypeCodeXH, "芯板厚度", cq);
                        if (!dataformat.equals("0") && name != null) {
                            if (name == null||name =="") {
                                name = "芯板厚度";
                            }
                            attrList.put(createAttribute(name, dataformat));
                            String ezobXbhddw = finE.get("ezob_xbhddw").toString();
                            String dw = getNameDw(matTypeCodeXH, "芯板厚度单位");
                            if (dw == null||name =="") {
                                dw = "芯板厚度单位";
                            }
                            attrList.put(createAttribute(dw, ezobXbhddw));
                        }
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("芯板厚度", null));
                }

                try {
                    String sfsyhscl = finE.getString("ezob_sfsyhscl");
                    if(sfsyhscl.equals("0")){
                        attrList.put(createAttribute("是否使用回收材料", "Y"));
                    }else{
                        attrList.put(createAttribute("是否使用回收材料", "N"));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("图纸版本", null));
                }

//                try {
//                    BigDecimal ezob_xbhdml = (BigDecimal) finE.get("ezob_xbhdml");
//                    if (ezob_xbhdml != null) {
//                        //attrList.put(createAttribute("板厚", ezob_xbhdml));
//                        DecimalFormat format = new DecimalFormat("#.##########");
//                        String dataformat = format.format(ezob_xbhdml);
//                        String name = getName(matTypeCodeXH, "芯板厚度", cq);
//                        if (!dataformat.equals("0")) {
//                            if (name == null||name =="") {
//                                name = "芯板厚度";
//                            }
//                            attrList.put(createAttribute(name, dataformat));
//                            String ezob_xbhddw = finE.get("ezob_xbhddw").toString();
//                            String dw = getNameDw(matTypeCodeXH, "芯板厚度单位");
//                            if (dw == null||name =="") {
//                                dw = "芯板厚度单位";
//                            }
//                            attrList.put(createAttribute(dw, ezob_xbhddw));
//                        }
//                    }
//                } catch (Exception e) {
//                    //attrList.put(createAttribute("芯板厚度", null));
//                }
//                try {
////                    BigDecimal ezob_dbzj = (BigDecimal) finE.get("ezob_dbzj");
//                    String ezob_xbhdgc = finE.getString("ezob_xbhdgc");
//                    if (ezob_xbhdgc != null) {
//                        DecimalFormat format = new DecimalFormat("#.##########");
//                        String dataformat = format.format(ezob_xbhdgc);
//                        String name = getName(matTypeCodeXH,"芯板厚度公差");
//                        //String names = getName(matTypeCodeXH,"板厚公差");
//                        if (!dataformat.equals("0")){
//                            attrList.put(createAttribute(name, dataformat));
//                        }
//
//
//                    }
//                }catch (Exception e){
//                    //attrList.put(createAttribute("芯板厚度", null));
//                }
//                try {
//                    String tbhd = finE.getString("ezob_tbhd");
////                  芯板厚度公差字段
//                    String ezob_xbhdgc = finE.getString("ezob_xbhdgc");
//                    String names = getName(matTypeCodeXH, "芯板厚度公差", cq);
//                    String gc = null;
//                    if (!tbhd.isEmpty()) {
//                        if (tbhd.contains("/")) {
//                            //根据/来进行拆分顶和底
//                            String[] part = tbhd.split("/");
//                            //拆分后的结果使用集合存储
//                            List<String> st = new ArrayList<>();
//                            for (String s : part) {
//                                st.add(s);
//                            }
//                            gc = st.get(1);
//
//                        } else {
//                            gc = tbhd;
//                        }
//
//                        if (matTypeCodeXH.equals("M1.15") && gc != null) {
//                            int tbhdt = Integer.parseInt(gc);
//                            double result = tbhdt * 0.0343;
//                            BigDecimal bd = new BigDecimal(result);
//                            //                        保留三位小数
//                            double b = bd.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
//
//                            String jk = String.valueOf(b);
//                            String name = getName(matTypeCodeXH, "芯板厚度公差", cq);
//                            if (!Objects.equals(ezob_xbhdgc, "")) {
//                                if (name == null) {
//                                    name = "芯板厚度公差";
//                                }
//                                attrList.put(createAttribute(name, jk));
//                            }
//
//
//                        }
//
//                    }
//
//
//                } catch (Exception e) {
//                    //attrList.put(createAttribute("夹具类型", null));
//                }

                try {
                    String xbhdgc = finE.getString("ezob_xbhdgc");
                    String names = getName(matTypeCodeXH, "芯板厚度公差/mm", cq);
                    if (!Objects.equals(xbhdgc, "")) {
                        if (names == null||names =="") {
                            names = "芯板厚度公差/mm";

                        }
                        //6.17会议沟通后确认SME个性化判断公差分为正负公差——值相同星瀚中不需要拆分，只传公差即可，erp接收方自动拆分
                        if(cq.equals("SME")&&matTypeCodeXH.equals("M1.15")){
                            String js = xbhdgc.replace("±", "");
                           //attrList.put(createAttribute(names, js));
                            attrList.put(createAttribute("正公差",js));
                            attrList.put(createAttribute("负公差",js));
                        }else{
                            String js = xbhdgc.replace("±", "");
                            attrList.put(createAttribute(names, js));
                        }


                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("设备名称", ""));
                }

                try {
//                    ||matTypeCodeXH=="M1.01"
                    if ((matTypeCodeXH.equals("M1.15")) || (matTypeCodeXH.equals("M1.01"))) {
                        String ezobXbhdsfht = finE.getString("ezob_xbhdsfht");
//                        String name = getName(matTypeCodeXH,"芯板厚度是否含铜");
                        if (ezobXbhdsfht.equals("true")) {

                            attrList.put(createAttribute("含铜", "是"));
                        } else {
                            attrList.put(createAttribute("含铜", "否"));
                        }
                    }


                } catch (Exception e) {
                    //attrList.put(createAttribute("芯板厚度是否含铜", null));
                }
                try {
                    String ezobDsm = finE.getString("ezob_dsm.name") == null ? "" : finE.getString("ezob_dsm.name");
                    String name = getName(matTypeCodeXH, "单面/双面", cq);
                    if (!ezobDsm.isEmpty()) {
                        if (name == null||name =="") {
                            name = "单面/双面";
                        }
                        attrList.put(createAttribute(name, ezobDsm));
                    }

                } catch (Exception e) {
                    // attrList.put(createAttribute("单面/双面", null));
                }
                try {
                    String ezobYydj = finE.getString("ezob_yydj.name") == null ? "" : finE.getString("ezob_yydj.name");
                    String name = getName(matTypeCodeXH, "压延/电解", cq);
                    if (!ezobYydj.isEmpty()) {
                        if (name == null||name =="") {
                            name = "压延/电解";
                        }
                        attrList.put(createAttribute(name, ezobYydj));
                    }

                } catch (Exception e) {
                    //attrList.put(createAttribute("压延/电解", null));
                }
                try {
                    String finEString = finE.getString("ezob_tbpp.name");
                    String name = getName(matTypeCodeXH, "铜箔品牌", cq);
                    if (finEString!=null) {
                        if (name == ""||name== null) {
                            name = "铜箔品牌";
                        }
                        attrList.put(createAttribute(name, finEString));
                    }

                } catch (Exception e) {
                    // attrList.put(createAttribute("铜箔品牌", null));
                }
                try {
                    String ezobTbxh = finE.getString("ezob_tbxh.name");
                    String name = getName(matTypeCodeXH, "铜箔型号", cq);
                    if (ezobTbxh!=null) {
                        if (name == ""||name== null) {
                            name = "铜箔型号";
                        }
                        attrList.put(createAttribute(name, ezobTbxh));
                    }

                } catch (Exception e) {
                    //attrList.put(createAttribute("铜箔型号", ""));
                }
                try {
                    String ezobTblx = finE.getString("ezob_tblx.name");
                    String name = getName(matTypeCodeXH, "铜箔类型", cq);
                    if (ezobTblx!=null) {
                        if (name == ""||name== null) {
                            name = "铜箔类型";
                        }
                        attrList.put(createAttribute(name, ezobTblx));
                    }

                } catch (Exception e) {
                    //attrList.put(createAttribute("铜箔类型", null));
                }
                try {
                    //星瀚字段:
                    String ezobTbhd = finE.getString("ezob_tbhd");
                    //6.1 根据需求新增铜箔厚度单位，由之前固定值改为可选 oz  um
                    String ezobDbhddw = finE.getString("ezob_dbhddw");
                    if (!ezobTbhd.isEmpty()) {
                        if (ezobTbhd.contains("/")) {
                            String[] parts = ezobTbhd.split("/");
                            List<String> sb = new ArrayList<>();
                            for (String ps : parts) {
                                sb.add(ps);
                            }
                            //6.1 自定义开发 会议沟通后确认选择铜箔厚度单位后 如选择um则将顶底都加上um,若是oz则不拼接
                            if(ezobDbhddw.equals("um")){
                                if(sb.size()==2){
                                    sb.set(0,sb.get(0)+"um");
                                    sb.set(1,sb.get(1)+"um");
                                }else {
                                    sb.set(0,sb.get(0)+"um");
                                }
                            }

                            if (matTypeCodeXH.equals("M1.18")) {
                                attrList.put(createAttribute("铜箔厚度(工业名称)", ezobTbhd));
                            }
                            if (matTypeCodeXH.equals("M1.16")) {
                                attrList.put(createAttribute("铜箔厚度", ezobTbhd));
                                attrList.put(createAttribute("铜箔厚度单位", ezobDbhddw));
                            }
                            if (matTypeCodeXH.equals("M1.17")) {
                                attrList.put(createAttribute("铜厚", ezobTbhd));
                                attrList.put(createAttribute("铜厚单位", ezobDbhddw));
                            }
                            if (matTypeCodeXH.equals("M1.15")&&!cq.equals("SME")) {
                                attrList.put(createAttribute("铜厚代号(顶)", sb.get(0)));
                                attrList.put(createAttribute("铜厚代号(底)", sb.get(1)));
                                attrList.put(createAttribute("铜箔厚度单位", ezobDbhddw));
                            }
                            //6.25 mvIT确认M1.15在SME中拆分为顶底，单位为空则不传
                            if (matTypeCodeXH.equals("M1.15")&&cq.equals("SME")) {
                                attrList.put(createAttribute("铜厚(顶)", sb.get(0)));
                                attrList.put(createAttribute("铜厚(底)", sb.get(1)));
                                //attrList.put(createAttribute("铜箔厚度单位", ezob_tbhddw));
                            }
                            if (matTypeCodeXH.equals("M1.01")&&!cq.equals("SME")) {
                                attrList.put(createAttribute("铜厚代号(顶)", sb.get(0)));
                                attrList.put(createAttribute("铜厚代号(底)", sb.get(1)));
                                attrList.put(createAttribute("铜箔厚度单位", ezobDbhddw));
                            }
                            if (matTypeCodeXH.equals("M1.01")&&cq.equals("SME")) {
                                attrList.put(createAttribute("铜厚(顶)", sb.get(0)));
                                attrList.put(createAttribute("铜厚(底)", sb.get(1)));
                                //attrList.put(createAttribute("铜箔厚度单位", ezob_tbhddw));
                            }


                        } else if(!cq.equals("SME")) {
                            if (matTypeCodeXH.equals("M1.15")) {
                                attrList.put(createAttribute("铜厚代号(顶)", ezobTbhd));

                            }
                            if (matTypeCodeXH.equals("M1.01")) {
                                attrList.put(createAttribute("铜厚代号(顶)", ezobTbhd));
                            }
                            if (matTypeCodeXH.equals("M1.18")) {
                                attrList.put(createAttribute("铜箔厚度(工业名称)", ezobTbhd));
                            }
                            if (matTypeCodeXH.equals("M1.16")) {
                                attrList.put(createAttribute("铜箔厚度", ezobTbhd));
                                attrList.put(createAttribute("铜箔厚度单位", ezobDbhddw));
                            }
                            if (matTypeCodeXH.equals("M1.17")) {
                                attrList.put(createAttribute("铜厚", ezobTbhd));
                                attrList.put(createAttribute("铜厚单位", ezobDbhddw));
                            }

                        }else{
                            //所以我的疑问是：如果不含/的会不会出现SME的其他类 比如  M1.04
                            if(matTypeCodeXH.equals("M1.15")){
                                attrList.put(createAttribute("铜厚(顶)", ezobTbhd));
                            }
                            if(matTypeCodeXH.equals("M1.18")){
                                attrList.put(createAttribute("铜箔厚度(工业名称)", ezobTbhd));
                            }

                        }
                    }

                } catch (Exception e) {
                    // attrList.put(createAttribute("铜箔厚度", null));
                }
                try {
                    BigDecimal ezobPihd = (BigDecimal) finE.get("ezob_pihd");
                    String name = getName(matTypeCodeXH, "PI厚度/um", cq);
                    if (ezobPihd != null) {
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String dataformat = format.format(ezobPihd);
                        if (!dataformat.equals("0")) {
                            if (name == null ||name=="") {
                                name = "PI厚度/um";
                            }
                            attrList.put(createAttribute(name, dataformat));
                            String dw = getNameDw(matTypeCodeXH, "PI厚度/um");
                            if (dw == null||dw=="") {
                                dw = "PI厚度单位";
                            }
//                        gme单位
                            attrList.put(createAttribute(dw, "um"));
                            attrList.put(createAttribute("铜箔厚度单位", "um"));
                        }
                    }

                } catch (Exception e) {
                    //attrList.put(createAttribute("PI厚度", null));
                }
                try {
                    String ezobCjxh = finE.getString("ezob_cjxh.name");
                    String name = getName(matTypeCodeXH, "纯胶型号", cq);
                    if (ezobCjxh!=null) {
                        if (name == null||name =="") {
                            name = "纯胶型号";
                        }
                        attrList.put(createAttribute(name, ezobCjxh));
                    }

                } catch (Exception e) {
                    //attrList.put(createAttribute("纯胶型号", ""));
                }
                try {
                    BigDecimal ezobJhd = (BigDecimal) finE.get("ezob_jhd");
                    String name = getName(matTypeCodeXH, "胶厚度/um", cq);
                    if (ezobJhd != null) {
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String dataformat = format.format(ezobJhd);
                        if (!dataformat.equals("0")) {
                            if (name == null||name=="") {
                                name = "胶厚度/um";
                            }
                            attrList.put(createAttribute(name, dataformat));
                            String dw = getNameDw(matTypeCodeXH, "胶厚度/um");
                            if (dw == null||dw =="") {
                                dw = "胶厚度单位";
                            }
                            attrList.put(createAttribute(dw, "um"));
                        }else {
                            if(name==null||name.equals("")){
                                name="胶厚度/um";
                            }
                            if(cq.equals("FPC")||cq.equals("GME")){

                                attrList.put(createAttribute(name, "N/A"));
                                String dw = getNameDw(matTypeCodeXH, "胶厚度/um");
                                if (dw == null||dw =="") {
                                    dw = "胶厚度单位";
                                }
                                attrList.put(createAttribute(dw, "um"));
                            }
                        }
                    }else {
                        if(cq.equals("FPC")||cq.equals("GME")){
                            if(name==null||name.equals("")){
                                name="胶厚度/um";
                            }
                            attrList.put(createAttribute(name, "N/A"));
                            String dw = getNameDw(matTypeCodeXH, "胶厚度/um");
                            if (dw == null||dw =="") {
                                dw = "胶厚度单位";
                            }
                            attrList.put(createAttribute(dw, "um"));
                        }

                    }

                } catch (Exception e) {
//                    attrList.put(createAttribute("胶厚度", null));
                }
                try {
                    String ezobBhmxh = finE.getString("ezob_bhmxh.name");
                    String name = getName(matTypeCodeXH, "保护膜型号", cq);
                    if (!Objects.equals(ezobBhmxh, "") && ezobBhmxh != null) {
                        if (name == null||name =="") {
                            name = "保护膜型号";
                        }
                        attrList.put(createAttribute(name, ezobBhmxh));

                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("保护膜型号", ""));
                }
                try {
                    BigDecimal ezobBhmhd = (BigDecimal) finE.get("ezob_bhmhd");
                    String name = getName(matTypeCodeXH, "保护膜厚度/um", cq);
                    if (ezobBhmhd != null) {
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String dataformat = format.format(ezobBhmhd);
                        if (!dataformat.equals("0")) {
                            if (name == null||name =="") {
                                name = "保护膜厚度/um";
                            }
                            attrList.put(createAttribute(name, dataformat));
                            String dw = getNameDw(matTypeCodeXH, "保护膜厚度/um");
                            if (dw == null||dw =="") {
                                dw = "保护膜厚度单位";
                            }
                            attrList.put(createAttribute(dw, "um"));
                        }
                    }
                } catch (Exception e) {
                    //ttrList.put(createAttribute("保护膜厚度", ""));
                }
                try {
                    String ezobDg = finE.getString("ezob_dg.name");
                    String name = getName(matTypeCodeXH, "叠构", cq);
                    if (!Objects.equals(ezobDg, "") && ezobDg != null) {
                        if (name == null||name=="") {
                            name = "叠构";
                        }
                        attrList.put(createAttribute(name, ezobDg));
                    }
                } catch (Exception e) {
                    // attrList.put(createAttribute("叠构", ""));
                }
                try {
                    String ezobWlmc = finE.getString("ezob_wlmc");
                    String name = getName(matTypeCodeXH, "面次", cq);
                    if (!Objects.equals(ezobWlmc, "")) {
                        if (name == null||name=="") {
                            name = "面次";
                        }
                        attrList.put(createAttribute(name, ezobWlmc));
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("物料名称", null));
                }
                try {
                    String ezobGysxh = finE.getString("ezob_gysxh");
                    String name = getName(matTypeCodeXH, "供应商型号", cq);
                    if (!Objects.equals(ezobGysxh, "")) {
                        if (name == null||name=="") {
                            name = "供应商型号";
                        }
                        attrList.put(createAttribute(name, ezobGysxh));
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("供应商型号", null));
                }
                try {
                    String ezobYcxhgg = finE.getString("ezob_ycxhgg");
                    String name = getName(matTypeCodeXH, "原材型号规格", cq);
                    if (!Objects.equals(ezobYcxhgg, "")) {
                        if (name == null||name=="") {
                            name = "原材型号规格";
                        }
                        attrList.put(createAttribute(name, ezobYcxhgg));
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("原材型号规格", null));
                }
                try {
                    String ezobCz = finE.getString("ezob_cz");
                    String name = getName(matTypeCodeXH, "材质", cq);
                    if (!Objects.equals(ezobCz, "")) {
                        if (name == null||name=="") {
                            name = "材质";
                        }
                        attrList.put(createAttribute(name, ezobCz));
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("材质", null));
                }
                try {
                    String ezobSk = finE.getString("ezob_sk");
                    String name = getName(matTypeCodeXH, "色块", cq);
                    if (!Objects.equals(ezobSk, "")) {
                        if (name == null||name=="") {
                            name = "色块";
                        }
                        attrList.put(createAttribute(name, ezobSk));
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("材质", null));
                }
                try {
                    String ezobLight = finE.getString("ezob_light");
                    String name = getName(matTypeCodeXH, "亮度", cq);
                    if (!Objects.equals(ezobLight, "")) {
                        if (name == null||name=="") {
                            name = "亮度";
                        }
                        attrList.put(createAttribute(name, ezobLight));
                    }
                } catch (Exception e) {

                }
                try {
                    String ezobDzz = finE.getString("ezob_dzz");
                    String name = getName(matTypeCodeXH, "电阻值", cq);
                    if (!Objects.equals(ezobDzz, "")) {
                        if (name == null||name=="") {
                            name = "电阻值";
                        }
                        attrList.put(createAttribute(name, ezobDzz));
                    }
                } catch (Exception e) {

                }
                try {
                    String ezobPs = finE.getString("ezob_ps");
                    String name = getName(matTypeCodeXH, "盘数", cq);
                    if (!Objects.equals(ezobPs, "")) {
                        if (name == null||name=="") {
                            name = "盘数";
                        }
                        attrList.put(createAttribute(name, ezobPs));
                    }
                } catch (Exception e) {

                }

                try {
                    String ezobWlyq = finE.getString("ezob_wlyq");
                    String name = getName(matTypeCodeXH, "物料要求", cq);
                    if (!Objects.equals(ezobWlyq, "")) {
                        if (name == null||name=="") {
                            name = "物料要求";
                        }
                        attrList.put(createAttribute(name, ezobWlyq));
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("物料要求", null));
                }
                try {
                    String ezobPpmake = finE.getString("ezob_ljd.name");
                    String name = getName(matTypeCodeXH, "流胶度", cq);
                    if (!Objects.equals(ezobPpmake, "") && ezobPpmake != null) {
                        if (name ==""||name== null) {
                            name = "流胶度";
                        }
                        attrList.put(createAttribute(name, ezobPpmake));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("设备名称", ""));
                }

                try {
                    String ezobHjl = finE.getString("ezob_hjl");
                    String name = getName(matTypeCodeXH, "含胶量", cq);
                    if (!Objects.equals(ezobHjl, "")) {
                        if (name == null||name=="") {
                            name = "含胶量";
                        }
                        //6.17 SME-M1.09工厂个性化分类字段拼接   7.25 郑工废弃当前，启用 7.24新规则
//                        if(cq.equals("SME")&&matTypeCodeXH.equals("M1.09")){
//                            String ezob_hjlgc = finE.getString("ezob_hjlgc");
//                            ezob_hjl = "RC"+ezob_hjl+"±"+ezob_hjlgc;
//                            attrList.put(createAttribute(name,ezob_hjl));
//                        }else
                        if(cq.equals("SME")){
                            String ezobHjlgc = finE.getString("ezob_hjlgc");
                            //7.24  SME 接囗回传规则:【含胶量:XX 】[含胶量公差:未填写】:RCXX%
                            //[含胶量:XX 】[含胶量公差:A】:RCXX±A%
                            if(ezobHjlgc!=null&&ezobHjlgc!=""){
                                ezobHjl = "RC"+ezobHjl+"±"+ezobHjlgc+"%";
                                attrList.put(createAttribute(name,ezobHjl));
                            }else{
                                ezobHjl = "RC"+ezobHjl+"%";
                                attrList.put(createAttribute(name,ezobHjl));
                            }
                        }else {
                            attrList.put(createAttribute(name, ezobHjl));
                        }

                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("含胶量", null));
                }
                try {
                    String ezobHjlgc = finE.getString("ezob_hjlgc");
                    String name = getName(matTypeCodeXH, "含胶量公差", cq);
                    if (!Objects.equals(ezobHjlgc, "")) {
                        if (name ==""||name== null) {
                            name = "含胶量公差";
                        }
                        String resultString = ezobHjlgc.replace("±", "");
                        //6.3 提出工厂自定义需求 GME后缀必须带%
                        if(cq.equals("GME")){
                            resultString = resultString+"%";
                        }

                        attrList.put(createAttribute(name, resultString));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("含胶量公差", null));
                }
                try {
                    String ezobType = finE.getString("ezob_type.name");
                    String name = getName(matTypeCodeXH, "Type", cq);
                    if (ezobType!=null) {
                        if (name == ""||name== null) {
                            name = "Type";
                        }
                        attrList.put(createAttribute(name, ezobType));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("Type", ""));
                }
                try {
                    String ezobBblb = finE.getString("ezob_bblb.name");
                    String name = getName(matTypeCodeXH, "玻布类别", cq);
                    if (ezobBblb!=null) {
                        if (name == ""||name== null) {
                            name = "玻布类别";
                        }
                        attrList.put(createAttribute(name, ezobBblb));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("玻布类别", ""));
                }
                try {
                    String ezobBbcd = finE.getString("ezob_bbcd.name");
                    String name = getName(matTypeCodeXH, "玻布产地", cq);
                    if ( ezobBbcd!=null) {
                        if (name == ""||name== null) {
                            name = "玻布产地";
                        }
                        attrList.put(createAttribute(name, ezobBbcd));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("玻布产地", ""));
                }
                try {
                    BigDecimal ezobZjdz = (BigDecimal) finE.get("ezob_zjdz");
                    String name = getName(matTypeCodeXH, "主剂单重/kg", cq);
                    if (ezobZjdz != null) {
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String dataformat = format.format(ezobZjdz);
                        if (!dataformat.equals("0")) {
                            if (name == null||name=="") {
                                name = "主剂单重/kg";
                            }
                            attrList.put(createAttribute(name, dataformat));
                            //attrList.put(createAttribute(name, dataformat));
                            String dw = getNameDw(matTypeCodeXH, "主剂单重/kg");
                            if (dw == null||dw=="") {
                                dw = "主剂单重单位";
                            }
                            attrList.put(createAttribute(dw, "KG"));

                        }
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("主剂单重", null));
                }
                try {
                    String ezobYhjxh = finE.getString("ezob_yhjxh.name");
                    String name = getName(matTypeCodeXH, "硬化剂型号", cq);
                    BigDecimal ezobYhjdz = (BigDecimal) finE.get("ezob_yhjdz");
                    if ((!Objects.equals(ezobYhjxh, "")) && ezobYhjxh != null) {
                        if (name == null||name=="") {
                            name = "硬化剂型号";
                        }
                        attrList.put(createAttribute(name, ezobYhjxh));
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("硬化剂型号", ""));
                }
                BigDecimal ezobYhjdz = null;
                try {
                    ezobYhjdz = (BigDecimal) finE.get("ezob_yhjdz");
                    String name = getName(matTypeCodeXH, "硬化剂单重/kg", cq);

                    if (ezobYhjdz != null) {
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String dataformat = format.format(ezobYhjdz);
                        if (!dataformat.equals("0")) {
                            if (name == null||name=="") {
                                name = "硬化剂单重/kg";
                            }
                            attrList.put(createAttribute(name, dataformat));
                            String dw = getNameDw(matTypeCodeXH, "硬化剂单重/kg");
                            if (dw == null||dw=="") {
                                dw = "硬化剂单重单位";
                            }
                            attrList.put(createAttribute(dw, "KG"));
                        }
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("硬化剂单重", ""));
                }
                try {
                    String ezobCd = finE.getString("ezob_cd.name");
                    String name = getName(matTypeCodeXH, "产地", cq);
                    if (ezobCd!=null) {
                        if (name == ""||name== null) {
                            name = "产地";
                        }
                        attrList.put(createAttribute(name, ezobCd));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("产地", null));
                }
                try {
                    String ezobSwts = finE.getString("ezob_swts");
                    String name = getName(matTypeCodeXH, "丝网T数", cq);
                    if (!ezobSwts.isEmpty()) {
                        if (name == null||name=="") {
                            name = "丝网T数";
                        }
                        attrList.put(createAttribute(name, ezobSwts));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("丝网T数", null));
                }
                try {
                    String ezobYd = finE.getString("ezob_yd");
                    String name = getName(matTypeCodeXH, "硬度", cq);
                    if (!Objects.equals(ezobYd, "")) {
                        if (name == null||name=="") {
                            name = "硬度";
                        }
                        attrList.put(createAttribute(name, ezobYd));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("硬度", null));
                }
                try {
                    String mjfl = finE.getString("ezob_mjfl");
                    String name = getName(matTypeCodeXH, "模具分类", cq);
                    if (!Objects.equals(mjfl, "")) {
                        if (name == null||name=="") {
                            name = "模具分类";
                        }
                        attrList.put(createAttribute(name, mjfl));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("模具分类", null));
                }
                try {
                    String ezobJjlx = finE.getString("ezob_jjlx");
                    String name = getName(matTypeCodeXH, "夹具类型", cq);
                    if (!Objects.equals(ezobJjlx, "")) {
                        if (name == null||name=="") {
                            name = "夹具类型";
                        }
                        attrList.put(createAttribute(name, ezobJjlx));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("夹具类型", null));
                }
                try {
                    String ezobGwlx = finE.getString("ezob_gwlx");
                    String name = getName(matTypeCodeXH, "钢网类型", cq);
                    if (!Objects.equals(ezobGwlx, "")) {
                        if (name == null||name=="") {
                            name = "钢网类型";
                        }
                        attrList.put(createAttribute(name, ezobGwlx));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("钢网类型", null));
                }
                try {
                    String ezobZjlx = finE.getString("ezob_zjlx");
                    String name = getName(matTypeCodeXH, "治具类型", cq);
                    if (!Objects.equals(ezobZjlx, "")) {
                        if (name == null||name=="") {
                            name = "治具类型";
                        }
                        attrList.put(createAttribute(name, ezobZjlx));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("治具类型", null));
                }
                try {
                    String ezobZsxsx = finE.getString("ezob_zsxsx");
                    String name = getName(matTypeCodeXH, "涨缩系数X", cq);
                    if (!Objects.equals(ezobZsxsx, "")) {
                        if (name == null||name=="") {
                            name = "涨缩系数X";
                        }
                        attrList.put(createAttribute(name, ezobZsxsx));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("涨缩系数X", null));
                }
                try {
                    String ezobZsxsy = finE.getString("ezob_zsxsy");
                    String name = getName(matTypeCodeXH, "涨缩系数Y", cq);
                    if (!Objects.equals(ezobZsxsy, "")) {
                        if (name == null||name=="") {
                            name = "涨缩系数Y";
                        }
                        attrList.put(createAttribute(name, ezobZsxsy));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("涨缩系数Y", null));
                }
                try {
                    String ezobPbqs = finE.getString("ezob_pbqs");
                    String name = getName(matTypeCodeXH, "配比/腔数", cq);
                    if (!Objects.equals(ezobPbqs, "")) {
                        if (name == null||name=="") {
                            name = "配比/腔数";
                        }
                        attrList.put(createAttribute(name, ezobPbqs));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("配比/腔数", null));
                }
                try {
                    String ezobPljl = finE.getString("ezob_pljl.name") == null ? "" : finE.getString("ezob_pljl.name");
                    String name = getName(matTypeCodeXH, "片料/卷料", cq);
                    if (!ezobPljl.isEmpty()) {
                        if (name == ""||name== null) {
                            name = "片料/卷料";
                        }
                        attrList.put(createAttribute(name, ezobPljl));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("片料/卷料", null));
                }
                try {
                    String ezobTzrq = finE.getString("ezob_tzrq") == null ? "" : finE.getString("ezob_tzrq");
                    String name = getName(matTypeCodeXH, "图纸日期", cq);
                    if (!Objects.equals(ezobTzrq, "") && name != null) {
                        DateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                        DateFormat outputFormat = new SimpleDateFormat("yyyy/MM/dd");
                        try {
                            Date date = inputFormat.parse(ezobTzrq);
                            ezobTzrq = outputFormat.format(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
//                    if (name==null){
//                        name="图纸日期";
//                    }
                        attrList.put(createAttribute(name, ezobTzrq));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("图纸日期", null));
                }
                try {
                    String ezobYj = finE.getString("ezob_yj.name");
                    String name = getName(matTypeCodeXH, "圆角", cq);
                    if (!Objects.equals(ezobYj, "") && ezobYj != null) {
                        if (name == ""||name== null) {
                            name = "圆角";
                        }
                        attrList.put(createAttribute(name, ezobYj));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("圆角", ""));
                }
                try {
                    String ezobBmclfs = finE.getString("ezob_bmclfs.name");
                    String name = getName(matTypeCodeXH, "表面处理方式", cq);
                    if (!Objects.equals(ezobBmclfs, "") && ezobBmclfs != null) {
                        if (name == null||name=="") {
                            name = "表面处理方式";
                        }

                        attrList.put(createAttribute(name, ezobBmclfs));

                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("表面处理方式", ""));
                }
                try {
                    String ezobBz = finE.getString("ezob_bz");
                    String name = getName(matTypeCodeXH, "工具后缀", cq);
                    if (!Objects.equals(ezobBz, "")) {
                        if (name == null||name=="") {
                            name = "工具后缀";
                        }

                        attrList.put(createAttribute(name, ezobBz));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("包装", null));
                }
                try {
                    String ezobSjlx = finE.getString("ezob_sjlx.name");
                    String name = getName(matTypeCodeXH, "酸碱类型", cq);
                    if (name == null||name=="") {
                        name = "酸碱类型";
                    }

                    if (!Objects.equals(ezobSjlx, "") && ezobSjlx != null) {
                        attrList.put(createAttribute(name, ezobSjlx));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("酸碱类型", ""));
                }
                try {
                    String ezobZzzl = finE.getString("ezob_zzzl.name");
                    String name = getName(matTypeCodeXH, "钻嘴种类", cq);
                    if (!Objects.equals(ezobZzzl, "") && ezobZzzl != null) {
                        if (name == ""||name== null) {
                            name = "钻嘴种类";
                        }
                        attrList.put(createAttribute(name, ezobZzzl));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("钻嘴种类", ""));
                }
                try {
                    String ezobXdzl = finE.getString("ezob_xdzl.name");
                    String name = getName(matTypeCodeXH, "铣刀种类", cq);
                    if (!Objects.equals(ezobXdzl, "") && ezobXdzl != null) {
                        if (name == ""||name== null) {
                            name = "铣刀种类";
                        }
                        attrList.put(createAttribute(name, ezobXdzl));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("铣刀种类", ""));
                }
                try {
                    BigDecimal ezobDj = (BigDecimal) finE.get("ezob_dj");
                    String name = getName(matTypeCodeXH, "刀径/mm", cq);
                    if (ezobDj != null) {
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String dataformat = format.format(ezobDj);
                        if (!dataformat.equals("0")) {
                            if (name == null||name=="") {
                                name = "刀径/mm";
                            }
                            attrList.put(createAttribute(name, dataformat));
                            String dw = getNameDw(matTypeCodeXH, "刀径/mm");
                            if (dw == null||dw=="") {
                                dw = "刀径单位";
                            }
                            attrList.put(createAttribute(dw, "mm"));
                        }
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("刀径", null));
                }
                try {
                    BigDecimal ezobCdrc = (BigDecimal) finE.get("ezob_cdrc");
                    String name = getName(matTypeCodeXH, "长度(刃长)/mm", cq);
                    if (ezobCdrc != null) {
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String dataformat = format.format(ezobCdrc);
                        if (!dataformat.equals("0")) {
                            if (name == null||name=="") {
                                name = "长度(刃长)/mm";
                            }
                            attrList.put(createAttribute(name, dataformat));
                            String dw = getNameDw(matTypeCodeXH, "长度(刃长)/mm");
                            if (dw == null||dw=="") {
                                dw = "长度单位";
                            }
                            attrList.put(createAttribute(dw, "mm"));
                        }
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("长度(刃长)", null));
                }
                try {
                    BigDecimal ezobDbzj = (BigDecimal) finE.get("ezob_dbzj");
                    String name = getName(matTypeCodeXH, "刀柄直径/mm", cq);
                    if (ezobDbzj != null) {
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String dataformat = format.format(ezobDbzj);
                        if (!dataformat.equals("0")) {
                            if (name == ""||name== null) {
                                name = "刀柄直径/mm";
                            }
                            attrList.put(createAttribute(name, dataformat));
                            String dw = getNameDw(matTypeCodeXH, "刀柄直径/mm");
                            if (dw == null||name=="") {
                                dw = "刀柄单位";
                            }
                            attrList.put(createAttribute(dw, "mm"));
                        }
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("刀柄直径", null));
                }
                try {

                    String ezobZzjlx = finE.getString("ezob_zzjlx.name");
                    String name = getName(matTypeCodeXH, "钻尖角类型", cq);
                    if (ezobZzjlx!=null){
                        if (name ==""||name== null) {
                            name = "钻尖角类型";
                        }
                        attrList.put(createAttribute(name, ezobZzjlx));
                    }
                } catch (Exception e) {
//                    attrList.put(createAttribute("钻尖角类型", null));
                }
                try {
                    String ezobDrlx = finE.getString("ezob_drlx.name");
                    String name = getName(matTypeCodeXH, "刀刃类型", cq);
                    if (!Objects.equals(ezobDrlx, "") && ezobDrlx != null) {
                        if (name == ""||name== null) {
                            name = "刀刃类型";
                        }
                        attrList.put(createAttribute(name, ezobDrlx));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("刀刃类型", ""));
                }
                try {
                    String ezobHtys = finE.getString("ezob_htys.name");
                    String name = getName(matTypeCodeXH, "套环颜色", cq);
                    if (!Objects.equals(ezobHtys, "") && ezobHtys != null) {
                        if (name == ""||name== null) {
                            name = "套环颜色";
                        }
                        attrList.put(createAttribute(name, ezobHtys));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("套环颜色", ""));
                }
                try {
                    String ezobJz = finE.getString("ezob_jz");
                    String name = getName(matTypeCodeXH, "基重", cq);
                    if (!Objects.equals(ezobJz, "")) {
                        if (name == null||name=="") {
                            name = "基重";
                        }
                        attrList.put(createAttribute(name, ezobJz));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("基重", null));
                }
                try {
                    String ezobBbcd = finE.getString("ezob_ll.name");
                    String name = getName(matTypeCodeXH, "频率", cq);
                    if (!Objects.equals(ezobBbcd, "") && ezobBbcd != null) {
                        if (name == null||name=="") {
                            name = "频率";
                        }
                        attrList.put(createAttribute(name, ezobBbcd));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("玻布产地", ""));
                }

                try {
                    BigDecimal ezobZj = (BigDecimal) finE.get("ezob_zj");
                    String name = getName(matTypeCodeXH, "直径/mm", cq);
                    if (ezobZj != null) {
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String dataformat = format.format(ezobZj);
                        if (!dataformat.equals("0")) {
                            if (name == ""||name== null) {
                                name = "直径/mm";
                            }
                            attrList.put(createAttribute(name, dataformat));
                            String dw = getNameDw(matTypeCodeXH, "直径/mm");
                            if (dw == ""||name== null) {
                                dw = "直径单位";
                            }
                            attrList.put(createAttribute(dw, "mm"));
                        }
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("直径", null));
                }
                try {
                    String ezobTzbb = finE.getString("ezob_tzbb");
                    String name = getName(matTypeCodeXH, "图纸版本", cq);
                    if (!Objects.equals(ezobTzbb, "")) {
                        if (name == null||name=="") {
                            name = "图纸版本";
                        }
                        attrList.put(createAttribute(name, ezobTzbb));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("图纸版本", null));
                }
                try {
                    String ezobDk = finE.getString("ezob_dk");
                    String name = getName(matTypeCodeXH, "DK", cq);
                    if (!Objects.equals(ezobDk, "") && ezobDk != null) {
                        if (name == ""||name== null) {
                            name = "DK";
                        }
                        attrList.put(createAttribute(name, ezobDk));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("图纸版本", null));
                }
                try {
                    String ezobDf = finE.getString("ezob_df");
                    String name = getName(matTypeCodeXH, "DF", cq);
                    if (!Objects.equals(ezobDf, "") && ezobDf != null) {
                        if (name == null||name=="") {
                            name = "DF";
                        }
                        attrList.put(createAttribute(name, ezobDf));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("图纸版本", null));
                }
                try {
                    String ezobTg = finE.getString("ezob_tg");
                    String name = getName(matTypeCodeXH, "TG", cq);
                    if (!Objects.equals(ezobTg, "") && ezobTg != null) {
                        if (name == null||name=="") {
                            name = "TG";
                        }
                        attrList.put(createAttribute(name, ezobTg));
                    }
                } catch (Exception e) {
                    //attrList.put(createAttribute("图纸版本", null));
                }




                // 6.24新增字段 公差等级
                try {
                    String ezobGcdj = finE.getString("ezob_gcdj");
                    String name = getName(matTypeCodeXH, "公差等级", cq);
                    if (!Objects.equals(ezobGcdj, "") && ezobGcdj != null) {
                        if (name == null||name=="") {
                            name = "公差等级";
                        }
                        attrList.put(createAttribute(name, ezobGcdj));
                    }
                } catch (Exception e) {

                }

                //基本单位
                purchaseUnitCode=finE.getString("baseunit.number");
                stockUnitCode=finE.getString("baseunit.number");
                //6.19 会议沟通后确认 将星瀚中单位吨（T）在传输Mverp物料接口中所有工厂转换为TON
                if(purchaseUnitCode.equals("T")){
                    purchaseUnitCode = "TON";
                }
                if(stockUnitCode.equals("T")){
                    stockUnitCode = "TON";
                }
                //data.put("location", "A(板材仓(常温))");

                //物料描述   5.30  正则做去除空格处理，保证每个字符之有一个空格
                String invPartDescs = invPartDesc.replaceAll("\\s{2,}"," ");
                System.err.println(invPartDescs);
                //去掉头尾空格
                data.put("invPartDesc", invPartDescs.trim());
                //物料分类
                data.put("halogenFlag", halogenFlag);
                data.put("purchaseUnitCode", purchaseUnitCode);
                data.put("stockUnitCode", stockUnitCode);
                data.put("unitWeight", unitWeight);
                data.put("shelfLifeDays", lifeDays);//shelfLifeDays    lifeDays
                data.put("domesticFlag", domesticFlag);
                data.put("attrList", attrList);
                data.put("单位重量kg", unitWeight);

                // 可质检 -6.17会议沟通后GME不传输是否可质检
                if(cq.equals("GME")){

                }else{
                    String enableinspect = finE.getString("enableinspect");
                    if (enableinspect.equals("true")){
                        data.put("inspectionFlag","Y");
                    }else {
                        data.put("inspectionFlag","N");

                    }
                }





                //仅元器件需要传输物料分类
                if (matTypeCodeXH.equals("EC.02")) {
                    String wlfl = finE.getString("ezob_wlfl.name");
                    String name = getName(matTypeCodeXH, "物料分类", cq);
                    System.out.println(wlfl);
                    if (name == null||name=="") {
                        name = "物料分类";
                    }
                    data.put(name, wlfl);
                }

                String sb = finE.get("name").toString();
                String name = getName(matTypeCodeXH, "物料名称", cq);
                if (sb != null ) {
                    if (name ==""||name== null) {
                        name = "物料名称";
                    }
                    attrList.put(createAttribute(name, sb));

                }


                String appSecret = "GME";
//                正式环境appid     7PT3CH9UGMFLSXW0NQQRV7LT4YDFJJE1
//                测试环境          L8ruVx0ZQPL8YL5zYO2q9BXfdHbw8u0E
                String appId ="L8ruVx0ZQPL8YL5zYO2q9BXfdHbw8u0E";
                String timestamp = String.valueOf(Instant.now().getEpochSecond());
                String s = "";

                headers.set("Content-Type", "application/json");
                headers.set("MV-Div", cq);
                headers.set("MV-AppId", appId);
                JsonNode rootNodeTS = null;
                JSONArray dataList = new JSONArray();
                dataList.put(data);
                JSONObject json = new JSONObject();
                json.put("data", dataList);
                String requestBody = json.toString();
                headers.set("MV-Div", cq);
                String o = null;
                //10.22.10.249:8023
               String ul = null;
//               ul = "http://10.22.10.249:8023/api/v2/app/kd/synMat";
                ul = "https://ctrsim.webapi.meadvilletech.com/api/v2/app/kd/synMat";
//               if(cq.equals("SKE")){
//                   ul="https://mverp.webapi.meadvilletech.com/api/v2/app/kd/synMat";
//               }else {
//                   ul="https://mverp.webapi.meadvilletech.com/api/v2/app/kd/synMat";
//               }
//                 http://10.22.10.249:8023/api/v2/app/kd/synMat
//                正式环境url   https://mverp.webapi.meadvilletech.com/api/v2/app/kd/synMat
//                测试环境url   https://ctrsim.webapi.meadvilletech.com/api/v2/app/kd/synMat
                rootNodeTS = getUrl(requestBody,ul, headers);
                return rootNodeTS;
//                if (rootNodeTS == null) {
//                    //无响应可能进入此处
//
//                } else {
//                    Boolean success = rootNodeTS.path("saveSuccess").asBoolean();
//                    if (success == false) {
//                        Message = Message + rootNodeTS.path("errorMsg").toString();
//                    }
//                }
//                if (Message == null) {
//
//                    msg = msg + invPartNumber + "物料推送成功！推送时间" + getCurrTime();
//                    finE.set("ezob_pushstatus", msg + "推送人：" + username);
//                    SaveServiceHelper.update(finE);
//                    finE.set("ezob_tbmv", 2);
////                    同步成功，追加工厂
//                    msgString.append(cq+",");
//                    this.getView().showMessage("同步成功！！！");

//                } else {
//                    //String Message = rootNodeTS.path("errorMsg").toString();
//                    log.error("报错原因" + Message);
//                    //this.getView().showErrMessage("同步失败！", Message);
//                    String errorMsgmsg = rootNodeTS.path("errorMsg").toString();
//                    int i = errorMsgmsg.indexOf("errorMsg");
//                    if (i != -1) {
//                        // 从 'c' 后面的位置开始截取（即 index + 1）
//                        String subStr = errorMsgmsg.substring(i+8);
//                        this.getView().showMessage("同步失败！" + " 失败原因："+subStr);
////                    this.getView().showErrorNotification("同步失败:" + Message);
//                        finE.set("ezob_pushstatus", "推送时间" + getCurrTime() + "推送失败" + msg + "推送人：" + username);
//                        log.error("物料推送失败" + msg);
//
//                    }
//                }
            }else {
                return  null;
            }
        }
    }


}
