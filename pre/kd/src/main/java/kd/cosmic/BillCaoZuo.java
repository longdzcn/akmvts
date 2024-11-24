package kd.cosmic;


import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.plugin.AbstractFormPlugin;

/**
 * 描述: 顾问提出插件:费用报销单计算最大金额，用于审核流判断，部署在费用报销单表单
 * 开发者: 易佳伟
 * 创建日期: 2期完成
 * 关键客户：韦经彬、SP胡工
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

public class BillCaoZuo extends AbstractFormPlugin {
    public void propertyChanged(PropertyChangedArgs e) {
        String name = e.getProperty().getName();
       if("expenseamount".equals(name))
       {
           IDataModel model = this.getModel();

           DynamicObjectCollection entryEntity = model.getEntryEntity("expenseentryentity");
           double max = 0;
           for (DynamicObject entry : entryEntity) {
               Object value = entry.get("expenseamount");
               String zhuan = value.toString();
               double amount = Double.parseDouble(zhuan);

               if(amount>max)
               {
                   max = amount;
               }
           }
           this.getModel().setValue("ezob_mxzdje",max);
       }
    }
}