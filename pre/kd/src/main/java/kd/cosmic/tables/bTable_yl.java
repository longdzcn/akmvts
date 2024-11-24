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
 * 描述: 原料条形码结余与账存比较
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：马丙丙
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class bTable_yl extends AbstractReportListDataPlugin implements Plugin {
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

//        String filter="";
//        if(fnumber.equals("")==false)filter+=" and tb.FNUMBER='"+fnumber+"'";
//
//        String strsql = "/*dialect*/select fnumber as kdxt_fnumber,fname as kdxt_fmodel from dev_3_sys.public.T_BD_Material tb \n" +
//                " where 1=1 \n"
//                +filter ;
        String strsql ="/*dialect*/CALL akmmv_prd_scm_test.KDRptYLTXMDZ("+fnumber+",'"+cjcnumber+"');";

        DataSet dataSet;
        dataSet = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, strsql);
        return dataSet;
    }
}
