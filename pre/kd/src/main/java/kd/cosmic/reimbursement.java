package kd.cosmic;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述: 从myHR同步到金蝶的报销级别
 * 开发者: 易佳伟
 * 创建日期:
 * 关键客户：
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
public class reimbursement extends AbstractListPlugin implements Plugin {

    private final static String KEY_BARITEM="ezob_baritemap";   //er_reimburselevel


    @Override
    public void registerListener(EventObject e)
    {
        super.registerListener(e);
        this.addItemClickListeners(KEY_BARITEM);
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt)
    {

        super.beforeItemClick(evt);

        Control source = (Control)evt.getSource();


        if (StringUtils.equals( evt.getItemKey(), KEY_BARITEM)) {

            //er_reimburselevel
            String url ="https://api15.sapsf.cn/odata/v2/Picklist('JobGrade')?$expand=picklistOptions&$format=JSON";
            Map<String, Object> map = new HashMap<>();
            map.put("$expand","picklistOptions");
            map.put("$format","JSON");
            HttpRequest request = HttpRequest.get(url).form(map).header("Authorization", "Basic SmluRGllQVBJQGFrbW12OkppbkRpZTEyMw==");
            //发起请求
            String bodys = request.execute().body();
            //将json字符串转成JSONOBJECT对象，方便迭代对象判断人员是否已纯在
            JSONObject jsonObject = JSONUtil.parseObj(bodys);
            // System.out.println(jsonObject);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(bodys);
                JsonNode resultsNode = rootNode.path("d").path("picklistOptions").path("results");
                int len = resultsNode.size();
                for (int i = 0; i < len; i++) {
                    String id = resultsNode.get(i).path("id").asText();

                    String externalCode=resultsNode.get(i).path("externalCode").asText();

                    String uri = resultsNode.get(i).path("picklistLabels").path("__deferred").path("uri").asText();

                    QFilter qFilter = new QFilter("number", QCP.equals,externalCode );
                    DynamicObject oneE = BusinessDataServiceHelper.loadSingle("er_reimburselevel","masterid",new QFilter[]{qFilter});
                    if(oneE!=null&&oneE.get(0).toString()!="")
                    {

                    }
                    else{
                        DynamicObject erreimburselevel = BusinessDataServiceHelper.newDynamicObject("er_reimburselevel");
                        erreimburselevel.set("number", externalCode);
                        erreimburselevel.set("name", externalCode);
                        //er_reimburselevel.set("masterid",masterid);
                        erreimburselevel.set("createorg",OrgUnitServiceHelper.getRootOrgId());
                        erreimburselevel.set("status","C");        //需修改可能为null
                        erreimburselevel.set("enable","1");
                        erreimburselevel.set("org", OrgUnitServiceHelper.getRootOrgId());
                        erreimburselevel.set("useorg",OrgUnitServiceHelper.getRootOrgId());
                        erreimburselevel.set("creator", UserServiceHelper.getCurrentUserId());
                        erreimburselevel.set("modifier",UserServiceHelper.getCurrentUserId() );
                        erreimburselevel.set("ctrlstrategy",5);
                        long d = OrgUnitServiceHelper.getRootOrgId();
                        //UserServiceHelper.getDirectUsersOfOrg(UserServiceHelper.getCurrentUserId());
                        //  UserServiceHelper.getCurrentUserId()
                        SaveServiceHelper.save(new DynamicObject[]{erreimburselevel});

                    }

                    //long masterid= DBServiceHelper.genGlobalLongId();
                    //String aaaa = DBServiceHelper.genStringId();
                    //QFilter qFilter = new QFilter("number", QCP.equals,externalCode );
                    // DynamicObject [] dynamicObject= BusinessDataServiceHelper.load("er_reimburselevel","status,createorg,enable，number，name,masterid",null);
                    // DynamicObject oneECK = BusinessDataServiceHelper.loadSingle(dynamicObject[0].get(0),"er_reimburselevel");
                    // SaveServiceHelper.saveOperate("er_reimburselevel", new DynamicObject[] {er_reimburselevel}, OperateOption.create());
                    // SaveServiceHelper.saveOperate("er_reimburselevel", new DynamicObject[] {er_reimburselevel}, OperateOption.create());


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            /*DynamicObject [] dynamicObject= BusinessDataServiceHelper.load("er_reimburselevel","status,createorg,enable，number，name,masterid",null);
                   for( int ii=0;ii<dynamicObject.length;ii++ )
                  {
                      DynamicObject oneE = BusinessDataServiceHelper.loadSingle(dynamicObject[ii].get(0),"er_reimburselevel");
                      String aa="";
                  }*/
            this.getView().showMessage("同步完成");

        }


    }

}