package kd.cosmic.rpt;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 描述: 报表取数
 * 开发者: 刘善强
 * 创建日期: 2024/11/01
 * 关键客户：
 * 已部署正式：false
 * 备注：
 */
public class Rptxjllb extends AbstractReportListDataPlugin implements Plugin {

    @Override
    public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {
        String FOrgNumber="",FItemNumber1="",FItemNumber2="",FBItem="";
        String FDate="";
        Boolean NChecked=false,GChecked=false,OtherChecked=false,YqChecked=false;
        FilterInfo filterInfo = reportQueryParam.getFilter();
        // 过滤面板的过滤参数 lsq6_qijian.name = 333 AND Fdate = 222 AND lsq6_cbillno = 111 AND Fdate = 222 AND lsq6_cbillno = 111
        FilterItemInfo filterorg = filterInfo.getFilterItem("ezob_org");
        FilterItemInfo filtertem1 = filterInfo.getFilterItem("ezob_item1");
        FilterItemInfo filtertem2 = filterInfo.getFilterItem("ezob_item2");

        FilterItemInfo filterdate = filterInfo.getFilterItem("ezob_date");
        FilterItemInfo filterncheck = filterInfo.getFilterItem("ezob_ncheck");
        FilterItemInfo filtergcheck = filterInfo.getFilterItem("ezob_gcheck");
        FilterItemInfo filterocheck= filterInfo.getFilterItem("ezob_ocheck");
        FilterItemInfo filteryqcheck= filterInfo.getFilterItem("ezob_yqonly");
        FilterItemInfo filterbitem = filterInfo.getFilterItem("ezob_bitem");
        

        List<FilterItemInfo> tableHeadFilters = filterInfo.getTableHeadFilterItems();
        QFilter resultFilter[] = new QFilter[tableHeadFilters.size()];

        if (filterorg != null && filterorg.getValue() instanceof DynamicObject) {
            DynamicObject dynamicObject = (DynamicObject) filterorg.getValue();
            FOrgNumber = dynamicObject.get("number").toString();
        }
        if (filtertem1 != null && filtertem1.getValue() instanceof DynamicObject) {
            DynamicObject dynamicObject = (DynamicObject) filtertem1.getValue();
            FItemNumber1 = dynamicObject.get("number").toString();
        }
        if (filtertem2 != null && filtertem2.getValue() instanceof DynamicObject) {
            DynamicObject dynamicObject = (DynamicObject) filtertem2.getValue();
            FItemNumber2 = dynamicObject.get("number").toString();
        }

        if (filterdate != null  ) {
            Date d=filterdate.getDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            FDate = sdf.format(d);
        }

        if (filterncheck != null ) {
            NChecked=filterncheck.getBoolean();
        }
        if (filtergcheck != null ) {
            GChecked=filtergcheck.getBoolean();
        }

        if (filterocheck != null ) {
            OtherChecked=filterocheck.getBoolean();
        }
        
        if (filteryqcheck != null ) {
        	YqChecked=filteryqcheck.getBoolean();
        }
        
        if (filterbitem != null && filterbitem.getValue() instanceof DynamicObject) {
            DynamicObject dynamicObject = (DynamicObject) filterbitem.getValue();
            FBItem = dynamicObject.get("number").toString();
        }
        String filter="";
        if(FOrgNumber.equals("")==false)filter+=" and tg.FNUMBER='"+FOrgNumber+"'";
        if(FItemNumber1.equals("")==false)filter+=" and tc.FNUMBER>='"+FItemNumber1+"'";
        if(FItemNumber2.equals("")==false)filter+=" and tc.FNUMBER<='"+FItemNumber2+"'";
        if(FBItem.equals("")==false)filter+=" and t3.FNUMBER='"+FBItem+"'";
        if(NChecked==false)filter+=" and ta.fbillstatus='C' ";
        if(GChecked==false)filter+=" and ta.fduedate<=('"+FDate+"')  ";
        if(YqChecked==true)filter+=" and LAST_DAY(ta.fduedate) <(now())";
        // 过滤条件 币别 应收单 添加按钮 更新到日期
        // '05.02.0011','01.01.0052','02.05.0019','01.01.0051','07.01.0010','04.01.0010','05.05.0006'

        if(OtherChecked==false)filter+="  and tc.FNUMBER not in('01.01.0001','01.01.0001-02','01.01.0001-03','01.01.0001-04','01.01.0001-05','01.01.0001-01','01.01.0179','FPC01','AK329','AK328','AK323','AK321','AK320','AK319','AK318','AK317','AK316','AK315','AK314','AK310','AJ320','02.05.0019','02.05.0019-1','R002','R001','AK304','AK312','AK306','AK311','AK303','AK307','AK308','AK309','AK305','AK322','AK326','AK324','AK325','AK331','AK327','AK313','AK330','0088','04.01.0010','04.0008','05.04.0028','01.01.0188','05.02.0011','01.01.0051','07.01.0010','01.01.0052','PRGME','PRFPC','03.02.0024','PRSME','03.02.0039','PRSPP','05.05.0006','04.0009','04.01.0025','FBZKH-0017','FBZKH-0002','FBZKH-0025','FBZKH-0005') " ;
// datediff(day, 部分 结算 要放出来 需要添加一个合计
        String strsql = "/*dialect*/"
        		+ " select ta.FBillNo,tg.FNUMBER as FOrgNumber,tgl.FNAME as FOrgName\r\n"
        		+ " ,rg.FNUMBER as FROrgNumber,rg1.FNAME as FROrgName\r\n"
        		+ " ,tc.FNUMBER as FCustNumber,tcl.FNAME as FCustName\r\n"
        		+ " ,case when IFNULL(ta1.finvoiceno,'')='' or IFNULL(ta1.finvoiceno,'')=' ' then ta.fk_ezob_fphm else ta1.finvoiceno end as FFAPIAO\r\n"
        		+ " ,ta.fduedate,t2.FNAME as FBIBIE \r\n"
        		+ "  ,LAST_DAY(ta.fduedate) FYQSJ\r\n"
        		+ " ,ta.fduedate FYQRQ\r\n"
        		+ "  ,case when LAST_DAY(ta.fduedate) <(date_format(now(), '%Y-%m-%d')) then '逾期' \r\n"
        		+ "  when LAST_DAY(ta.fduedate) >=(date_format(now(), '%Y-%m-%d')) and month(ta.fduedate)=month(date_format(now(), '%Y-%m-%d')) then '即将逾期' else '未逾期' end FSFYQ\r\n"
        		+ "  ,datediff(date_format(now(), '%Y-%m-%d'),LAST_DAY(ta.fduedate)) FYQTS\r\n"
        		+ " ,ta.fbizdate,ta.famount,ta.fremark,ta.funsettleamount funverifyamount\r\n"
        		+ " ,p1.FName FBillTypeName,ta.funsettleamount as FWHXJE -- 未核销金额 \r\n"
        		+ " ,ta.fk_ezob_dyfphm,tj.fname fpaycondname,bm.fname fdeptment,ch.fname fchfs,js.FNAME FJSFS,z2.FNumber FPZH,dx.FNAME FDDLX\r\n"
        		+ " from akmmv_prd_fi_test.t_ar_finarbill ta \r\n"
        		+ " inner join akmmv_prd_fi_test.t_ar_finarbill_e ta1 on ta.fid=ta1.fid \r\n"
        		+ " left join akmmv_prd_eip_test.t_org_org tg on tg.fid=ta.forgid \r\n"
        		+ " left join akmmv_prd_eip_test.t_org_org_l tgl on tgl.fid=ta.forgid and tgl.FLOCALEID='zh_CN' \r\n"
        		+ " left join akmmv_prd_eip_test.t_org_org rg on rg.fid=ta.frecorgid \r\n"
        		+ " left join akmmv_prd_eip_test.t_org_org_l rg1 on rg1.fid=ta.frecorgid and rg1.FLOCALEID='zh_CN' \r\n"
        		+ " left join akmmv_prd_eip_test.t_bd_customer tc on tc.fid=ta.fpaymentcustomerid \r\n"
        		+ " left join akmmv_prd_eip_test.t_bd_customer_l tcl on tcl.fid=ta.fpaymentcustomerid and tcl.FLOCALEID='zh_CN' \r\n"
        		+ " left join akmmv_prd_eip_test.T_BD_CURRENCY_L t2 on t2.fid=ta.fcurrencyid and t2.FLOCALEID='zh_CN' \r\n"
        		+ " left join akmmv_prd_eip_test.T_BD_CURRENCY t3 on t3.fid=ta.fcurrencyid \r\n"
        		+ " left join akmmv_prd_eip_test.T_BAS_BILLTYPE_L p1 on p1.fid=ta.fbilltypeid and p1.FLOCALEID='zh_CN' \r\n"
        		+ " left join akmmv_prd_eip_test.t_bd_reccondition tj on tj.fid=ta.fpaycond \r\n"
        		+ " left join akmmv_prd_eip_test.t_org_org_l bm on bm.fid=ta1.fdepartmentid and bm.flocaleid='zh_CN' \r\n"
        		+ " left join akmmv_prd_eip_test.t_bas_assistantdataentry ch on ch.fentryid=ta.fk_ezob_chfs \r\n"
        		+ "  left join akmmv_prd_eip_test.t_bas_assistantdataentry dx on dx.fentryid=ta.fk_ezob_ddlx\r\n"
        		+ " left join akmmv_prd_eip_test.t_bd_settlementtype_L js on js.fid=ta.fsettlementtypeid and js.flocaleid='zh_CN' \r\n"
        		+ " left join akmmv_prd_fi_test.t_ai_daptracker z1 on z1.fsourcebillid=ta.FID and z1.FBillType='ar_finarbill'\r\n"
        		+ " left join akmmv_prd_fi_test.t_gl_voucher z2 on z1.FVOucherID=z2.FID  \r\n"
        		+ " where 1=1  and (ta.funsettleamount>0 or ta.funsettleamount<0) "
                +filter ;
        strsql+="  group by  ta.FBillNo,tg.FNUMBER ,tgl.FNAME ,tc.FNUMBER ,tcl.FNAME ,ta.fk_ezob_fphm,ta1.finvoiceno  \r\n"
        		+ "  ,ta.fduedate ,t2.FNAME ,rg.FNUMBER ,rg1.FNAME ,ta.fbizdate,ta.famount,ta.fremark,ta.funverifyamount,p1.FName,ta.fk_ezob_dyfphm \r\n"
        		+ "  ,tj.fname,bm.fname,ch.fname,js.FNAME,ta.funsettleamount,z2.FNumber,dx.FNAME";


        DataSet dataSet;
        dataSet = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, strsql);
        return dataSet;
    }
}