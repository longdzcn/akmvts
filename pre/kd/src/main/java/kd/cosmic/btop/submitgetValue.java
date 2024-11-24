package kd.cosmic.btop;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.MulBasedataDynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.BeforeOperationArgs;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * 单据界面
 *
 *  kd.cosmic.btop.submitgetValue
 */
public class submitgetValue extends AbstractOperationServicePlugIn  {
    /**
     * 操作校验通过，开启了事务，准备把数据提交到数据库之前触发此事件；
     * @param
     */


    @Override
    public void   beforeExecuteOperationTransaction(BeforeOperationArgs e) {
       DynamicObject[] dynamicObjects = e.getDataEntities();
       DynamicObject dynamicObject =dynamicObjects[0];
       DynamicObjectCollection rows = dynamicObject.getDynamicObjectCollection("tripentry");
       for (DynamicObject row:rows)
       {
           DynamicObjectCollection entryentitys = row.getDynamicObjectCollection("entryentity");
           for (DynamicObject entry:entryentitys)
           {
               MulBasedataDynamicObjectCollection trip2travelers = (MulBasedataDynamicObjectCollection) entry.get("trip2travelers");
               String id = trip2travelers.get(0).getString("fbasedataid_id");
               entry.set("ezob_bxr",getUser(id));
           }


       }
        SaveServiceHelper.save(new DynamicObject[]{dynamicObject});
    }

    // getBosUser
    public DynamicObject getUser(String fid)
    {
        DynamicObject user = BusinessDataServiceHelper.loadSingle(fid, "bos_user");

        return  user;
    }

}