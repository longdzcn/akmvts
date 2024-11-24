package kd.cosmic.btop;

import kd.bos.dataentity.entity.CloneUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.MulBasedataDynamicObjectCollection;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.dataentity.metadata.dynamicobject.DynamicProperty;
import kd.bos.entity.BillEntityType;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.botp.ConvertOpType;
import kd.bos.entity.botp.ConvertRuleElement;
import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
import kd.bos.entity.botp.plugin.args.AfterConvertEventArgs;
import kd.bos.entity.botp.plugin.args.AfterGetSourceDataEventArgs;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.Map;

/**
 *
 *
 */

public class billConvert extends AbstractConvertPlugIn {

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

    DynamicObject kyxm ;

    /**
     * 单据转换后事件，最后执行
     *
     * @param e
     * @remark
     * 插件可以在这个事件中，对生成的目标单数据，进行最后的修改
     */
    @Override
    public void afterConvert(AfterConvertEventArgs e) {
        this.getContext();

        String tgtEntityNumber = this.getTgtMainType().getName();
        ExtendedDataEntity[] billDataEntitys = e.getTargetExtDataEntitySet().FindByEntityKey(tgtEntityNumber);
        //DynamicObjectType entryType = entryentity.getDynamicObjectType();

        for(ExtendedDataEntity billDataEntity : billDataEntitys){
            DynamicObject dataEntity = billDataEntity.getDataEntity();
            //获取到每张单据下的行程信息
            DynamicObjectCollection tripentrys = (DynamicObjectCollection) dataEntity.get("tripentry");
            //获取科研项目信息
            //DynamicObject kyxm = (DynamicObject) dataEntity.get("ezob_kyxm");
            for(DynamicObject tripentry : tripentrys) {
                //获取每个行程信息下的差旅明细
                DynamicObjectCollection entryentitys = (DynamicObjectCollection) tripentry.get("entryentity");
                int entryentitysSize = entryentitys.size();
                for(int j = 0;j<entryentitysSize;j++) {
                // 单据体第0行
                 DynamicObject entryentity = entryentitys.get(j);
                //获取每个差旅明细下的出差人
                DynamicObjectType entryType = entryentitys.getDynamicObjectType();
                MulBasedataDynamicObjectCollection trip2travelers = (MulBasedataDynamicObjectCollection) entryentity.get("trip2travelers");
                int a = 0;
                if (trip2travelers.size() > 1) {
                    for (int i = 0; i < trip2travelers.size() - 1; i++) {
                        String fid = String.valueOf(trip2travelers.get(i).get("fbasedataid_id"));

                        //复制单据体第0行，作为新增行
                        CloneUtils cloneUtils = new CloneUtils(false, true);
                        DynamicObject copyRow = (DynamicObject) cloneUtils.clone(entryentity);
                        //修改新增行的出差人trip2travelers字段
                        MulBasedataDynamicObjectCollection temp = new MulBasedataDynamicObjectCollection();
                        temp.add(trip2travelers.get(i));
                        copyRow.set("trip2travelers", temp);
                        copyRow.set("ezob_bxr", getUser(fid));

                        //修改新增行的成本中心字段
                        copyRow.set("travelcostcenter", getCostCenter(fid));
                        copyRow.set("travelcostdept", getDept(fid));
                        //添加行
                        entryentitys.add(copyRow);
                    }
                    String fid = String.valueOf(trip2travelers.get(trip2travelers.size() - 1).get("fbasedataid_id"));
                    MulBasedataDynamicObjectCollection temp = new MulBasedataDynamicObjectCollection();
                    temp.add(trip2travelers.get(trip2travelers.size() - 1));
                    entryentity.set("trip2travelers", temp);
                    entryentity.set("travelcostcenter", getCostCenter(fid));
                    entryentity.set("travelcostdept", getDept(fid));
                    entryentity.set("ezob_bxr", getUser(fid));

                }else {
                    String fid = String.valueOf(trip2travelers.get(0).get("fbasedataid_id"));
                    entryentity.set("ezob_bxr", getUser(fid));
                }
            }
            }

        }
    }

    public DynamicObject getCostCenter(String fid) {
        // "人员"基础资料
        DynamicObject user = BusinessDataServiceHelper.loadSingle(fid, "bos_user");

        // 获取”人员“中的”默认成本中心“
        DynamicObject boscostcenter = (DynamicObject) user.get("ezob_cbzx");


        return boscostcenter;
    }

    public DynamicObject getDept(String fid) {
        // "人员"基础资料
        QFilter idQFilter = new QFilter("id", QCP.equals, fid);
        DynamicObject user = BusinessDataServiceHelper.loadSingle(fid, "bos_user");
        // 获取”人员“ 中的”部门信息“分录下的”部门“
        DynamicObjectCollection entryentitydpt = (DynamicObjectCollection) user.get("entryentity");
        DynamicObject dpt = (DynamicObject) entryentitydpt.get(0).get("dpt");
        String dptname = dpt.getString("name");
        Long dptid = dpt.getLong("id");

        return dpt;
    }
    // getBosUser
    public DynamicObject getUser(String fid)
    {
        DynamicObject user = BusinessDataServiceHelper.loadSingle(fid, "bos_user");

        return  user;
    }

    //取源单事件后
    @Override
    public void afterGetSourceData(AfterGetSourceDataEventArgs e) {
        Map<String, DynamicProperty> fldProperties = e.getFldProperties();
        int a = 0;
    }

}
