package kd.cosmic;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * 描述: 库存检验单审核更新批号主档延迟次数
 * 开发者: 李四辉
 * 创建日期:2024-04-01
 * 关键客户：仓库
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */

public class kcjydgxphzd extends AbstractOperationServicePlugIn {
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        try {
            for (DynamicObject dy : e.getDataEntities()) {
                Long billId = Long.valueOf(String.valueOf(dy.getPkValue()));
                DynamicObject aBILL = BusinessDataServiceHelper.loadSingle(billId, "qcnp_invbalinspec", "lotnumber,expirydate,newarrdate");
                DynamicObjectCollection dynamicObjectCollection = aBILL.getDynamicObjectCollection("matintoentity");
                for (DynamicObject dynamicObject : dynamicObjectCollection) {
                    String lotnumber = dynamicObject.getString("lotnumber");//批号
                        //更新批号主档延迟次数
                        QFilter q2 = new QFilter("number", "=", lotnumber);
                        DynamicObject[] phzdBill = BusinessDataServiceHelper.load("bd_lot", "ezob_yccs,ezob_yyxq,ezob_yxq", new QFilter[] { q2 });
                        for (DynamicObject phzddy : phzdBill) {
                            phzddy.set("ezob_yccs", phzddy.getInt("ezob_yccs")+1);//延迟次数
                            //原有效期为空，则更新批号主档的原有效期
                            if(phzdBill[0].get("ezob_yyxq") == null){
                                phzddy.set("ezob_yyxq", dynamicObject.get("expirydate"));//原有效期
                                phzddy.set("ezob_yxq", dynamicObject.get("newarrdate"));//有效期
                            }else{
                                phzddy.set("ezob_yxq", dynamicObject.get("newarrdate"));//有效期
                            }

                            SaveServiceHelper.update(phzddy);

//                            String sql = "/*dialect*/ update akmmv_prd_eip_test.t_bd_lot set fk_ezob_yccs = ifnull(fk_ezob_yccs,0) + 1 where fnumber = '"+lotnumber+"'";
//                            DB.update(DBRoute.basedata, sql);

                        }
                    }
                }
        } catch (Exception e1) {
            this.operationResult.setMessage(e1.toString());
            return;
        }
    }
}
