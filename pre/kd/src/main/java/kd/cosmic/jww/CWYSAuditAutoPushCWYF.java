package kd.cosmic.jww;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.botp.runtime.ConvertOperationResult;
import kd.bos.entity.botp.runtime.PushArgs;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.operate.OperateOptionConst;
import kd.bos.entity.operate.result.IOperateInfo;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.entity.plugin.args.BeginOperationTransactionArgs;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.botp.ConvertServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 描述: 财务应收单审核自动查询暂估应付单，下推生成财务应付单
 * 开发者: 江伟维
 * 创建日期: 2024/6/12 9:15
 * 关键客户：
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class CWYSAuditAutoPushCWYF extends AbstractOperationServicePlugIn {

    private static final Log log = LogFactory.getLog(CWYSAuditAutoPushCWYF.class);
    private static final String sjcjxwldmxid = "ezob_jcjxwldmxid";    //[zoc]集采集销物流单明细ID
    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        super.onPreparePropertys(e);
        e.getFieldKeys().add("org");//库存组织
        e.getFieldKeys().add("billno");//单据编码
        //e.getFieldKeys().add("supplier");//供货供应商
        e.getFieldKeys().add("billentry");//物料明细
        e.getFieldKeys().add("e_corebillno");//核心单据id
        e.getFieldKeys().add("e_corebillentryseq");//核心单据行id
        e.getFieldKeys().add("e_quantity");//数量
        e.getFieldKeys().add(sjcjxwldmxid);//[zoc]集采集销物流单明细ID
        e.getFieldKeys().add("billtype");//单据类型
    }
    @Override
    public void beginOperationTransaction(BeginOperationTransactionArgs e) {
        super.beginOperationTransaction(e);
    }

    /**
     * 根据上有财务应收数量+暂估应付单（数量-关联数量），进行进行下推
     *
     * @param equantity
     * @param zgyfds  暂估应付单集合
     * @param billno
     * @param billId
     * @param billentryId
     */
    private List<DynamicObject> processAutoPush(BigDecimal equantity, DynamicObjectCollection zgyfds, String billno, String billId, String billentryId,Long orgId) {
        List<DynamicObject> dynamicObjects = new ArrayList<>();
        List<Long> id= new ArrayList<>();
        List<Long> billentryid= new ArrayList<>();
        String rulerId = "764222085420581888";
        if(orgId==1724820510136626176L){
            rulerId ="1936376226146970624";
        }
            //单据转换规则标识，注意在正式环境中可能会有多个自定义单据转换，要根据实际规则启用条件判断
        for (DynamicObject zgyfd : zgyfds) {
            long tmpid = zgyfd.getLong("id");
            id.add(tmpid);
            long tmpbillentryid = zgyfd.getLong("entry.id");
            billentryid.add(tmpbillentryid);
        }
        PushArgs args = buildPushArg(rulerId, id, billentryid);

        //下推数量
        BigDecimal pushQty = BigDecimal.ZERO;
        //给botp传参，修改下推数量
        pushQty = equantity;

        //参数传入botp
        args.getCustomParams().put("qty",pushQty.toString());//在botp插件处理给数量重新赋值
        args.getCustomParams().put("billno",billno);//在botp插件处理传暂估应付单编码
        args.getCustomParams().put("billid",billId);//在botp插件处理传暂估应付单ID
        args.getCustomParams().put("billentryid",billentryId);//在botp插件处理传暂估应付单ID
        try {
            //调用下推
                                                                                                                                                                                ConvertOperationResult pushResult = ConvertServiceHelper.push(args);
            if (pushResult.isSuccess()){
                MainEntityType targetMainType = EntityMetadataCache.getDataEntityType("ap_finapbill");
                List<DynamicObject> outbills = pushResult.loadTargetDataObjects(BusinessDataServiceHelper::loadRefence, targetMainType);
                dynamicObjects.addAll(outbills);
            }else {
//                    String errMsg = BillOutSendHelper.getErrMsg(pushResult);
//                    throw new KDBizException(new ErrorCode("Sample_endOperationTransaction_AddErrorInfo","采购订单下推采购入库单失败:"+errMsg));
                throw new KDBizException(new ErrorCode("Sample_endOperationTransaction_AddErrorInfo","暂估应收单下推财务应收单失败:"));
            }
        }catch (Exception e){
            throw new KDBizException(new ErrorCode("endOperationTransaction_AddErrorInfo","暂估应付单下推财务应付单失败:"+e.getMessage()));
        }
        return dynamicObjects;
    }

    private PushArgs buildPushArg(String ruleId,List<Long> srcId,List<Long> srcentryId){
        PushArgs args = new PushArgs();
        //源单标识
        args.setSourceEntityNumber("ap_busbill");
        //目标单标识
        args.setTargetEntityNumber("ap_finapbill");
        //单据转换规则id --  1633334429688650752
        //args.setRuleId(ruleId);
        //是否输出失败原因
        args.setBuildConvReport(true);
        List<ListSelectedRow> selectedRows = new ArrayList<>();
        for(int i=0;i< srcId.size();i++) {
            ListSelectedRow selectedRow = new ListSelectedRow();
            //必填，设置源单单据id
            selectedRow.setPrimaryKeyValue(srcId.get(i));
            //可选，设置源单分录标识
            selectedRow.setEntryEntityKey("detailentry");
            //可选，设置源单分录id
            selectedRow.setEntryPrimaryKeyValue(srcentryId.get(i));
            selectedRows.add(selectedRow);
        }
        args.setSelectedRows(selectedRows);
        return args;
    }

    /**
     * 根据核心单据id和核心单据行id查询暂估应付单行
     * @param ecorebillno  //[zoc]已弃用
     * @param ecorebillentryseq  //[zoc]后续改为了用自定义字段集采集销物流单明细ID来关联
     */
    private DynamicObjectCollection queryZGYFD(List<String> ecorebillno, List<Long> ecorebillentryseq) {
        QFilter qFilter = new QFilter("1", QCP.not_equals, 1);
        for (long t : ecorebillentryseq) {
            qFilter.or("entry."+sjcjxwldmxid, QCP.equals, String.valueOf(t));
        }
        //核心单据行ID无法获取，因为没有从mvERP那边传递过来

        //查询暂估应付单
        DynamicObjectCollection zgyfds = QueryServiceHelper.query("ap_busbill", "id,entry,entry.id,entry.e_material.id,entry.e_quantity,entry.e_corebillno,entry.corebillentryid,entry."+sjcjxwldmxid, qFilter.toArray(), "bizdate");

        /**
         * 重新过滤一下数据，存在不符合查询条件的数据
         */
        List<Integer> list = new ArrayList<>();
        int i = 0;
        for (DynamicObject zgyfd : zgyfds) {
            long aLong = zgyfd.getLong("entry."+sjcjxwldmxid);
            boolean b = false;
            for (long t : ecorebillentryseq) {
                if (aLong == t) {
                    b=true;
                    break;
                }
            }
                if(b==false)
            {
                list.add(i);
            }
            i++;
        }
        for(int j =list.size();j>0;j-- ){
            int j1 = list.get(j-1);
            zgyfds.remove(j1);
        }
        return zgyfds;
    }

    /**
     * 生成的财务应付单保存提交审核
     * @param cwysBills
     */
    private void submitAndAuditBill(List<DynamicObject> cwysBills, Set<Object>  billids){
        long l8 = System.currentTimeMillis();
        //保存下推单据
        OperateOption saveOption = OperateOption.create();
        //不执行警告级校验器
        saveOption.setVariableValue(OperateOptionConst.IGNOREINTERACTION, String.valueOf(true));
        // 不显示交互提示，自动执行到底
        saveOption.setVariableValue(OperateOptionConst.IGNOREWARN,String.valueOf(true));
        // 全部校验通过才保存
        saveOption.setVariableValue(OperateOptionConst.STRICTVALIDATION, String.valueOf(true));
        OperationResult saveOperateResult = SaveServiceHelper.saveOperate("ap_finapbill", cwysBills.toArray(new DynamicObject[0]), saveOption);
        long l9 = System.currentTimeMillis();
        log.info("处理保存逻辑耗时:"+(l9-l8));
        if (!saveOperateResult.isSuccess()){
            String message = saveOperateResult.getMessage();
            String infoMessage = "";
            for (IOperateInfo operateInfo : saveOperateResult.getAllErrorOrValidateInfo()) {
                infoMessage = operateInfo.getMessage();
            }
//            throw new KDBizException(message+infoMessage);
            this.getOperationResult().setMessage(this.getOperationResult().getMessage()+","+message+" "+infoMessage);
        }
    }

        //提交下推单据
