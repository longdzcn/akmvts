package kd.cosmic;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.workflow.WorkflowServiceHelper;
import kd.bos.workflow.component.approvalrecord.IApprovalRecordGroup;
import kd.bos.workflow.component.approvalrecord.IApprovalRecordItem;

import java.util.List;
/**
 * 描述: 审核后将审批信息反写至表头字段，以便列表可以套打出来
 * 开发者: 江伟维
 * 创建日期:
 * 关键客户：
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */

public class AOSPFKSQDUpdateSPXX extends AbstractOperationServicePlugIn {
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        try {

            for (DynamicObject dy : e.getDataEntities()) {
                String billId = String.valueOf(dy.getPkValue());

                //返回单据的整个审批记录
                List<IApprovalRecordGroup> la= WorkflowServiceHelper.getAllApprovalRecord(billId);
                String curNodeStr="";
                for (int j=0; j<la.size(); j++) {
                    IApprovalRecordGroup ia= la.get(j);
                    List<IApprovalRecordItem> ir= ia.getChildren();
                    for (int k=0; k<ir.size(); k++) {
                        IApprovalRecordItem iritem=ir.get(k);
                        if(curNodeStr!="")
                        {
                            curNodeStr=curNodeStr + "\n";
                        }
                        curNodeStr =curNodeStr +iritem.getActivityName()+"：" + iritem.getAssignee() + "/审批时间：" + iritem.getTime() + "/审批意见：" + iritem.getMessage();
                    }
                }
                DynamicObject dyo= BusinessDataServiceHelper.loadSingle(billId,"ap_payapply");
                dyo.set("ezob_spxx",curNodeStr); //赋值审批信息
                SaveServiceHelper.update(dyo); //更新数据

            }
        } catch (Exception e1) {
            this.operationResult.setMessage(e1.toString());
            return;
        }
    }
}