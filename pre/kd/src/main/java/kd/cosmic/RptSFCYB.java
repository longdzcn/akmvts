package kd.cosmic;

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
 * 描述: 报表取数_思方库存对账表
 * 开发者: 易佳伟
 * 创建日期:
 * 关键客户：
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
public class RptSFCYB extends AbstractReportListDataPlugin implements Plugin {

    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        String fnumber="",fname="";

        FilterInfo filterInfo = reportQueryParam.getFilter();
        // 过滤面板的过滤参数
        FilterItemInfo filterorgnumber= filterInfo.getFilterItem("ezob_orgnumber");

        List<FilterItemInfo> tableHeadFilters = filterInfo.getTableHeadFilterItems();
        QFilter resultFilter[] = new QFilter[tableHeadFilters.size()];

        if (filterorgnumber != null && filterorgnumber.getValue() instanceof DynamicObject) {
            DynamicObject dynamicObject = (DynamicObject) filterorgnumber.getValue();
            fnumber = dynamicObject.get("number").toString();
        }

//        String filter="";
//        if(fnumber.equals("")==false)filter+=" and tb.FNUMBER='"+fnumber+"'";

        String strsql = "/*dialect*/call akmmv_prd_scm_test.KDKCCYB('"+fnumber+"')";

        DataSet dataSet;
        dataSet = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, strsql);
        return dataSet;
    }
}