//        OperateOption submitOption = OperateOption.create();
//        // 不执行警告级别校验器
//        submitOption.setVariableValue(OperateOptionConst.IGNOREWARN, String.valueOf(true));
//        // 不显示交互提示，自动执行到底
//        submitOption.setVariableValue(OperateOptionConst.IGNOREINTERACTION, String.valueOf(true));
//        // 全部校验通过
//        submitOption.setVariableValue(OperateOptionConst.STRICTVALIDATION, String.valueOf(true));
//        OperationResult submitResult = OperationServiceHelper.executeOperate("submit", "ap_finapbill", saveOperateResult.getSuccessPkIds().toArray(), submitOption);
//        long l10 = System.currentTimeMillis();
//        log.info("处理提交逻辑耗时:"+(l10-l9));
//        if (!submitResult.isSuccess()){
            //删除单据
//            Object[] ids = saveOperateResult.getSuccessPkIds().toArray();
//            tarList.addAll(saveOperateResult.getSuccessPkIds());
//            OperationServiceHelper.executeOperate("delete","im_saloutbill",ids,OperateOption.create());
            //删除反写互斥锁
//            deleteMutexLock(billids);
//            String message = submitResult.getMessage();
//            String infoMessage = "";
//            for (IOperateInfo operateInfo : submitResult.getAllErrorOrValidateInfo()) {
//                infoMessage = operateInfo.getMessage();
//            }
//            throw new KDBizException(message+infoMessage);
//            this.getOperationResult().setMessage(this.getOperationResult().getMessage()+","+message+" "+infoMessage);
//        }
        //审核下推单据
