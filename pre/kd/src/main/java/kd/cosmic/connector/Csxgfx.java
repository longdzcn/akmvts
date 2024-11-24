package kd.cosmic.connector;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;


/**
 * 描述: CS相关反写客户地址
 * 开发者: 钟有吉
 * 关键客户：keller
 * 已部署正式：ture
 * 备注：目前只在测试环境，还在测试阶段
 */

public class Csxgfx extends AbstractOperationServicePlugIn {
    public void endOperationTransaction(EndOperationTransactionArgs e) {
        DynamicObject[] load = null;
        for (DynamicObject dy : e.getDataEntities()) {

//            获取单据id
            Long billId = Long.valueOf(String.valueOf(dy.getPkValue()));//获取客户变更单ID
//            获取CS相关数据
            DynamicObject bILL = BusinessDataServiceHelper.loadSingle(billId, "ezob_fsxg", "ezob_entryentity1,ezob_bghzdkh,ezob_khyhxx,ezob_bghyhzh,ezob_bghzhmc,ezob_bghkhyh,ezob_bghbb,ezob_bghshdzmc,ezob_bghshlxrmc,ezob_bghmysy,ezob_bghxzqh,ezob_bghxxdzdz,ezob_bghyzbmdz,ezob_bghxzqh,ezob_khdzbm,ezob_bgheroi,ezob_bghhscode,ezob_bghysfs,ezob_bghhdgs,ezob_bghlxdh,ezob_bghdzyx,ezob_bghsfsx,ezob_bghsfmr");
//            获取客户编码
            String number = bILL.getString("number");
            QFilter q = new QFilter("number", QCP.equals, number);
//            获取客户信息
            DynamicObject kh = BusinessDataServiceHelper.loadSingle("bd_customer", "ezob_entry_khxx,ezob_zdkh,entry_bank,bankaccount,accountname,bank,currency,number,entry_linkman,contactperson,contactpersonpost,email,masterid", new QFilter[]{q});
//            主数据内码
            String idkh = kh.getString("masterid");
//            送货地址变更
            DynamicObjectCollection ezobentryentity2 = bILL.getDynamicObjectCollection("ezob_entryentity2");
//           CS相关地址分录
            for (DynamicObject dynamicObject : ezobentryentity2) {
//                获取客户地址编码
                String ezobKhdzbm = dynamicObject.getString("ezob_khdzbm");
                QFilter q1 = new QFilter("number", QCP.equals, ezobKhdzbm);
//            获取客户地址客户地址
                load = BusinessDataServiceHelper.load("bd_address", "bd_customer,detailaddress,ezob_shddmc,addemail,name,phone,zipcode,ezob_svmif,hihn_tradeterms,hihn_tradeterms.number,addnumber,number,ezob_ysfs,admindivision,ezob_eroi,ezob_hscode,hihn_clearanceco,hihn_forwarderco,phone,addemail,invalid,default", new QFilter[]{q1});

                //                送货联系人名称
                String ezobBgqshddmc = dynamicObject.getString("ezob_bghshlxrmc");
//                    变更后送货地点名称
                String bgqshddmc = dynamicObject.getString("ezob_bghshdzmc");
//                    变更后贸易术语
                DynamicObject ezobBghmysy = dynamicObject.getDynamicObject("ezob_bghmysy");
//                    变更后行政区划
                Object ezobBghxzqh = dynamicObject.getString("ezob_bghxzqh");
//                    变更后详细地址
                String ezobBghxxdzdz = dynamicObject.getString("ezob_bghxxdzdz");
//                    变更后邮政编码
                String ezobBghyzbmdz = dynamicObject.getString("ezob_bghyzbmdz");
//                    变更后EROI
                String ezobbgheroi = dynamicObject.getString("ezob_bgheroi");
//                    变更后HS Code
                String ezobbghhscode = dynamicObject.getString("ezob_bghhscode");
//                    变更后送货方式
                DynamicObject bghysfs = dynamicObject.getDynamicObject("ezob_bghysfs");
//                    变更后货代公司
                String ezobbghhdgs = dynamicObject.getString("ezob_bghhdgs");
//                    变更后联系电话
                String ezobbghlxdh = dynamicObject.getString("ezob_bghlxdh");
//                    变更后电子邮箱
                String ezobbghdzyx = dynamicObject.getString("ezob_bghdzyx");
//                    变更后是否失效
                String ezobbghsfsx = dynamicObject.getString("ezob_bghsfsx");
//                    变更后是否默认
                String ezobbghsfmr = dynamicObject.getString("ezob_bghsfmr");
//                    当有地址的时候
                if (load.length > 0) {
                    for (DynamicObject khdz : load) {
                        khdz.set("name", ezobBgqshddmc);
//                    送货地点名称
                        khdz.set("ezob_shddmc", bgqshddmc);
//                    贸易术语
                        khdz.set("hihn_tradeterms", ezobBghmysy);
//                    行政区分
                        khdz.set("admindivision", ezobBghxzqh);
//                    详细地址
                        khdz.set("detailaddress", ezobBghxxdzdz);
//                    邮政编码
                        khdz.set("zipcode", ezobBghyzbmdz);
//                        EROI
                        khdz.set("ezob_eroi", ezobbgheroi);
//                        变更后HS Code
                        khdz.set("ezob_hscode", ezobbghhscode);

//                     送货方式
                        khdz.set("ezob_ysfs", bghysfs);
//                    货代公司
                        khdz.set("hihn_forwarderco", ezobbghhdgs);
//                    联系电话
                        khdz.set("phone", ezobbghlxdh);
//                    电子邮箱
                        khdz.set("addemail", ezobbghdzyx);
//                    是否失效
                        khdz.set("invalid", ezobbghsfsx);
//                    是否默认
                        khdz.set("default", ezobbghsfmr);
                        SaveServiceHelper.save(load);
                    }
                } else {
                    //                获取一个新的地址模型模型
                    DynamicObject addnew = BusinessDataServiceHelper.newDynamicObject("bd_address");
//                    获取客户地址编码
                    String ezobKhdzbm1 = dynamicObject.getString("ezob_khdzbm");
                    //                数据状态
                    addnew.set("status", "C");
//                使用状态
                    addnew.set("enable", 1);
//                    给编码赋值
                    addnew.set("number", ezobKhdzbm1);
//                    给客户编码赋值
                    addnew.set("customer", kh.getPkValue());
//                    给客户id赋值
                    addnew.set("customerid", idkh);
//                    是否是客户
                    addnew.set("iscustomeradd", 1);

                    addnew.set("name", ezobBgqshddmc);
//                    送货地点名称
                    addnew.set("ezob_shddmc", bgqshddmc);
//                    贸易术语
                    addnew.set("hihn_tradeterms", ezobBghmysy);
//                    行政区分
                    addnew.set("admindivision", ezobBghxzqh);
//                    详细地址
                    addnew.set("detailaddress", ezobBghxxdzdz);
//                    邮政编码
                    addnew.set("zipcode", ezobBghyzbmdz);
//                        EROI
                    addnew.set("ezob_eroi", ezobbgheroi);
//                        变更后HS Code
                    addnew.set("ezob_hscode", ezobbghhscode);

//                     送货方式
                    addnew.set("ezob_ysfs", bghysfs);
//                    货代公司
                    addnew.set("hihn_forwarderco", ezobbghhdgs);
//                    联系电话
                    addnew.set("phone", ezobbghlxdh);
//                    电子邮箱
                    addnew.set("addemail", ezobbghdzyx);
//                    是否失效
                    addnew.set("invalid", ezobbghsfsx);
//                    是否默认
                    addnew.set("default", ezobbghsfmr);


                    Object[] save = SaveServiceHelper.save(new DynamicObject[]{addnew});
                }
                OperationServiceHelper.executeOperate("save", "ezob_khbgd", new DynamicObject[]{kh});
            }
        }
    }
}
