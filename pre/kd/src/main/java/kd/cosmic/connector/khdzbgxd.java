package kd.cosmic.connector;

import com.alibaba.druid.util.StringUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.list.IListView;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;

public class khdzbgxd extends AbstractListPlugin implements Plugin {
    //存储需要传输的数据集合
    private final static String KEY_BARITEM = "pushCW";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(KEY_BARITEM);
    }
    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
//        try {
        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {
//            获取数据列表
            ListSelectedRowCollection selectedRows = ((IListView) this.getView()).getSelectedRows();
            for (ListSelectedRow selectedRow : selectedRows) {
                DynamicObject customer = BusinessDataServiceHelper.loadSingle(selectedRow.getPrimaryKeyValue(), "bd_customer");
//                客户编码
                String number = customer.getString("number");
                QFilter q = new QFilter("customer.number", QCP.equals, number);
//                获取变更客户实体
                QFilter q1 = new QFilter("number", QCP.equals, number);
                DynamicObject[] khbg = BusinessDataServiceHelper.load("ezob_khbgd", "ezob_entryentity2,number,ezob_bgqshddmc,ezob_bgqshlxrmc,ezob_bgqmysy,ezob_bgqxzqh,ezob_bgqxxdzdz,ezob_bgqyzbmdz", new QFilter[]{q1});
//                获取客户地址
                DynamicObject[] load = BusinessDataServiceHelper.load("bd_address", "bd_customer,detailaddress,ezob_shddmc,addemail,name,phone,zipcode,ezob_svmif,hihn_tradeterms,hihn_tradeterms.number,addnumber,number,hihn_clearanceco,admindivision", new QFilter[]{q});
                //                    给送货地址变更的变更前字段赋值
                for (DynamicObject object : khbg) {
                    DynamicObjectCollection ezobEntryentity2 = object.getDynamicObjectCollection("ezob_entryentity2");
//                    清除行
//                    ezobEntryentity2.clear();
                for (DynamicObject dynamicObject : load) {
//                    送货地址名称
                    String ezobshddmc = dynamicObject.getString("ezob_shddmc");
//                    送货联系人名称
                    String name = dynamicObject.getString("name");
//                    变更前贸易术语
                    DynamicObject  hihntradeterms = dynamicObject.getDynamicObject("hihn_tradeterms.number");
//                    行政区划
                    String admindivision = dynamicObject.getString("admindivision");
//                    详细地址
                    String detailaddress = dynamicObject.getString("detailaddress");
//                    邮政编码
                    String zipcode = dynamicObject.getString("zipcode");
//                    获取新增行
                    DynamicObject addNew = ezobEntryentity2.addNew();
//                        变更前送货地点名称
                         addNew.set("ezob_bgqshddmc", ezobshddmc);
                    String ezobbgqshddmc = addNew.getString("ezob_bgqshddmc");
//                        变更前送货联系人名称
                           addNew.set("ezob_bgqshlxrmc", name);
                    String ezobbgqshlxrmc = addNew.getString("ezob_bgqshlxrmc");
//                        变更前贸易术语
                        addNew.set("ezob_bgqmysy", hihntradeterms);
                    String ezobbgqmysy = addNew.getString("ezob_bgqmysy");
//                        变更前行政区划
                        addNew.set("ezob_bgqxzqh", admindivision);
                    String ezobbgqxzqh = addNew.getString("ezob_bgqxzqh");
//                        变更前详细地址
                        addNew.set("ezob_bgqxxdzdz", detailaddress);
                    String ezobbgqxxdzdz = addNew.getString("ezob_bgqxxdzdz");
//                        变更前邮政编码
                        addNew.set("ezob_bgqyzbmdz", zipcode);

                }
                }

//                Object[] save = SaveServiceHelper.save(khbg);
                // 刷新整个页面
                OperationServiceHelper.executeOperate("save","ezob_khbgd",khbg);
//                SaveServiceHelper.update(khbg);
//                SaveServiceHelper.update(khbg, OperateOption.create());
//                SaveServiceHelper.save(khbg);
            }


        }
//        } catch (Exception e1) {
//            this.operationResult.setMessage(e1.getMessage());
//            return;
//        }
    }

}
