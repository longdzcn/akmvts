package kd.cosmic;


import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.BillEntityType;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.botp.ConvertOpType;
import kd.bos.entity.botp.ConvertRuleElement;
import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
import kd.bos.entity.botp.plugin.args.AfterConvertEventArgs;
import kd.bos.entity.botp.runtime.ConvertConst;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.List;

/**
 * 描述：出差申请单下推带出科研项目到差旅报销单、差旅报销单上拉出差申请单带科研项目
 * 开发者：梁远健
 * 挂载：下推，上拉，按人拆分行程。
 */
public class ccsqdjzh extends AbstractConvertPlugIn implements Plugin {
    BillEntityType srcMainType = null;
    BillEntityType tgtMainType = null;
    private void getContext(){
        // 源单主实体
        srcMainType = this.getSrcMainType();
        // 目标单主实体
        tgtMainType = this.getTgtMainType();
        // 转换规则
        ConvertRuleElement rule = this.getRule();
        // 转换方式：下推、选单
        ConvertOpType opType = this.getOpType();
    }

    @Override
    public void afterConvert(AfterConvertEventArgs e) {
        super.afterConvert(e);
        String ezobKyxm = null;
        String ezobzjly = null;
        String ezobzjtrjd = null;
        this.getContext();
        String tgtEntityNumber = this.getTgtMainType().getName();
        ExtendedDataEntity[] billDataEntitys = e.getTargetExtDataEntitySet().FindByEntityKey(tgtEntityNumber);
        for (ExtendedDataEntity extendedDataEntity : billDataEntitys){
            List<DynamicObject> value = (List<DynamicObject>) extendedDataEntity.getValue(ConvertConst.ConvExtDataKey_SourceRows);
//            if (value.size() != 0) {
//                for (int i = 0; i < value.size(); i++) {
                    DynamicObject dynamicObject = value.get(0);
                    Object id = e.getFldProperties().get("id").getValue(dynamicObject);
                    DynamicObject dynamicObject1 = BusinessDataServiceHelper.loadSingle(id, "er_tripreqbill");
                    if (dynamicObject1 != null) {
                        DynamicObject kyxm = dynamicObject1.getDynamicObject("ezob_kyxm");
                        if (kyxm != null) {
                            ezobKyxm = kyxm.getString("id");
                        } else {
                            ezobKyxm = null;
                        }
                        DynamicObject zjly = dynamicObject1.getDynamicObject("ezob_zjly");
                        if (zjly != null) {
                            ezobzjly = zjly.getString("id");
                        } else {
                            ezobzjly = null;
                        }
                        DynamicObject zjtrjd = dynamicObject1.getDynamicObject("ezob_zjtrjd");
                        if (zjtrjd != null) {
                            ezobzjtrjd = zjtrjd.getString("id");
                        } else {
                          ezobzjtrjd = null;
                        }
                    }
                    DynamicObject dataEntity = extendedDataEntity.getDataEntity();
                    //获取到每张单据下的行程信息
                    DynamicObjectCollection tripentrys = (DynamicObjectCollection) dataEntity.get("tripentry");

                    for(DynamicObject tripentry : tripentrys) {
                            //获取每个行程信息下的差旅明细
                        DynamicObjectCollection entryentitys = (DynamicObjectCollection) tripentry.get("entryentity");
                        int size = entryentitys.size();
                        for (int j = 0; j < size; j++) {
                            // 单据体第0行
                            DynamicObject entryentity = entryentitys.get(j);
                            entryentity.set("ezob_kyxm_id", ezobKyxm);
                            entryentity.set("ezob_zjly_id", ezobzjly);
                            entryentity.set("ezob_zjtrjd_id", ezobzjtrjd);
//                                entryentity.set("ezob_kyxmlx");
                            }
                        }
                    }
                }
            }
//        }
//    }
//

