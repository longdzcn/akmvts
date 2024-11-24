package kd.cosmic;

import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.FilterInfo;
import kd.bos.entity.report.FilterItemInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.sdk.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RptYCLZLFXB extends AbstractReportListDataPlugin implements Plugin {
    public RptYCLZLFXB() {
    }

    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        String fnumber = "";
        Date start_time = new Date();
        Date end_time = new Date();
        FilterInfo filterInfo = reportQueryParam.getFilter();
        FilterItemInfo filterzzdm = filterInfo.getFilterItem("ezob_zzdm");
        if (filterzzdm != null && filterzzdm.getValue() instanceof DynamicObject) {
            DynamicObject dynamicObject = (DynamicObject)filterzzdm.getValue();
            fnumber = dynamicObject.get("number").toString();
        }

        FilterItemInfo filterkssj = filterInfo.getFilterItem("ezob_kssj");
        if (filterkssj != null) {
            start_time = (Date)filterkssj.getValue();
        }

        FilterItemInfo filterjssj = filterInfo.getFilterItem("ezob_jssj");
        if (filterjssj != null) {
            end_time = (Date)filterjssj.getValue();
        }

        String formatStr = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat outputFormat = new SimpleDateFormat(formatStr);
        String Date_start = outputFormat.format(start_time);
        String Date_end = outputFormat.format(end_time);
        String sql = "/*dialect*/call akmmv_prd_fi_test.KDReportCHDZD('" + fnumber + "','" + Date_start + "','" + Date_end + "')";
        DataSet dataSet = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, sql);
        return dataSet;
    }
}
