package kd.cosmic;

import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.control.Button;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;

/**
 * 描述: 配合scfjlbcj插件使用，打开上传附件动态表单
 * 开发者: 梁远健
 * 创建日期: 1期
 * 关键客户：马丙丙
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
public class FormSelectPlugin extends AbstractFormPlugin implements Plugin {
    @Override
    public void registerListener(EventObject e) {
        Button btt = this.getView().getControl("btnok");
        btt.addClickListener(this);
        super.registerListener(e);
    }
    @Override
    public void click(EventObject evt) {
        super.click(evt);
        if (evt.getSource() instanceof Button) {
            Button source = (Button) evt.getSource();
            if (source.getKey().equals("btnok")) {
                DynamicObjectCollection attCol = (DynamicObjectCollection) this.getModel().getValue("ezob_fj");
//                DynamicObject test = (DynamicObject) this.getModel().getValue("ezob_yfxm");
//                DynamicObject ezob_yfxm = (DynamicObject) test.get("ezob_yfxm");
//                long aLong1 = test.getLong("masterid");
                this.getView().returnDataToParent(attCol);
//                this.getView().returnDataToParent(test);
                this.getView().close();
            }
        }
    }
}
