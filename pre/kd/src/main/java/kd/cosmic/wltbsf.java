package kd.cosmic;

import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.OrmLocaleValue;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
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
 * 描述: 物料同步思方
 * 开发者: 李四辉
 * 创建日期: 2024/04/01
 * 关键客户：郑楷绚
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
public class wltbsf extends AbstractListPlugin implements Plugin {

    private final static String KEY_BARITEM = "ezob_tbsf";

    private static Log log = LogFactory.getLog(wltbsf.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(KEY_BARITEM);
    }


    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        String msg = "";
        // 定义请求接口URL
//        String url = "http://10.101.23.218:8489/api/api/MaterialProject";//测试
        String url = "http://10.101.238.243:8072/api/api/MaterialProject";//正式
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {
            try {
                ListSelectedRowCollection selectedRows = ((IListView) this.getView()).getSelectedRows();
                for (ListSelectedRow list : selectedRows) {
                    DynamicObject finE = BusinessDataServiceHelper.loadSingle(list.getPrimaryKeyValue(), "bd_material");
                    //新增修改标识（所有都用新增，先不做修改）
                    int addFlag = 1;
                    if (finE.getInt("ezob_tbsfbs") != 1) {
                        addFlag = finE.getInt("ezob_tbsfbs");
                    }
                    //类别代号
                    String groupCode = finE.getString("ezob_wlfl.number");
                    //自定义判断类型
                    String pdlx = "M1.01,M1.02,M1.03,M1.05,M1.06,M1.08,M1.10,M1.15,M1.18,N1,N2,EC,MT,R1";
                    //状态
                    String status = finE.getString("status");
                    //编码
                    String materialNumber = (String) finE.get("number");
                    //保税/非保税
                    String bsfbs = "";
                    //客户简称
                    String khjc = "";
                    if(finE.get("ezob_khdm.simplename") != null){
                        OrmLocaleValue localeString = (OrmLocaleValue) finE.get("ezob_khdm.simplename");
                        if(localeString.getLocaleValue_zh_CN() != ""){
                            khjc = "/"+localeString.getLocaleValue_zh_CN();
                        }
                        if(localeString.get("GLang") != ""){
                            khjc = "/"+localeString.get("GLang");
                        }
                    }
                    int useType = finE.getInt("ezob_bsykgldbs");
                    if(useType == 1){
                        bsfbs = "/保税";
                    }else if(useType == 2){
                        bsfbs = "/客供";
                    }else if(useType == 4){
                        bsfbs = "/客供(样品)";
                    }
                    //名称
                    String materialName = finE.getString("name");
                    //备选材料编码
                    DynamicObject dynamicObject = (DynamicObject)finE.get("ezob_bxcl");
                    String alternatePartPtr = "";
                    if (dynamicObject != null) {
                        alternatePartPtr = dynamicObject.get("number").toString();
                    }
                    String materialDescrip = "";//传思方材料规格用
                    String manufacturer = "";//传思方特别要求用
                    String rohs = "";//传思方拼接特别要求用
                    String hf = "";//传思方拼接特别要求用
                    //供应商
                    String gys = "";
                    if((String) finE.get("ezob_gys") != ""){
                        gys = "/"+(String) finE.get("ezob_gys");
                    }

                    //型号
                    String modelnum = finE.getString("modelnum");
                    //规格
                    String gg = finE.getString("ezob_gg");
                    if (finE.getBoolean("ezob_rohs")) {
                        rohs = "ROHS/";
                    }
                    if (finE.getBoolean("ezob_hf")) {
                        hf = "HF/";
                    }
                    if(pdlx.contains(groupCode) || groupCode.substring(0,2).contains("N1")|| groupCode.substring(0,2).contains("N2")|| groupCode.substring(0,2).contains("EC")|| groupCode.substring(0,2).contains("MT")|| groupCode.substring(0,2).contains("R1")){
                        materialDescrip =modelnum;
                        if(gg.equals("")){
                            manufacturer = rohs + hf;
                        }else{
                            manufacturer = rohs + hf + gg;
                        }
                    }else{
                        if(gg.equals("")){
                            materialDescrip =modelnum;
                            if(groupCode.contains("C1")){
                                materialDescrip =modelnum + gys;//C1添加供应商
                            }
                        }else{
                            materialDescrip =modelnum + "/" + gg;
                            if(groupCode.contains("C1")){
                                materialDescrip =modelnum + gys + "/" + gg;//C1添加供应商
                            }
                        }
                        manufacturer = rohs + hf;
                    }
//                    if(Manufacturer.length()>1){
//                        Manufacturer = Manufacturer.substring(0,Manufacturer.length()-1);
//                    }
                    if (modelnum.isEmpty()){
                        log.error("推送失败" + materialNumber + "物料型号不能为空!");
                        msg += ("推送失败" + materialNumber + "物料型号不能为空!");
                        continue;
                    }


                    if (!status.equals("C")) {
                        log.error("报错原因:" + materialNumber + "物料未审核");
                        msg += ("推送失败" + materialNumber + "物料未审核");
                        continue;
                    }


                    String sqlcg = "/*dialect*/ select tbm.fnumber as 物料编码,tbmu.FNUMBER\n" +
                            "from akmmv_prd_eip_test.T_BD_Material tbm\n" +
                            "left join akmmv_prd_eip_test.t_bd_materialpurinfo tbmp on tbm.fid = tbmp.FMASTERID\n" +
                            "left join akmmv_prd_eip_test.T_bd_Measureunit tbmu on tbmu.fid = tbmp.fpurchaseunitid \n" +
                            "left join akmmv_prd_eip_test.t_ORG_ORG too on tbm.fcreateorgid = too.FID\n" +
                            "where tbm.fnumber ='" + materialNumber + "'";
                    DataSet cg = DB.queryDataSet(wltbsf.class.getName(), DBRoute.of("eip"), sqlcg);
                    String purchUnit = "";
                    if (!cg.isEmpty()) {
                        Row next = cg.next();
                        if (next.get(1) == null) {
                            log.error("推送失败" + materialNumber + "物料没有生成采购信息!");
                            msg += ("推送失败" + materialNumber + "物料没有生成采购信息!");
                            continue;
                        }
                        purchUnit = next.get(1).toString();
                    }
                    String sqlkc = "/*dialect*/ select tbm.fnumber as 物料编码,tbmu.FNUMBER,tbmix.fshelflife,tbmi.fk_ezob_sfczwl,tbmi.fmininvqty,tbmi.fmaxinvqty\n" +
                            "from akmmv_prd_eip_test.T_BD_Material tbm\n" +
                            "left join akmmv_prd_eip_test.t_bd_materialinvinfo tbmi on tbm.fid = tbmi.FMASTERID\n" +
                            "left join akmmv_prd_eip_test.T_bd_Measureunit tbmu on tbmu.fid = tbmi.finventoryunitid \n" +
                            "left join akmmv_prd_eip_test.t_bd_materialinvinfo_x tbmix on tbmix.fid = tbmi.fid \n" +
                            "left join akmmv_prd_eip_test.t_ORG_ORG too on tbm.fcreateorgid = too.FID\n" +
                            "where tbm.fnumber ='" + materialNumber + "'";
                    DataSet kc = DB.queryDataSet(wltbsf.class.getName(), DBRoute.of("eip"), sqlkc);


//               库存单位代号
                    String stockUnit = "";
                    //车载
                    int bussubject = 0;
//                保质期
                    int shelpLife = 0;
                    float minStock = 0;
                    float maxStock = 0;

                    if (!kc.isEmpty()) {
                        Row next = kc.next();
                        if (next.get(1) == null) {
                            log.error("推送失败" + materialNumber + "物料没有生成库存信息!");
                            msg += ("推送失败" + materialNumber + "物料没有生成库存信息!");
                            continue;
                        }
                        stockUnit = next.get(1).toString();
                        if (next.get(2) != null) {
                            shelpLife = next.getInteger(2);
                        }
//                        if (!next.get("fk_ezob_sfczwl").equals("")) {
//                            bussubject = 1;
//                        }
                        if (next.get("fmininvqty") != null) {
                            minStock = next.getInteger("fmininvqty");
                        }
                        if (next.get("fmaxinvqty") != null) {
                            maxStock = next.getInteger("fmaxinvqty");
                        }
                    }


//                //获取单位信息单据体
                    DynamicObjectCollection planEntries = finE.getDynamicObjectCollection("entryentity");
                    float stockPurch = 1;
                    for (DynamicObject dataE : planEntries) {
                        stockPurch = (float) dataE.getInt("numerator");
                    }


//                来料检验
                    String sqllljy = "/*dialect*/ select tbm.fnumber as 物料编码,tbie.finspecttype\n" +
                            "from akmmv_prd_eip_test.T_BD_Material tbm\n" +
                            "left join akmmv_prd_eip_test.t_bd_inspect_cfg tbic on tbic.FMASTERID = tbm.fid\n" +
                            "left join akmmv_prd_eip_test.t_bd_insptcfgentry tbie on tbie.fid = tbic.fid\n" +
                            "left join akmmv_prd_eip_test.t_ORG_ORG too on tbm.fcreateorgid = too.FID\n" +
                            "where tbm.fnumber= '" + materialNumber + "'";
                    DataSet lljy = DB.queryDataSet(wltbsf.class.getName(), DBRoute.of("eip"), sqllljy);
                    String inspect = "N";
                    if (!lljy.isEmpty()) {
                        //遍历
                        while (lljy.hasNext()) {
                            Row next = lljy.next();
                            if (next.get("finspecttype") == null) {
                                msg += ("推送失败" + materialNumber + "物料没有生成质检信息!");
                                continue;
                            }
                            if (next.getLong("finspecttype") == 959929179238017024L) {
                                inspect = "Y";
                                break;
                            }
                        }
                    }


                    int rohsFlag = 0;
                    int halogenfree = 0;
                    int gpFlag = 0;
                    int ulFlag = 0;
                    int czxmFlag = 0;
                    Integer mrpflag = null;
                    Integer moflag = null;
                    Integer priceFlag = null;
                    if (finE.getBoolean("ezob_rohs")) {
                        rohsFlag = 1;
                    }
                    if (finE.getBoolean("ezob_hf")) {
                        halogenfree = 1;
                    }
                    if (finE.getBoolean("ezob_gpflag")) {
                        gpFlag = 1;
                    }
                    if (finE.getBoolean("ezob_ul")) {
                        ulFlag = 1;
                    }
                    if (finE.getBoolean("ezob_cznn")) {
                        czxmFlag = 1;
                    }
                    if (finE.getString("ezob_rmb").equals("1")) {
                        mrpflag = 1;
                    } else if (finE.getString("ezob_rmb").equals("0")) {
                        mrpflag = 0;
                    }
                    if (finE.getString("ezob_pld").equals("1")) {
                        moflag = 1;
                    } else if (finE.getString("ezob_pld").equals("0")) {
                        moflag = 0;
                    }
                    if (finE.getString("ezob_jfpc").equals("1")) {
                        priceFlag = 1;
                    } else if (finE.getString("ezob_jfpc").equals("0")) {
                        priceFlag = 0;
                    }
                    //添加禁用思方同步
                    String stopPurch = "N";
                    if(finE.getString("enable").equals("0")){
                        stopPurch = "Y";
                    }

//                Integer use_type = finE.getInt("ezob_bsykgldbs");
                    //Integer use_type = 0;
                    //              创建时间
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String createtime = finE.getString("createtime");
                    Date date = inputFormat.parse(createtime);
                    createtime = outputFormat.format(date);
                    //新增人
                    String userId = "admin";
                    if (finE.getString("creator") != null) {
                        DynamicObject creator = finE.getDynamicObject("creator");
                        userId = creator.getString("number");
                    }

                    Map<String, Object> paramBody = new HashMap<String, Object>();
                    paramBody.put("AddFlag", addFlag);
                    paramBody.put("MaterialNumber", materialNumber);
                    paramBody.put("MaterialName", materialName+bsfbs+khjc);//名称+保税/客供
                    paramBody.put("MaterialDescrip", materialDescrip);
                    paramBody.put("GroupCode", groupCode);
                    if(purchUnit.equals("PNL")){
                        purchUnit = "P";
                    }
                    paramBody.put("PurchUnit", purchUnit);
                    if(stockUnit.equals("PNL")){
                        purchUnit = "P";
                    }
                    paramBody.put("StockUnit", stockUnit);
                    paramBody.put("StockPurch", stockPurch);
                    paramBody.put("ShelpLife", shelpLife);
                    paramBody.put("Inspect", inspect);
                    paramBody.put("Manufacturer", manufacturer.trim());//传拼接特别要求
                    paramBody.put("ROHS_FLAG", rohsFlag);
                    paramBody.put("HalogenFree", halogenfree);
                    paramBody.put("GP_FLAG", gpFlag);
                    paramBody.put("UL_FLAG", ulFlag);
                    paramBody.put("bussubject", czxmFlag);
                    //paramBody.put("bussubject", bussubject);
                    paramBody.put("mrpflag", mrpflag);
                    paramBody.put("moflag", moflag);
                    paramBody.put("price_flag", priceFlag);
                    paramBody.put("alternate_part_ptr", alternatePartPtr);
                    paramBody.put("min_stock", minStock);
                    paramBody.put("max_stock", maxStock);
                    paramBody.put("use_type", 0);
                    paramBody.put("CreateDate", createtime);
                    paramBody.put("UserId", userId);
                    paramBody.put("STOP_PURCH", stopPurch);

                    JSONObject json = JSONObject.fromObject(paramBody);
                    JSONArray jry = new JSONArray();
                    jry.add(json);
                    String cs = json.toString();
                    HttpEntity<String> entity = new HttpEntity<String>(cs, headers);
                    // 发送请求
                    RestTemplate template = new RestTemplate();
                    ResponseEntity<String> exchange = template.exchange(url, HttpMethod.POST, entity, String.class);
                    ObjectMapper mapperTS = new ObjectMapper();
                    JsonNode rootNodeTS = mapperTS.readTree(exchange.getBody());
                    Boolean code = rootNodeTS.path("Success").asBoolean();
                    String message = rootNodeTS.path("Message").toString();
                    if (exchange.getStatusCodeValue() == 200) {
                        if (code) {
                            msg = msg + materialNumber + "物料推送成功！推送时间" + getCurrTime();
                            finE.set("ezob_tbsfbs", 2);
                            SaveServiceHelper.update(finE);
                        } else {
                            msg += materialNumber + "物料推送失败！推送时间" + getCurrTime() + " 失败原因:" + message;
                        }
                    } else {
                        log.error("报错原因" + message);
                        msg += "请求异常:" + exchange.getStatusCode();
                    }
                }
                this.getView().showMessage(msg);
            } catch (Exception e) {
                this.getView().showMessage("代码异常:" + e.getMessage());
                log.error("代码异常" + e.getMessage());
            }
        }

    }

    public static String getCurrTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd :HH:mm:ss");
        return dateFormat.format(date);
    }
}