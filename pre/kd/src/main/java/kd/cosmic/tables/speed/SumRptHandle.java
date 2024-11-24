package kd.cosmic.tables.speed;

import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.report.AbstractReportColumn;
import kd.bos.entity.report.DecimalReportColumn;
import kd.bos.entity.report.ReportColumn;
import kd.bos.orm.query.QFilter;
import kd.bplat.scmc.report.conf.SrcBlockConf;
import kd.bplat.scmc.report.core.ReportDataCtx;
import kd.bplat.scmc.report.core.tpl.IDataXTransform;
import kd.bplat.scmc.report.core.tpl.IReportDataHandle;
import kd.bplat.scmc.report.util.ReportUtil;
import kd.scmc.im.report.algox.sum.SumRptParam;
import kd.scmc.im.report.algox.sum.trans.AddBalCol;
import kd.scmc.im.report.algox.sum.trans.FilterQty;
import kd.scmc.im.report.algox.sum.trans.HandleCols4BigTable;
import kd.scmc.im.report.algox.sum.trans.OnlyShowRowCount;
import kd.scmc.im.report.algox.util.RptHandle;
import kd.scmc.im.report.algox.util.RptUtil;
import kd.scmc.im.report.algox.util.trans.AddMaterialGroup;
import kd.scmc.im.report.algox.util.trans.ZeroQtyFilterTransform;

import java.util.*;


