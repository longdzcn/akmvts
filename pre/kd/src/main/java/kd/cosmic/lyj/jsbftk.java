package kd.cosmic.lyj;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;

/**
 * 动态表单插件
 */
public class jsbftk extends AbstractFormPlugin implements Plugin {

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        IDataModel model = this.getModel();
        DynamicObject invscheme = (DynamicObject) model.getValue("invscheme");
        String number = invscheme.getString("number");
        if (number.equals("3551")){
            DynamicObject dataEntity = this.getModel().getDataEntity(true);
            DynamicObjectCollection goodsEntities = dataEntity.getDynamicObjectCollection("billentry");
            double i = 0;
            for (DynamicObject dynamicObject : goodsEntities) {
                String amount = dynamicObject.getString("amount");
                i = Double.parseDouble(amount);
                i = -i;
            }
            this.getView().setEnable(true, "ezob_hzje");
            model.setValue("ezob_hzje", i);
        }
    }
}