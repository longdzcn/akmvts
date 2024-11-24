package kd.cosmic;

import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kd.bos.algo.DataSet;
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
import kd.bos.orm.ORM;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;


/**
 * 描述: 推送mverp 原材料存货收发存报表，【20241010】修正了数量为整型的错误
 * 开发者: 易佳伟、江伟维
 * 创建日期: 2期完成
 * 关键客户：韦经彬、SP胡工
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

public class clcb extends AbstractListPlugin implements Plugin {


    private final static String KEY_BARITEM = "ezob_pushinvoice";

    private static Log log = LogFactory.getLog(clcb.class);

    private static Map<String, Object> paramBody;

    private static  JsonNode rootNodeTS = null;

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
            GY gy = new GY();
            List<Map<String,Object>> list = new ArrayList<>();
            ListSelectedRowCollection selectedRows = ((IListView) this.getView()).getSelectedRows();
            if(selectedRows.size()==0)
            {
                this.getView().showMessage("请选择任意一行核算成本记录后再进行本操作！");
                return;
            }
            for (ListSelectedRow selectedRow : selectedRows) {
                DynamicObject customer = BusinessDataServiceHelper.loadSingle(selectedRow.getPrimaryKeyValue(), "cal_costrecord_subentity");
                try {
                    String url = gy.url+"api/v2/app/kd/synMatCostInfo";

                    // 定义header对象4
                    HttpHeaders headers = new HttpHeaders();
                    //header请求参数
                    String appSecret=null;
                    String appId = gy.appid;

//                获取核算组织
                    String value = customer.getString("calorg.number");
//                判断分厂
                    if(value.equals("LE0006")){
                        appSecret = "GME";
                    } else if (value.equals("LE0004")) {
                        appSecret = "SP";
                    }else if (value.equals("LE0003")) {
                        appSecret = "SME";
                    }else if (value.equals("LE0005")) {
                        appSecret = "SKE";
                    }
                    //苏州新厂是不是也要加进去？
                    headers.set("Content-Type", "application/json");
                    headers.set("MV-Div", appSecret);
                    headers.set("MV-AppId", appId);

                    int fnumber = 0;
                    String forg =null;
                    String strsql=null;
                    DataSet ds=null;

                    // 获取当前日期
                    LocalDate today = LocalDate.now();

                    // 获取上个月的第一天
                    LocalDate firstformatDate = today.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
                    // 获取上个月的最后一天
                    LocalDate lastformatDate = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                    // 将上个月的第1天的年月转换为整型YYYYMM
                    fnumber = firstformatDate.getYear() * 100 + firstformatDate.getMonthValue();

                    strsql = "/*dialect*/ call akmmv_prd_fi_test.KDReportCost('"+value+"','"+fnumber+"')";
                    ORM orm = ORM.create();
                    ds = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, strsql);
                    DynamicObjectCollection rows = orm.toPlainDynamicObjectCollection(ds);
                    if (rows.size()==0)
                    {
                        this.getView().showMessage("没有找到上个月任意一条存货核算数据，请确认上个月是否已经进行存货核算！");
                        return;
                    }
                    for (DynamicObject row:rows)
                    {
                        //String material_id = row.getString("物料ID");
                        String org = row.getString("组织代码");
                        String  materialnumber = excludePrefix(appSecret,row.getString("物料代码"));
                        String  materialname = row.getString("物料名称");
                        double  fperiodbeginqty = Double.parseDouble(row.getString("期初数量"));
                        double  fperiodbeginactualcost = Double.parseDouble(row.getString("期初金额"));
                        double  qcSgPrice = Double.parseDouble(row.getString("期初单价"));
                        double  fperiodinqty = Double.parseDouble(row.getString("本期收入数量"));
                        double  fperiodinactualcost = Double.parseDouble(row.getString("本期收入金额"));
                        double  fperiodissueqty = Double.parseDouble(row.getString("本期发出数量"));
                        double  fperiodissueactualcost = Double.parseDouble(row.getString("本期发出金额"));
                        double  bqSgPrice = Double.parseDouble(row.getString("本期发出单价"));
                        double  fperiodendqty = Double.parseDouble(row.getString("结余数量"));
                        double  fperiodendactualcost = Double.parseDouble(row.getString("结余金额"));
                        double  jySgPrice = Double.parseDouble(row.getString("平均单价"));
                        String  varReMark = row.getString("varReMark");

//                        QFilter qFilter = new QFilter("number", "=", material_number);
//                        DynamicObject mt = BusinessDataServiceHelper.loadSingle("bd_material", new QFilter[]{qFilter});
//                        String  mverp_number =  mt.getString("ezob_mverpnumber");

                        paramBody = new HashMap<>();
//                        paramBody.put("itemDes",material_name);
                        paramBody.put("ItemCode", materialnumber);
                        paramBody.put("itemType", "4");
                        paramBody.put("beginPrice", qcSgPrice);
                        paramBody.put("avgDirmat", jySgPrice);
                        paramBody.put("beginQty", fperiodbeginqty);
                        paramBody.put("beginAmount", fperiodbeginactualcost);
                        paramBody.put("recQty", fperiodinqty);
                        paramBody.put("recAmount", fperiodinactualcost);
                        paramBody.put("issQty", fperiodissueqty);
                        paramBody.put("issAmount", fperiodissueactualcost);
                        paramBody.put("endQty", fperiodendqty);
                        paramBody.put("endAmount", fperiodendactualcost);
                        paramBody.put("accountPeriod",fnumber);
                        paramBody.put("divCode", appSecret);
                        paramBody.put("varRemark",varReMark);
                        paramBody.put("beginDate",firstformatDate.toString());
                        paramBody.put("endDate",lastformatDate.toString());
                        list.add(paramBody);
                    }

                    rootNodeTS = stratUseApi(list,headers,url);
                    String jsonString = rootNodeTS.toString();
                    String toCheck = "\"success\":true";

                    boolean containsSuccessTrue = jsonString.contains(toCheck);
                    if(containsSuccessTrue==true) {
                        this.getView().showMessage("同步mvERP原材料存货报表成功，您可以在mvERP继续进行产成品成本计算了！具体返回结果：" + jsonString);
                    }
                    else {
                        this.getView().showErrMessage("同步mvERP原材料存货报表失败，您可以根据具体的返回结果分析错误原因并解决：" + jsonString,"同步失败");
                    }

                }catch (Exception e)
                {
                    StackTraceElement stackTraceElement=  e.getStackTrace()[0];
                    this.getView().showMessage("异常发生在: " + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + " - " + stackTraceElement.getMethodName());
                    System.out.println();
                }

                //只需要执行一次就可以了
                break;
            }

        }


    }

    public static String excludePrefix(String gc, String number) {
        // 检查工厂代码的长度是否合理（避免负数）
        int prefixLength = Math.min(gc.length(), number.length());
        // 使用substring排除前缀
        return number.substring(prefixLength);

//        //【ZOC】以上是之前开发人员易佳伟的写法，但是他只考虑旧编码的处理，他不知道后续新物料编码在金蝶里面创建了，所以以上代码会导致物料编码缺少一部分传递到mvERP，需进行以下修正：
//        //【ZOC】但是用来因为时间关系，用户紧急结账，所以还是用回了之前易佳伟的处理逻辑，只是在从存储过程中查询出新物料判断的时候，在sql中给它加上了工厂的前缀
//        String wlnumber = number;
//        if(number.substring(0,3).equals("SME")){
//            wlnumber = number.substring(3);
//        }
//        if(number.substring(0,2).equals("SP")){
//            wlnumber = number.substring(2);
//        }
//        if(number.substring(0,3).equals("SKE")){
//            wlnumber = number.substring(3);
//        }
//        if(number.substring(0,3).equals("GME")){
//            wlnumber = number.substring(3);
//        }
//        if(number.substring(0,3).equals("FPC")){
//            wlnumber = number.substring(3);
//        }
//        return wlnumber;
    }

    public JsonNode stratUseApi(List<Map<String,Object>> list , HttpHeaders headers, String url) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        JSONArray jsonArray = new JSONArray();
        for (Map<String, Object> map : list) {
            JSONObject jsonObject = JSONObject.fromObject(map);
            jsonArray.add(map);
        }

        JSONObject j2 = new JSONObject();
        j2.put("data", jsonArray);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // 对于Jackson，注册Java 8日期时间模块
        String json = mapper.writeValueAsString(j2);
        log.info("存货核算的推送mverp:"+json+"长度"+list.size());
        HttpEntity<String> entity = new HttpEntity<String>(json, headers);
        // 发送请求
        RestTemplate template = new RestTemplate(RestTemplateConfiguration.generateHttpRequestFactory());
        ResponseEntity<String> exchange = template.exchange(url, HttpMethod.POST, entity, String.class);
        ObjectMapper mapperTS = new ObjectMapper();
        JsonNode rootNodeTS = mapperTS.readTree(exchange.getBody());
        return rootNodeTS;
    }

    public String getFormatDate(Date date)
    {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String aDate =simpleDateFormat.format(date);
        return aDate;
    }


    public static String getCurrTime()
    {
        Date date = new Date();
        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd :HH:mm:ss");
        String currTime = dateFormat.format(date);
        return currTime;
    }
    public String getFormatDateup(Date date)//202406
    {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMM");
        String aDate =simpleDateFormat.format(date);
        return aDate;
    }
    //    获取一个月的天数
    public static int getDaysOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }





}



