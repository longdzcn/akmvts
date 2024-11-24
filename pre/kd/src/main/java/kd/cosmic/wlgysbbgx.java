package kd.cosmic;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.BeforeOperationArgs;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.List;

/**
 * 描述: 物料供应商版本更新
 * 开发者: 李四辉
 * 创建日期:2024-04-01
 * 关键客户：仓库
 * 已部署正式：true
 * 备注：已投入正式环境使用，无问题
 */

public class wlgysbbgx extends AbstractOperationServicePlugIn implements Plugin {


    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        List<String> fieldKeys = e.getFieldKeys();
        fieldKeys.add("number");
        fieldKeys.add("ezob_wlfl");  //ezob_wlfl.masterid
        fieldKeys.add("name");
        fieldKeys.add("modelnum");
        fieldKeys.add("ezob_gg");
        fieldKeys.add("ezob_gys");
        fieldKeys.add("ezob_bsykgldbs");
        fieldKeys.add("ezob_bcbs");
    }


    @Override
    public void beforeExecuteOperationTransaction(BeforeOperationArgs e) {
        super.beforeExecuteOperationTransaction(e);

        String number = "";
        DynamicObject[] tripList = e.getDataEntities();
        for (DynamicObject entrydata : tripList) {
            number = entrydata.getString("number");
            Long wlfl = entrydata.getLong("ezob_wlfl.masterid");
            String name = entrydata.getString("name");
            String xh = entrydata.getString("modelnum");
            String gg = entrydata.getString("ezob_gg");
            String gys = entrydata.getString("ezob_gys");
            String kg = entrydata.getString("ezob_bsykgldbs");

            int bcbs = entrydata.getInt("ezob_bcbs");
            int index = number.indexOf("-");
            if (bcbs == 0) {
                String sql = "/*dialect*/ select fnumber from akmmv_prd_eip_test.t_bd_material \n" +
                        "WHERE fk_ezob_wlfl =" + wlfl + " and fname ='" + name + "' and FModel ='" + xh
                        + "' and fk_ezob_gg = '" + gg + "' and fk_ezob_gys='" + gys + "' and fk_ezob_bsykgldbs='" +
                        kg + "'";
                DataSet ds = DB.queryDataSet(wltbsf.class.getName(), DBRoute.of("eip"), sql);
                if (!ds.isEmpty()) {
                    Row row = ds.next();
                    this.operationResult.setMessage("与物料" + row.getString(0) + "的内容一致,操作失败");
                    e.setCancel(true);
                    return;
                }
            }
            char character = number.charAt(index + 1);
            //根据第一个点的位置 获得第二个点的位置
            if (character >= '0' && character <= '9') {
                index++;
            }
            char[] wlbm = number.substring(index + 1).toCharArray();

            for (char a : wlbm) {
                if (a > 'Z') {
                    this.operationResult.setMessage("供应商版本或者版本升级已经超过Z版本！");
                    e.setCancel(true);
                    return;
                }
            }
        }
    }

    @Override
    public void endOperationTransaction(EndOperationTransactionArgs e) {
        super.endOperationTransaction(e);
        DynamicObject[] tripList = e.getDataEntities();
        for (DynamicObject entrydata : tripList) {
            String number = "";
            number = entrydata.getString("number");
            QFilter qFilter = new QFilter("number", QCP.equals, number);
            DynamicObject fin = BusinessDataServiceHelper.loadSingle("bd_material",new QFilter[]{qFilter});
            fin.set("ezob_bcbs", 1);
            SaveServiceHelper.update(fin);
        }
    }
}