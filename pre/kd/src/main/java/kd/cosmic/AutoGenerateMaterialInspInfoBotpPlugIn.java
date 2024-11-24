package kd.cosmic;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.ExtendedDataEntitySet;
import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
import kd.bos.entity.botp.plugin.args.AfterCreateTargetEventArgs;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.util.CollectionUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 物料下推物料质检信息设置业务类型插件样例（未部署使用）
 */
public class AutoGenerateMaterialInspInfoBotpPlugIn extends AbstractConvertPlugIn {

    @Override
    public void afterCreateTarget(AfterCreateTargetEventArgs e) {
        super.afterCreateTarget(e);

        ExtendedDataEntitySet entitySet = e.getTargetExtDataEntitySet();
        Set<Long> bizTypeIdSet = new HashSet<>(16);
        // TODO 设置需要添加的业务类型
        bizTypeIdSet.add(959929179238017024L);
        bizTypeIdSet.add(1176040910430171136L);


        // 赋值时需要使用动态对象，否则会报错
        Map<Object, DynamicObject> bizTypeMap = BusinessDataServiceHelper.loadFromCache(bizTypeIdSet.toArray(), "bd_biztype");

        ExtendedDataEntity[] entitys = entitySet.FindByEntityKey(this.getTgtMainType().toString());
        for (ExtendedDataEntity entity : entitys) {
            DynamicObject dynamicObject = entity.getDataEntity();
            DynamicObjectCollection entryentity = dynamicObject.getDynamicObjectCollection("entryentity");
            // 当前已存在的业务类型集合
            Set<Long> bizTypeIdSetNow = entryentity.stream().filter(x -> null != x.getDynamicObject("inspecttype"))
                    .map(x -> x.getDynamicObject("inspecttype").getLong("id")).collect(Collectors.toSet());

            // 需要添加的业务类型集合
            Set<Long> bizTypeIdSetToAdd = bizTypeIdSet.stream().filter(x -> !bizTypeIdSetNow.contains(x)).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(bizTypeIdSetToAdd)) {
                int beginSeq = entryentity.size() + 1;
                for (Long bizTypeId : bizTypeIdSetToAdd) {
                    DynamicObject newEntrRow = entryentity.addNew();
                    newEntrRow.set("inspecttype", bizTypeMap.get(bizTypeId));
                    newEntrRow.set("seq", beginSeq++);
                }
            }
        }
    }
}