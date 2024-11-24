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
 * 描述: CS相关携带客户地址
 * 开发者: 钟有吉
 * 关键客户：keller
 * 已部署正式：ture
 * 备注：目前只在测试环境，还在测试阶段
 */
public class Csxgxd extends AbstractConvertPlugIn {


    public void afterConvert(AfterConvertEventArgs e) {
            //
            ExtendedDataEntitySet targetExtDataEntitySet = e.getTargetExtDataEntitySet();
//        获取变更单实体
            ExtendedDataEntity[] extendedDataEntities = targetExtDataEntitySet.FindByEntityKey("ezob_fsxg");
            for (ExtendedDataEntity extendedDataEntity : extendedDataEntities) {
                DynamicObject dataEntity = extendedDataEntity.getDataEntity();//单个单据数据
//            获取变更单地址分录
                DynamicObjectCollection ezobEntryentity2 = dataEntity.getDynamicObjectCollection("ezob_entryentity2");
//            获取变更单编码
                String number = extendedDataEntity.getDataEntity().getString("number");
                QFilter q = new QFilter("customer.number", QCP.equals, number);
                //                获取客户地址
                DynamicObject[] load = BusinessDataServiceHelper.load("bd_address", "ezob_ysfs,bd_customer,detailaddress,ezob_shddmc,addemail,name,phone,zipcode,ezob_svmif,hihn_tradeterms,hihn_tradeterms.number,addnumber,number,hihn_clearanceco,admindivision,number,ezob_eroi,ezob_hscode,hihn_clearanceco,hihn_forwarderco,phone,addemail,invalid,default", new QFilter[]{q});
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
//                    邮政编码
                    String zipcode = dynamicObject.getString("zipcode");
//                    EROI
                    String ezoberoi = dynamicObject.getString("ezob_eroi");
//                    HS Code
                    String ezobhscode = dynamicObject.getString("ezob_hscode");

//                    送货方式
                    DynamicObject hihnclearanceco = dynamicObject.getDynamicObject("ezob_ysfs");
//                    货代公司
                    String hihnforwarderco = dynamicObject.getString("hihn_forwarderco");
//                    联系电话
                    String phone = dynamicObject.getString("phone");
//                    电子邮箱
                    String addemail = dynamicObject.getString("addemail");
//                    是否失效
                    String invalid = dynamicObject.getString("invalid");
//                    是否默认
                    String defaultm = dynamicObject.getString("default");


//                新增行
                    DynamicObject ent = ezobEntryentity2.addNew();
                    //                        变更前送货地点名称
                    ent.set("ezob_bgqshddmc", ezobshddmc);
                    ent.set("ezob_bghshdzmc",ezobshddmc);
//                        变更前送货联系人名称
                    ent.set("ezob_bgqshlxrmc", name);
                    ent.set("ezob_bghshlxrmc", name);
//                        变更前贸易术语
                    ent.set("ezob_bgqmysy", hihntradeterms);
                    ent.set("ezob_bghmysy", hihntradeterms);
//                        变更前行政区划
                    ent.set("ezob_bgqxzqh", admindivision);
                    ent.set("ezob_bghxzqh", admindivision);
//                        变更前详细地址
                    ent.set("ezob_bgqxxdzdz", detailaddress);
                    ent.set("ezob_bghxxdzdz", detailaddress);
//                        变更前邮政编码
                    ent.set("ezob_bgqyzbmdz", zipcode);
                    ent.set("ezob_bghyzbmdz", zipcode);
//                地址编码
                    ent.set("ezob_khdzbm",n);
                    // EROI
                    ent.set("ezob_bgqeroi",ezoberoi);
                    ent.set("ezob_bgheroi",ezoberoi);
//                    HS Code
                    ent.set("ezob_bgqhscode",ezobhscode);
                    ent.set("ezob_bghhscode",ezobhscode);
//                    变更前送货方式
                    ent.set("ezob_bgqysfs",hihnclearanceco);
                    ent.set("ezob_bghysfs",hihnclearanceco);
//                    变更前货代公司
                    ent.set("ezob_bgqhdgs",hihnforwarderco);
                    ent.set("ezob_bghhdgs",hihnforwarderco);
//                    变更前联系电话
                    ent.set("ezob_bgqlxdh",phone);
                    ent.set("ezob_bghlxdh",phone);
//                    变更前电子邮箱
                    ent.set("ezob_bgqdzyx",addemail);
                    ent.set("ezob_bghdzyx",addemail);
//                    变更前是否失效
                    ent.set("ezob_bgqsfsx",invalid);
                    ent.set("ezob_bghsfsx",invalid);
//                    变更前是否默认
                    ent.set("ezob_bgqsfmr",defaultm);
                    ent.set("ezob_bghsfmr",defaultm);
                    OperationServiceHelper.executeOperate("save","ezob_fsxg",new DynamicObject[]{dataEntity});
                }
            }

    }
}






