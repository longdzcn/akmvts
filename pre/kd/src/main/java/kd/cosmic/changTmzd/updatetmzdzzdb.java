package kd.cosmic.changTmzd;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.List;

/**
 * 描述: 直接调拨单审核后更新条码主档结余数量
 * 开发者: 李四辉
 * 创建日期: 1期
 * 关键客户：马丙丙
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
public class updatetmzdzzdb extends AbstractOperationServicePlugIn {
    //一定要预加载字段，否则审核后提示报错提示没有自定义字段实体类型tripentry中不存在名为ezob_kssj的属性
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        List<String> fieldKeys = e.getFieldKeys();
        fieldKeys.add("ezob_tmzd");
    }
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        try{
            //获取当前领料出库数据
            for (DynamicObject dy : e.getDataEntities()){
                //获取当前id
                String billId = String.valueOf(dy.getPkValue());
                //获取单据组织
                DynamicObject org = dy.getDynamicObject("outorg");
                String orgnumber = org.getString("number");
                //当为苏州工厂、南沙工厂、安博工厂时
                if(orgnumber.equals("LE0011") || orgnumber.equals("LE0012") || orgnumber.equals("LE0013") || orgnumber.equals("LE0014") || orgnumber.equals("LE0015")) {
                    //执行存储过程
                    String sql = "/*dialect*/call akmmv_prd_scm_test.KDUpdateBarcode(" + billId + ",5)";
                    DB.update(DBRoute.basedata, sql);

                    //必须要清除基础资料的缓存
                    //否则会导致在领料申请单选择的结余数量带不出来，基础资料都是有缓存的，如果不是在基础资料界面去修改数据，需要自行去清除缓存；
                    //通过kd.bos.servicehelper.BusinessDataServiceHelper#removeCache方法可以清除缓存。
                    //也可以调用基础资料界面的保存操作去更新缓存
                    DynamicObjectCollection entryEntity = dy.getDynamicObjectCollection("billentry");
                    DynamicObject dynamicObject = BusinessDataServiceHelper.newDynamicObject("ezob_tmzd");
                    for (DynamicObject entry : entryEntity) {
                        if(entry.getLong("ezob_tmzd.id")!=0) {
                            dynamicObject.set("id", entry.getLong("ezob_tmzd.id"));
                            BusinessDataServiceHelper.removeCache(dynamicObject.getDynamicObjectType());
                        }
                    }
                }
            }
        }catch (Exception e1){
            this.operationResult.setMessage(e1.toString());
            return;
        }
    }
}
