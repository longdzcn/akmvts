package kd.cosmic.sc;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * 描述: 采购订单点提交生成合同号
 * 开发者: 钟有吉
 * 关键客户：刘雨婕，黄淑玲
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */

public class ReceivingContract  extends AbstractOperationServicePlugIn {
    private static Log log = LogFactory.getLog(ReceivingContract.class);
    @Override
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
            log.info("插件执行");
//        进到循环，把数据循环一遍
        String  newName = "hth";
        QFilter w = new QFilter("number", QCP.equals, newName);
        DynamicObject hth = BusinessDataServiceHelper.loadSingle("bos_assistantdatagroup", new QFilter[]{w});
        log.info("hth:" + hth);
        for (DynamicObject dy : e.getDataEntities()) {
                final  String zc="pm_PurOrderBill_Asset_BT_S";
                String billId = String.valueOf(dy.getPkValue());
                dy.getString("billno");
                String tr=null;
                DynamicObject aBILL = BusinessDataServiceHelper.loadSingle(billId, "pm_purorderbill", "billno,org,dvconap,linetype,billentry,ezob_textfield");
                String n = aBILL.getDynamicObject("org").getPkValue().toString();
                DynamicObject norg = aBILL.getDynamicObject("org");
                QFilter q = new QFilter("number", QCP.equals, aBILL.getString("billno"));
//                辅助资料的编码与名称
                DynamicObject[] load = BusinessDataServiceHelper.load("bos_assistantdata_detail", "number,name,group,status,enable,createorg,ctrlstrategy,ezob_textfield", new QFilter[]{q});

                String billtype = dy.getString("billtype.number");
//                LE0006	广州美维科技有限公司
//                LE0002	美维科技有限公司
//                LE0003	上海美维电子有限公司
//                LE0004	上海美维科技有限公司
//                LE0005	上海凯思尔电子有限公司
//                LE0009    厦门安捷利美维科技有限公司
                log.info("n:" + n);
                if (load.length == 0 &&(n.equals("1724820510136626176")||n.equals("1724819622093417472")||n.equals("1724819792365379584")||n.equals("1724820077292841984")||n.equals("1724820279533791232")||n.equals("1724821433898526720")) ) {
//                    判断单据类型是否是资产类
                    if (billtype.equals(zc)){

                        //  订单获取编号
                        String bm = aBILL.getString("billno");
//                分录订单明细
                        DynamicObjectCollection billentry = aBILL.getDynamicObjectCollection("billentry");
//                获取一个新的合同号模型
                        DynamicObject jfzxAcceptance = BusinessDataServiceHelper.newDynamicObject("bos_assistantdata_detail");

                        jfzxAcceptance.set("number", bm);
                        log.info("采购订单编码：" + bm);
//               设置合同号类别
//                Long t = Long.valueOf(1724711498447609856l);
//                        Long newName = 1724711498447609856l;
//                        jfzxAcceptance.set("group", htnewNameh);
                        jfzxAcceptance.set("group", hth);
//                数据状态
                        jfzxAcceptance.set("status", "C");
//                使用状态
                        jfzxAcceptance.set("enable", 1);
//                创建组织
//                Integer t = new Integer("100000");
//                创建组织
//                        jfzxAcceptance.set("createorg", n);
                        jfzxAcceptance.set("createorg", norg);

//                控制策略
                        jfzxAcceptance.set("ctrlstrategy", "6");
//                    主数据内码
//                    jfzxAcceptance.set("masterid",bm);
//                    长编码
//                    jfzxAcceptance.set("longnumber",bm);
                        log.info("for循环操作执行前");
                        for (DynamicObject flObject : billentry) {
                            Object l = flObject.get("ezob_textfield");
                            tr= bm+l;
//                            截断前八十个字符
                            String result = tr.substring(0, Math.min(tr.length(), 80));
                            jfzxAcceptance.set("name",result);
                            break;
//                        loadn.set("number", bm);
                        }
                        log.info("for循环操作执行后");
//                 }
//                SaveServiceHelper.save(new DynamicObject[]{jfzxAcceptance});
                        //重新保存，注意要用saveOperate才会经过校验，用save就直接保存数据库了。
//               OperationResult operationResult =
//                      SaveServiceHelper.saveOperate("bos_assistantdata_detail", new DynamicObject[]{jfzxAcceptance},
//                               OperateOption.create());

                        Object[] save = SaveServiceHelper.save(new DynamicObject[]{jfzxAcceptance});
                        //返回
                        log.info("保存操作执行前");
                        OperationResult result = OperationServiceHelper.executeOperate("save","bos_assistantdata_detail", new DynamicObject[]{jfzxAcceptance}, OperateOption.create());
                        log.info("保存操作执行后");
                    }


                }
//
                }
            }
    }





