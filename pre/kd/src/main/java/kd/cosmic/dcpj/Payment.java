package kd.cosmic.dcpj;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * 描述: 付款关联反写付款排程
 * 开发者: 钟有吉
 * 关键客户：熊艳菲
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */

public class Payment extends  AbstractOperationServicePlugIn {


    @Override
    public void endOperationTransaction(EndOperationTransactionArgs e) {
        int n=0;
        try {
//        进到循环，把数据循环一遍
            for (DynamicObject dy : e.getDataEntities()) {
                //获取单据编号
                Long billId = Long.valueOf(String.valueOf(dy.getPkValue()));
//                根据编号加载所需要的字段
                DynamicObject aBILL = BusinessDataServiceHelper.loadSingle(billId, "ezob_fkpcclbgd", "billno,ezob_fid,entryentity,entryentity.seq,entryentity.ezob_yhtkdh,entryentity.ezob_qytkdh,ezob_entryentity.ezob_yhcretor,ezob_entryentity.ezob_qycretor,cfm_loancontractbill,cfm_loancontractbill.textcreditor");//
//                分录entryentity获取付款关联的两个字段
                DynamicObjectCollection dynamicObjectCollection = aBILL.getDynamicObjectCollection("entryentity");
                //根据单据编号筛选
                QFilter q = new QFilter("billno", "=", aBILL.getString("ezob_fid"));
                //加载上游单据锯信息
                DynamicObject[] simpleBill = BusinessDataServiceHelper.load("psd_schedealbill", "ezob_entryentity,ezob_entryentity.seq,ezob_entryentity.ezob_yhtkdh,ezob_entryentity.ezob_qytkdh,ezob_entryentity.ezob_yhcretor,ezob_entryentity.ezob_qycretor,cfm_loancontractbill,cfm_loancontractbill.textcreditor", new QFilter[]{q});
                for (DynamicObject tmzddy : simpleBill) {
                    DynamicObjectCollection childEntity = tmzddy.getDynamicObjectCollection("ezob_entryentity");
                    //清空单据体数据
                    childEntity.clear();
                    for (DynamicObject dynamicObject : dynamicObjectCollection) {
                        //新增行
                        DynamicObject entry = childEntity.addNew();
                        entry.set("seq", dynamicObject.getLong("seq"));
                        entry.set("ezob_yhtkdh", dynamicObject.getDynamicObject("ezob_yhtkdh"));
                        entry.set("ezob_qytkdh", dynamicObject.getDynamicObject("ezob_qytkdh"));
                        DynamicObject doy = dynamicObject.getDynamicObject("ezob_yhtkdh");
                        DynamicObject doq = dynamicObject.getDynamicObject("ezob_qytkdh");
                        //根据单据编号筛选

                    if(doy!=null){
                        QFilter q1 = new QFilter("billno", "=", doy.getString("billno"));
                        //加载上游单据锯信息
                        DynamicObject[] simpleBill1 = BusinessDataServiceHelper.load("cfm_loancontractbill", "textcreditor", new QFilter[]{q1});
                        for (DynamicObject sb1 : simpleBill1) {
                            entry.set("ezob_qycretor", sb1.getString("textcreditor"));
                        }
                    }
                    if (doq!=null){
                        //根据单据编号筛选
                        QFilter q2 = new QFilter("billno", "=", doq.get(1));
                        //加载上游单据锯信息
                        DynamicObject[] simpleBill2 = BusinessDataServiceHelper.load("cfm_loancontractbill", "textcreditor", new QFilter[]{q2});
                        for (DynamicObject sb2 : simpleBill2) {
                            entry.set("ezob_yhcretor", sb2.getString("textcreditor"));

                    }
                        }
//                        获取银行债权人分录
                    }
                    SaveServiceHelper.save(new DynamicObject[]{tmzddy});
                }
            }
        }catch (Exception e1){
            this.operationResult.setMessage(e1.getMessage());
            return;
        }
    }
}











