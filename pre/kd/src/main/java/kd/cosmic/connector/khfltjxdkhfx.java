package kd.cosmic.connector;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;


/**
 * 描述: 客户分类添加下单客户反写
 * 开发者: 钟有吉
 * 关键客户：keller
 * 已部署正式：ture
 * 备注：目前只在测试环境，还在测试阶段
 */

public class khfltjxdkhfx extends AbstractOperationServicePlugIn {
    private static Log log = LogFactory.getLog(khfltjxdkhfx.class);
    public void endOperationTransaction(EndOperationTransactionArgs e) {

        for (DynamicObject dy : e.getDataEntities()) {

                String khid = null;
//            获取单据id
                Long billId = Long.valueOf(String.valueOf(dy.getPkValue()));//获取客户变更单ID
//            获取客户分类添加下单客户数据
                DynamicObject aBILL = BusinessDataServiceHelper.loadSingle(billId, "ezob_khfltjxdkh", "ezob_bgqyzbmdz,ezob_bgqxxdzdz,ezob_bgqxzqh,ezob_bgqmysy,ezob_bgqshlxrmc,ezob_bgqshddmc,ezob_entryentity1,ezob_bghzdkh,ezob_khyhxx,ezob_bghyhzh,ezob_bghzhmc,ezob_bghkhyh,ezob_bghbb,ezob_bghshdzmc,ezob_bghshlxrmc,ezob_bghmysy,ezob_bghxzqh,ezob_bghlxdh,ezob_bghxxdzdz,ezob_bghyzbmdz,ezob_bghxzqh,ezob_khdzbm,ezob_entryentity,ezob_bghmz,ezob_bghzw,ezob_bghyx,ezob_bgqdzyj,ezob_bghdzyj,ezob_dzbgqlxdh,ezob_dzbghlxdh");
//            获取客户编码
                String number = aBILL.getString("number");
                QFilter q = new QFilter("number", QCP.equals, number);

//            获取客户信息
                DynamicObject kh = BusinessDataServiceHelper.loadSingle("bd_customer","addphone,ezob_entry_khxx,ezob_zdkh,entry_bank,phone,bankaccount,accountname,bank,currency,number,entry_linkman,contactperson,contactpersonpost,email,masterid",new QFilter[]{q});

//            获取终端客户分录
                DynamicObjectCollection ezobEntryKhxx = kh.getDynamicObjectCollection("ezob_entry_khxx");
//           终端客户变更分录
                DynamicObjectCollection ezobEntryentity1 = aBILL.getDynamicObjectCollection("ezob_entryentity1");
//            送货地址变更
                DynamicObjectCollection ezobentryentity2 = aBILL.getDynamicObjectCollection("ezob_entryentity2");
//            清空终端客户分录数据
                ezobEntryKhxx.clear();
                for (DynamicObject dynamicObject : ezobEntryentity1) {
//                变更后终端客户
//                String ezobBghzdkh = dynamicObject.getString("ezob_bghzdkh.number");
                    DynamicObject ezobBghzdkh = dynamicObject.getDynamicObject("ezob_bghzdkh");
//                遍历终端客户分录
                    DynamicObject add = ezobEntryKhxx.addNew();
//                反写终端客户
                    add.set("ezob_zdkh",ezobBghzdkh);
                    Object o = add.get("ezob_zdkh");
                }
//            String idkh = kh.getString("masterid");
//
////            客户分类添加地址分录
//                for (DynamicObject dynamicObject : ezobentryentity2) {
////                获取客户地址编码
//                    String ezobKhdzbm = dynamicObject.getString("ezob_khdzbm");
//                    QFilter q1 = new QFilter("number", QCP.equals, ezobKhdzbm);
////            获取客户地址客户地址
//                    DynamicObject[] load = BusinessDataServiceHelper.load("bd_address", "customer,detailaddress,ezob_shddmc,addemail,name,phone,zipcode,ezob_svmif,hihn_tradeterms,hihn_tradeterms.number,addnumber,number,hihn_clearanceco,admindivision,admindivisiondata,masterid", new QFilter[]{q1});
//                    if (load.length>0){
//                        //                送货联系人名称
//                        String ezobBghshlxrmc = dynamicObject.getString("ezob_bghshlxrmc");
//                        String ezobBgqshlxrmc = dynamicObject.getString("ezob_bgqshlxrmc");
////                    变更后送货地点名称
//                        String bghshddmc = dynamicObject.getString("ezob_bghshdzmc");
//                        String bgqshddmc = dynamicObject.getString("ezob_bgqshddmc");
////                    变更后贸易术语
//                        DynamicObject ezobBghmysy = dynamicObject.getDynamicObject("ezob_bghmysy");
//                        DynamicObject ezobBgqmysy = dynamicObject.getDynamicObject("ezob_bgqmysy");
////                    变更后行政区划
//                        String ezobBghxzqh = dynamicObject.getString("ezob_bghxzqh");
//                        String ezobBgqxzqh = dynamicObject.getString("ezob_bgqxzqh");
////                    变更后详细地址
//                        String ezobBghxxdzdz = dynamicObject.getString("ezob_bghxxdzdz");
//                        String ezobBgqxxdzdz = dynamicObject.getString("ezob_bgqxxdzdz");
////                    变更后邮政编码
//                        String ezobBghyzbmdz = dynamicObject.getString("ezob_bghyzbmdz");
//                        String ezobBgqyzbmdz = dynamicObject.getString("ezob_bgqyzbmdz");
////                        变更后电子邮件
//                        String ezobBghdzyj = dynamicObject.getString("ezob_bghdzyj");
//                        String ezobBgqdzyj = dynamicObject.getString("ezob_bgqdzyj");
//                        //                        变更后联系电话
//                        String ezobDzbghlxdh = dynamicObject.getString("ezob_dzbghlxdh");
//                        String ezobDzbgqlxdh = dynamicObject.getString("ezob_dzbgqlxdh");
//                        for (DynamicObject khdz : load) {
////                       object= khdz.getDynamicObject("customer");
//                            //                    行政区划(后台逻辑字段)
////                        String admindivisiondata = khdz.getString("admindivisiondata");
////                    获取客户id     内码数据masterid
////                            String customerid = khdz.getString("masterid");
////                            if (customerid != null && !customerid.isEmpty()) {
////                                khid=customerid;
////                            } else {
////
////                            }
//                            //送货联系人名称
//                            if (ezobBghshlxrmc != "") {
//                                khdz.set("name", ezobBghshlxrmc);
//                            } else {
//                                khdz.set("name", ezobBgqshlxrmc);
//                            }
////                    送货地点名称
//                            if (bghshddmc != "") {
//                                khdz.set("ezob_shddmc", bghshddmc);
//                            } else {
//                                khdz.set("ezob_shddmc", bgqshddmc);
//                            }
////                    贸易术语
//                            if (ezobBghmysy != null) {
//                                khdz.set("hihn_tradeterms", ezobBghmysy);
//                            } else {
//                                khdz.set("hihn_tradeterms", ezobBgqmysy);
//                            }
////                    行政区分
//                            if (ezobBghxzqh != "") {
//                                khdz.set("admindivision", ezobBghxzqh);
//                            } else {
//                                khdz.set("admindivision", ezobBgqxzqh);
//                            }
////                    详细地址
//                            if (ezobBghxxdzdz != ""){
//                                khdz.set("detailaddress",ezobBghxxdzdz);
//                            } else {
//                                khdz.set("detailaddress",ezobBgqxxdzdz);
//                            }
////                    邮政编码
//                            if (ezobBghyzbmdz != "") {
//                                khdz.set("zipcode", ezobBghyzbmdz);
//                            } else {
//                                khdz.set("zipcode", ezobBgqyzbmdz);
//                            }
////                            电子邮件
//                            if (ezobBghdzyj != "") {
//                                khdz.set("addemail", ezobBghdzyj);
//                            } else {
//                                khdz.set("addemail", ezobBgqdzyj);
//                            }
//                            //                            联系电话
//                            if (ezobDzbghlxdh != "") {
//                                khdz.set("phone", ezobDzbghlxdh);
//                            } else {
//                                khdz.set("phone", ezobDzbgqlxdh);
//                            }
//                        }
//
//                        SaveServiceHelper.save(load);
//                    }else {
//                        //                获取一个新的地址模型模型
//                        DynamicObject addnew = BusinessDataServiceHelper.newDynamicObject("bd_address");
//                        //                送货联系人名称
//                        String ezobBghshlxrmc = dynamicObject.getString("ezob_bghshlxrmc");
//                        String ezobBgqshlxrmc = dynamicObject.getString("ezob_bgqshlxrmc");
////                    变更后送货地点名称
//                        String bghshddmc = dynamicObject.getString("ezob_bghshdzmc");
//                        String bgqshddmc = dynamicObject.getString("ezob_bgqshddmc");
////                    变更后贸易术语
//                        DynamicObject ezobBghmysy = dynamicObject.getDynamicObject("ezob_bghmysy");
//                        DynamicObject ezobBgqmysy = dynamicObject.getDynamicObject("ezob_bgqmysy");
////                    变更后行政区划
//                        String ezobBghxzqh = dynamicObject.getString("ezob_bghxzqh");
//                        String ezobBgqxzqh = dynamicObject.getString("ezob_bgqxzqh");
////                    变更后详细地址
//                        String ezobBghxxdzdz = dynamicObject.getString("ezob_bghxxdzdz");
//                        String ezobBgqxxdzdz = dynamicObject.getString("ezob_bgqxxdzdz");
////                    变更后邮政编码
//                        String ezobBghyzbmdz = dynamicObject.getString("ezob_bghyzbmdz");
//                        String ezobBgqyzbmdz = dynamicObject.getString("ezob_bgqyzbmdz");
//                        //                        变更后电子邮件
//                        String ezobBghdzyj = dynamicObject.getString("ezob_bghdzyj");
//                        String ezobBgqdzyj = dynamicObject.getString("ezob_bgqdzyj");
//                        //                        变更后联系电话
//                        String ezobDzbghlxdh = dynamicObject.getString("ezob_dzbghlxdh");
//                        String ezobDzbgqlxdh = dynamicObject.getString("ezob_dzbgqlxdh");
////                    获取客户地址编码
//                        String ezobKhdzbm1 = dynamicObject.getString("ezob_khdzbm");
//                        //                数据状态
//                        addnew.set("status", "C");
////                使用状态
//                        addnew.set("enable", 1);
////                    给编码赋值
//                        addnew.set("number",ezobKhdzbm1);
////                    给客户编码赋值
//                        addnew.set("customer",kh.getPkValue());
////                    给客户id赋值
//                        addnew.set("customerid",idkh);
////                    送货联系人名称
//                        if (ezobBghshlxrmc != "") {
//                            addnew.set("name", ezobBghshlxrmc);
//                        } else {
//                            addnew.set("name", ezobBgqshlxrmc);
//                        }
////                    送货地点名称
//                        if (bghshddmc != "") {
//                            addnew.set("ezob_shddmc", bghshddmc);
//                        } else {
//                            addnew.set("ezob_shddmc", bgqshddmc);
//                        }
////                    贸易术语
//                        if (ezobBghmysy != null) {
//                            addnew.set("hihn_tradeterms", ezobBghmysy);
//                        } else {
//                            addnew.set("hihn_tradeterms", ezobBgqmysy);
//                        }
////                    行政区分
//                        if (ezobBghxzqh != "") {
//                            addnew.set("admindivision", ezobBghxzqh);
//                        } else {
//                            addnew.set("admindivision", ezobBgqxzqh);
//                        }
////                    详细地址
//                        if (ezobBghxxdzdz != ""){
//                            addnew.set("detailaddress",ezobBghxxdzdz);
//                        } else {
//                            addnew.set("detailaddress",ezobBgqxxdzdz);
//                        }
////                    邮政编码
//                        if (ezobBghyzbmdz != "") {
//                            addnew.set("zipcode", ezobBghyzbmdz);
//                        } else {
//                            addnew.set("zipcode", ezobBgqyzbmdz);
//                        }
//                        //                            电子邮件
//                        if (ezobBghdzyj != "") {
//                            addnew.set("addemail", ezobBghdzyj);
//                        } else {
//                            addnew.set("addemail", ezobBgqdzyj);
//                        }
//                        //                            联系电话
//                        if (ezobDzbghlxdh != "") {
//                            addnew.set("phone", ezobDzbghlxdh);
//                        } else {
//                            addnew.set("phone", ezobDzbgqlxdh);
//                        }
////                    是否是客户
//                        addnew.set("iscustomeradd",1);
//
//                        Object[] save = SaveServiceHelper.save(new DynamicObject[]{addnew});
//
////                        OperationResult result = OperationServiceHelper.executeOperate("save","bd_address", new DynamicObject[]{addnew}, OperateOption.create());
//
//                    }
//
//
//                }
////            获取（变更联系人分录）
//                DynamicObjectCollection ezobentryentity = aBILL.getDynamicObjectCollection("ezob_entryentity");
////            获取客户联系人分录
//                DynamicObjectCollection entrylinkman = kh.getDynamicObjectCollection("entry_linkman");
////            清除分录数据
//                entrylinkman.clear();
//                for (DynamicObject dynamicObject : ezobentryentity) {
////                变更后名字
//                    String ezobBghmz = dynamicObject.getString("ezob_bghmz");
////                变更后职务
//                    String ezobbghzw = dynamicObject.getString("ezob_bghzw");
////                变更后邮箱
//                    String ezobbghyx = dynamicObject.getString("ezob_bghyx");
////                    变更后联系电话
//                    String ezoblxdh = dynamicObject.getString("ezob_bghlxdh");
////                获取客户分录新增行
//                    DynamicObject khaddw = entrylinkman.addNew();
////                名字
//                    khaddw.set("contactperson",ezobBghmz);
////                职务
//                    khaddw.set("contactpersonpost",ezobbghzw);
////                邮箱
//                    khaddw.set("email",ezobbghyx);
////                    联系电话
//                    khaddw.set("phone",ezoblxdh);
//                }


                //            获取（变更）开户银行信息分录
                DynamicObjectCollection ezobKhyhxx = aBILL.getDynamicObjectCollection("ezob_khyhxx");
//            获取开户银行信息分录
                DynamicObjectCollection entryBank = kh.getDynamicObjectCollection("entry_bank");
//            清除开户银行信息分录
                entryBank.clear();
                for (DynamicObject khyhxx : ezobKhyhxx) {
//                变更后银行账号
                    String ezobBghyhzh = khyhxx.getString("ezob_bghyhzh");
//                变更后账户名称
                    String ezobBghzhmc = khyhxx.getString("ezob_bghzhmc");
//                变更后开户银行
//                String ezobBghkhyh = khyhxx.getString("ezob_bghkhyh.number");
                    DynamicObject ezobBghkhyh = khyhxx.getDynamicObject("ezob_bghkhyh");
//                变更后币别
//                String ezobBghbb = khyhxx.getString("ezob_bghbb.number");
                    DynamicObject ezobBghbb = khyhxx.getDynamicObject("ezob_bghbb");
//                获取新增行
                    DynamicObject add = entryBank.addNew();
//                反写银行账号
                    add.set("bankaccount",ezobBghyhzh);
                    Object o = add.get("bankaccount");
//                反写账户名称
                    add.set("accountname",ezobBghzhmc);
                    Object p = add.get("accountname");
//                反写开户银行
                    add.set("bank",ezobBghkhyh);
                    Object t = add.get("bank");
//                反写币别
                    add.set("currency",ezobBghbb);
                    Object o1 = add.get("currency");
                }
//            OperationServiceHelper.executeOperate("save","ezob_khbgd",new DynamicObject[]{kh});


//
                SaveServiceHelper.save(new DynamicObject[]{kh});



        }



    }



}
