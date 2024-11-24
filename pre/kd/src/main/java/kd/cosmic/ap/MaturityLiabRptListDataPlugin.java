package kd.cosmic.ap;

import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.FilterInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.fi.ap.helper.OrgHelper;
import kd.fi.arapcommon.report.ReportHelper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 描述: 到期债务表新增发票号码、结算方式、付款条件、凭证号、物料名称（首行物料名称，即可凭证摘要）这五个字段
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：关敏婷
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */

public class MaturityLiabRptListDataPlugin extends AbstractReportListDataPlugin {
    private FilterInfo filterInfo;
    private static final Log logger = LogFactory.getLog(MaturityLiabRptListDataPlugin.class);
  
  public DataSet query(ReportQueryParam queryParam, Object selectedObj) throws Throwable {
    this.filterInfo = queryParam.getFilter();
    String billscope = this.filterInfo.getString("billscope");
    try {
      DataSet ds = null;
      if ("FIN".equals(billscope)) {
        ds = getFinDataSet();
      } else if ("BUS".equals(billscope)) {
      
      } 
      return ds;
    } catch (Exception e) {
      logger.error(e);
      throw e;
    } 
  }

/*  protected DataSet getFinDataSet() {
    DataSet dataSet = QueryServiceHelper.queryDataSet("Query.Fin", "ap_finapbill", "id pk,isperiod,org orgcol,asstacttype asstacttypecol,asstact asstactcol,currency currencycol,payorg,billtypeid billtype,billno,billstatus,bizdate,duedate,pricetaxtotal,unsettleamount,detailentry.lockedamt lockedamt,detailentry.settledamt settledamt,0 dueamt,0 dueunverifyamt,0 duenoninvamt,remark", this.getFinQueryFilter().toArray(), "bizdate,duedate");
    dataSet = dataSet.groupBy(new String[]{"pk", "isperiod", "orgcol", "asstacttypecol", "asstactcol", "currencycol", "payorg", "billtype", "billno", "billstatus", "bizdate", "duedate", "pricetaxtotal", "unsettleamount", "dueamt", "dueunverifyamt", "duenoninvamt", "remark"}).sum("lockedamt").sum("settledamt").finish();
    dataSet = dataSet.addField("lockedamt-settledamt", "topayamt");
    dataSet = dataSet.filter("orgcol>0");
    dataSet = dataSet.addField("0", "sumlevel");
    DataSet sumDataSet = dataSet.groupBy().sum("pricetaxtotal").sum("unsettleamount").sum("topayamt").finish();
    sumDataSet = sumDataSet.select(new String[]{"null pk", "null isperiod", "null orgcol", "null asstacttype", "null asstact", "null currencycol", "null payorg", "null billtype", "'' billno", "'' billstatus", "null bizdate", "null duedate", "pricetaxtotal", "unsettleamount", "null dueamt", "null dueunverifyamt", "null duenoninvamt", "'' remark", "null lockedamt", "null settledamt", "topayamt", "2 sumlevel"});
    dataSet = dataSet.union(sumDataSet);
    dataSet = dataSet.orderBy(new String[]{"sumlevel", "orgcol", "currencycol"});
    return dataSet;
  }*/

