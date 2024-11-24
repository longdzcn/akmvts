//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package kd.cosmic.tables.Rptdqzlb;

import kd.bplat.scmc.report.core.ReportDataCtx;
import kd.bplat.scmc.report.core.tpl.IReportDataHandle;
import kd.bplat.scmc.report.core.transform.plan.GroupPlan;
import kd.fi.arapcommon.report.acctagev2.AcctageParam;

import java.util.Collection;
import java.util.Set;

/**
 * 描述: 应付账龄表（新）,报表Handle插件，用于预加载需要用到的字段
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：关敏婷
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

public class AcctageRptHandle implements IReportDataHandle {
    private AcctageParam reportParam;




    public void setupCtx(ReportDataCtx ctx) {
        this.reportParam = (AcctageParam) ctx.getParam(AcctageParam.class.getName());
    }



    public void beforeGroupData(ReportDataCtx ctx) {
        if (this.reportParam.isShowByBill()) {
            GroupPlan groupPlan = ctx.getGroupPlan();
            Collection<String> keyCols = groupPlan.getKeyCols();

            keyCols.add("ezob_fphm");
            keyCols.add("ezob_hth");
            keyCols.add("ezob_pzzh");
            keyCols.add("ezob_textfield3");
            keyCols.add("ezob_cgzl");
            keyCols.add("ezob_datefield2");
            keyCols.add("paycond");
            keyCols.add("settlementtype");
            keyCols.add("ezob_hl");
            keyCols.add("settleamount");
            keyCols.add("remark");
            keyCols.add("receivingsupplierid");
            keyCols.add("ezob_fprq");
            keyCols.add("ezob_mverpzt");
            keyCols.add("ezob_bzfk");








        }


    }

    @Override
    public void handleBigtableCols(Set<String> repoColFilter, ReportDataCtx ctx) {

        if (this.reportParam.isShowByBill()) {
            repoColFilter.add("ezob_fphm");
            repoColFilter.add("ezob_hth");
            repoColFilter.add("ezob_pzzh");
            repoColFilter.add("ezob_textfield3");
            repoColFilter.add("ezob_cgzl");
            repoColFilter.add("ezob_datefield2");
            repoColFilter.add("paycond");
            repoColFilter.add("settlementtype");
            repoColFilter.add("ezob_hl");
            repoColFilter.add("settleamount");
            repoColFilter.add("remark");
            repoColFilter.add("receivingsupplierid");
            repoColFilter.add("ezob_fprq");
            repoColFilter.add("ezob_mverpzt");
            repoColFilter.add("ezob_bzfk");
      /*      repoColFilter.add("pricetaxtotalbase");
            repoColFilter.add("unsettleamountbase");
            repoColFilter.add("settleamountbase");*/


        }


    }
}
