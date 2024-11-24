package kd.cosmic;

import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.list.IListView;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
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
import java.util.*;

/**
 * 单据插件kd.cosmic.ListFinCard
 * 金蝶云星瀚财务卡片推送mvERP，以便在mvERP进行清理申请或报废申请的时候，可以看到卡片的历史折旧信息。
 * 目前仅针对GME，因为上海3家工厂没有固资的清理申请或者报废申请流程在mvERP的功能
 */

public class ListFinCard extends AbstractListPlugin implements Plugin {

    private final static String KEY_BARITEM = "ezob_pushinvoice";

    private static Log log = LogFactory.getLog(ListFinCard.class);

    public static String getCurrTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd :HH:mm:ss");
        String currTime = dateFormat.format(date);
        return currTime;
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);

        this.addItemClickListeners(KEY_BARITEM);
    }

    //vatCode 明细dono,明细factory
    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        GY gy = new GY();
        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {
            List<Map<String, Object>> listParamBody = new ArrayList<>();
            ListSelectedRowCollection selectedRows = ((IListView) this.getView()).getSelectedRows();

            try {
                String url = gy.url+"api/v2/app/kd/faDepAdjust";

                // 定义header对象4
                HttpHeaders headers = new HttpHeaders();
                //header请求参数
                String appSecret = "";
                String appId = gy.appid;

                int z=0;
                for (ListSelectedRow listselrow : selectedRows) {
                    z=z+1;
                    System.out.println(z);
                    //财务卡片取值过滤条件
                    QFilter[] cardFilters = new QFilter[]{new QFilter("id", QFilter.equals, listselrow.getPrimaryKeyValue())};

                    DynamicObject[] aFC = BusinessDataServiceHelper.load("fa_card_fin", "number,billno,depreuse,assetcat,ezob_basedatafield,depremethod,preusingamount,currency,originalval,preresidualval,networth,accumdepre,addupyeardepre,org,finaccountdate", cardFilters);

                    for (int i = 0; i < aFC.length; i++) {
                        String org = aFC[i].getString("org.number");
                        //查找实物卡片
                        if(org.equals("LE0006")){
                            appSecret = "GME";
                        } else if (org.equals("LE0004")) {
                            appSecret = "SP";
                        }else if (org.equals("LE0003")) {
                            appSecret = "SME";
                        }else if (org.equals("LE0005")) {
                            appSecret = "SkE";
                        }

                        QFilter qFilter = new QFilter("number", "=", aFC[i].getString("number"));
                        DynamicObject aRC = BusinessDataServiceHelper.loadSingle("fa_card_real", new QFilter[]{qFilter});

                        //折旧用途
                        String deNumber = "";
                        if (aFC[i].getDynamicObject("depreuse") != null) {
                            deNumber = aFC[i].getDynamicObject("depreuse").getString("number");
                        }

                        //资产类别
                        String assetType = "";
                        if (aRC.getDynamicObject("assetcat") != null) {
                            assetType = aRC.getDynamicObject("assetcat").getString("number");
                        }

                        //使用部门
                        String headusedept = "";
                        if (aRC.getDynamicObject("headusedept") != null) {
                            headusedept = aRC.getDynamicObject("headusedept").getString("name");
                        }
                        //理论上来说，在黄埔工厂，如果不等于GME肯定就是等于FPC，但是就怕在测试账套里面有些瞎搞的数据会批量同步不通过，所以这里做了一个默认值
                        if(!headusedept.equals("GME") && !headusedept.equals("FPC")){
                            headusedept="GME";
                        }

                        //财务卡片编码
                        String assetNumber = aFC[i].getString("number");

                        //标签号（财务自编）:分厂代码+‘-’+卡片编码   先只传编码
                        String tagNumber = "";
                        tagNumber = appSecret + "-" + aFC[i].getString("billno");

                        //财务卡片资产类别 （传编码+名称）
                        String category = "";
                        if (aFC[i].getDynamicObject("assetcat") != null) {
                            DynamicObject assetcatDynamic = aFC[i].getDynamicObject("assetcat");
                            category = assetcatDynamic.getString("number") + assetcatDynamic.getString("name");
                        }

                        //实物卡片成本中心编码
                        String costCentre = "";
                        if (aRC.getDynamicObject("costcentrer") != null) {
                            costCentre = aRC.getDynamicObject("costcentrer").getString("number");
                        }

                        //实物卡片规格型号
                        String modelNumber = aRC.getString("model");

                        //实物卡片出厂编号
                        String serialNumber = aRC.getString("ezob_ccbh");

                        //实物卡片供应商名
                        String vendorName = "";
                        if (aRC.getDynamicObject("supplier") != null) {
                            aRC.getDynamicObject("supplier").getString("number");
                        }

                        //实物卡片启用日期
                        Date fmDate = aRC.getDate("realaccountdate");
                        String datePlacedInService = "";
                        if (fmDate != null) {
                            datePlacedInService = getFormatDate(fmDate);
                        }

                        //折旧方法默认：STL（注：直线法）
                        String depreciationMethod = "";
                        if (aFC[i].getDynamicObject("depremethod") != null) {
                            depreciationMethod = aFC[i].getDynamicObject("depremethod").getString("number");
                        }

                        // 预计使用年限  取的是预计使用期间
                        double lifeMonth = Double.valueOf(aFC[i].getString("preusingamount"));

                        //币别
                        //资产来源统一用LOCAL的币别逻辑，购置日期需要分开
                        String originalCurr = "";
                        if (aFC[i].getDynamicObject("currency") != null) {
                            originalCurr = aFC[i].getDynamicObject("currency").getString("number");
                            if (originalCurr.equals("CNY")) {
                                originalCurr = "RMB";
                            }
                        }

                        //资产原值
                        String originalAmount = aFC[i].getString("originalval");

                        //local原值 （如果是传TBOOK，此处为0）
                        String originalCost = originalAmount;

                        //TBOOK原值（如果是传LOCAL，此处为0）
                        String cost = originalAmount;

                        //默认1
                        int currentUnit = 1;

                        //资产残值（LOCAL/TBOOK） 取的预计净残值
                        double salvageValue = Double.parseDouble(aFC[i].getString("preresidualval"));

                        //账面净值（LOCAL/TBOOK）  取的净值
                        double netBookValue = Double.parseDouble(aFC[i].getString("networth"));

                        //财务入账日期
                        String orgStartDate = "";
                        orgStartDate=datePlacedInService;
                        if (!deNumber.equals("01")) {
                            Date sfinaccountdate = aFC[i].getDate("finaccountdate");
                            if (sfinaccountdate != null) {
                                datePlacedInService = getFormatDate(sfinaccountdate);
                            }
                        }

                        //累计折旧
                        double accumdepre = Double.parseDouble(aFC[i].getString("accumdepre"));

                        Map<String, Object> paramBody = getListParamBody(aFC[i].getString("billno"),assetType,assetNumber,category,costCentre,
                                modelNumber,serialNumber,vendorName,
                                datePlacedInService,depreciationMethod,
                        lifeMonth,originalCurr,originalAmount,originalCost,cost,currentUnit,salvageValue,
                        netBookValue,orgStartDate,accumdepre,headusedept,deNumber);
                        listParamBody.add(paramBody);
                    }
                }

                headers.set("Content-Type", "application/json");
                headers.set("MV-Div", appSecret);
                headers.set("MV-AppId", appId);

//                //200为1批，分批进行传送，否则容易超时
//                int ibatchSize;
//                if (listParamBody.size() <= 200) {
//                    ibatchSize = 1;
//                } else {
//                    ibatchSize = listParamBody.size() / 200;
//                }
//                //分批进行传输
//                JsonNode rootNodeTS = stratUseApiWithBatch(listParamBody, headers, url, ibatchSize);
                JsonNode rootNodeTS = stratUseApi(listParamBody, headers, url);
                String jsonString = rootNodeTS.toString();
                String toCheck = "\"success\":true";

                boolean containsSuccessTrue = jsonString.contains(toCheck);
                if(containsSuccessTrue==true) {
                  this.getView().showMessage("同步成功，您可以在mvERP查看同步结果了！具体返回结果："+rootNodeTS.toString());
                }else {
                  this.getView().showErrMessage("同步失败，您可以根据具体的返回结果分析错误原因并解决："+rootNodeTS.toString(),"同步失败");
                }

            } catch (Exception e) {
                StackTraceElement stackTraceElement = e.getStackTrace()[0];
                this.getView().showMessage("异常发生在: " + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + " - " + stackTraceElement.getMethodName() + e.getMessage());
                System.out.println();
            }
        }


    }
    public Map<String, Object> getListParamBody(String billno,String assetType,String assetNumber,String category,String costCentre,
                                                String modelNumber,String serialNumber,String vendorName,
                                                String datePlacedInService,String depreciationMethod,
                                                double lifeMonth,String originalCurr,String originalAmount,
                                                String originalCost,String cost, int currentUnit, double salvageValue,
                                                double netBookValue,String orgStartDate,double accumdepre,
                                                String tagname,String deNumber) {
        Map<String, Object> paramBody = new HashMap<String, Object>();
        // paramBody.put("book", book);
        paramBody.put("assetType", assetType);
        paramBody.put("assetNumber", assetNumber);
        paramBody.put("category", category);
        paramBody.put("costCentre", costCentre);
//        paramBody.put("modelNumber", modelNumber);        //由于mvERP限制了规格型号只允许同步40，超出就报错，后来了解到他们其实会用自己的，所以压根就无需同步此字段
        paramBody.put("serialNumber", serialNumber);
        paramBody.put("vendorName", vendorName);
        paramBody.put("datePlacedInService", datePlacedInService);
        paramBody.put("depreciationMethod", depreciationMethod);
        paramBody.put("lifeMonth", lifeMonth);
        paramBody.put("originalCurr", originalCurr);
        paramBody.put("originalAmount", originalAmount);
        paramBody.put("originalCost", originalCost);
        paramBody.put("cost", cost);
        paramBody.put("currentUnit", currentUnit);
        paramBody.put("salvageValue", salvageValue);
        paramBody.put("netBookValue", netBookValue); //资产净值
        paramBody.put("orgStartDate", orgStartDate);
        paramBody.put("depreciationReserve", accumdepre);
        paramBody.put("tagNumber", tagname + "-" + billno);
        String tbook="";
        if (deNumber.equals("01")) {
            tbook = tagname + " CORP";
        } else if (deNumber.equals("03")) {
            tbook = tagname + " TAX REV";
        }
        paramBody.put("book", tbook);
        return paramBody;
    }

    public JsonNode stratUseApi(List<Map<String, Object>> list, HttpHeaders headers, String url) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        JSONArray jsonArray = new JSONArray();
        for (Map<String, Object> map : list) {
            JSONObject jsonObject = JSONObject.fromObject(map);
            jsonArray.add(map);
        }

        JSONObject aJ2 = new JSONObject();
        aJ2.put("data", jsonArray);
        System.out.println(aJ2);
        String cs = aJ2.toString();
        log.info("财务卡片的推送mverp:" + cs + "长度" + list.size());
        HttpEntity<String> entity = new HttpEntity<String>(cs, headers);
        // 发送请求
        RestTemplate template = new RestTemplate(RestTemplateConfiguration.generateHttpRequestFactory());
        ResponseEntity<String> exchange = template.exchange(url, HttpMethod.POST, entity, String.class);
        ObjectMapper aMapperTS = new ObjectMapper();
        JsonNode rootNodeTS = aMapperTS.readTree(exchange.getBody());
        return rootNodeTS;
    }

    public JsonNode stratUseApiWithBatch(List<Map<String, Object>> list, HttpHeaders headers, String url, int batchSize)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        int totalSize = list.size();
        int batches = (int) Math.ceil(totalSize / (double) batchSize);
        ArrayNode results = new ObjectMapper().createArrayNode();

        //这里好像有点不合理，只能明天再继续试了
        for (int i = 0; i < batchSize; i++) {
            int start = i * batches;
            int end = Math.min((i + 1) * batches, totalSize);
            List<Map<String, Object>> subList = list.subList(start, end);

            // 构建JSON数组
            JSONArray jsonArray = new JSONArray();
            for (Map<String, Object> map : subList) {
                JSONObject jsonObject = JSONObject.fromObject(map);
                jsonArray.add(jsonObject);
            }

            JSONObject aJ2 = new JSONObject();
            aJ2.put("data", jsonArray);
            String cs = aJ2.toString();

            // 发送HTTP请求
            HttpEntity<String> entity = new HttpEntity<>(cs, headers);
            RestTemplate template = new RestTemplate(RestTemplateConfiguration.generateHttpRequestFactory());
            ResponseEntity<String> exchange = template.exchange(url, HttpMethod.POST, entity, String.class);

            // 记录响应
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode rootNodeTS = mapper.readTree(exchange.getBody());
                results.add(rootNodeTS);
            } catch (IOException e) {
                // 处理异常，例如打印日志、抛出运行时异常等
                System.err.println("Error parsing JSON response: " + e.getMessage());
                throw new RuntimeException("Failed to parse JSON response", e);
            }
        }
        // 返回累积的响应
        return results;
    }

    public String getFormatDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String aDate = simpleDateFormat.format(date);
        return aDate;
    }
}


