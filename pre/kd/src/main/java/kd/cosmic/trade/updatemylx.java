package kd.cosmic.trade;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.AfterOperationArgs;

/**
 * 单据操作kd.cosmic.updatemylx
 * 领料出库审核前更新条码主档结余数量，因为如果放在审核后则无法触发自动下推的服务，如果放在审核中则会提示同一个事务不允许更新多数据库
 */
public class updatemylx extends AbstractOperationServicePlugIn {
//    //一定要预加载字段，否则审核后提示报错提示没有自定义字段实体类型tripentry中不存在名为ezob_kssj的属性
//    public void onPreparePropertys(PreparePropertysEventArgs e) {
//        List<String> fieldKeys = e.getFieldKeys();
//        fieldKeys.add("ezob_tmzd");
//    }

    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        try {
//            获取当前领料出库数据
           for (DynamicObject dy : e.getDataEntities()) {
                //获取当前id
                Long billId = Long.valueOf(String.valueOf(dy.getPkValue()));
                //获取单据组织
                DynamicObject org = dy.getDynamicObject("org");
                String orgnumber = org.getString("number");
                //当为黄埔工厂时、上海工厂、厦门工厂、厦门母公司、MTC时
                //【ZOC】【20240905】后续改为适用于所有组织
//                if (orgnumber.equals("LE0006") || orgnumber.equals("LE0003") || orgnumber.equals("LE0004") || orgnumber.equals("LE0005") || orgnumber.equals("LE0002") || orgnumber.equals("LE0001")) {
                    //执行存储过程
                    String sql = "/*dialect*/call akmmv_prd_scm_test.KDUpdateMYLX(" + String.valueOf(billId) + ")";
//                    DB.execute(DBRoute.basedata,sql);
//                    DataSet Ds = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, sql);
                    DB.update(DBRoute.basedata, sql);
//                }
            }
        } catch (Exception e1) {
            this.operationResult.setMessage(e1.toString());
            return;
        }
    }
}