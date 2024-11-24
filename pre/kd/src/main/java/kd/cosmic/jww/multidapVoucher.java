package kd.cosmic.jww;

import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.BeforeOperationArgs;


/**
 * 描述: [ZOC]同一单据，允许生成多张凭证，目前只挂到研发资产分摊中
 * 开发者: 江伟维
 * 创建日期:
 * 关键客户：
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class multidapVoucher extends AbstractOperationServicePlugIn{

    @Override
    public void beforeExecuteOperationTransaction(BeforeOperationArgs e){
        super.beforeExecuteOperationTransaction(e);
        this.getOption().setVariableValue("multidap","true");
        this.getOption().setVariableValue("ignoretemplateunique","true");
    }

}