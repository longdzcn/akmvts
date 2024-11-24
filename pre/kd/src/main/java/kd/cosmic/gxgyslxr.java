package kd.cosmic;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * 描述: 单据界面：更新供应商联系人
 * 开发者: 李四辉
 * 创建日期:2024-04-01
 * 关键客户：黄小清
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */

//kd.cosmic.gxgyslxr
public class gxgyslxr extends AbstractOperationServicePlugIn {
    public void endOperationTransaction(EndOperationTransactionArgs e) {
        try {
            for (DynamicObject dy : e.getDataEntities()) {
                Long billId = Long.valueOf(String.valueOf(dy.getPkValue()));//获取供应商变更单ID
                DynamicObject aBILL = BusinessDataServiceHelper.loadSingle(billId, "ezob_lxrbg1", "number");
                String fbnumber = aBILL.getString("number");//获取供应商变更单编码
                //更新供应商联系人
                String sql = "/*dialect*/ DELETE from T_BD_SupplierLinkMan where FEntryID in\n" +
                        "(SELECT FEntryID from\n" +
                        "(select  b.FEntryID FROM T_BD_Supplier a LEFT JOIN T_BD_SupplierLinkMan b \n" +
                        "on a.FID=b.FID\n" +
                        "where b.FContactPerson='' and fnumber='"+fbnumber+"')c);";
                DB.update(DBRoute.basedata, sql);
                //查询是否有新增供应商联系人
                String sqlxz = "/*dialect*/ select te.fk_ezob_bghmc as 变更后名称,te.fk_ezob_bghzw as 变更后职位," +
                        "te.fk_ezob_bghmverpzt as 变更后mvERP账套,te.fk_ezob_bghlxdh as 变更后联系电话," +
                        "te.fk_ezob_bghsjhm as 变更后手机号码,te.fk_ezob_bghyx as 变更后邮箱," +
                        "te.fk_ezob_bghsfjsdzpo as 变更后是否接收电子PO\n" +
                        "from tk_ezob_lxrbgentry t \n" +
                        "left join tk_ezob_lxrbgentry1 te on t.fid = te.fid \n" +
                        "where t.fnumber = '"+fbnumber+"' and t.fid = '"+billId+"' and te.fk_ezob_bgqmc = '';";
                DataSet ds = DB.queryDataSet("insertgyslxr",DBRoute.of("eip"),sqlxz);
                //查询对应供应商
                QFilter q = new QFilter("number", "=", fbnumber);
                DynamicObject[] simpleBill = BusinessDataServiceHelper.load("bd_supplier", "entry_linkman,contactperson,ezob_zw,ezob_mverpzt,phone,ezob_sjhm,email,ezob_sfjsdzpo1", new QFilter[] { q });
                DynamicObjectCollection colentity = simpleBill[0].getDynamicObjectCollection
                        ("entry_linkman");
                if(!ds.isEmpty()){
                    //遍历
                    while(ds.hasNext()) {
                        Row row = ds.next();
                        DynamicObject subRow = colentity.addNew();
                        subRow.set("contactperson", row.get(0).toString());//名称
                        subRow.set("ezob_zw", row.get(1).toString());//职位
                        subRow.set("ezob_mverpzt", row.get(2).toString());//mverp账套
                        subRow.set("phone", row.get(3).toString());//联系电话
                        subRow.set("ezob_sjhm", row.get(4).toString());//手机号码
                        subRow.set("email", row.get(5).toString());//邮箱
                        subRow.set("ezob_sfjsdzpo1", row.get(6).toString());//是否接收电子PO
                    }
                    SaveServiceHelper.save(simpleBill);
                }
            }
        } catch (Exception e1) {
            this.operationResult.setMessage(e1.toString());
            return;
        }
    }
}
