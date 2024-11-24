package kd.cosmic.connector;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;

/**
 * 单据操作插件
 */
public class khcwxgxgyhxx extends AbstractOperationServicePlugIn implements Plugin {

    private static Log log = LogFactory.getLog(khfltjxdkhfx.class);
    public void endOperationTransaction(EndOperationTransactionArgs e) {

        for (DynamicObject dy : e.getDataEntities()) {

            String khid = null;
//            获取单据id
            Long billId = Long.valueOf(String.valueOf(dy.getPkValue()));//获取客户变更单ID
//            获取客户分类添加下单客户数据
            DynamicObject aBILL = BusinessDataServiceHelper.loadSingle(billId, "ezob_cwxgzdzl", "ezob_bgqyhzh,ezob_bghyhzh,ezob_bgqzhmc,ezob_bghzhmc,ezob_bgqkhyh,ezob_bghkhyh,ezob_bgqbb,ezob_bghbb,ezob_entryentity");
//            获取客户编码
            String number = aBILL.getString("number");
            QFilter q = new QFilter("number", QCP.equals, number);

//            获取客户信息
            DynamicObject kh = BusinessDataServiceHelper.loadSingle("bd_customer","ezob_entry_khxx,ezob_zdkh,entry_bank,phone,bankaccount,accountname,bank,currency,number,contactperson,contactpersonpost,email,masterid",new QFilter[]{q});

//            获取终端客户分录
//            DynamicObjectCollection ezobEntryKhxx = kh.getDynamicObjectCollection("ezob_entry_khxx");
//           终端客户变更分录
//            DynamicObjectCollection ezobEntryentity1 = aBILL.getDynamicObjectCollection("ezob_entryentity1");
//            送货地址变更
//            DynamicObjectCollection ezobentryentity2 = aBILL.getDynamicObjectCollection("ezob_entryentity2");
//            清空终端客户分录数据
//            ezobEntryKhxx.clear();
//            for (DynamicObject dynamicObject : ezobEntryentity1) {
////                变更后终端客户
////                String ezobBghzdkh = dynamicObject.getString("ezob_bghzdkh.number");
//                DynamicObject ezobBghzdkh = dynamicObject.getDynamicObject("ezob_bghzdkh");
////                遍历终端客户分录
//                DynamicObject add = ezobEntryKhxx.addNew();
////                反写终端客户
//                add.set("ezob_zdkh",ezobBghzdkh);
//                Object o = add.get("ezob_zdkh");
//            }
            DynamicObjectCollection ezobKhyhxx = aBILL.getDynamicObjectCollection("ezob_entryentity");
//            获取开户银行信息分录
            DynamicObjectCollection entryBank = kh.getDynamicObjectCollection("entry_bank");
//            清除开户银行信息分录
            entryBank.clear();
            for (DynamicObject khyhxx : ezobKhyhxx) {
//                变更后银行账号
                String ezobBghyhzh = khyhxx.getString("ezob_bghyhzh");
//                变更后账户名称
                String ezobBghzhmc = khyhxx.getString("ezob_bghzhmc");
//                变更后开户银行
//                String ezobBghkhyh = khyhxx.getString("ezob_bghkhyh.number");
                DynamicObject ezobBghkhyh = khyhxx.getDynamicObject("ezob_bghkhyh");
//                变更后币别
//                String ezobBghbb = khyhxx.getString("ezob_bghbb.number");
                DynamicObject ezobBghbb = khyhxx.getDynamicObject("ezob_bghbb");
//                获取新增行
                DynamicObject add = entryBank.addNew();
//                反写银行账号
                add.set("bankaccount",ezobBghyhzh);
                Object o = add.get("bankaccount");
//                反写账户名称
                add.set("accountname",ezobBghzhmc);
                Object p = add.get("accountname");
//                反写开户银行
                add.set("bank",ezobBghkhyh);
                Object t = add.get("bank");
//                反写币别
                add.set("currency",ezobBghbb);
                Object o1 = add.get("currency");
            }
//            OperationServiceHelper.executeOperate("save","ezob_khbgd",new DynamicObject[]{kh});


//
            SaveServiceHelper.save(new DynamicObject[]{kh});
        }
    }
}