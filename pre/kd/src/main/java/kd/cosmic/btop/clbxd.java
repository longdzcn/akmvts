package kd.cosmic.btop;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.MulBasedataDynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;

/**
 * author : 张蓉
 *
 *
 *
 */


public class clbxd extends AbstractFormPlugin {
    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        String name = e.getProperty().getName();
        if (StringUtils.equals("trip2travelers", name)) {
            MulBasedataDynamicObjectCollection trip2travelers =(MulBasedataDynamicObjectCollection)  this.getModel().getValue("trip2travelers");
            ChangeData changeData = e.getChangeSet()[0];
            int rowNum = changeData.getRowIndex();
            int     currentRow = this.getModel().getEntryCurrentRowIndex("entryentity");
            Object newValue = changeData.getNewValue();
            DynamicObject dt = (DynamicObject)((MulBasedataDynamicObjectCollection) newValue).get(0).get("fbasedataid");
            // bos_user（表t_SEC_User）的fid，能查出姓名工号等信息
            Object fid = dt.getLong("id");

            if(trip2travelers.size() > 1) {
                for (int i = 0;i<trip2travelers.size()-1;i++) {
                    this.getModel().insertEntryRow("entryentity",rowNum);
                    DynamicObject traveler = (DynamicObject)  trip2travelers.get(i+1).get("fbasedataid");
                    String id = String.valueOf(trip2travelers.get(i+1).get("fbasedataid_id"));

                    DynamicObjectCollection temp = new DynamicObjectCollection();
                    temp.add(traveler);
                     this.getModel().setValue("trip2travelers",temp,rowNum); //出差人
                    this.getModel().setValue("ezob_bxr",getUser(id),rowNum); //报销人


                }
                DynamicObjectCollection temp = new DynamicObjectCollection();
                temp.add(dt);
                this.getModel().setValue("ezob_bxr",getUser(fid.toString()),currentRow+trip2travelers.size()-1); //报销人

                this.getModel().setValue("trip2travelers",temp,currentRow+trip2travelers.size()-1);
                return;
            }else {
                this.getModel().setValue("ezob_bxr",getUser(fid.toString()),0); //报销人

            }



            // "人员"基础资料
            DynamicObject user = BusinessDataServiceHelper.loadSingle(fid, "bos_user");

//            // 获取”人员“中的”默认成本中心“
            DynamicObject boscostcenter = (DynamicObject) user.get("ezob_cbzx");


            // 获取”人员“ 中的”部门信息“分录下的”部门“
            DynamicObjectCollection entryentity = (DynamicObjectCollection) user.get("entryentity");
            DynamicObject dpt = (DynamicObject) entryentity.get(0).get("dpt");

            this.getModel().setValue("travelcostcenter",boscostcenter,rowNum); // 成本中心
            this.getModel().setValue("travelcostdept",dpt,rowNum);    // 费用承担部门
            int a = 0;
            if(newValue == null) {
                return;
            }
        }
    }
    // getBosUser
    public DynamicObject getUser(String fid)
    {
        DynamicObject user = BusinessDataServiceHelper.loadSingle(fid, "bos_user");

        return  user;
    }
}


