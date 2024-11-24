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
 * 描述: 应付单下推付款申请预付超额控制转换插件。只针对应付下推付款申请控制，绑定在应付下推付款申请的计划行结算转换规则上
 * 开发者: 易佳伟
 * 创建日期: 1期完成
 * 关键客户：无
 * 已部署正式：ture
 * 备注：无
 */
public class FinApToApplyControlConvertPlugin extends AbstractConvertPlugIn {

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
        
        //3.通过核心单据号查询采购订单
        BigDecimal payamountTatol4pur =BigDecimal.ZERO;
        DynamicObject[] purOrderBills = BusinessDataServiceHelper.load("pm_purorderbill",
                "payamount,isprepay", new QFilter[]{new QFilter("billno", QCP.in, coreBillNos)});
        for (DynamicObject purOrderBill : purOrderBills) {
            DynamicObjectCollection entrys = purOrderBill.getDynamicObjectCollection("purbillentry_pay");
            for (DynamicObject entry : entrys) {
                if(entry.getBoolean("isprepay")){
                    payamountTatol4pur =payamountTatol4pur.add(entry.getBigDecimal("payamount"));
                }
            }
        }

        //采购订单预付分录金额为0不需要下面的校验
        if(payamountTatol4pur.compareTo(BigDecimal.ZERO)==0) return;

        //4.通过核心单据号查看是否存在未结算的相同核心单据号的预付款单，如果不存在，拦截下推付款申请
        BigDecimal payamountTatol4Cas =BigDecimal.ZERO;
        DynamicObject[] payBills = BusinessDataServiceHelper.load("cas_paybill",
                "id,paymenttype,entry.e_unsettledamt,entry.e_payableamt", new QFilter[]{new QFilter("entry.e_corebillno", QCP.in, coreBillNos)});
        for (DynamicObject payBill : payBills) {
            DynamicObject paymenttype = payBill.getDynamicObject("paymenttype");//付款类型
            if("202".equals(paymenttype.getString("number"))){//预付款单
                DynamicObjectCollection entries = payBill.getDynamicObjectCollection("entry");
                for (DynamicObject entry : entries) {
                    BigDecimal unSettleAmt = entry.getBigDecimal("e_unsettledamt");//未结算金额
                    if(unSettleAmt.compareTo(BigDecimal .ZERO) != 0){
                        throw new KDBizException("检测到存在跟应付单核心单据号相同的预付款单未结算的数据，请先跟预付款单结算后再下推付款申请单");
                    }
                    payamountTatol4Cas = payamountTatol4Cas.add(entry.getBigDecimal("e_payableamt"));
                }
            }
        }

        //采购订单的预付分录金额未完全下推预付款单
        if(payamountTatol4pur.compareTo(payamountTatol4Cas)!=0){
            throw new KDBizException("检测到应付单上游的订单存在预付分录未完全确认预付款的情况，请先完全下推预付单后再下推付款申请单");
        }
    }
}