package kd.cosmic.lyj;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
import kd.bos.entity.botp.plugin.args.AfterConvertEventArgs;
import kd.sdk.plugin.Plugin;

/**
 * 单据转换插件
 */
public class jsbftkdjzh extends AbstractConvertPlugIn implements Plugin {
    @Override
    public void afterConvert(AfterConvertEventArgs e) {
        super.afterConvert(e);
        double j = 0;
        String tgtEntityNumber = this.getTgtMainType().getName();
        ExtendedDataEntity[] billDataEntitys = e.getTargetExtDataEntitySet().FindByEntityKey(tgtEntityNumber);
        for (ExtendedDataEntity extendedDataEntity : billDataEntitys) {
            DynamicObject dataEntity = extendedDataEntity.getDataEntity();
            DynamicObjectCollection billentry = dataEntity.getDynamicObjectCollection("billentry");
            for (DynamicObject dynamicObject : billentry) {
                String amount = dynamicObject.getString("amount");
                j = Double.parseDouble(amount);
                j += j;
                j = -j;
             }
            dataEntity.set("ezob_gysxd","测试");
            dataEntity.set("ezob_hzje",j);
        }
    }
}