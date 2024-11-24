package kd.cosmic.connector;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QFilter;

import java.util.EventObject;

/**
 * 动态表单插件
 */
public class jczlgltj extends AbstractFormPlugin implements BeforeF7SelectListener {

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        BasedataEdit gdzcbm = this.getControl("ezob_gdzcbm");
        gdzcbm.addBeforeF7SelectListener(this);
    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {
        if (beforeF7SelectEvent.getProperty().getName().equals("ezob_gdzcbm")){
            DynamicObject org = (DynamicObject) this.getModel().getValue("org");
            String number = org.getString("number");
            QFilter q = new QFilter("org.number", QFilter.equals, number);
            ListShowParameter showParameter  = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            showParameter.getListFilterParameter().getQFilters().add(q);
        }
    }
}