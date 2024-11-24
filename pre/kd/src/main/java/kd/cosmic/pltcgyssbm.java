package kd.cosmic;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.field.LargeTextEdit;

import java.math.BigDecimal;
import java.util.EventObject;

/**
 * 描述: 单据界面：采购收货单批量填充供应商识别码
 * 开发者: 李四辉
 * 创建日期:2023-12-01
 * 关键客户：仓库
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */

public class pltcgyssbm extends AbstractBillPlugIn {

    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 侦听主菜单按钮点击事件
        addItemClickListeners(new String[] { "advcontoolbarap" });
    }
    public void beforeItemClick(BeforeItemClickEvent evt) {
        if (StringUtils.equals("ezob_pltcgyssbm", evt.getItemKey())) {
            // TODO 在此添加业务逻辑
            // 需要通过如下方式，获取大文本详情属性名
            LargeTextEdit largeTextEdit = this.getView().getControl("ezob_tcgyssbm");
            String tagPropName = largeTextEdit.getTagFieldKey();

            // 获取单据头大文本的详情
            String largeTextTag = (String) this.getModel().getValue(tagPropName);

            //String tcgyssbm = getModel().getValue("ezob_tcgyssbm").toString();//获取表头供应商识别码
            String[] splitArray = largeTextTag.split("\n");
            int index = 0;
            int irowcount = getModel().getEntryRowCount("billentry");//明细
            int izmxrowcount = getModel().getEntryRowCount("ezob_subentryentity");//子明细
            for (int parentRow = 0; parentRow < irowcount; parentRow++) {
                if(izmxrowcount == 0){
                    this.getView().showMessage("请先生成条码主档！");
                    return;
                }
                //获取条码生成数
                BigDecimal tmscs = (BigDecimal)getModel().getValue("ezob_tmscs", parentRow);
                //强制转换为整型，因为条码生成数不可能为小数型
                int t = tmscs.intValue();
                IDataModel iDataModel = getModel();
                // 指定父单据体行号 (必须)
                iDataModel.setEntryCurrentRowIndex("billentry", parentRow);
                for (int j = 0; j < t; j++) {
                    //赋值供应商识别码
                    if(splitArray.length > index){
                        getModel().setValue("ezob_gyssbm", splitArray[index], j, parentRow);//原始周期
                        index++;
                    }
                }
            }
            this.getView().updateView("ezob_subentryentity");//更新子单据体View
            getModel().setValue("ezob_tcgyssbm","");//清空表头供应商识别码
            this.getModel().setValue(tagPropName, "");  // 清空详情值
        }

        if (StringUtils.equals("ezob_pltcsk", evt.getItemKey())) {
            // TODO 在此添加业务逻辑
            // 需要通过如下方式，获取大文本详情属性名
            LargeTextEdit largeTextEdit = this.getView().getControl("ezob_tcsk");
            String tagPropName = largeTextEdit.getTagFieldKey();

            // 获取单据头大文本的详情
            String largeTextTag = (String) this.getModel().getValue(tagPropName);

            String[] splitArray = largeTextTag.split("\n");
            int index = 0;
            int irowcount = getModel().getEntryRowCount("billentry");//明细
            int izmxrowcount = getModel().getEntryRowCount("ezob_subentryentity");//子明细
            if(izmxrowcount == 0){
                this.getView().showMessage("请先生成条码主档！");
                return;
            }
            for (int parentRow = 0; parentRow < irowcount; parentRow++) {
                BigDecimal tmscs = (BigDecimal)getModel().getValue("ezob_tmscs", parentRow);
                //强制转换为整型，因为条码生成数不可能为小数型
                int t = tmscs.intValue();
                IDataModel iDataModel = getModel();
                // 指定父单据体行号 (必须)
                iDataModel.setEntryCurrentRowIndex("billentry", parentRow);
                for (int j = 0; j < t; j++) {
                    //赋值供应商识别码
                    if(splitArray.length > index){
                        getModel().setValue("ezob_sk", splitArray[index], j, parentRow);//原始周期
                        index++;
                    }
                }
            }
            this.getView().updateView("ezob_subentryentity");//更新子单据体View
            getModel().setValue("ezob_tcsk","");//清空表头色块
            this.getModel().setValue(tagPropName, "");  // 清空详情值
        }
    }
}
