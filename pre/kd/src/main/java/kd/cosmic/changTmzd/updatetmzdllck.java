package kd.cosmic.changTmzd;

import kd.bos.data.BusinessDataReader;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.IDataEntityType;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.botp.runtime.ConvertOperationResult;
import kd.bos.entity.botp.runtime.PushArgs;
import kd.bos.entity.botp.runtime.SourceBillReport;
import kd.bos.entity.datamodel.IRefrencedataProvider;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.operate.OperateOptionConst;
import kd.bos.entity.operate.result.IOperateInfo;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.exception.KDBizException;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.botp.ConvertServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述: 领料出库审核前更新条码主档结余数量，因为如果放在审核后则无法触发自动下推的服务，如果放在审核中则会提示同一个事务不允许更新多数据库
 * 开发者: 李四辉
 * 创建日期: 1期
 * 关键客户：马丙丙
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
public class updatetmzdllck extends AbstractOperationServicePlugIn {
    //一定要预加载字段，否则审核后提示报错提示没有自定义字段实体类型tripentry中不存在名为ezob_kssj的属性
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        List<String> fieldKeys = e.getFieldKeys();
        fieldKeys.add("ezob_tmzd");
    }

    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        try{
            //获取当前领料出库数据
            for (DynamicObject dy : e.getDataEntities()){
                //获取当前id
                Long billId = Long.valueOf(String.valueOf(dy.getPkValue()));
                //获取单据组织
                DynamicObject org = dy.getDynamicObject("org");
                String orgnumber = org.getString("number");
                //当为苏州工厂、南沙工厂、安博工厂时
                if(orgnumber.equals("LE0011") || orgnumber.equals("LE0012") || orgnumber.equals("LE0013") || orgnumber.equals("LE0014") || orgnumber.equals("LE0015")) {
                    //执行存储过程
                    String sql = "/*dialect*/call akmmv_prd_scm_test.KDUpdateBarcode(" + String.valueOf(billId) + ",3)";
                    //测试
//                    String sql = "/*dialect*/update dev_3_scm.PUBLIC.t_bd_barcodemain set fname='测试'";
                    DB.update(DBRoute.basedata, sql);

                    //必须要清除基础资料的缓存
                    //否则会导致在领料申请单选择的结余数量带不出来，基础资料都是有缓存的，如果不是在基础资料界面去修改数据，需要自行去清除缓存；
                    //通过kd.bos.servicehelper.BusinessDataServiceHelper#removeCache方法可以清除缓存。
                    //也可以调用基础资料界面的保存操作去更新缓存
                    DynamicObjectCollection entryEntity = dy.getDynamicObjectCollection("billentry");
                    DynamicObject dynamicObject = BusinessDataServiceHelper.newDynamicObject("ezob_tmzd");
                    for (DynamicObject entry : entryEntity) {
                        if(entry.getLong("ezob_tmzd.id")!=0) {
                            dynamicObject.set("id", entry.getLong("ezob_tmzd.id"));
                            BusinessDataServiceHelper.removeCache(dynamicObject.getDynamicObjectType());
                        }
                    }
                    //下推下游的直接调拨单
                    push("im_materialreqoutbill","im_transdirbill", billId,"");

                }
            }
        }catch (Exception e1){
            this.operationResult.setMessage(e1.toString());
            return;
        }
    }

    /**
     * @param oriBill   源单标识
     * @param toBill    目标单标识
     * @param oriBillId 源单下推的单ID的集合
     * @param ruleId    下推使用转换规则的ID，使用默认转换规则传入""
     */
    public void push(String oriBill , String toBill, long oriBillId, String ruleId){
        List<ListSelectedRow> selectedRows = new ArrayList<>();
//        for (Object pkValue : oriBillId){
            ListSelectedRow listSelectedRow = new ListSelectedRow();
            listSelectedRow.setPrimaryKeyValue(oriBillId);
            selectedRows.add(listSelectedRow);
//        }
        PushArgs pushArgs = new PushArgs();
        pushArgs.setSourceEntityNumber(oriBill);		//必选，源单标识
        pushArgs.setTargetEntityNumber(toBill);        	//必选，目标单标识
        pushArgs.setSelectedRows(selectedRows);			//必选，源单下推单的id集合
        pushArgs.setBuildConvReport(true);				//可选，是否输出详细错误报告
        pushArgs.setHasRight(false);					//可选，传入true，不检查目标单新增权
        pushArgs.setAppId(""); 							//可选，传入目标单验权使用的应用编码
        pushArgs.setDefOrgId(0L);						//可选，传入目标单主组织默认值
        pushArgs.setRuleId(ruleId);	//可选，传入本次下推使用的转换规则id；传入空值，由系统自动寻找合适的转换规则

        ConvertOperationResult pushResult = ConvertServiceHelper.push(pushArgs);


        if (!pushResult.isSuccess()) {
            String errMessage = pushResult.getMessage();    // 错误摘要
            String billMessage = "";
            for (SourceBillReport billReport : pushResult.getBillReports()) {
                if (!billReport.isSuccess()) {
                    billMessage = billReport.getFailMessage();
                }
            }
            throw new KDBizException("下推下游单据失败:" + billMessage);
        }

        // 获取生成的目标单数据包
        MainEntityType targetMainType = EntityMetadataCache.getDataEntityType(toBill);
        List<DynamicObject> targetBillObjs = pushResult.loadTargetDataObjects(
                new IRefrencedataProvider() {
                    @Override
                    public void fillReferenceData(Object[] objs, IDataEntityType dType) {
                        BusinessDataReader.loadRefence(objs, dType);
                    }
                }, targetMainType);
        OperateOption saveOption = OperateOption.create();
        // 不执行警告级别校验器
        saveOption.setVariableValue(OperateOptionConst.IGNOREWARN, String.valueOf(true));
        // 不显示交互提示，自动执行到底
        saveOption.setVariableValue(OperateOptionConst.IGNOREINTERACTION, String.valueOf(true));
        // 全部校验通过才保存
        saveOption.setVariableValue(OperateOptionConst.STRICTVALIDATION, String.valueOf(true));

        //下推保存
        OperationResult saveResult = SaveServiceHelper.saveOperate(
                toBill,
                targetBillObjs.toArray(new DynamicObject[0]),
                saveOption);

        if (!saveResult.isSuccess()) {
            String errMessage = saveResult.getMessage();    // 错误摘要
            // 演示提取保存详细错误
            String detailMessage = "";
            for (IOperateInfo errInfo : saveResult.getAllErrorOrValidateInfo()) {
                detailMessage += errInfo.getMessage();
            }
            throw new KDBizException("保存下游单据失败：" + detailMessage);
        }
        else
        {
            //自动提交审核
            OperationServiceHelper.executeOperate("submit", toBill, targetBillObjs.toArray(new DynamicObject[0]), OperateOption.create());
            OperationServiceHelper.executeOperate("audit", toBill, targetBillObjs.toArray(new DynamicObject[0]), OperateOption.create());
        }
    }
}
