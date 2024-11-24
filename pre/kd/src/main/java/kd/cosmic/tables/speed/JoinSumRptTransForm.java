package kd.cosmic.tables.speed;

import kd.bos.algo.input.OrmInput;
import kd.bos.algox.DataSetX;
import kd.bos.algox.JobSession;
import kd.bplat.scmc.report.core.ReportDataCtx;
import kd.bplat.scmc.report.core.tpl.IDataXTransform;

import java.util.Set;

/**
 * 描述: 物料收发汇总表,报表TransForm插件，用于报表的关联和计算，无部署地址，是被Handle插件调用的类
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：马丙丙
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

public class JoinSumRptTransForm implements IDataXTransform {
    private ReportDataCtx ctx;

    public JoinSumRptTransForm(ReportDataCtx ctx)
    {
        this.ctx =ctx;
    }
        @Override
    public DataSetX doTransform(DataSetX srcData) {

        JobSession job =ctx.getCurrentJob();


        Set<String> fieldKeyNames =  ctx.getShowKeyCols();
        Set<String> fieldQtyNames =  ctx.getShowQtyCols();
        Set<String> fieldsumNames =  ctx.getShowQtyCols();
        Set<String> filedsumQtyCols4Count = ctx.getSumQtyCols4Count();
        String[] showkeyCols=    fieldKeyNames.toArray(new String[fieldKeyNames.size()]);
        String[] showQtyCols=    fieldQtyNames.toArray(new String[fieldQtyNames.size()]);
        String[] sumKeyCols=    fieldsumNames.toArray(new String[fieldsumNames.size()]);
        String[] sumQtyCols4Count=    filedsumQtyCols4Count.toArray(new String[filedsumQtyCols4Count.size()]);



        DataSetX rightData2;
        //关联物料信息库存表
        rightData2 = job.fromInput(new OrmInput("materialid","bd_materialinventoryinfo","masterid,createorg,ezob_cfwz", null));

         //即时库存物料标识material对应物料库存信息的masterid,组织对应创建组织
        srcData = srcData.leftJoin(rightData2).on("material","masterid").on("org","createorg").select(showkeyCols)
                .select(sumQtyCols4Count).select("ezob_cfwz");
         return srcData;
    }


/*new String[]{
        "baseqty","baseqty_in","baseqty_out","qty_bal","qty_out","baseqty_bal"},
            new String[]{"ezob_cfwz"},*/
/**
 *
 */

    /**
     * 过滤条件
     * List<QFilter> fixedFs = ctx.getFixedFs();
     *                 fixedFs.get(0);
     *                new QFilter[]{fixedFs.get(0)})
     *
     *
     *
     */

}
