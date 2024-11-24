package kd.cosmic.rpt;

import kd.bos.algo.DataSet;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.ReportQueryParam;
import kd.sdk.plugin.Plugin;
/**
 * 描述: 报表取数，应收到期表
 * 开发者: 刘善强
 * 创建日期: 1期
 * 关键客户：李琼
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class Rptystz extends AbstractReportListDataPlugin implements Plugin {
    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {  
        //FilterInfo filterInfo = reportQueryParam.getFilter();
       // List<FilterItemInfo> tableHeadFilters = filterInfo.getTableHeadFilterItems();

             // 平衡表 账套类型，加组织 akmmv_prd akmmv_prd
            // 香港按当前会计期间汇率折算 ， 收款单 取 收款单日期对应的 期间汇率
            //【20241119】【ZOC】改为了调用存储过程，否则sql实在太长了
            String strsql="/*dialect*/ call akmmv_prd_fi_test.KDReportXYYEB();";
            DataSet dataSet;
            dataSet = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, strsql);
        return dataSet;
    }
}