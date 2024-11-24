package kd.cosmic.hsj;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.fi.er.business.trip.supplier.ctrip.invoke.CTripUserInvoke;

import java.util.HashSet;
import java.util.Set;
/**
 * 描述: 人员集成插件——根据需求过滤部分组织——挂在费用核算商旅集成携程扩展插件
 * 开发者: 梁远健、胡思江
 * 创建日期: 2024/08/28
 * 关键客户：马衍浩
 * 已部署正式：ture
 * 备注：已经部署，但目前尚未投入使用8.28
 */

public class Invoke extends CTripUserInvoke {
@Override
public Set<JSONObject> convert(Object s) {
    System.err.println("Before conversion,s:" + s.toString());
    Set<JSONObject> sb = super.convert(s);
    // 查看打印的数据类型
    System.err.println(sb.toString());
    // 创建新的集合，存放过滤后的数据
    Set<JSONObject> newSb = new HashSet<>();
    for (JSONObject jsonObject : sb){
        // 根据键获取对应的值
        Object authenticationInfoListObject = jsonObject.get("AuthenticationInfoList");
        if (authenticationInfoListObject instanceof JSONArray){
            //因为是多层JSON数据集合，需要再进一步得到上个键中存在的值集合
            JSONArray authInfoList = (JSONArray) authenticationInfoListObject;
            JSONArray filteredAuthInfoList = new JSONArray();
            for (int i = 0; i < authInfoList.size(); i++) {
                JSONObject authInfo = authInfoList.getJSONObject(i);
                // 继续从键中拿到对应的值
                JSONObject authentication = authInfo.getJSONObject("Authentication");
                // 获取工号
                String employeeID = authentication.getString("EmployeeID");
                DynamicObject numberObject = null;
                // 过滤查询
                String frzt = null;
                QFilter qFilter = new QFilter("number", "=", employeeID);
                numberObject = BusinessDataServiceHelper.loadSingle("bos_user", "ezob_frzt", qFilter.toArray());
                frzt = numberObject.getString("ezob_frzt");
                if (!(frzt.equals("LE0002") || frzt.equals("LE0016") || frzt.equals("LE0017") || frzt.equals("LE0018") || frzt.equals("LE0089") || frzt.equals("LE0900") || frzt !=null)) {
                    filteredAuthInfoList.add(authInfo);
                }
            }
            // 当filteredAuthInfoList是[]时，不存放到newSb集合中，因为会产生
            // [{"AuthenticationInfoList":[],"Ticket":"66cb74a6b20bb0e409e315c5","CorporationID":"MWDZ","Appkey":"obk_MWDZ"}]
            if (filteredAuthInfoList.isEmpty()) {
                continue;
            }
            // 创建一个新的 JSONObject 并复制原始 jsonObject 的键值对
            JSONObject newJsonObject = new JSONObject();
            for (String key : jsonObject.keySet()) {
                newJsonObject.put(key, jsonObject.get(key));
            }
            // 替换原始的 AuthenticationInfoList
            newJsonObject.put("AuthenticationInfoList", filteredAuthInfoList);
            // 将新的 JSONObject 添加到 newSb
            newSb.add(newJsonObject);
        }
    }
    return newSb;
}
}