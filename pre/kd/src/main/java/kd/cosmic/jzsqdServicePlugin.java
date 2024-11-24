package kd.cosmic;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.AfterOperationArgs;

/**
 * 描述: 记账申请单，根据规则更新成本中心。由于从myhr同步过来的工资记账申请单不愿意推送成本中心，只能在金蝶这边根据规则更新成本中心
 * 开发者: 江伟维
 * 创建日期:
 * 关键客户：仓库
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */

public class jzsqdServicePlugin extends AbstractOperationServicePlugIn {

    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        try {
            //获取当前领料出库数据
            for (DynamicObject dy : e.getDataEntities()) {
                //获取当前id
                Long billId = Long.valueOf(String.valueOf(dy.getPkValue()));
                //获取单据组织
//                DynamicObject org = dy.getDynamicObject("org");
//                String orgnumber = org.getString("number");
                //当为黄埔工厂时、上海工厂、厦门工厂、厦门母公司、MTC时
//                if (orgnumber.equals("LE0006") || orgnumber.equals("LE0003") || orgnumber.equals("LE0004") || orgnumber.equals("LE0005") || orgnumber.equals("LE0002") || orgnumber.equals("LE0001")) {
                    //执行存储过程
                    String sql = "/*dialect*/call akmmv_prd_fi_test.KDUpdateCostCenter(" + String.valueOf(billId) + ")";
                    DB.update(DBRoute.basedata, sql);
//                }
            }
        } catch (Exception e1) {
            this.operationResult.setMessage(e1.toString());
            return;
        }
    }
}
