//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package kd.cosmic.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.fi.er.business.servicehelper.CoreBaseBillServiceHelper;
import kd.fi.er.business.servicehelper.HttpServiceHelper;
import kd.fi.er.business.trip.exception.TripSyncLogParam;
import kd.fi.er.business.trip.model.UserModel;
import kd.fi.er.business.trip.service.function.AbstractUserInvoke;
import kd.fi.er.business.trip.supplier.Supplier;
import kd.fi.er.business.trip.supplier.ctrip.systemexternal.CorpSync;
import kd.fi.er.business.trip.util.TripCommonUtil;
import kd.fi.er.business.trip.util.TripSyncConfigUtil;
import kd.fi.er.business.utils.ErCommonUtils;
import org.apache.commons.lang.StringUtils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

public class CTripUserInvoke extends AbstractUserInvoke<Set<JSONObject>> {
    private static final Log logger = LogFactory.getLog(CTripUserInvoke.class);
    private static final String SUCCESS = "Success";

    public CTripUserInvoke() {
    }

    protected String supplier() {
        return Supplier.XIECHENG.name();
    }

    public Set<JSONObject> convert(Object s) {
        Set<UserModel> set = (Set)s;
        Set<JSONObject> userJsonObject = new HashSet();
        int batch = TripSyncConfigUtil.getIntValue("trip_sync_org_user_size", 10);
        int batchTemp = batch;
        JSONArray userBatch = new JSONArray();
        Iterator var7 = set.iterator();

        while(true) {
            UserModel user;
            JSONObject userJson;
            do {
                if (!var7.hasNext()) {
                    if (batchTemp != 0 && batchTemp != batch) {
                        this.generateBatchJSON(userJsonObject, userBatch);
                    }

                    return userJsonObject;
                }

                user = (UserModel)var7.next();
                userJson = new JSONObject();
            } while(!StringUtils.isNotEmpty(user.getSubaccountName()));
            DynamicObject numberObject=null;
            //DynamicObject userInfos = BusinessDataServiceHelper.loadSingleFromCache("bos_user", "id,number,entryentity.dpt,entryentity.ispartjob,entryentity.id", new QFilter[]{new QFilter("number", "=", user.getEmployeeID())});
           //自行查询人员信息，过滤部分组织人员
            QFilter qFilter = new QFilter("number", QCP.equals,user.getEmployeeID() );
            numberObject = BusinessDataServiceHelper.loadSingle("bos_user", "ezob_frzt", qFilter.toArray());
            String frzt = numberObject.getString("ezob_frzt.number");
            if(frzt.equals("LE0002")||frzt.equals("LE0016")||frzt.equals("LE0017")||frzt.equals("LE0018")||frzt.equals("LE0089")){
                continue;
            }
            userJson.put("SubAccountName", user.getSubaccountName());
            --batchTemp;
            userJson.put("EmployeeID", user.getEmployeeID());
            userJson.put("Name", user.getName());
            userJson.put("MobilePhone", user.getMobilePhone());
            userJson.put("Email", user.getEmail());
            userJson.put("Dept1", user.getDept1());
            String valid = user.getEnableFlag().equals("Y") ? "A" : "I";
            userJson.put("Valid", valid);
            if (null != user.getDeptId() && !user.getDeptId().isEmpty() && !"null".equals(user.getDeptId())) {
                Long orgId = CoreBaseBillServiceHelper.getAccountOrgId(Long.parseLong(user.getDeptId()));
                Map<String, Object> orgMap = OrgUnitServiceHelper.getCompanyByAdminOrg(orgId);
                if (null != orgId && 0L != orgId && null != orgMap) {
                    userJson.put("CostCenter", orgMap.get("name"));
                }
            }

            this.setCostCenter2(userJson, user);
            int paramValue = TripSyncConfigUtil.getIntValue("ctrip_personnel_rank", 2);
            DynamicObject dynamicObject = null;
            if (null != user.getDeptId() && !user.getDeptId().isEmpty() && !"null".equals(user.getDeptId())) {
                Long companyId = CoreBaseBillServiceHelper.initCompanyByDept(Long.parseLong(user.getDeptId()));
                dynamicObject = BusinessDataServiceHelper.loadSingleFromCache("bos_org", "id,name,number", new QFilter[]{new QFilter("id", "in", companyId)});
            }

            String reimburseleveName = user.getReimburseleveName();
            switch (paramValue) {
                case 1:
                    if (null != dynamicObject && StringUtils.isNotEmpty(reimburseleveName)) {
                        userJson.put("RankName", dynamicObject.getString("number") + reimburseleveName);
                    }
                    break;
                case 2:
                default:
                    if (StringUtils.isNotEmpty(reimburseleveName)) {
                        userJson.put("RankName", reimburseleveName);
                    }
                    break;
                case 3:
                    if (dynamicObject != null && StringUtils.isNotEmpty(reimburseleveName)) {
                        userJson.put("RankName", dynamicObject.getString("number") + reimburseleveName);
                    } else if (dynamicObject != null && StringUtils.isEmpty(reimburseleveName)) {
                        String defaultName = ResManager.loadKDString("普通员工", "CTripUserInvoke_0", "fi-er-business", new Object[0]);
                        userJson.put("RankName", dynamicObject.getString("number") + defaultName);
                    }
                    break;
                case 4:
                    if (StringUtils.isNotEmpty(reimburseleveName)) {
                        userJson.put("RankName", reimburseleveName);
                    } else {
                        userJson.put("RankName", ResManager.loadKDString("普通员工", "CTripUserInvoke_0", "fi-er-business", new Object[0]));
                    }
            }

            JSONObject authenticationInfoObject = new JSONObject();
            authenticationInfoObject.put("Sequence", getSequence());
            authenticationInfoObject.put("Authentication", userJson);
            userBatch.add(authenticationInfoObject);
            if (batchTemp == 0) {
                this.generateBatchJSON(userJsonObject, userBatch);
                batchTemp = batch;
                userBatch = new JSONArray();
            }
        }
    }

