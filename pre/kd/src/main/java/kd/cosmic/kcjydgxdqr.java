package kd.cosmic;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 描述: 库存检验单更新条码主档到期日，清空回温次数
 * 开发者: 李四辉
 * 创建日期:2024-04-01
 * 关键客户：仓库
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
public class kcjydgxdqr extends AbstractOperationServicePlugIn {
    public void endOperationTransaction(EndOperationTransactionArgs e) {
        try {
            for (DynamicObject dy : e.getDataEntities()) {
                Long billId = Long.valueOf(String.valueOf(dy.getPkValue()));
                DynamicObject aBILL = BusinessDataServiceHelper.loadSingle(billId, "qcnp_invbalinspec", "lotnumber,newarrdate,expirydate");
                DynamicObjectCollection dynamicObjectCollection = aBILL.getDynamicObjectCollection("matintoentity");
                for (DynamicObject dynamicObject : dynamicObjectCollection) {
                    String lotnumber = dynamicObject.getString("lotnumber");//批号
                    if(dynamicObject.get("newarrdate") != null){
                        String newarrdate =  dynamicObject.getString("newarrdate");//新到期日
                        String expirydate =  dynamicObject.getString("expirydate");//原到期日
                        //日期转换
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = format.parse(newarrdate);
                        Date ydate = format.parse(expirydate);
                        QFilter q = new QFilter("flot", "=", lotnumber);
                        DynamicObject[] simpleBill = BusinessDataServiceHelper.load("ezob_tmzd", "fexpirationdate,ezob_hwcss,ezob_yyxq", new QFilter[] { q });
                        for (DynamicObject tmzddy : simpleBill) {
                            tmzddy.set("fexpirationdate", date);//到期日
                            tmzddy.set("ezob_hwcss", 0);//新回温次数清0
                            tmzddy.set("ezob_yyxq", ydate);//原有效期
                            SaveServiceHelper.update(tmzddy);
                        }
                    }
                }
            }
        } catch (Exception e1) {
            this.operationResult.setMessage(e1.toString());
            return;
        }
    }
}
