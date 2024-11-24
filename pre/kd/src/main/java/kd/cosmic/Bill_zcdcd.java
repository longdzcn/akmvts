package kd.cosmic;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

public class Bill_zcdcd extends AbstractFormPlugin implements Plugin {
    public void propertyChanged(PropertyChangedArgs e) {
        String name = e.getProperty().getName();
        if (name.equals("realcard")) {
            IDataModel model = getModel();
            DynamicObjectCollection entryEntity = model.getEntryEntity("dispatchentry");
            for (DynamicObject entry : entryEntity) {
                QFilter qSourseid = new QFilter("number", "=", entry.getDynamicObject("realcard").getString("number"));
                long id = 0L;
                DynamicObject[] Aps = BusinessDataServiceHelper.load("fa_card_fin_base", "id", new QFilter[] { qSourseid });
                DynamicObject[] arrayOfDynamicObject1 = Aps;
                int j = arrayOfDynamicObject1.length;
                byte b = 0;
                if (b < j) {
                    DynamicObject ap = arrayOfDynamicObject1[b];
                    id = Long.parseLong(ap.getString("id"));
                }
                for (int i = 0; i < entryEntity.getRowCount(); i++)
                    model.setValue("ezob_cwkbm", Long.valueOf(id), i);
            }
            getView().updateView();
        }
    }
}
