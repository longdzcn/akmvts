package kd.cosmic.connector;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * 描述: 客户变更反写客户
 * 开发者: 钟有吉
 * 关键客户：keller
 * 已部署正式：ture
 * 备注：目前只在测试环境，还在测试阶段
 */
//kd.cosmic.gxgyslxr
public class gxkhlxr extends AbstractOperationServicePlugIn {
    public void endOperationTransaction(EndOperationTransactionArgs e) {
//        try {
        for (DynamicObject dy : e.getDataEntities()) {
            Long billId = Long.valueOf(String.valueOf(dy.getPkValue()));//获取客户变更单ID
            DynamicObject bill = BusinessDataServiceHelper.loadSingle(billId, "ezob_khfltjxdkh", "number,ezob_entryentity,ezob_entryentity1,ezob_entryentity2,ezob_bgqmz,ezob_bghlxdh,ezob_bghmz,,ezob_bgqzw,ezob_bghzw,ezob_bgqyx,ezob_bghyx,ezob_bghzdkh,ezob_bghzdkh.number,ezob_bgqshddmc,ezob_bgqshlxrmc,ezob_bgqmysy,ezob_bghmysy,ezob_bgqxzqh,ezob_bghxzqh,ezob_bgqxxdzdz,ezob_bghxxdzdz,ezob_bgqyzbmdz,ezob_bghyzbmdz,ezob_bghshdzmc,ezob_bghshlxrmc,addfulladdress");
//               变更联系人
            DynamicObjectCollection entry = bill.getDynamicObjectCollection("ezob_entryentity");
//                终端客户变更
            DynamicObjectCollection entry1 = bill.getDynamicObjectCollection("ezob_entryentity1");
//                地址变更
            DynamicObjectCollection entry2 = bill.getDynamicObjectCollection("ezob_entryentity2");
//                DynamicObject bdcustomer = BusinessDataServiceHelper.loadSingle(billId, "bd_customer");
            String number = bill.getString("number");
//                根据变更客户查询客户
            QFilter q = new QFilter("number", QCP.equals, bill.getString("number"));
            DynamicObject[] kh = BusinessDataServiceHelper.load( "bd_customer", "entry_linkman,ezob_entry_khxx,entry_address,contactperson,contactpersonpost,phone,email,ezob_zdkh,ezob_zdkh.number,addname,ezob_shddmc,hihn_tradeterms,addadmindivision,addpostalcode,addfulladdress,hihn_tradeterms",new QFilter[]{q});
//
            //                联系人变更
//                    反写到客户联系人列表
            for (DynamicObject bgkhfz : kh) {
                DynamicObjectCollection entryLinkman = bgkhfz.getDynamicObjectCollection("entry_linkman");
                //                            清除行
                entryLinkman.clear();
                for (DynamicObject entryq : entry) {
//                    更改名字
                    String ezobBghmz = entryq.getString("ezob_bghmz");
//                    变更职务
                    String ezobBghzw = entryq.getString("ezob_bghzw");
//                    变更邮箱
                    String ezobBghyx = entryq.getString("ezob_bghyx");
//                    变更后联系电话
                    String ezoblxdh = entryq.getString("ezob_bghlxdh");
                    //                            获取新增行
                    DynamicObject row = entryLinkman.addNew();
                    row.set("contactperson",ezobBghmz);
                    row.set("contactpersonpost",ezobBghzw);
                    row.set("email",ezobBghyx);
                    row.set("phone",ezoblxdh);
                }

            }
//                终端客户变更
            for (DynamicObject dynamicObject : kh) {
                //                        获取客户表的字段
                DynamicObjectCollection ezobEntryKhxx = dynamicObject.getDynamicObjectCollection("ezob_entry_khxx");
//                清除行
                ezobEntryKhxx.clear();
//                终端客户变更
                for (DynamicObject entryjd : entry1) {
                    Object ezobBghzdkh = entryjd.getDynamicObject("ezob_bghzdkh").getPkValue().toString();
                    String o = entryjd.getString("ezob_bghzdkh.number");

                    //                            获取新增行
                    DynamicObject row = ezobEntryKhxx.addNew();
//                            给客户表字段复制
                    row.set("ezob_zdkh",ezobBghzdkh);


                }

            }
//            地址变更
            for (DynamicObject dynamicObject : kh) {
                DynamicObjectCollection entryAddress = dynamicObject.getDynamicObjectCollection("entry_address");
//                清除行
//                entryAddress.clear();
                for (DynamicObject object : entry2) {
//                    送货联系人名称
                    String ezobBghshlxrmc = object.getString("ezob_bghshlxrmc");
                    String ezobBgqshlxrmc = object.getString("ezob_bgqshlxrmc");
//                    变更后送货地点名称
                    String bghshddmc = object.getString("ezob_bghshdzmc");
                    String bgqshddmc = object.getString("ezob_bgqshddmc");//ezob_bgqshddmc
//                    变更后贸易术语
                    DynamicObject ezobBghmysy = object.getDynamicObject("ezob_bghmysy");
                    DynamicObject ezobBgqmysy = object.getDynamicObject("ezob_bgqmysy");
//                    变更后行政区划
                    String ezobBghxzqh = object.getString("ezob_bghxzqh");
                    String ezobBgqxzqh = object.getString("ezob_bgqxzqh");
//                    变更后详细地址
                    String ezobBghxxdzdz = object.getString("ezob_bghxxdzdz");
                    String ezobBgqxxdzdz = object.getString("ezob_bgqxxdzdz");
//                    变更后邮政编码
                    String ezobBghyzbmdz = object.getString("ezob_bghyzbmdz");
                    String ezobBgqyzbmdz = object.getString("ezob_bgqyzbmdz");
                    DynamicObject row = entryAddress.addNew();
//                    送货联系人名称
                    if (ezobBghshlxrmc != "") {
                        row.set("addname", ezobBghshlxrmc);
                    }else {
                        row.set("addname", ezobBgqshlxrmc);
                    }
//                    送货地点名称
                    if (bghshddmc != "") {
                        row.set("ezob_shddmc", bghshddmc);
                    } else {
                        row.set("ezob_shddmc", bgqshddmc);
                    }
//                    贸易术语
                    if (ezobBghmysy != null) {
                        row.set("hihn_tradeterms", ezobBghmysy);
                    } else {
                        row.set("hihn_tradeterms", ezobBgqmysy);
                    }
//                    行政区分
                    if (ezobBghxzqh != "") {
                        row.set("addadmindivision", ezobBghxzqh);
                    } else {
                        row.set("addadmindivision", ezobBgqxzqh);
                    }
//                    详细地址
                    if (ezobBghxxdzdz != "") {
                        row.set("addfulladdress", ezobBghxxdzdz);
                    } else {
                        row.set("addfulladdress", ezobBgqxxdzdz);
                    }
//                    邮政编码
                    if (ezobBghyzbmdz != "") {
                        row.set("addpostalcode", ezobBghyzbmdz);
                    } else {
                        row.set("addpostalcode", ezobBgqyzbmdz);
                    }
                }

            }
//            OperationServiceHelper.executeOperate("save","bd_address",new DynamicObject[]{kh});
            SaveServiceHelper.save(kh);
//                SaveServiceHelper.update(kh);

        }
//        } catch (Exception e1) {
//            this.operationResult.setMessage(e1.toString());
//            return;
//        }
    }
}
