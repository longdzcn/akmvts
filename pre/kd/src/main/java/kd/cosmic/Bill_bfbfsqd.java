package kd.cosmic;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

/**
 * 描述: 固定资产报废单在选择固定资产编码带出对应的财务卡片，插件在报废单表单
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：顾问夏晓君
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class Bill_bfbfsqd extends AbstractFormPlugin implements Plugin {

    public void propertyChanged(PropertyChangedArgs e) {
        String name = e.getProperty().getName();

        if(name.equals("ezob_gdzcbm"))
        {
            IDataModel model = this.getModel();

            DynamicObjectCollection entryEntity = model.getEntryEntity("entryentity");

            for (DynamicObject entry : entryEntity) {

                QFilter qSourseid = new QFilter("number", "=", entry.getDynamicObject("ezob_gdzcbm").getString("number"));

                long id = 0;

                DynamicObject[] aPs = BusinessDataServiceHelper.load("fa_card_fin_base", "id", new QFilter[]{qSourseid});


                for (DynamicObject ap : aPs) {
                    id = Long.parseLong(ap.getString("id"));
                    break;
                }

                for (int i = 0; i < entryEntity.getRowCount(); i++) {

                    model.setValue("ezob_cwkbm", id, i);

                }




            }
            getView().updateView();

        }
    }
}