package kd.cosmic;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
import kd.bos.entity.botp.plugin.args.AfterGetSourceDataEventArgs;
import kd.bos.exception.KDBizException;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.fi.arapcommon.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 描述: 应付单下推付款申请预付超额控制转换插件。只针对应付下推付款申请控制，绑定在应付下推付款申请的计划行结算转换规则上。逻辑只针对上游订单的预付比例累加是100%的情况
 * 开发者: 易佳伟
 * 创建日期: 1期完成
 * 关键客户：无
 * 已部署正式：ture
 * 备注：无
 */

public class FinApToApplyPrePayTotalCtrlPlugin extends AbstractConvertPlugIn {

    @Override
    public void afterGetSourceData(AfterGetSourceDataEventArgs e) {
        //1.获取源单id集合
        Set<Long> sourceIds = new HashSet<>(8);
        List<DynamicObject> sourceRows = e.getSourceRows();
        for (DynamicObject sourceRow : sourceRows) {
            sourceIds.add(sourceRow.getLong("id"));
        }

        //2.查询应付单的分录行的核心单据号集合
        DynamicObject[] finApBills = BusinessDataServiceHelper.load("ap_finapbill",
                "id,corebillno", new QFilter[]{new QFilter("id", QCP.in, sourceIds)});
        Set<String> coreBillNos = new HashSet<>(8);
        for (DynamicObject finApBill : finApBills) {
            DynamicObjectCollection detailEntries = finApBill.getDynamicObjectCollection("detailentry");
            for (DynamicObject detailEntry : detailEntries) {
                String corebillno = detailEntry.getString("corebillno");
                if(StringUtils.isNotEmpty(corebillno)){
                    coreBillNos.add(corebillno);
                }
            }
        }
        //应付单的核心单据号为空。直接返回，不需要走下面的校验逻辑
        if (coreBillNos.size() == 0) return;

        //3.通过核心单据号查询采购订单并且校验预付比例为100的不允许下推付款申请
        DynamicObject[] purOrderBills = BusinessDataServiceHelper.load("pm_purorderbill",
                "payamount,isprepay,payrate", new QFilter[]{new QFilter("billno", QCP.in, coreBillNos)});
        BigDecimal hurendRate = new BigDecimal("100.00");
        for (DynamicObject purOrderBill : purOrderBills) {
            DynamicObjectCollection entrys = purOrderBill.getDynamicObjectCollection("purbillentry_pay");
            BigDecimal totalRate = BigDecimal.ZERO;
            for (DynamicObject entry : entrys) {
                if(entry.getBoolean("isprepay")){//预付
                    BigDecimal payrate = entry.getBigDecimal("payrate");
                    totalRate = totalRate.add(payrate);
                }
            }
            if(totalRate.compareTo(hurendRate) == 0){//预付比例为100时，禁止下推付款申请
                throw new KDBizException("检测到应付单上游的采购订单的预付比例为100%，禁止下推付款申请");
            }
        }
    }
}