package kd.cosmic;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.control.events.BeforeItemClickEvent;

import java.math.BigDecimal;
import java.util.EventObject;

/**
 * 描述:单据界面：采购收货子明细逐行汇总明细数量
 * 开发者: 李四辉
 * 创建日期:2024-04-01
 * 关键客户：仓库
 * 已部署正式：true
 * 备注：已投入正式环境使用，无问题
 */

//kd.cosmic.zhhz
public class zhhz extends AbstractBillPlugIn{
    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String name = e.getProperty().getName();
        if ("ezob_iqty".equals(name)) {
            DynamicObjectCollection entryEntity = getModel().getEntryEntity("billentry");
            int i = 0;
            // 步骤3.从单据体的每一行中获取gai行的子单据体对象，这里通过遍历的方式展现该效果
            for (DynamicObject entry : entryEntity) {
                double qtyhz = 0;
                // 获取当前单据体行的子单据体
                DynamicObjectCollection subEntryEntity = entry.getDynamicObjectCollection("ezob_subentryentity");
                // 从子单据体的每一行中获取当前行的子单据体的字段值，这里通过遍历的方式展现该效果
                for (DynamicObject subEntry : subEntryEntity) {
                    BigDecimal qty = (BigDecimal)subEntry.get("ezob_iqty");
                    qtyhz = qtyhz + qty.doubleValue();
                }
                getModel().setValue("ezob_hzsl",qtyhz,i);
                i++;
            }
        }
    }

    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 侦听主菜单按钮点击事件
        addItemClickListeners(new String[] { "ezob_advcontoolbarap" });
    }
    public void beforeItemClick(BeforeItemClickEvent evt) {
        if (StringUtils.equals("ezob_advconbaritemap", evt.getItemKey())) {
            // TODO 在此添加业务逻辑
            DynamicObjectCollection entryEntity = getModel().getEntryEntity("billentry");
            int i = 0;
            // 步骤3.从单据体的每一行中获取gai行的子单据体对象，这里通过遍历的方式展现该效果
            for (DynamicObject entry : entryEntity) {
                double qtyhz = 0;
                // 获取当前单据体行的子单据体
                DynamicObjectCollection subEntryEntity = entry.getDynamicObjectCollection("ezob_subentryentity");
                // 从子单据体的每一行中获取当前行的子单据体的字段值，这里通过遍历的方式展现该效果
                for (DynamicObject subEntry : subEntryEntity) {
                    BigDecimal qty = (BigDecimal)subEntry.get("ezob_iqty");
                    qtyhz = qtyhz + qty.doubleValue();
                }
                getModel().setValue("ezob_hzsl",qtyhz,i);
                i++;
            }

            //this.getView().invokeOperation("refresh");



//            int irowcount = getModel().getEntryRowCount("billentry");//明细
//            for (int parentRow = 0; parentRow < irowcount; parentRow++) {
//                double qtyhz = 0;
//                IDataModel iDataModel = getModel();
//                // 指定父单据体行号 (必须)
//                iDataModel.setEntryCurrentRowIndex("billentry", parentRow);
//                int izmxrowcount = getModel().getEntryRowCount("ezob_subentryentity");//子明细
//                for (int j = 0; j < izmxrowcount; j++) {
//                    BigDecimal qty = (BigDecimal)getModel().getValue("ezob_iqty",j);//原始周期
//                    qtyhz = qtyhz + qty.doubleValue();
//                }
//                getModel().setValue("ezob_hzsl",qtyhz,parentRow);
//            }
        }
    }
}
