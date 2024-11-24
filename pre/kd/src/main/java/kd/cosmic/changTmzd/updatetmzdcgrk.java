package kd.cosmic.changTmzd;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.AfterOperationArgs;

/**
 * 描述: 采购入库审核后更新条码主档结余数量
 * 开发者: 李四辉
 * 创建日期: 1期
 * 关键客户：马丙丙
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
public class updatetmzdcgrk extends AbstractOperationServicePlugIn {
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        try{
            //获取当前领料出库数据
            for (DynamicObject dy : e.getDataEntities()){
                //获取当前id
                String billId = String.valueOf(dy.getPkValue());
                //获取单据组织
                DynamicObject org = dy.getDynamicObject("org");
                String orgnumber = org.getString("number");
                //当为苏州工厂、南沙工厂、安博工厂时
                if(orgnumber.equals("LE0011") || orgnumber.equals("LE0012") || orgnumber.equals("LE0013") || orgnumber.equals("LE0014") || orgnumber.equals("LE0015")) {
                    //执行存储过程
                    String sql = "/*dialect*/call akmmv_prd_scm_test.KDUpdateBarcode(" + billId + ",1)";
                    DB.update(DBRoute.basedata, sql);
                }
            }
        }catch (Exception e1){
            this.operationResult.setMessage(e1.toString());
            return;
        }
    }
}