  protected DataSet getFinDataSet() {


    DataSet dataSet = QueryServiceHelper.queryDataSet("Query.Fin", "ap_finapbill", "id pk,isperiod,org orgcol,asstacttype asstacttypecol,asstact asstactcol,currency currencycol,payorg,billtypeid billtype,billno,billstatus,bizdate,duedate,pricetaxtotal,unsettleamount,detailentry.lockedamt lockedamt,detailentry.settledamt settledamt,0 dueamt,0 dueunverifyamt,0 duenoninvamt,remark,ezob_fphm,settlementtype,paycond,detailentry.materialname,receivingsupplierid",getFinQueryFilter().toArray(), "bizdate,duedate");
    dataSet = dataSet.groupBy(new String[] {"pk", "isperiod", "orgcol", "asstacttypecol", "asstactcol", "currencycol", "payorg", "billtype", "billno", "billstatus", "bizdate", "duedate", "pricetaxtotal", "unsettleamount", "dueamt", "dueunverifyamt", "duenoninvamt", "remark","ezob_fphm","settlementtype","paycond","detailentry.materialname","receivingsupplierid"}).sum("lockedamt").sum("settledamt").finish();
    dataSet = dataSet.addField("lockedamt-settledamt", "topayamt");
    dataSet = dataSet.filter("orgcol>0");
    dataSet = dataSet.addField("0", "sumlevel");
    DataSet sumDataSet = dataSet.groupBy().sum("pricetaxtotal").sum("unsettleamount").sum("topayamt").finish();
    sumDataSet = sumDataSet.select(new String[] { "null pk", "null isperiod", "null orgcol", "null asstacttype", "null asstact", "null currencycol", "null payorg", "null billtype", "'' billno", "'' billstatus", "null bizdate", "null duedate", "pricetaxtotal", "unsettleamount", "null dueamt", "null dueunverifyamt", "null duenoninvamt", "'' remark", "'' ezob_fphm", "null settlementtype"," null paycond"," null detailentry.materialname","null receivingsupplierid","null lockedamt", "null settledamt","topayamt", "2 sumlevel" });
    dataSet = dataSet.union(sumDataSet);
    dataSet = dataSet.orderBy(new String[] { "sumlevel", "orgcol", "currencycol" });
    return dataSet;
    //,"ezob_fphm","settlementtype","paycond","detailentry.materialname"
    //, "'' ezob_fphm", "null settlementtype"," null paycond"," null detailentry.materialname"
  }
  
  @Deprecated
  protected QFilter getFinQueryFilter() {
    QFilter filter = getBaseQueryFilter();
    Date date = this.filterInfo.getDate("date");
    if (date != null)
      filter = filter.and("duedate", "<", date); 
    filter = filter.and("settlestatus", "in", new String[] { "unsettle", "partsettle" });
    filter = filter.and("pricetaxtotal", "<>", BigDecimal.ZERO);
    return filter;
  }
  
  protected QFilter getBusQueryFilter() {
    QFilter filter = getBaseQueryFilter();
    Date date = this.filterInfo.getDate("date");
    if (date != null)
      filter = filter.and("duedate", "<", date); 
    filter = filter.and("verifystatus", "in", new String[] { "A", "C" });
    return filter;
  }
  
  protected QFilter getBaseQueryFilter() {
    DynamicObjectCollection orgColl = this.filterInfo.getDynamicObjectCollection("org");
    QFilter filter = new QFilter("org", "in", OrgHelper.getIdList((List)orgColl));
    String asstacttype = this.filterInfo.getString("asstacttype");
    if (asstacttype != null && !"".equals(asstacttype)) {
      filter = filter.and("asstacttype", "=", asstacttype);
      DynamicObject asstact = this.filterInfo.getDynamicObject("asstact");
      List<Object> asstactIds = ReportHelper.getBasedataIds(asstact);
      if (!asstactIds.isEmpty())
        filter = filter.and("asstact", "in", asstactIds); 
    } else {
      filter = filter.and(new QFilter("asstacttype", "in", Arrays.asList(new String[] { "bd_supplier", "bd_customer", "bos_user" })));
    } 
    DynamicObjectCollection currencyColl = this.filterInfo.getDynamicObjectCollection("currency");
    if (currencyColl != null && currencyColl.size() > 0)
      filter = filter.and("currency", "in", OrgHelper.getIdList((List)currencyColl)); 
    boolean isIncludeUnaudit = this.filterInfo.getBoolean("isincludesubmit");
    if (isIncludeUnaudit) {
      filter = filter.and("billstatus", "in", new String[] { "B", "C" });
    } else {
      filter = filter.and("billstatus", "=", "C");
    }
    return filter;
  }
}
