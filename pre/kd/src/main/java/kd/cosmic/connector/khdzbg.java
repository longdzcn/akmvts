package kd.cosmic.connector;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.ExtendedDataEntitySet;
import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
import kd.bos.entity.botp.plugin.args.AfterConvertEventArgs;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;




/**
 * 描述: 客户地址携带数据到客户分类添加地址
 * 开发者: 钟有吉
 * 关键客户：keller
 * 已部署正式：ture
 * 备注：目前只在测试环境，还在测试阶段
 */
public class khdzbg  extends AbstractConvertPlugIn {


    @Override
    public void afterConvert(AfterConvertEventArgs e) {
//        try {
        //
        ExtendedDataEntitySet targetExtDataEntitySet = e.getTargetExtDataEntitySet();
//        获取变更单实体
        ExtendedDataEntity[] extendedDataEntities = targetExtDataEntitySet.FindByEntityKey("ezob_khfltjxdkh");
        for (ExtendedDataEntity extendedDataEntity : extendedDataEntities) {
            DynamicObject dataEntity = extendedDataEntity.getDataEntity();//单个单据数据
//            获取变更单地址分录
            DynamicObjectCollection ezobEntryentity2 = dataEntity.getDynamicObjectCollection("ezob_entryentity2");
//            获取变更单编码
            String number = extendedDataEntity.getDataEntity().getString("number");
            QFilter q = new QFilter("customer.number", QCP.equals, number);
            //                获取客户地址
            DynamicObject[] load = BusinessDataServiceHelper.load("bd_address", "bd_customer,detailaddress,ezob_shddmc,,addemail,name,phone,zipcode,ezob_svmif,hihn_tradeterms,hihn_tradeterms.number,addnumber,number,hihn_clearanceco,admindivision,number", new QFilter[]{q});
//            清空分录数据
            ezobEntryentity2.clear();
            for (DynamicObject dynamicObject : load) {
//                获取编码
                Object n = dynamicObject.get("number");
                //                    送货地址名称
                String ezobshddmc = dynamicObject.getString("ezob_shddmc");
//                    送货联系人名称
                String name = dynamicObject.getString("name");
//                    变更前贸易术语
                DynamicObject hihntradeterms = dynamicObject.getDynamicObject("hihn_tradeterms");
//                    行政区划
                String admindivision = dynamicObject.getString("admindivision");
//                    详细地址
                String detailaddress = dynamicObject.getString("detailaddress");

//                电子邮件
                String dzyj = dynamicObject.getString("addemail");

//                联系电话
                String lxdh = dynamicObject.getString("phone");
//                    邮政编码
                String zipcode = dynamicObject.getString("zipcode");
//                新增行
                DynamicObject ent = ezobEntryentity2.addNew();
                //                        变更前送货地点名称
                ent.set("ezob_bgqshddmc", ezobshddmc);
//                        变更前送货联系人名称
                ent.set("ezob_bgqshlxrmc", name);
//                变更前电子邮件
                ent.set("ezob_bgqdzyj",dzyj);
                //                变更前联系电话
                ent.set("ezob_dzbgqlxdh",lxdh);
//                        变更前贸易术语
                ent.set("ezob_bgqmysy", hihntradeterms);
//                        变更前行政区划
                ent.set("ezob_bgqxzqh", admindivision);
//                        变更前详细地址
                ent.set("ezob_bgqxxdzdz", detailaddress);
//                        变更前邮政编码
                ent.set("ezob_bgqyzbmdz", zipcode);

//                地址编码
                ent.set("ezob_khdzbm",n);


                OperationServiceHelper.executeOperate("save","ezob_khfltjxdkh",new DynamicObject[]{dataEntity});
            }
        }

    }
}






