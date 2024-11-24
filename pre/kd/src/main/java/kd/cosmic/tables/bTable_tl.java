
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
 * 描述: 退料单条形码差异
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：马丙丙
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class bTable_tl extends AbstractReportListDataPlugin implements Plugin {
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




        String strsql ="/*dialect*/select ti.FNumber as ezob_wldm,ti.fname as ezob_wlmc,ti.fmodel as ezob_ggxh,pe.FLOTNUMBER as ezob_ph,\n" +
                "\tper.fsrcbillnumber as ezob_sldh,\n" +
                "\tpe.fqty as ezob_tlsl,te.FOQty as ezob_xlhsl,abs(pe.fqty)-te.FOQty as ezob_cysl \n" +
                "From akmmv_prd_scm_test.t_im_purrecbillentry pe\n" +
                "left join akmmv_prd_scm_test.t_im_purrecbillentry_r per on pe.fentryid=per.fentryid\n" +
                "left join akmmv_prd_scm_test.t_im_purrecbill p on pe.FID=p.FID\n" +
                "inner join (select FBILLCODE,flot,SUM(fk_ezob_cksl) as FOQty from akmmv_prd_scm_test.T_BD_BARCODEMAIN\n" +
                "\t\tgroup by FBILLCODE,flot\n" +
                "\t) te on te.FBILLCODE=per.fsrcbillnumber and pe.FLOTNUMBER=te.flot\n" +
                "left join akmmv_prd_eip_test.T_BD_MATERIAL ti on ti.FID=pe.FMATERIALMASTERID\n" +
                "where pe.fqty<0 and abs(pe.fqty)>te.FOQty and p.forgid='"+fnumber+"'\n" +
                "order by per.fsrcbillnumber";


        DataSet dataSet;
        dataSet = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, strsql);
        return dataSet;
    }
}
