package kd.cosmic.connector;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

//生成客户地址
public class UpdateAddress extends AbstractOperationServicePlugIn {
    @Override
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
//        try {
//        进到循环，把数据循环一遍
            for (DynamicObject dy : e.getDataEntities()) {
//                获取编码
                String number = dy.getString("number");
//                获取地址分录信息
                DynamicObjectCollection dz = dy.getDynamicObjectCollection("ezob_entryentity2");
                for (DynamicObject dynamicObject : dz) {
//                    变更后送货地点名称
                    String ezobBghshdzmc = dynamicObject.getString("ezob_bghshdzmc");
//                    变更后送货联系人名称
                    String ezobbghshlxrmc = dynamicObject.getString("ezob_bghshlxrmc");
                    //                    变更后贸易术语
                    DynamicObject ezobBghmysy = dynamicObject.getDynamicObject("ezob_bghmysy");
                    //                    变更后行政区划
                    DynamicObject ezobBghxzqh = dynamicObject.getDynamicObject("ezob_bghxzqh");
                    //                    变更后详细地址
                    String ezobbghxxdzdz = dynamicObject.getString("ezob_bghxxdzdz");
                    //                    变更后邮政编码
                    String ezobbghyzbmdz = dynamicObject.getString("ezob_bghyzbmdz");
//                    获取地址模型
                    DynamicObject load = BusinessDataServiceHelper.newDynamicObject("bd_address");
                    //                    给地址编号编码设置
//                    设置客户编码
//                    load.set("customer.number",number);
                    //                数据状态
                    load.set("status", "C");
//                使用状态
                    load.set("enable", 1);
//                    给编码赋值
                    load.set("number",number);
//                    给客户编码赋值
//                    load.set("customer.number",number);
//                    送货地点名称
                    load.set("ezob_shddmc",ezobBghshdzmc);
//                    送货联系人名称
                    load.set("name",ezobbghshlxrmc);
                    //                    贸易术语
                    load.set("hihn_tradeterms",ezobBghmysy);
                    //                    行政区划
                    load.set("admindivision",ezobBghxzqh);
                    //                    详细地址
                    load.set("detailaddress",ezobbghxxdzdz);
                    //                    邮政编码
                    load.set("zipcode",ezobbghyzbmdz);

                    OperationResult result = OperationServiceHelper.executeOperate("save","bd_address", new DynamicObject[]{load}, OperateOption.create());

            }


                }










                Object[] save = SaveServiceHelper.save(new DynamicObject[]{});
                        //返回
                        OperationResult result = OperationServiceHelper.executeOperate("save","bos_assistantdata_detail", new DynamicObject[]{}, OperateOption.create());
                    }


                }

//


//        }catch (Exception e1){
//            this.operationResult.setMessage(e1.getMessage());
//            return;
//