/**
 * 描述: 物料收发汇总表,报表Handle插件，各种方法和扩展方法，JoinSumRptTransForm
 *   （下面方法都是用的未扩展插件那复制过来的），插件部署在业务设置里面的极速报表中
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：马丙丙
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public final class SumRptHandle implements IReportDataHandle {
    private SumRptParam reportParam;

    private RptHandle rptHandle;

    public void setupCtx(ReportDataCtx ctx) {
        this.reportParam = (SumRptParam)ctx.getParam(SumRptParam.class.getName());
        /**   加条件
         *   List<QFilter> fixedFs = new ArrayList<>();
         *         fixedFs.add("");
         *         ctx.setFixedFs(fixedFs);
         */

        getRptHandle().addNecessaryCol(ctx);
        setFixedFs(ctx);
        setCountQtyCols(ctx);
        getRptHandle().setImHeadFilter(ctx, this.reportParam);
    }


    private void setFixedFs(ReportDataCtx ctx) {
        List<QFilter> fixedFs = ctx.getFixedFs();
        if (fixedFs == null) {
            fixedFs = new ArrayList<>();
            ctx.setFixedFs(fixedFs);
        }
        fixedFs.addAll(getFixedFilter());
    }

    private void setCountQtyCols(ReportDataCtx ctx) {
        Set<Object> receiverIds = getReceiverIds();
        Set<String> qtyCols = ctx.getShowQtyCols();
        Set<String> allQtyCols = new HashSet<>();
        RptUtil.buildQtyCols(allQtyCols, qtyCols, receiverIds);
        ctx.setSumQtyCols4Count(allQtyCols);
    }

    private Set<Object> getReceiverIds() {
        Set<Object> receiverIds = new HashSet(this.reportParam.getReceiverType().keySet());
        receiverIds.addAll(this.reportParam.getFixdReceiverType().keySet());
        return receiverIds;
    }

    public void handleBigtableCols(Set<String> repoColFilter, ReportDataCtx ctx) {
        repoColFilter.add("biztime");
        repoColFilter.add("datatype");
        int size = this.reportParam.getReceiverType().size() + this.reportParam.getFixdReceiverType().size();
        if (size > 0)
            repoColFilter.add("transceivertype");
    }

    public void modifyBlocks(List<SrcBlockConf> blockCollector, ReportDataCtx ctx) {
        String srcEntity = null;
        RptHandle handle = getRptHandle();
        for (SrcBlockConf blockConf : blockCollector) {
            srcEntity = blockConf.getSrcEntity();
            if ("im_inv_periodbalance".equals(srcEntity))
                handleInitQtyBlock(blockConf);
            handle.handleInitBill(blockConf, this.reportParam.isIncludeUnUpdateBal());
            handle.handleMaterialFs(blockConf);
            handle.handleNoUpdateCols(blockConf);
        }
    }

    private void handleInitQtyBlock(SrcBlockConf blockConf) {
        int period = RptUtil.getPeriod(this.reportParam.getBillFromDate(), -1);
        getRptHandle().handlePerBalFs(blockConf, period);
    }


    /*@Override
    public void transformResult(List<IDataTransform> transCollector, ReportDataCtx ctx) {


        transCollector.add(new JoinSumSetFilterRptTransForm(ctx));

    }*/

    public void transFormAfterGroup(List<IDataXTransform> transCollector, ReportDataCtx ctx) {
        transCollector.add(new AddBalCol(ctx.getShowQtyCols()));
        Collection<String> qtyFilters = this.reportParam.getQtyFilters();
        if (!qtyFilters.isEmpty())
            transCollector.add(new FilterQty(ctx));
        transCollector.add(new ZeroQtyFilterTransform(ctx));
        transCollector.add(new JoinSumRptTransForm(ctx));
        AddMaterialGroup addMaterialGroup = new AddMaterialGroup(ctx, this.reportParam);
        addMaterialGroup.setRepoMaterialCol("materialgroup");
        addMaterialGroup.setRepoMaterialCol("material");
        transCollector.add(addMaterialGroup);
    }

    public void transFormAfterAddSumRow(List<IDataXTransform> transCollector, ReportDataCtx ctx) {
        if (this.reportParam.isOnlyShowRowCount())
            transCollector.add(new OnlyShowRowCount());
    }

    public void transFormAfterUnion(List<IDataXTransform> transCollector, ReportDataCtx ctx) {
        transCollector.add(new HandleCols4BigTable(ctx));
    }

    private List<QFilter> getFixedFilter() {
        List<QFilter> fs = new ArrayList<>();
        fs.add(RptUtil.buildStatusFs("status", this.reportParam.isIncludeSubmitStatus()));
        fs.add(RptUtil.buildBizTimeFs("biztime", this.reportParam.getBillFromDate(), this.reportParam.getToDate()));
        if (!this.reportParam.isIncludeUnUpdateBal())
            fs.add((new QFilter("isupdatebal", "=", "0")).and("isvirtualbill", "=", "0"));
        getRptHandle().appendCommonFs(fs);
        getRptHandle().appendWarehouseFs(fs);
        return fs;
    }

    private RptHandle getRptHandle() {
        return (this.rptHandle == null) ? (this.rptHandle = (new RptHandle()).setRptParam(this.reportParam)) : this.rptHandle;
    }

    public List<AbstractReportColumn> buildShowColumn(List<AbstractReportColumn> columns, ReportDataCtx ctx) {
        Set<String> showQtyCols = ctx.getShowQtyCols();
        List<AbstractReportColumn> showCols = new ArrayList<>(columns.size() + showQtyCols.size() * 3);
        ReportColumn reportCol = null;
        boolean showInitAndBal = this.reportParam.isShowInitAndBalQty();
        boolean showIn = this.reportParam.isShowInQty();
        boolean showOut = this.reportParam.isShowOutQty();
        Map<Object, String> receiverType = new HashMap<>(this.reportParam.getReceiverType());
        receiverType.putAll(this.reportParam.getFixdReceiverType());
        String field = null;
        List<AbstractReportColumn> receiverQtyCol = new ArrayList<>(showQtyCols.size() * receiverType.size());
        String qtyColName = null;
        ReportColumn tempQtyCol = null;
        boolean zeroShow = false;
        boolean scaleZero = false;
        boolean summary = false;
        for (AbstractReportColumn col : columns) {
            reportCol = (ReportColumn)col;
            field = reportCol.getFieldKey();
            zeroShow = reportCol.isZeroShow();
            scaleZero = reportCol.isNoDisplayScaleZero();
            if (showQtyCols.contains(field)) {
                if (col instanceof DecimalReportColumn)
                    summary = (((DecimalReportColumn)col).getSummary() == 1);
                qtyColName = col.getCaption().getLocaleValue();
                String unitCol = reportCol.getMeasureUnitField();
                if (showInitAndBal) {
                    DecimalReportColumn decimalReportColumn = ReportUtil.buildQtyCol(field, new LocaleString(String.format(ResManager.loadKDString("%1$s（期初）", "InvSumReportHandle_0", "scmc-im-report", new Object[0]), new Object[] { qtyColName })), unitCol, summary);
                    decimalReportColumn.setZeroShow(zeroShow);
                    decimalReportColumn.setNoDisplayScaleZero(scaleZero);
                    showCols.add(decimalReportColumn);
                }
                if (showIn) {
                    DecimalReportColumn decimalReportColumn = ReportUtil.buildQtyCol(field + "_in", new LocaleString(
                            String.format(ResManager.loadKDString("%1$s（收入）", "InvSumReportHandle_1", "scmc-im-report", new Object[0]), new Object[] { qtyColName })), unitCol, summary);
                    decimalReportColumn.setZeroShow(zeroShow);
                    decimalReportColumn.setNoDisplayScaleZero(scaleZero);
                    showCols.add(decimalReportColumn);
                }
                if (showOut) {
                    DecimalReportColumn decimalReportColumn = ReportUtil.buildQtyCol(field + "_out", new LocaleString(
                            String.format(ResManager.loadKDString("%1$s（发出）", "InvSumReportHandle_2", "scmc-im-report", new Object[0]), new Object[] { qtyColName })), unitCol, summary);
                    decimalReportColumn.setZeroShow(zeroShow);
                    decimalReportColumn.setNoDisplayScaleZero(scaleZero);
                    showCols.add(decimalReportColumn);
                }
                if (showInitAndBal) {
                    DecimalReportColumn decimalReportColumn = ReportUtil.buildQtyCol(field + "_bal", new LocaleString(
                            String.format(ResManager.loadKDString("%1$s（结余）", "InvSumReportHandle_3", "scmc-im-report", new Object[0]), new Object[] { qtyColName })), unitCol, summary);
                    decimalReportColumn.setZeroShow(zeroShow);
                    decimalReportColumn.setNoDisplayScaleZero(scaleZero);
                    showCols.add(decimalReportColumn);
                }
                for (Map.Entry<Object, String> entry : receiverType.entrySet()) {
                    DecimalReportColumn decimalReportColumn = ReportUtil.buildQtyCol(field + entry.getKey(), new LocaleString(qtyColName + "（" + (String)entry
                            .getValue() + "）"), unitCol, summary);
                    decimalReportColumn.setZeroShow(zeroShow);
                    decimalReportColumn.setNoDisplayScaleZero(scaleZero);
                    receiverQtyCol.add(decimalReportColumn);
                }
            } else {
                showCols.add(col);
            }
            if ("material".equals(field))
                reportCol.setHyperlink(true);
        }
        showCols.addAll(receiverQtyCol);
        return showCols;
    }
}