//        OperateOption auditOption = OperateOption.create();
        // 不执行警告级别校验器
//        auditOption.setVariableValue(OperateOptionConst.IGNOREWARN, String.valueOf(true));
        // 不显示交互提示，自动执行到底
//        auditOption.setVariableValue(OperateOptionConst.IGNOREINTERACTION, String.valueOf(true));
        // 全部校验通过
//        auditOption.setVariableValue(OperateOptionConst.STRICTVALIDATION, String.valueOf(true));
//        OperationResult auditResult  = OperationServiceHelper.executeOperate("audit", "ap_finapbill", submitResult.getSuccessPkIds().toArray(), auditOption);
//        long l11 = System.currentTimeMillis();
//        log.info("处理审核逻辑耗时:"+(l11-l10));
//        if (!auditResult.isSuccess()){
            //撤销删除单据
//            List<Object> successPkIds = submitResult.getSuccessPkIds();
//            tarList.addAll(successPkIds);
//            OperationServiceHelper.executeOperate("unsubmit","im_saloutbill",submitResult.getSuccessPkIds().toArray(),OperateOption.create());
//            OperationServiceHelper.executeOperate("delete","im_saloutbill",submitResult.getSuccessPkIds().toArray(),OperateOption.create());
            //删除反写互斥锁
//            deleteMutexLock(billids);
//            String message = auditResult.getMessage();
//            String infoMessage = "";
//            for (IOperateInfo operateInfo : auditResult.getAllErrorOrValidateInfo()) {
//                infoMessage = operateInfo.getMessage();
//            }
//            throw new KDBizException(message+infoMessage);
//            this.getOperationResult().setMessage(this.getOperationResult().getMessage()+","+message+" "+infoMessage);
//        }

    @Override
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        super.afterExecuteOperationTransaction(e);

        //开始执行财务应收单审核插件
        long l = System.currentTimeMillis();
        DynamicObject[] dataEntities1 = e.getDataEntities();
        //循环处理单据
        for (DynamicObject dynamicObject : dataEntities1) {
            String billno = dynamicObject.getString("billno");
            String billId = dynamicObject.getPkValue().toString();
            Long orgId = dynamicObject.getLong("org.id");
            List<Long> orgIds = new ArrayList<>();
            orgIds.add(orgId);

            DynamicObjectCollection billentry = dynamicObject.getDynamicObjectCollection("entry");
            boolean flag = false;
            for (DynamicObject object : billentry) {
                String ecorebillno = object.get(sjcjxwldmxid).toString();
                if (ecorebillno == null) {
                    flag = true;
                }
            }
            if (flag) {
                log.info("集采集销物流单明细ID为空，不执行下推");
                continue;
            }

            String billentryId = null;
            DynamicObjectCollection zgyfds = new DynamicObjectCollection();
            BigDecimal qty = BigDecimal.ZERO;
            List<String> ecorebillno = new ArrayList<>();
            List<Long> ecorebillentryseq = new ArrayList<>();
            Set<Object> set2 = new HashSet<>();
            for (DynamicObject object : billentry) {
                billentryId = object.getPkValue().toString();
                qty = object.getBigDecimal("e_quantity");
                String tmpbillno = object.get("e_corebillno").toString();
                ecorebillno.add(tmpbillno);
                long tmpbillentryseq = object.getLong(sjcjxwldmxid);
                if (tmpbillentryseq != 0) {
                    ecorebillentryseq.add(tmpbillentryseq);
                }
            }
            //起码有一行集采集销物流单明细ID的情况下，才会触发自动生成财务应付单
            if (ecorebillentryseq.size() > 0) {
                //根据核心单据id和核心单据行id查询暂估应付单符合条件的行
                //[ZOC]后来改为创建了集采集销物流单明细ID来关联查找，实际是用了e_corebillentryseq，e_corebillno就无效了
                zgyfds = queryZGYFD(ecorebillno, ecorebillentryseq);
                if (zgyfds == null || zgyfds.size() == 0) {
                    log.info("未查询到对应的暂估应付单，跳过");
                    continue;
                }
                for (DynamicObject zgyfd : zgyfds) {
                    Object id = zgyfd.get("id");
                    set2.add(id);
                }
                //处理下推逻辑
                List<DynamicObject> dynamicObjects = processAutoPush(qty, zgyfds, billno, billId, billentryId,orgId);
                if (dynamicObjects.size() > 0) {
                    submitAndAuditBill(dynamicObjects, set2);
                }
            }
        }
    }
}
