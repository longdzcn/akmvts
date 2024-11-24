package kd.cosmic.connector;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.ExtendedDataEntitySet;
import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
import kd.bos.entity.botp.plugin.args.AfterConvertEventArgs;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;

/**
 * 客户地址携带数据到财务相关
 */
public class cwxg extends AbstractConvertPlugIn {


    @Override
    public void afterConvert(AfterConvertEventArgs e) {
            //
            ExtendedDataEntitySet targetExtDataEntitySet = e.getTargetExtDataEntitySet();
//        获取财务相关实体
            ExtendedDataEntity[] extendedDataEntities = targetExtDataEntitySet.FindByEntityKey("ezob_cwxg");
            for (ExtendedDataEntity extendedDataEntity : extendedDataEntities) {
                DynamicObject dataEntity = extendedDataEntity.getDataEntity();//单个单据数据
//            获取送货地址变更分录
                DynamicObjectCollection ezobEntryentity2 = dataEntity.getDynamicObjectCollection("ezob_shdzbg");
//            获取客户地址表数据
                String number = extendedDataEntity.getDataEntity().getString("number");
                QFilter q = new QFilter("customer.number", QCP.equals, number);
                //                获取客户地址
                DynamicObject[] load = BusinessDataServiceHelper.load("bd_address", "bd_customer,ezob_buy_sell,ezob_svmif", new QFilter[]{q});
//            清空分录数据
                ezobEntryentity2.clear();
                for (DynamicObject dynamicObject : load) {
//                获取编码
                    Object n = dynamicObject.get("number");
//                    是否VMI
                    String ezobsvmif = dynamicObject.getString("ezob_svmif");
//                    Buy Sell
                    String ezobbuysell = dynamicObject.getString("ezob_buy_sell");
//                新增行
                    DynamicObject ent = ezobEntryentity2.addNew();
                    //                地址编码
                    ent.set("ezob_khdzbm",n);
//                        变更前是否VMI
                    ent.set("ezob_bgqsfvmi", ezobsvmif);
//                        变更前是否Buy Sell
                    ent.set("ezob_bgqsfbysell", ezobbuysell);

                    OperationServiceHelper.executeOperate("save","ezob_cwxg",new DynamicObject[]{dataEntity});
            }
        }
    }
}



