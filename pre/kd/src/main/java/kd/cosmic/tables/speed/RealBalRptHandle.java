package kd.cosmic.tables.speed;

import kd.bos.biz.balance.model.BalanceTB;
import kd.bos.entity.balance.BizDataType;
import kd.bos.entity.report.AbstractReportColumn;
import kd.bos.entity.report.ReportColumn;
import kd.bos.exception.KDBizException;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bplat.scmc.report.conf.SrcBlockConf;
import kd.bplat.scmc.report.core.ReportDataCtx;
import kd.bplat.scmc.report.core.tpl.IDataTransform;
import kd.bplat.scmc.report.core.tpl.IDataXTransform;
import kd.bplat.scmc.report.core.tpl.IReportDataHandle;
import kd.scmc.im.report.algox.realbal.RealBalRptParam;
import kd.scmc.im.report.algox.realbal.trans.CalAvlQty;
import kd.scmc.im.report.algox.util.RptHandle;
import kd.scmc.im.report.algox.util.trans.AddMaterialGroup;
import kd.scmc.im.report.algox.util.trans.QtyHeadFilterTransform;
import kd.scmc.im.report.algox.util.trans.TailDiffFilterTransform;
import kd.scmc.im.report.algox.util.trans.ZeroQtyFilterTransform;

import java.util.*;


