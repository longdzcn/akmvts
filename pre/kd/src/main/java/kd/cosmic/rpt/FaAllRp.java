package kd.cosmic.rpt;

import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.FilterInfo;
import kd.bos.entity.report.FilterItemInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.sdk.plugin.Plugin;

/**
 * 描述: 固定资产全流程表单取数，全流程报表查询插件
 * 开发者: 易佳伟，李四辉（有总部老师参与提供代码）
 * 创建日期: 2024/07/22
 * 关键客户：黄淑玲，刘雨婕，张薇
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

public class FaAllRp extends AbstractReportListDataPlugin implements Plugin {

    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        String fOrgNumber="",pobillno="";


        FilterInfo filterInfo = reportQueryParam.getFilter();
        //po单号
        FilterItemInfo filterpobillno = filterInfo.getFilterItem("ezob_pobillno");

        if(filterpobillno!=null)
        {
            pobillno = filterpobillno.getValue().toString();
        }
        //组织
        FilterItemInfo filterorg = filterInfo.getFilterItem("ezob_org");
        if (filterorg != null && filterorg.getValue() instanceof DynamicObject) {
            DynamicObject dynamicObject = (DynamicObject) filterorg.getValue();
            fOrgNumber = dynamicObject.get("number").toString();
        }

        String filter ="";
        if(pobillno.equals("")==false)filter+=" and PO.`PO-单据编码`='"+pobillno+"'";
        if(fOrgNumber.equals("")==false)filter+=" and PO.PO.`组织编码`='"+fOrgNumber+"'";

        String filter2 ="";
        if(pobillno.equals("")==false)filter2+=" and t2.fmainbillnumber='"+pobillno+"'";

        String strsql = "/*dialect*/"
        +"select *,PT.`付款处理-付款金额`/PO.`PO-价税合计(表头)` as '付款处理-已支付比例',\n" +
                "\t\t\tPO.`PO-价税合计`*(PT.`付款处理-付款金额`/PO.`PO-价税合计(表头)`) as '付款处理-付款金额2'\n" +
                "\n" +
                "from(\n" +
                "\n" +
                "select PO.fbillno as 'PO-单据编码',PO_ENTRY.fk_ezob_ms as 'PO-描述',PO.fbiztime as 'PO-单据日期',\n" +
                "\t\t\t PO.fsettlecurrencyid as 'PO-结算币别',sum(PO_ENTRY.fqty) as 'PO-数量',\tPO_ENTRY.fprice as 'PO-单价',\n" +
                "\t\t\t PO_ENTRY.fcuramountandtax as 'PO-价税合计(本位币)',sum(PO_ENTRY.famountandtax) as 'PO-价税合计',\n" +
                "\t\t\t PO.ftotalallamount as 'PO-价税合计(表头)',PO.fexchangerate as 'PO-汇率',\n" +
                "\t\t\t PO_ENTRY.ftaxrateid as 'PO-税率',PO_ENTRY.fcuramount as 'PO-金额(本位币)',PO.fcurrencyid as 'PO-本位币',\n" +
                "\t\t\t PO.fpayconditionid as 'PO-付款条件',PO.fcomment as 'PO-备注',tg.FNUMBER as '组织编码',PO.fbilltypeid as '单据类型'\n" +
                "from  akmmv_prd_scm_test.t_pm_purorderbill PO \n" +
                "INNER JOIN akmmv_prd_scm_test.t_pm_purorderbillentry PO_ENTRY ON PO.FID = PO_ENTRY.FID    \n" +
                "left join akmmv_prd_eip_test.t_org_org tg on tg.fid=PO.forgid    \n" +
                "where PO.fbilltypeid=545330973332499456\n" +
                "GROUP BY PO.fbillno,PO_ENTRY.fk_ezob_ms\n" +
                "-- 采购订单数据集\n" +
                ")   as PO  \n" +
                "-- --------------------------------------------------------------------------------------------------------------------------------\n" +
                "left JOIN (\n" +
                "select t3.fbillno as '采购收货-单据编码',t3.FID,t3.fk_ezob_sjjzrq as '采购收货-实际业务日期',t2.fmainbillnumber as '采购收货-核心单据号',\n" +
                "\t\t\t sum(t1.fqty) as '采购收货-数量',t1.fk_ezob_ms as '采购收货-描述',t1.fprice as '采购收货-单价',\n" +
                "\t\t\t t1.famountandtax as '采购收货-价税合计(本位币)',t3.fexchangerate as '采购收货-汇率',sum(t1.fcuramountandtax) as all_jsh,\n" +
                "\t\t\t sum(t1.famount) as '采购收货-金额（本位币）',t1.fk_ezob_checkboxfield2 as '主设备',t1.ftaxrateid as '采购收货-税率',\n" +
                "\t\t\t t1.fk_ezob_zclb1 as '采购收货-资产类别',IF(t1.fk_ezob_jtlsm  IS NOT NULL AND t1.fk_ezob_jtlsm  != '', GROUP_CONCAT(t1.fk_ezob_jtlsm SEPARATOR '/'),t1.fk_ezob_jtlsm) as '采购收货-资产卡片编码',Max(TR.FREALACCOUNTDATE) as '采购收货-转固日期',\n" +
                "\t\t\t t2.fremainpurqty * t1.fprice as '采购收货-在建工程账面金额',t1.fk_ezob_lb1 as '类别'  from \n" +
                "       akmmv_prd_scm_test.t_im_purrecbillentry  t1\n" +
                "left join akmmv_prd_scm_test.t_im_purrecbillentry_r t2 on t1.FENTRYID = t2.FENTRYID\n" +
                "left join akmmv_prd_scm_test.t_im_purrecbill t3 on t1.FID =t3.FID\n" +
                "left join akmmv_prd_fi_test.T_FA_CARD_REAL TR on TR.FNumber = t1.fk_ezob_jtlsm\n" +
                "left join akmmv_prd_scm_test.t_im_purrecbillentry_r t4 on t4.FENTRYID =t2.FENTRYID\n" +
                "\n" +
                "GROUP BY t2.fmainbillnumber,t1.fk_ezob_ms) as PC ON PC.`采购收货-核心单据号` = PO.`PO-单据编码` and PO.`PO-描述` =PC.`采购收货-描述` -- 关联采购收货单集合\n" +
                "-- --------------------------------------------------------------------------------------------------------------------------------\n" +
                "left join (\n" +
                "select fk_ezob_hxdh1,fk_ezob_ms,sum(fk_ezob_jebwb) as '验收报告金额' from akmmv_prd_fi_test.tk_ezob_gdzcyshgbgentry ) \n" +
                "\t\t\t as TT on TT.fk_ezob_hxdh1 = PO.`PO-单据编码` and PO.`PO-描述` =TT.fk_ezob_ms -- 关联验收报告\n" +
                "-- --------------------------------------------------------------------------------------------------------------------------------\n" +
                "left join (\n" +
                "select t1.fbillno,t1.FActPayAmount as '付款处理-付款金额',t1.FLocalAmount as '付款处理-付款金额本位币',t2.fcorebillno \n" +
                "from akmmv_prd_fi_test.T_CAS_PaymentBill t1 inner join  akmmv_prd_fi_test.T_CAS_PaymentBillEntry t2\n" +
                "on t1.FID =t2.FID) as PT on PT.fcorebillno = PO.`PO-单据编码`-- 关联付款处理 \n" +
                "-- --------------------------------------------------------------------------------------------------------------------------------\n" +
                "left JOIN (\n" +
                "select t1.fk_ezob_ms,t2.fmainbillnumber,sum(TD.fassetvalue)as 'local账簿-清理资产原值' ,\n" +
                "           sum(TD.fpreresidualval) as 'local账簿-清理资产残值',sum(TD.faddupdepre) as 'local账簿-清理累计折旧',\n" +
                "\t\t\t\t\t ZC.*\n" +
                "from akmmv_prd_scm_test.t_im_purrecbillentry  t1\n" +
                "left join akmmv_prd_scm_test.t_im_purrecbillentry_r t2 on t1.FENTRYID = t2.FENTRYID\n" +
                "left join akmmv_prd_fi_test.T_FA_CARD_REAL TR on TR.FID = t1.fk_ezob_km\n" +
                "left join akmmv_prd_fi_test.T_FA_CLRBILLENTRY_D TD on TD.FREALCARDID =t1.fk_ezob_km\n" +
                "-- ---------------------------------------------------\n" +
                "left Join (\n" +
                "select t1.fbizdate as '处置单-业务日期',sum(t2.fk_ezob_czsr) as '处置单-处置收入' ,\n" +
                "\t\t\t     t2.fk_ezob_kpsl as '处置单-开票税率',sum(t2.fk_ezob_czsr)*(1+t3.ftaxrate) as '处置单-出售金额（含税）',\n" +
                "\t\t\t\t\t t1.fbuyerid as '处置单-购买方名称',t1.fbuyertypeid as '处置单-购买方类型',t2.FREALCARDID\n" +
                "\t\t\t\t\t from akmmv_prd_fi_test.t_fa_disposal t1\n" +
                "left join akmmv_prd_fi_test.t_fa_disposal_detail t2 on t1.FID=t2.FID\n" +
                "left join akmmv_prd_eip_test.t_bd_taxrate t3  on t3.FID = t2.fk_ezob_kpsl\n" +
                "GROUP BY t2.FID) ZC on ZC.FREALCARDID = TD.FREALCARDID  -- 清理单关联处置单\n" +
                "where  TD.FDEPREUSEID=418714318096331776\n" +
                "GROUP BY t1.fk_ezob_ms) as PD ON PD.fmainbillnumber =PO.`PO-单据编码`-- 根据关联采购收货单集合再关联清理单-- 目前只要local的信息\n" +
                "-- --------------------------------------------------------------------------------------------------------------------------------\n" +
                "where 1=1\n"+filter+
                "group by PO.`PO-单据编码`,PO.`PO-描述`";


        DataSet dataSet;
        dataSet = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, strsql);
        return dataSet;
    }
}