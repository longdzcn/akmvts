package kd.cosmic.connector;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;


/**
 * 描述: 财务相关f反写客户地址
 * 开发者: 钟有吉
 * 关键客户：keller
 * 已部署正式：ture
 * 备注：目前只在测试环境，还在测试阶段
 */
public class cwxgdzfx extends AbstractOperationServicePlugIn {
    public void endOperationTransaction(EndOperationTransactionArgs e) {

        for (DynamicObject dy : e.getDataEntities()) {
//        try {
//            获取单据id
            Long billId = Long.valueOf(String.valueOf(dy.getPkValue()));//获取客户变更单ID
//            获取财务相关
            DynamicObject bILL = BusinessDataServiceHelper.loadSingle(billId, "ezob_cwxg", "ezob_shdzbg,ezob_bghsfvmi,ezob_bghsfbysell,ezob_khdzbm");
            DynamicObjectCollection shdzbg = bILL.getDynamicObjectCollection("ezob_shdzbg");
            String number = bILL.getString("number");
            QFilter q = new QFilter("number", QCP.equals, number);
//            获取客户信息
            DynamicObject kh = BusinessDataServiceHelper.loadSingle("bd_customer","ezob_entry_khxx,ezob_zdkh,entry_bank,bankaccount,accountname,bank,currency,number,entry_linkman,contactperson,contactpersonpost,email,masterid",new QFilter[]{q});
            String idkh = kh.getString("masterid");
            for (DynamicObject dynamicObject : shdzbg) {
//                获取客户地址编码
                String ezobKhdzbm = dynamicObject.getString("ezob_khdzbm");
                QFilter q1 = new QFilter("number", QCP.equals, ezobKhdzbm);
//            获取客户地址客户地址
                DynamicObject[] load = BusinessDataServiceHelper.load("bd_address", "ezob_buy_sell,ezob_svmif", new QFilter[]{q1});
                //                变更后是否VMI
                String ezobbghsfvmi = dynamicObject.getString("ezob_bghsfvmi");
//                    变更后是否Buy Sell
                String ezobbghsfbysell = dynamicObject.getString("ezob_bghsfbysell");
//                判断客户地址是否存在
                if (load.length>0){
                    for (DynamicObject khdz : load) {
//                    变更后是否VMI
                        khdz.set("ezob_svmif",ezobbghsfvmi);
//                    变更后是否Buy Sell
                        khdz.set("ezob_buy_sell",ezobbghsfbysell);
                    }
                }else {
                    //                获取一个新的地址模型模型
                    DynamicObject addnew = BusinessDataServiceHelper.newDynamicObject("bd_address");
//                    获取客户地址编码
                    String ezobKhdzbm1 = dynamicObject.getString("ezob_khdzbm");
                    //                数据状态
                    addnew.set("status", "C");
//                使用状态
                    addnew.set("enable", 1);
//                    给编码赋值
                    addnew.set("number",ezobKhdzbm1);
//                    给客户编码赋值
                    addnew.set("customer",kh.getPkValue());
//                    给客户id赋值
                    addnew.set("customerid",idkh);
//                    是否是客户
                    addnew.set("iscustomeradd",1);
//                    变更后是否VMI
                    addnew.set("ezob_svmif",ezobbghsfvmi);
//                    变更后是否Buy Sell
                    addnew.set("ezob_buy_sell",ezobbghsfbysell);

                    Object[] save = SaveServiceHelper.save(new DynamicObject[]{addnew});

                }

                SaveServiceHelper.save(load);

            }


//        }catch (Exception e1){
//            this.operationResult.setMessage(e1.getMessage());
//            return;
//        }

        }



    }



}