/**
 * 描述: 即时库存,报表Handle插件，各种方法和扩展方法，关联JoinRealBalTable开发计算类
 *   （下面方法都是用的未扩展插件那复制过来的），插件部署在业务设置里面的极速报表中
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：马丙丙
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public final class RealBalRptHandle implements IReportDataHandle {
    private RealBalRptParam reportParam;

    private RptHandle rptHandle;

//    private ReportDataCtx ctx;
//
//    public RealBalRptHandle(ReportDataCtx ctx)
//    {
//        this.ctx =ctx;
//    }



    public void setupCtx(ReportDataCtx ctx) {
        this.reportParam = (RealBalRptParam)ctx.getParam(RealBalRptParam.class.getName());
        getRptHandle().addNecessaryCol(ctx);
        setFixedFs(ctx);
       /* Map<String, Object> params =ctx.getParams();
        params.put("kd.cosmic.tables.speed.Param.RealBalRptParam","");
        ctx.setParams(params);*/
        //setTextHeadFilter(ctx);

        setCountQtyCols(ctx);
        getRptHandle().setImHeadFilter(ctx, this.reportParam);
    }
    /*private void setTextHeadFilter(ReportDataCtx ctx) {
        // 从自定义参数中获取漏斗原始过滤条件
        List<QFilter> headFilters = ctx.getParam(RptConst.KEY_HEAD_FILTERS);
        if (headFilters == null || headFilters.isEmpty()) {
            return;
        }

        // 这里假设文本字段为name，根据标识获取其漏斗过滤QFilter
        List<QFilter> billHeadFilters = new LinkedList<>();
        for (QFilter headFilter : headFilters) {
            if("name".equals(headFilter.getProperty())){
                billHeadFilters.add(headFilter);
            }
        }

        // 将漏斗过滤条件设置到pageFs中供查询使用
        ctx.getPageFs().addAll(billHeadFilters);
    }*/




    private void setFixedFs(ReportDataCtx ctx) {
        List<QFilter> fixedFs = ctx.getFixedFs();
        if (fixedFs == null) {
            fixedFs = new ArrayList<>();
            ctx.setFixedFs(fixedFs);
        }
        fixedFs.addAll(getFixedFilter());
    }

    private RptHandle getRptHandle() {
        return (this.rptHandle == null) ? (this.rptHandle = (new RptHandle()).setRptParam(this.reportParam)) : this.rptHandle;
    }
    private List<QFilter> getFixedFilter() {
        List<QFilter> fs = new ArrayList<>();
        getRptHandle().appendCommonFs(fs);
        return fs;
    }

    private void setCountQtyCols(ReportDataCtx ctx) {
        Set<String> qtyCols = ctx.getShowQtyCols();
        Set<String> allQtyCols = new HashSet<>(qtyCols);
        for (String qtyCol : qtyCols) {
            allQtyCols.add(qtyCol + "_lock");
            allQtyCols.add(qtyCol + "_avb");
        }
        ctx.setSumQtyCols4Count(allQtyCols);
    }


    public void beforeGroupData(ReportDataCtx ctx) {
        List<SrcBlockConf> srcBlockConf = ctx.getReportConf().getSrcBlockConf();
        if (srcBlockConf.size() == 1 && "im_inv_realbalance".equals(((SrcBlockConf)srcBlockConf.get(0)).getSrcEntity())) {
            Map<String, String> repoColSrcColMap = ((SrcBlockConf)srcBlockConf.get(0)).getRepoColSrcColMap();
            Set<String> showKeyCols = ctx.getShowKeyCols();
            Set<String> balCols = new HashSet<>();
            for (String keyCol : showKeyCols) {
                String balCol = repoColSrcColMap.get(keyCol);
                if (balCol != null)
                    balCols.add(balCol);
            }
            BalanceTB tb = BalanceTB.getBalanceTB("im_inv_realbalance");
            if (balCols.containsAll(tb.getColsByDataType(new BizDataType[] { BizDataType.DIM })))
                ctx.setGroupPlans(null);
        }
    }

    public void modifyBlocks(List<SrcBlockConf> blockCollector, ReportDataCtx ctx) {
        try {
            boolean aggregate = ((Boolean)DispatchServiceHelper.invokeBizService("mpscmm", "mscommon", "MpsReserveService", "getAggregate", new Object[0])).booleanValue();
            Map<String, Boolean> totalAggQty = (Map<String, Boolean>)DispatchServiceHelper.invokeBizService("mpscmm", "mscommon", "MpsReserveService", "totalAggQty", new Object[0]);
            List<String> aggregateField = (List<String>)DispatchServiceHelper.invokeBizService("mpscmm", "mscommon", "MpsReserveService", "getAggregateField", new Object[0]);
            List<String> aggDimList = new ArrayList<>(aggregateField.size());
            List<Long> wareHouseIds = this.reportParam.getWareHouseIds();
            if (aggregate && ((Boolean)totalAggQty.get("showreport")).booleanValue()) {
                Map<String, String> colMap = (Map<String, String>)DispatchServiceHelper.invokeBizService("mpscmm", "mscommon", "MpsReserveService", "colsBillMap", new Object[0]);
                aggDimList = new ArrayList<>(aggregateField.size());
                for (String colString : aggregateField) {
                    String dim = colMap.get(colString);
                    aggDimList.add(dim);
                }
            }
            Set<String> showKeyCols = ctx.getShowKeyCols();
            Map<String, String> showKeyMap = getShowKeyMap(blockCollector);
            List<String> invDimList = new ArrayList<>(6);
            for (String showKeyCol : showKeyCols)
                invDimList.add(showKeyMap.get(showKeyCol));
            if (!((Boolean)totalAggQty.get("showreport")).booleanValue() || !aggregate || !aggDimList.containsAll(invDimList))
                blockCollector.removeIf(s -> "msmod_reserve_record".equals(s.getSrcEntity()));
            for (SrcBlockConf blockConf : blockCollector) {
                appendIdMap2Block(blockConf);
                if (wareHouseIds.size() != 0) {
                    QFilter dataFs = blockConf.getDataFs();
                    if (!"msmod_reserve_record".equals(blockConf.getSrcEntity()))
                        if (dataFs == null) {
                            blockConf.setDataFs(new QFilter("warehouse", "in", wareHouseIds));
                        } else {
                            blockConf.setDataFs(dataFs.and(new QFilter("warehouse", "in", wareHouseIds)));
                        }
                    if ("msmod_reserve_record".equals(blockConf.getSrcEntity()) && aggDimList.contains("warehouse")) {
                        if (dataFs == null) {
                            blockConf.setDataFs(new QFilter("warehouse", "in", wareHouseIds));
                            continue;
                        }
                        blockConf.setDataFs(dataFs.and(new QFilter("s_warehouse", "in", wareHouseIds)));
                    }
                }
            }
        } catch (KDBizException e) {
            throw new KDBizException(e.getMessage());
        }
    }

    private Map<String, String> getShowKeyMap(List<SrcBlockConf> srcBlockConf) {
        Map<String, String> showKeyMap = new HashMap<>(16);
        for (int i = 0; i < srcBlockConf.size(); i++) {
            SrcBlockConf srcBlock = srcBlockConf.get(i);
            if ("im_inv_realbalance".equals(srcBlock.getSrcEntity()))
                return ((SrcBlockConf)srcBlockConf.get(i)).getRepoColSrcColMap();
        }
        return showKeyMap;
    }



    private void appendIdMap2Block(SrcBlockConf blockConf) {
        Map<String, String> colMap = blockConf.getRepoColSrcColMap();
        colMap.put("id", "id");
        colMap = blockConf.getRepoColFullSrcColMap();
        colMap.put("id", "id");
    }

    public void handleBigtableCols(Set<String> repoColFilter, ReportDataCtx ctx) {
        Set<String> showQtyCols = ctx.getShowQtyCols();
        for (String qtyCols : showQtyCols) {
            repoColFilter.add(qtyCols + "_lock");
            repoColFilter.add(qtyCols + "_avb");
        }
    }

    public void transFormAfterGroup(List<IDataXTransform> transCollector, ReportDataCtx ctx) {
        transCollector.add(new ZeroQtyFilterTransform(ctx));
        transCollector.add(new JoinRealBalTable(ctx));
        AddMaterialGroup addMaterialGroup = new AddMaterialGroup(ctx, this.reportParam);
        addMaterialGroup.setRepoMaterialCol("materialgroup");
        addMaterialGroup.setRepoMaterialCol("material");
        transCollector.add(addMaterialGroup);
        if (this.reportParam.isQueryTailDiff())
            transCollector.add(new TailDiffFilterTransform());
    }

    @Override
  /*  public void transFormAfterUnion(List<IDataXTransform> transCollector, ReportDataCtx ctx) {
        transCollector.add(new RealBalGetFiles(ctx));
        IReportDataHandle.super.transFormAfterUnion(transCollector, ctx);
    }*/


    public void transformResult(List<IDataTransform> transCollector, ReportDataCtx ctx) {

        Set<String> showQtyCols = ctx.getShowQtyCols();
        //计算可用量
        transCollector.add(new CalAvlQty(showQtyCols));
        //计算库龄天数
        //transCollector.add(new JoinRealBalRptSetDate(ctx));
        //数字类型字段漏斗过滤
        RealBalRptParam param = (RealBalRptParam)ctx.getParam(RealBalRptParam.class.getName());

        List<QFilter> headFilters = param.getHeadFilters();
        transCollector.add(new QtyHeadFilterTransform(headFilters));
    }

    public List<AbstractReportColumn> buildShowColumn(List<AbstractReportColumn> columns, ReportDataCtx ctx) {

        for (AbstractReportColumn col : columns) {

            ReportColumn reportCol = (ReportColumn)col;
            String field = reportCol.getFieldKey();
            if ("material".equals(field) || "lotnumber".equals(field) || "qty"
                    .equals(field) || "warehouse".equals(field) || "baseqty_lock".equals(field))
                reportCol.setHyperlink(true);
            if ("id".equals(field))
                reportCol.setHide(true);
        }
        return columns;
    }

}
