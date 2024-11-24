package kd.cosmic.tables.speed;

import kd.bos.algo.input.OrmInput;
import kd.bos.algox.DataSetX;
import kd.bos.algox.JobSession;
import kd.bplat.scmc.report.core.ReportDataCtx;
import kd.bplat.scmc.report.core.tpl.IDataXTransform;

/**
 * 描述: 即时库存,报表TransForm插件，用于报表的关联和计算，无部署地址，是被Handle插件调用的类
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：马丙丙
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

public class JoinRealBalTable implements IDataXTransform {
    private ReportDataCtx ctx;

    public JoinRealBalTable(ReportDataCtx ctx)
    {
        this.ctx =ctx;
    }
        @Override
    public DataSetX doTransform(DataSetX srcData) {
        //【ZOC】原本为了快速上线，所以同意让佳伟暂时先设置以下蠢方法，因为一开始数据量并不大，可以暂时采用，但是后续上线完成后，就取消了，改为了任务插件执行。
//        String sql = "/*dialect*/update t_bd_lot set fk_ezob_zlts=DATEDIFF(now(),fk_ezob_slrq) where fk_ezob_slrq is not null";
//        int count = DB.update(DBRoute.basedata, sql);

        // 分析:左表已有批号字段(lotnumber)，需要关联加上批号主档(bd_lot)的自定义字段
        // 开发则需要思考，怎么查询关联计算得到批号主档(bd_lot)的自定义字段的数据包。下面例子假设能一次查询取到
        // 使用上下文的流式任务，用于数据接入
        JobSession job =ctx.getCurrentJob();
        // 是ormInput或其他方式，构建出被关联表的Input定义即可
        DataSetX rightData = job.fromInput(new OrmInput("FID","bd_lot", "number,material,ezob_qgr,ezob_zdgys,ezob_zy,ezob_gys,ezob_cgdd,ezob_cgy,ezob_gysph,ezob_bzq,ezob_slrq,ezob_zlts,ezob_dj,ezob_je,ezob_qgyy,ezob_yjzt,ezob_yyxq", null));

        // 无法一次查询得到的，则需要多长用job获取数据包，和rightData计算得到
        //....
        // 设置关联方式和最终输出的字段
        srcData = srcData.leftJoin(rightData).on("lotnumber","number").on("material","material").select(new String[]{"ezob_qgr","ezob_zy","ezob_zdgys","ezob_cgdd","ezob_cgy","ezob_gys","ezob_gysph","ezob_bzq",
            "ezob_slrq","ezob_zlts","ezob_dj","ezob_je","ezob_qgyy",
            "lotnumber","material","auxpty","warehouse","location","invstatus","invtype","unit","baseunit","unit2nd","qty","qty_lock","baseqty",
            "baseqty_lock","qty_avb","baseqty_avb","producedate","expirydate","tracknumber","configuredcode","project","org","ownertype","owner","keepertype","keeper","ezob_yjzt","ezob_yyxq"});

        DataSetX rightData2 = job.fromInput(new OrmInput("materialid","bd_materialinventoryinfo","masterid,createorg,ezob_cfwz",null));

        //即时库存物料标识material对应物料库存信息的masterid,组织对应创建组织
        srcData = srcData.leftJoin(rightData2).on("material","masterid").on("org","createorg").select(new String[]{"ezob_qgr","ezob_zy","ezob_zdgys","ezob_cgdd","ezob_cgy","ezob_gys","ezob_gysph","ezob_bzq",
                "ezob_slrq","ezob_zlts","ezob_dj","ezob_je","ezob_qgyy",
                "lotnumber","material","auxpty","warehouse","location","invstatus","invtype","unit","baseunit","unit2nd","qty","qty_lock","baseqty",
                "baseqty_lock","qty_avb","baseqty_avb","producedate","expirydate","tracknumber","configuredcode","project","org","ownertype","owner","keepertype","keeper","ezob_yjzt","ezob_yyxq"},
                new String[]{"ezob_cfwz"});


        return srcData;
    }




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
