package kd.cosmic.click;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * 描述: 采购收货单审核后自动带出集团流水码
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：丁建华，顾问：夏晓君
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
public class auditJTLSM extends AbstractOperationServicePlugIn {


    public void onPreparePropertys(PreparePropertysEventArgs e) {

        // 需要被使用到的字段
        e.getFieldKeys().add("biztype");
        e.getFieldKeys().add("billentry");
        e.getFieldKeys().add("ezob_checkboxfield2");
        e.getFieldKeys().add("ezob_jtlsm");
        e.getFieldKeys().add("ezob_zclb1");

    }


    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        DynamicObject[] dynamicObject = e.getDataEntities();
        for (DynamicObject dynamic : dynamicObject) {

            String name = dynamic.getDynamicObject("biztype").getString("name");

                if (name.equals("资产类采购")) {
                    //获取单据体信息
                    DynamicObjectCollection entrys = dynamic.getDynamicObjectCollection("billentry");
                    for (DynamicObject entry : entrys) {
                        boolean zsb = entry.getBoolean("ezob_checkboxfield2");
                        String lsm = entry.getString("ezob_jtlsm");
                        DynamicObject azcType = entry.getDynamicObject("ezob_zclb1");
                        String zcType = null;
                        if (azcType != null) {
                            zcType = azcType.getString("name");
                        }
                        if (zsb == true && lsm == "")  //判断主设备是否选中
                        {
                            int success = 0;//用于判断流水码值是否+1修改成功
                            String inssql = "/*dialect*/update akmmv_prd_scm_test.t_KD_ICCardNoBasis set FserNo=FserNo+1 where FNumber =1"; //执行的SQL
                            if ("无形资产".equals(zcType) && zcType != null) {
                                inssql = "/*dialect*/update akmmv_prd_scm_test.t_KD_ICCardNoBasis set FWNumber=FWNumber+1 where FNumber =1";
                            }

                            success = DB.update(DBRoute.basedata, inssql);


                            String selsql = "/*dialect*/SELECT FSerNo FROM akmmv_prd_scm_test.t_KD_ICCardNoBasis WHERE FNumber ='1' and FValue=1";
                            if ("无形资产".equals(zcType) && zcType != null) {
                                selsql = "/*dialect*/SELECT FWNumber FROM akmmv_prd_scm_test.t_KD_ICCardNoBasis WHERE FNumber ='1' and FValue=1";
                            }
                            DataSet selDs = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, selsql);
                            String value = "";
                            String lsmValue = "";
                            while (selDs.hasNext()) {
                                Row row = selDs.next();
                                value = row.getString(0);
                                int intValue = Integer.parseInt(value);
                                if ("无形资产".equals(zcType) && zcType != null) {
                                    lsmValue = String.format("W%08d", intValue);

                                } else {
                                    lsmValue = String.format("L%08d", intValue);

                                }

                            }
                            if (success > 0) {
                                entry.set("ezob_jtlsm", lsmValue);
                                SaveServiceHelper.update(dynamic);

                            }


                        }
                    }
                }

        }
    }
}
