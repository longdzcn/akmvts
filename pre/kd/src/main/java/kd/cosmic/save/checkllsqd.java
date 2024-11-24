package kd.cosmic.save;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;

/**
 * 描述: 料出库审核前更新条码主档结余数量，因为如果放在审核后则无法触发自动下推的服务，如果放在审核中则会提示同一个事务不允许更新多数据库
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：马丙丙
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class checkllsqd extends AbstractOperationServicePlugIn {
    public void endOperationTransaction(EndOperationTransactionArgs e){
        try{
            //获取当前领料出库数据
            for (DynamicObject dy : e.getDataEntities()){
                //获取当前id
                Long billId = Long.valueOf(String.valueOf(dy.getPkValue()));
                //获取单据组织
                DynamicObject org = dy.getDynamicObject("org");
                String orgnumber = org.getString("number");
                //当为苏州工厂、南沙工厂、安博工厂时
                if(orgnumber.equals("LE0011") || orgnumber.equals("LE0012") || orgnumber.equals("LE0013") || orgnumber.equals("LE0014") || orgnumber.equals("LE0015")) {
                    //执行存储过程
                    String sql = "/*dialect*/select t.fk_ezob_txm as 条形码,t.fk_ezob_iqty as 条码结余数量,t.FQty as 出库总数量,t.FCY as 差异数量 from (\n" +
                            "SELECT tme.fk_ezob_iqty,SUM(tme.fk_ezob_cksl) AS FQty,\n" +
                            "\ttme.fk_ezob_iqty-SUM(tme.fk_ezob_cksl) AS FCY,\n" +
                            "\ttme.fk_ezob_tmzd,tme.fk_ezob_txm\n" +
                            "FROM tk_ezob_mreqbillentrysub tme\n" +
                            "left join t_im_mreqbillentry tm on tme.FEntryId=tm.FENTRYID\n" +
                            "WHERE tm.FID = ?\n" +
                            "GROUP BY tme.fk_ezob_iqty,tme.fk_ezob_tmzd,tme.fk_ezob_txm\n" +
                            ") t where t.FCY<0";
                    //测试
//                    String sql = "/*dialect*/update dev_3_scm.PUBLIC.t_bd_barcodemain set fname='测试'";
                    Object[] params = {String.valueOf(billId)};
                    try (DataSet ds = DB.queryDataSet(this.getClass().getName(), DBRoute.of("scm"), sql, params)) {
                            while (ds.hasNext()) {
                                Row row = ds.next();
                                String err;
                                err="保存成功！但是注意超出条形码结余数量。条形码["+row.get(0)+"]结余数量为["+row.get(1)+"],出库总数量为["+row.get(2)+"],差异数量为["+row.get(3)+"]，请重新修改后再保存";
//                                throw new KDException(err); // 抛出中断型异常，数据自动回滚

                                this.operationResult.setMessage(err);
                            }
                        }
                }
            }
        }catch (Exception e1){
            this.operationResult.setMessage(e1.toString());
            return;
        }
    }
}
