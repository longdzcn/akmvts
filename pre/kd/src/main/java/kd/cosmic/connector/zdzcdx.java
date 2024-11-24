package kd.cosmic.connector;

import com.alibaba.druid.util.StringUtils;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;

/**
 * 动态表单插件
 */
public class zdzcdx extends AbstractFormPlugin implements Plugin {

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String name = e.getProperty().getName();
        if ("name".equals(name)) {
            String userName  = this.getModel().getValue("name").toString();
            String upperCase = userName.toUpperCase();
            if (!StringUtils.isEmpty(upperCase)) {
                this.getModel().setValue("name", upperCase);
            }

        }
        else if ("simplename".equals(name)) {
            String simpleName = this.getModel().getValue("simplename").toString();
            String upperCase1 = simpleName.toUpperCase();
            if (!StringUtils.isEmpty(upperCase1)) {
                this.getModel().setValue("simplename", upperCase1);
            }
        }
        else if ("ezob_bghjc".equals(name)) {
            String ezobBghjc = this.getModel().getValue("ezob_bghjc").toString();
            String upperCase1 = ezobBghjc.toUpperCase();
            if (!StringUtils.isEmpty(upperCase1)) {
                this.getModel().setValue("ezob_bghjc", upperCase1);
            }
        }
    }
}