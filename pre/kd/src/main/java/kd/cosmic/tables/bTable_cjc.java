package kd.cosmic.tables;
import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.FilterInfo;
import kd.bos.entity.report.FilterItemInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.orm.query.QFilter;
import kd.sdk.plugin.Plugin;

import java.util.List;
/**
 *   车间仓条形码结余与账存比较
 */
public class bTable_cjc extends AbstractReportListDataPlugin implements Plugin {
    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        long fnumber= 0,fname= 0;
        String  cjcnumber="",cjcname="";
        FilterInfo filterInfo = reportQueryParam.getFilter();
        // 过滤面板的过滤参数
        //库存组织代码

        FilterItemInfo filterwldm= filterInfo.getFilterItem("ezob_kczz");

        List<FilterItemInfo> tableHeadFilters = filterInfo.getTableHeadFilterItems();

        QFilter resultFilter[] = new QFilter[tableHeadFilters.size()];

        if (filterwldm != null && filterwldm.getValue() instanceof DynamicObject) {
            DynamicObject dynamicObject = (DynamicObject) filterwldm.getValue();
            fnumber = (long) dynamicObject.getPkValue();  //getID
        }

        //车间仓
        FilterItemInfo filtercjcdm= filterInfo.getFilterItem("ezob_cjc");

        if (filtercjcdm != null && filtercjcdm.getValue() instanceof DynamicObject) {
            DynamicObject dynamicObject = (DynamicObject) filtercjcdm.getValue();
            cjcnumber =  dynamicObject.getString("number");  //getNumber
        }

        String strsql ="/*dialect*/CALL akmmv_prd_scm_test.KDRptCJCDZ("+fnumber+",'"+cjcnumber+"');";

        DataSet dataSet;
        dataSet = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, strsql);
        return dataSet;
    }
}