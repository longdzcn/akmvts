package kd.cosmic.fa;

import com.alibaba.druid.util.StringUtils;
import kd.bos.coderule.api.CodeRuleInfo;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.botp.runtime.TableDefine;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.BeforeOperationArgs;
import kd.bos.openapi.common.result.OpenApiResult;
import kd.bos.openapi.common.util.OpenApiSdkUtil;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.coderule.CodeRuleServiceHelper;
import kd.fi.fa.business.utils.FaBizUtils;
import net.sf.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 描述: 变更申请单下推资产变更单并携带诸多字段
 * 开发者: 易佳伟
 * 创建日期:
 * 关键客户：
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class UseApiFa_Change  extends AbstractOperationServicePlugIn {
    private static final String targetEntityNumber ="fa_change_dept";

    private static final String srcEntityNumber ="ezob_zcczsqd";

    private static final String API_URL = "/v2/ezob/fa/fa_change_dept/dept_save";

    private static Date setDateDifPeriod(DynamicObject org, boolean futureBusiness) {
        Date now = new Date();
        if (org == null) return now;
        DynamicObject mainBook = FaBizUtils.getAsstBookByOrg(Long.valueOf(org.getLong("id")));
        if (mainBook != null) {
            Date begindate = (Date) mainBook.get("begindate");
            Date enddate = (Date) mainBook.get("enddate");
            if (begindate == null) return now;
            boolean flag = true;
            if (futureBusiness) {
                flag = now.after(begindate);
            } else {
                flag = (now.after(begindate) && now.before(enddate));
            }
            if (flag) return now;
            return enddate;
        }
        //throw new KDBizException(String.format(ResManager.loadKDString("%s, "EngineeringToFinCardGenerate_1", "fi-fa-opplugin", new Object[0]), new Object[] { org.getString("name") }));
        return now;    //此处需修正
    }

    public void onPreparePropertys(PreparePropertysEventArgs e) {

        // 需要被使用到的字段
        e.getFieldKeys().add("entryentity");
        e.getFieldKeys().add("org");
        e.getFieldKeys().add("ezob_bgyy");
        e.getFieldKeys().add("ezob_basedatafield");
        e.getFieldKeys().add("ezob_cfwz");
        e.getFieldKeys().add("ezob_syr");
        e.getFieldKeys().add("ezob_cbzx");
        e.getFieldKeys().add("ezob_zchsywz");
        e.getFieldKeys().add("ezob_cfwz");
        e.getFieldKeys().add("ezob_sqlx");
        e.getFieldKeys().add("entryentity.seq");

    }

    //获得当前期间最后日期
    @Override
    public void beforeExecuteOperationTransaction(BeforeOperationArgs e) {

        if(StringUtils.equals(e.getOperationKey(), "audit"))
        {

            DynamicObject[] dynamicObject = e.getDataEntities();
            TableDefine tagetTableDefine = EntityMetadataCache.loadTableDefine(targetEntityNumber, "fieldentry");
            TableDefine srcTableDefine = EntityMetadataCache.loadTableDefine(srcEntityNumber, "entryentity");
            String msg = "";  //返回成功信息
            String msgErr = "";  //返回错误信息

            for (DynamicObject dy:dynamicObject)
            {
                String  ifexit = "";
                if(dy.getString("ezob_sqlx").contains("外"))
                {
                    ifexit ="y";
                }

                //获取生成单据对象的编码生成规则
                CodeRuleInfo codeRule= CodeRuleServiceHelper.getCodeRule(dy.getDataEntityType().getName(),dy,null);
                String billno=CodeRuleServiceHelper.getNumber(codeRule,dy);

                //计算日期
                String bizDate = parseDate(setDateDifPeriod(dy.getDynamicObject("org"), Boolean.FALSE.booleanValue()));

                //固定为部门变更
                String deptChange ="008";

                //组织
                String org ="";
                if(dy.getDynamicObject("org")!=null)
                {
                    org =dy.getDynamicObject("org").getString("number");
                }
                //固定生成单据状态为暂存
                String billstatus ="A";

                //数据集合
                Map<String, Object> map = new HashMap<>();
                Map<String, Object> data = new HashMap<>();
                List<Map<String,Object>> dataList = new ArrayList<>();

                data.put("billno",billno);
                data.put("org_number",org);
                data.put("changetype_number",deptChange);
                data.put("billstatus",billstatus);
                data.put("changedate",bizDate);
                data.put("remark","");

                //单据ID
                long id = (long) dy.getPkValue();
                DynamicObjectCollection cn = dy.getDynamicObjectCollection("entryentity");
                //明细集合
                List<Map<String,Object>> entrytity = new ArrayList<>();
                //link集合
                List<Map<String,Object>> entryLink = new ArrayList<>();

                //循环变更单明细
                for (DynamicObject entry:cn)
                {
                    Map<String, Object> entryMap = new HashMap<>();
                    Map<String, Object> linkMap = new HashMap<>();
                    String seq = entry.getString("seq");

                    String entryId =  entry.getPkValue().toString();
                    linkMap.put("id",entryId);
                    linkMap.put("seq",seq);
                    linkMap.put("billhead_lk_stableid",srcTableDefine.getTableId());
                    linkMap.put("billhead_lk_sbillid",dy.getPkValue());
                    linkMap.put("billhead_lk_sid",entryId);
                    entryLink.add(linkMap);

                    //备注
                    String  reason =  entry.getString("ezob_bgyy");
                    entryMap.put("reason",reason);
                    entryMap.put("bizdate1",bizDate);
                    String  depreuse ="01";
                    //资产编码
                    String realNumber ="";
                    long realCardId =0;
                    if(entry.getDynamicObject("ezob_basedatafield")!=null)
                    {
                        realNumber = entry.getDynamicObject("ezob_basedatafield").getString("number");
                        realCardId = entry.getDynamicObject("ezob_basedatafield").getLong("id");
                    }
                    DynamicObject rd = BusinessDataServiceHelper.loadSingle(realCardId,"fa_card_real");
                    String  befcfwz = rd.getString("ezob_cfwz");

                    String aftcfwz ="";
                    //变更后存放地点
                    if(entry.getString("ezob_zchsywz")!=null&&!entry.getString("ezob_zchsywz").equals(""))
                    {
                        entryMap = new HashMap<>();
                        aftcfwz =entry.getString("ezob_zchsywz");
                        entryMap.put("assetnumber",realNumber);
                        entryMap.put("reason",reason);
                        entryMap.put("bizdate1",bizDate);
                        entryMap.put("depreuse1_number",depreuse);
                        entryMap.put("beforevalue",befcfwz);
                        entryMap.put("aftervalue",aftcfwz);
                        entryMap.put("field","fa_card_real.ezob_cfwz");
                        entrytity.add(entryMap);
                    }

                    //变更后使用用户
                    String aftuser = "";
                    if(entry.getDynamicObject("ezob_syr")!=null)
                    {
                        entryMap = new HashMap<>();
                        entryMap.put("assetnumber",realNumber);
                        entryMap.put("reason",reason);
                        entryMap.put("bizdate1",bizDate);
                        entryMap.put("depreuse1_number",depreuse);
                        aftuser =entry.getDynamicObject("ezob_syr").getString("number");
                        entryMap.put("aftervalue",aftuser);
                        entryMap.put("field","fa_card_real.headuseperson");
                        entrytity.add(entryMap);
                    }

                    //变更后成本中心
                    String aftcenter = "";
                    if(entry.getDynamicObject("ezob_cbzx")!=null&&ifexit.equals("y"))
                    {
                        entryMap = new HashMap<>();
                        entryMap.put("assetnumber",realNumber);
                        entryMap.put("reason",reason);
                        entryMap.put("bizdate1",bizDate);
                        entryMap.put("depreuse1_number",depreuse);
                        aftcenter =entry.getDynamicObject("ezob_cbzx").getString("number");
                        entryMap.put("aftervalue",aftcenter);
                        entryMap.put("field","fa_card_real.costcentrer");
                        entrytity.add(entryMap);
                    }
                }
                data.put("billhead_lk",entryLink);
                data.put("fieldentry",entrytity);
                dataList.add(data);
                map.put("data",dataList);

                OpenApiResult result;
                JSONObject json = new JSONObject() ;
                json=JSONObject.fromObject(map);
                String cs = json.toString();
                result = OpenApiSdkUtil.invoke(API_URL,map);
                if(result.isStatus())
                {
                    msg = msg + "单号["+billno+"]审核通过,下游变更单生成成功";
//                    this.operationResult.setMessage("审核通过,下游变更单生成成功");
//                    return;
                }else {
                    msgErr = msgErr + "单号["+billno+"]审核失败，下游变更单生成失败，错误码(详情可看接口日志):"+result.getMessage();
//                    this.operationResult.setMessage("审核失败，下游变更单生成失败，错误码(详情可看接口日志):"+result.getMessage());
//                    e.setCancel(true);
//                    return;
                }
            }
            if(msgErr != "")
            {
                this.operationResult.setMessage(msgErr);
                e.setCancel(true);
            }
        }
    }

    public String parseDate(Date date)
    {
        SimpleDateFormat shortDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String shortDateString = shortDateFormat.format(calendar.getTime()); // 获取格式化后的短日期字符串
        return shortDateString;
    }

}

