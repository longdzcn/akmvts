package kd.cosmic.tables.yszltables;

import kd.bplat.scmc.report.core.ReportDataCtx;
import kd.bplat.scmc.report.core.tpl.IReportDataHandle;
import kd.bplat.scmc.report.core.transform.plan.GroupPlan;
import kd.fi.arapcommon.report.acctagev2.AcctageParam;

import java.util.Collection;

/**
 * 描述: 应收账龄表（新）,报表Handle插件，用于预加载需要用到的字段
 */
public class YsAcctageRptHandle implements IReportDataHandle {

    private AcctageParam acctageParam;

    @Override
    public void setupCtx(ReportDataCtx ctx) {
        this.acctageParam = ctx.getParam(AcctageParam.class.getName());
    }

    @Override
    public void beforeGroupData(ReportDataCtx ctx) {
        IReportDataHandle.super.beforeGroupData(ctx);
        if (this.acctageParam.isShowByBill()) {
            GroupPlan groupPlan = ctx.getGroupPlan();
            Collection<String> keyCols = groupPlan.getKeyCols();

//            keyCols.add("ezob_nbywdy");
//            keyCols.add("ezob_mverpzt");
//            keyCols.add("ezob_sktj");
//            keyCols.add("ezob_fphm");
//            keyCols.add("ezob_fprq");
//            keyCols.add("ezob_xsbm");
//            keyCols.add("ezob_jsfs");
//            keyCols.add("ezob_fisrtinvoice");
//            keyCols.add("ezob_xsfl");
//            keyCols.add("ezob_exrate");
        }
    }
}