    private static long getSequence() {
        long seq = 0L;
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
        String temp = sf.format(new Date());
        SecureRandom secRandom = new SecureRandom();
        int random = (int)(secRandom.nextDouble() * 10000.0);
        seq = Long.parseLong(temp + random);
        return seq;
    }

    private void generateBatchJSON(Set<JSONObject> userJsonObject, JSONArray userBatch) {
        JSONObject corpCustInfo = new JSONObject();
        Map<String, String> corpMap = TripCommonUtil.getTripServiceInfo(this.supplier());
        corpCustInfo.put("Language", (Object)null);
        corpCustInfo.put("Appkey", corpMap.get("appkey"));
        corpCustInfo.put("Ticket", CorpSync.getTicket("https://ct.ctrip.com/SwitchAPI/Order/Ticket", corpMap));
        corpCustInfo.put("CorporationID", corpMap.get("orationid"));
        corpCustInfo.put("AuthenticationInfoList", userBatch);
        userJsonObject.add(corpCustInfo);
    }

    protected String batPushProcess(Object set) {
        Set<JSONObject> userJSONObjectSet = (Set)set;
        String code = "200";
        Iterator var4 = userJSONObjectSet.iterator();

        while(var4.hasNext()) {
            JSONObject postJson = (JSONObject)var4.next();
            logger.info("商旅集成,携程人员,入参: " + postJson.toJSONString());
            TripSyncLogParam currentLog = this.tripSyncLogParam.clone();
            currentLog.setRequestdata(postJson);
            currentLog.setRequesturl("https://ct.ctrip.com/corpservice/CorpCustService/SaveCorpCustInfoList");
            JSONObject backJson = HttpServiceHelper.httpPost("https://ct.ctrip.com/corpservice/CorpCustService/SaveCorpCustInfoList", postJson, 50000);
            currentLog.setResponsedata(backJson);
            if (backJson != null) {
                logger.info("商旅集成,携程人员,返回: " + backJson.toJSONString());
                String backcode = (String)backJson.get("Result");
                if (!backcode.equals("Success")) {
                    code = "300";
                    currentLog.setStatus("B");
                    logger.info("商旅集成,携程人员,返回失败:" + ((JSONArray)backJson.get("ErrorMessageList")).toJSONString());
                }
            } else {
                currentLog.setStatus("B");
            }

            this.addTripSyncLogParam(currentLog);
            this.tripSyncLogParamList.add(currentLog);
        }

        return code;
    }

    protected boolean syncUserByPage() {
        return true;
    }

    private void setCostCenter2(JSONObject userJson, UserModel user) {
        DynamicObject[] userInfos = BusinessDataServiceHelper.load("bos_user", "id,number,entryentity.dpt,entryentity.ispartjob,entryentity.id", new QFilter[]{new QFilter("number", "=", user.getEmployeeID())});
        if (userInfos != null && userInfos.length > 0) {
            DynamicObject userInfo = userInfos[0];
            DynamicObjectCollection entryEntity = userInfo.getDynamicObjectCollection("entryentity");

            for(int i = 0; i < entryEntity.size(); ++i) {
                DynamicObject dynamicObject = (DynamicObject)entryEntity.get(i);
                boolean ispartjob = dynamicObject.getBoolean("ispartjob");
                if (!ispartjob) {
                    Long id = ErCommonUtils.getPk(dynamicObject.get("dpt"));
                    DynamicObject org = BusinessDataServiceHelper.loadSingle("bos_adminorg", "id,name,number", new QFilter[]{new QFilter("id", "=", id)});
                    if (org != null) {
                        userJson.put("CostCenter2", org.getString("name"));
                    }
                }
            }
        }

    }
}
