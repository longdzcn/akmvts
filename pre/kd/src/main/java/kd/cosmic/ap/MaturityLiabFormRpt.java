//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package kd.cosmic.ap;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.orm.query.QFilter;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.fi.ap.util.DateUtils;

import java.util.Date;
import java.util.Iterator;

/**
 * 描述: 应付账龄表（新）,报表表体插件，新增了一些字段，具体字段可看下面代码标识
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：关敏婷
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */

public class MaturityLiabFormRpt extends AbstractReportFormPlugin {
    public MaturityLiabFormRpt() {
    }


    public void processRowData(String gridPK, DynamicObjectCollection rowData, ReportQueryParam queryParam) {
        Iterator var4 = rowData.iterator();
        String pk = "";
        try {

            while (var4.hasNext()) {
                DynamicObject row = (DynamicObject) var4.next();
                pk = row.getString("pk");
                if(pk.equals("0"))
                {
                    continue;
                }
                QFilter qFilter = new QFilter("id", "=", pk);

                DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle(pk, "ap_finapbill", "detailentry,detailentry.e_materialname,detailentry.materialname,detailentry.ezob_hth");

                DynamicObjectCollection entrys = dynamicObject.getDynamicObjectCollection("detailentry");


                for (DynamicObject entry : entrys) {

                    if (entry.get("materialname") != null && entry.get("materialname") != "") {
                        row.set("materialname", entry.get("materialname"));
                    }
                    if(entry.get("ezob_hth") != null && entry.get("ezob_hth") != "")
                    {
                        row.set("ezob_hth", entry.get("ezob_hth.number"));
                    }

                    break;
                }

                String selsql = "/*dialect*/select fvoucherid from akmmv_prd_fi_test.t_ai_daptracker where Fsourcebillid=" + pk;
                DataSet selDs = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, selsql);
                long fvoucherid = 0;

                while (selDs.hasNext()) {
                    Row roww = selDs.next();
                    fvoucherid = roww.getLong(0);
                }

                String selsql2 = "/*dialect*/select fnumber from  akmmv_prd_fi_test.t_gl_voucher where fid=" + fvoucherid;
                DataSet selDs2 = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, selsql2);
                String fnumber = "";
                while (selDs2.hasNext()) {
                    Row roww2 = selDs2.next();
                    fnumber = roww2.getString(0);
                    if(fnumber!="")
                    {
                        row.set("ezob_pzzh",fnumber);
                    }
                }





                Date duedate = row.getDate("duedate");
                Date date = queryParam.getFilter().getDate("date");
                if (duedate != null && date != null) {
                    row.set("duedays", DateUtils.getDiffDaysIgnoreTime(duedate, date));
                }
            }

            }catch(Exception e)
            {
                this.getView().showMessage("pk:" + pk+"错误:"+e.getMessage());
            }


        }
    }







