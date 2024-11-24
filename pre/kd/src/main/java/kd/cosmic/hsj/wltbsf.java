package kd.cosmic.hsj;

import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
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
 * 描述: 物料同步思方,插件放在物料列表
 * 开发者: 李四辉
 * 创建日期: 2024/08/28
 * 关键客户：郑楷绚
 * 已部署正式：ture
 * 备注：已投入正式环境使用，真实路径应是李四辉工程包下，此类仅仅是同步Mv调用到
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
        String url = "http://10.101.23.218:8489/api/api/MaterialProject";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {
            try {
                ListSelectedRowCollection selectedRows = ((IListView) this.getView()).getSelectedRows();
                for (ListSelectedRow list : selectedRows) {
                    DynamicObject finE = BusinessDataServiceHelper.loadSingle(list.getPrimaryKeyValue(), "bd_material");
                    //新增修改标识
                    int addFlag = 1;
                    if (finE.getInt("ezob_tbsfbs") != 1) {
                        addFlag = finE.getInt("ezob_tbsfbs");
                    }
                    //状态
                    String status = finE.getString("status");
                    //编码
                    String materialNumber = (String) finE.get("number");
                    //名称
                    String materialName = finE.getString("name");
//                规格
                    String materialDescrip = finE.getString("ezob_gg");
//                            != null ? finE.getString("ezob_gg") : "";
                    if (materialDescrip.isEmpty()){
                        log.error("推送失败" + materialNumber + "物料规格不能为空!");
                        msg += ("推送失败" + materialNumber + "物料规格不能为空!");
                        continue;
                    }

                    if (!status.equals("C")) {
                        log.error("报错原因:" + materialNumber + "物料未审核");
                        msg += ("推送失败" + materialNumber + "物料未审核");
                        continue;
                    }
                    //类别代号
                    String groupCode = finE.getString("ezob_wlfl.number");

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
                        if (!next.get("fk_ezob_sfczwl").equals("")) {
                            bussubject = 1;
                        }
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

                    // 特别要求
                    String manufacturer = (String) finE.get("ezob_tbyq");

                    int rohsflag = 0;
                    int halogenFree = 0;
                    int gpflag = 0;
                    int ulFlag = 0;
                    Integer mrpflag = null;
                    Integer moflag = null;
                    Integer priceFlag = null;
                    if (finE.getBoolean("ezob_rohs")) {
                        rohsflag = 1;
                    }
                    if (finE.getBoolean("ezob_hf")) {
                        halogenFree = 1;
                    }
                    if (finE.getBoolean("ezob_gpflag")) {
                        gpflag = 1;
                    }
                    if (finE.getBoolean("ezob_ul")) {
                        ulFlag = 1;
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


//                Integer use_type = finE.getInt("ezob_bsykgldbs");
                    Integer useType = 0;
                    //              创建时间
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String createDate = finE.getString("createtime");
                    Date date = inputFormat.parse(createDate);
                    createDate = outputFormat.format(date);
                    //新增人
                    String userId = "admin";
                    if (finE.getString("creator") != null) {
                        DynamicObject creator = finE.getDynamicObject("creator");
                        userId = creator.getString("number");
                    }

                    Map<String, Object> paramBody = new HashMap<String, Object>();
                    paramBody.put("AddFlag", addFlag);
                    paramBody.put("MaterialNumber", materialNumber);
                    paramBody.put("MaterialName", materialName);
                    paramBody.put("MaterialDescrip", materialDescrip);
                    paramBody.put("GroupCode", groupCode);
                    paramBody.put("PurchUnit", purchUnit);
                    paramBody.put("StockUnit", stockUnit);
                    paramBody.put("StockPurch", stockPurch);
                    paramBody.put("ShelpLife", shelpLife);
                    paramBody.put("Inspect", inspect);
                    if (!manufacturer.isEmpty()) {
                        paramBody.put("Manufacturer", manufacturer);
                    }
                    paramBody.put("ROHS_FLAG", rohsflag);
                    paramBody.put("HalogenFree", halogenFree);
                    paramBody.put("GP_FLAG", gpflag);
                    paramBody.put("UL_FLAG", ulFlag);
                    paramBody.put("bussubject", bussubject);
                    paramBody.put("mrpflag", mrpflag);
                    paramBody.put("moflag", moflag);
                    paramBody.put("price_flag", priceFlag);
                    paramBody.put("min_stock", minStock);
                    paramBody.put("max_stock", maxStock);
                    paramBody.put("use_type", useType);
                    paramBody.put("CreateDate", createDate);
                    paramBody.put("UserId", userId);

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