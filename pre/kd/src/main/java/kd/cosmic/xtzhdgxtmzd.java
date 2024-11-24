package kd.cosmic;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * 描述:形态转换单审核后更新条码主档
 * 开发者: 李四辉
 * 创建日期:2024-04-01
 * 关键客户：仓库
 * 已部署正式：true
 * 备注：已投入正式环境使用，无问题
 */
public class xtzhdgxtmzd extends AbstractOperationServicePlugIn {
    public void endOperationTransaction(EndOperationTransactionArgs e) {
        try {
            for (DynamicObject dy : e.getDataEntities()) {
                Long billId = Long.valueOf(String.valueOf(dy.getPkValue()));
                DynamicObject bill = BusinessDataServiceHelper.loadSingle(billId, "im_adjustbill", "billno,lotnumber1,invtype1,invstatus1,warehouse1,warehouse1");
                DynamicObjectCollection dynamicObjectCollection = bill.getDynamicObjectCollection("afterentity");
                for (DynamicObject dynamicObject : dynamicObjectCollection) {
                    String lotnumber = dynamicObject.getString("lotnumber1");
                    Long invtypeId = Long.valueOf(dynamicObject.get("invtype1.id").toString());
                    Long invstatusId = Long.valueOf(dynamicObject.get("invstatus1.id").toString());
                    Long warehouseId = Long.valueOf(dynamicObject.get("warehouse1.id").toString());
                    Long locationId = 0L;//初始仓位
                    if(dynamicObject.get("location1.id") != null){
                        locationId = Long.valueOf(dynamicObject.get("location1.id").toString());
                    }

                    QFilter q = new QFilter("flot", "=", lotnumber);
                    DynamicObject[] simpleBill = BusinessDataServiceHelper.load("ezob_tmzd", "ezob_cgrkdh,ezob_rkkclx,ezob_rkkczt,fstockid,fstocklocid,fmonumber", new QFilter[] { q });
                    for (DynamicObject tmzddy : simpleBill) {
                        tmzddy.set("ezob_rkkclx", invtypeId);//入库库存类型
                        tmzddy.set("ezob_rkkczt", invstatusId);//入库库存状态
                        tmzddy.set("fstockid", warehouseId);//仓库
                        tmzddy.set("fstocklocid", locationId);//仓位
                        SaveServiceHelper.update(tmzddy);
                    }
                }
            }
        } catch (Exception e1) {
            this.operationResult.setMessage(e1.toString());
            return;
        }
    }
}
