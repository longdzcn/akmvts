package kd.cosmic;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * 描述:操作插件：入库更新条码主档库存状态
 * 开发者: 李四辉
 * 创建日期:2024-04-01
 * 关键客户：仓库
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
public class gxtmzdkczt extends AbstractOperationServicePlugIn {
    public void endOperationTransaction(EndOperationTransactionArgs e) {
        try {
            for (DynamicObject dy : e.getDataEntities()) {
                Long billId = Long.valueOf(String.valueOf(dy.getPkValue()));
                DynamicObject aBILL = BusinessDataServiceHelper.loadSingle(billId, "im_purinbill", "billno,lotnumber,invtype,invstatus,warehouse,location");
                String fbillno = aBILL.getString("billno");
                DynamicObjectCollection dynamicObjectCollection = aBILL.getDynamicObjectCollection("billentry");
                for (DynamicObject dynamicObject : dynamicObjectCollection) {
                    String lotnumber = dynamicObject.getString("lotnumber");
                    Long invtypeId = Long.valueOf(dynamicObject.get("invtype.id").toString());
                    Long invstatusId = Long.valueOf(dynamicObject.get("invstatus.id").toString());
                    Long warehouseId = Long.valueOf(dynamicObject.get("warehouse.id").toString());
                    Long locationId = 0L;//初始仓位
                    if(dynamicObject.get("location.id") != null){
                        locationId = Long.valueOf(dynamicObject.get("location.id").toString());
                    }

                    QFilter q = new QFilter("flot", "=", lotnumber);
                    DynamicObject[] simpleBill = BusinessDataServiceHelper.load("ezob_tmzd", "ezob_cgrkdh,ezob_rkkclx,ezob_rkkczt,fstockid,fstocklocid,fmonumber", new QFilter[] { q });
                    for (DynamicObject tmzddy : simpleBill) {
//                        DynamicObjectCollection col = tmzddy.getDynamicObjectCollection("");//莫名其妙多的代码导致插件报错被喷
                        tmzddy.set("ezob_cgrkdh", fbillno);//入库单号
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
