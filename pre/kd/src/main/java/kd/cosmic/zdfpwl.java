package kd.cosmic;

import kd.bos.data.BusinessDataReader;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.metadata.IDataEntityType;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.botp.runtime.ConvertOperationResult;
import kd.bos.entity.botp.runtime.PushArgs;
import kd.bos.entity.datamodel.IRefrencedataProvider;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.operate.OperateOptionConst;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.basedata.BaseDataServiceHelper;
import kd.bos.servicehelper.botp.ConvertServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 描述:审核后自动分配物料并自动下推物料采购信息、物料质检信息、物料库存信息
 * 开发者: 李四辉
 * 创建日期:2024-04-01
 * 关键客户：仓库
 * 已部署正式：true
 * 备注：已投入正式环境使用，无问题
 */

public class zdfpwl extends AbstractOperationServicePlugIn {
    @Override
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        for (DynamicObject dy : e.getDataEntities()){
            String entityID = "bd_material";//物料实体
            //获取当前物料id
//        DynamicObject[] dy =  e.getDataEntities();
            Long billIds = Long.valueOf(String.valueOf(dy.getPkValue()));
            //获取物料对应组织ID

            DynamicObject wlbill = BusinessDataServiceHelper.loadSingle(billIds,"bd_material",
                    "ezob_wldyzz,number,ezob_wllx,creator");
            String mverpzt = "";
            //获取MVERP账套
            if(wlbill.get("creator.ezob_mverpzt3") != null){
                mverpzt = wlbill.get("creator.ezob_mverpzt3").toString();
            }
            List<Long> orgIDs = new ArrayList<Long>(16);
            if(wlbill.get("ezob_wldyzz") != null) {
                Long wldyzzid = Long.valueOf(wlbill.get("ezob_wldyzz.id").toString());
                orgIDs.add(wldyzzid);//分配到目标组织以及厦门母公司
                orgIDs.add(1724780663468483584L);//LE0001
                orgIDs.add(1724822236310824960L);//LE0012
                orgIDs.add(1724819622093417472L);//LE0002
                orgIDs.add(1724819792365379584L);//LE0003
                orgIDs.add(1724820077292841984L);//LE0004
                orgIDs.add(1724820279533791232L);//LE0005
                orgIDs.add(1724820510136626176L);//LE0006
                orgIDs.add(1724821433898526720L);//LE0009
            }
            else
            {
                //表示创建人是AKM的
                if(mverpzt.equals("")){
                    orgIDs.add(1724780663468483584L);//LE0001新增转单业务物料
//                //先分配到南沙工厂和苏州工厂，其余工厂等到上线后再修改
////                orgIDs.add(1727018577904587776L);//BU-00012
                    orgIDs.add(1724819622093417472L);//LE0002   //【20241108】【ZOC】后续发现MTC也参与了与南沙工厂等交易，所以也要设置自动分配
                    orgIDs.add(1724821971004320768L);//LE0011
                    orgIDs.add(1838391834745816064L);//LE0089
                    orgIDs.add(1724822236310824960L);//LE0012
                    orgIDs.add(1724822469556070400L);//LE0013
                    orgIDs.add(1724822759483141120L);//LE0014
//                orgIDs.add(1727015954182886400L);//LE0015
                    orgIDs.add(1726904003092075520L);//LE0016
//                orgIDs.add(1727016761368301568L);//LE0017
                }else{//表示创建人是MV的
//                    Long WLDYZZID = Long.valueOf(WLBILL.get("ezob_wldyzz.id").toString());
//                    orgIDs.add(WLDYZZID);//分配到目标组织以及厦门母公司

                    orgIDs.add(1724780663468483584L);//LE0001
                    orgIDs.add(1724822236310824960L);//LE0012
                    orgIDs.add(1724819622093417472L);//LE0002
                    orgIDs.add(1724819792365379584L);//LE0003
                    orgIDs.add(1724820077292841984L);//LE0004
                    orgIDs.add(1724820279533791232L);//LE0005
                    orgIDs.add(1724820510136626176L);//LE0006
                    orgIDs.add(1724821433898526720L);//LE0009
                }
//                break;//物料对应组织ID为空不执行后续代码
//                if(WLBILL.get("number").toString().substring(0,3).toUpperCase().equals("AB.")){
//                    orgIDs.add(1724822469556070400L);//LE0013
//                }
//                else if(WLBILL.get("number").toString().substring(0,3).toUpperCase().equals("FZ.")){
//                    orgIDs.add(1724822759483141120L);//LE0014
//                }
//                else if(WLBILL.get("ezob_wllx").toString().equals("2")){
//                    orgIDs.add(1724780663468483584L);//LE0001新增转单业务物料
//                    //产成品分配全部安捷利组织
//                    orgIDs.add(1724822469556070400L);//LE0013
//                    orgIDs.add(1724822759483141120L);//LE0014
//                    orgIDs.add(1724821971004320768L);//LE0011
//                    orgIDs.add(1838391834745816064L);//LE0089
//                    orgIDs.add(1724822236310824960L);//LE0012
//                    orgIDs.add(1726904003092075520L);//LE0016
//                }
//                else
//                {
//                orgIDs.add(1724780663468483584L);//LE0001新增转单业务物料
                //先分配到南沙工厂和苏州工厂，其余工厂等到上线后再修改
//                orgIDs.add(1727018577904587776L);//BU-00012
//                orgIDs.add(1724821971004320768L);//LE0011
//                orgIDs.add(1838391834745816064L);//LE0089
//                orgIDs.add(1724822236310824960L);//LE0012
//                orgIDs.add(1724822469556070400L);//LE0013
//                orgIDs.add(1724822759483141120L);//LE0014
////                orgIDs.add(1727015954182886400L);//LE0015
//                orgIDs.add(1726904003092075520L);//LE0016
//                orgIDs.add(1727016761368301568L);//LE0017
//            }

            }

            Long assignOrgID = 100000L;//分配组织ID
            List<Long> dataIDs = new ArrayList<Long>(16);
            dataIDs.add(billIds);//待分配数据ID

            Map<Long,Map<Long, String>> resultMap = BaseDataServiceHelper.
                    batchAssignWithDetail(entityID,assignOrgID,dataIDs,orgIDs);
            if (resultMap.isEmpty()){

                //循环所有组织
                for (int i = 0; i < orgIDs.size(); i++) {
                    List<String> sTargetEntity = new ArrayList<String>(16);
                    sTargetEntity.add("bd_materialpurchaseinfo");  //物料采购信息
                    if(wlbill.get("ezob_wldyzz") == null) {  //如果是mv的物料则不需要质检信息
                        sTargetEntity.add("bd_inspect_cfg");  //物料质检信息
                    }
                    sTargetEntity.add("bd_materialinventoryinfo");  //物料库存信息
                    sTargetEntity.add("bd_materialsalinfo");  //物料销售信息

                    //调用自动下推服务
                    for (int j = 0; j < sTargetEntity.size(); j++) {
                        PushArgs pushArgs = new PushArgs();
                        pushArgs.setSourceEntityNumber("bd_material");
                        pushArgs.setTargetEntityNumber(sTargetEntity.get(j));
                        // 可选，传入true，不检查目标单新增权
                        pushArgs.setHasRight(false);
                        //注意一定要设置当前组织，不然下推会以安捷利美维集团组织下推
                        pushArgs.setCurrentOrgId(orgIDs.get(i));
                        // 可选，传入目标单主组织默认值
                        pushArgs.setDefOrgId(orgIDs.get(i));
                        // 生成转换结果报告
                        pushArgs.setBuildConvReport(true);
                        // 必填，设置需要下推的单据，或分录行
                        List<ListSelectedRow> rows = new ArrayList<>();
                        //第一行
                        ListSelectedRow row1 = new ListSelectedRow();
                        //必填，设置源单单据id
                        row1.setPrimaryKeyValue(billIds);
                        rows.add(row1);

                        pushArgs.setSelectedRows(rows);
                        // 执行下推服务
                        ConvertOperationResult pushResult = ConvertServiceHelper.push(pushArgs);
//自动提交审核
                        // 获取生成的目标单数据包
                        MainEntityType targetMainType = EntityMetadataCache.getDataEntityType(sTargetEntity.get(j));
                        List<DynamicObject> targetBillObjs = pushResult.loadTargetDataObjects(
                                new IRefrencedataProvider() {
                                    @Override
                                    public void fillReferenceData(Object[] objs, IDataEntityType dType) {
                                        BusinessDataReader.loadRefence(objs, dType);
                                    }
                                }, targetMainType);
                        //下推保存
                        OperateOption.create().setVariableValue(OperateOptionConst.ISHASRIGHT, "true");//不校验权限保存提交审核
                        OperationResult saveResult = SaveServiceHelper.saveOperate(
                                sTargetEntity.get(j),
                                targetBillObjs.toArray(new DynamicObject[0]));
                        OperationServiceHelper.executeOperate("submit", sTargetEntity.get(j), targetBillObjs.toArray(new DynamicObject[0]), OperateOption.create());
                        OperationServiceHelper.executeOperate("audit", sTargetEntity.get(j), targetBillObjs.toArray(new DynamicObject[0]), OperateOption.create());
                        //先不管下推是否成功了，因为如果其中一个不成功，搞不好另外一个可以成功。
                        // 判断下推是否成功，如果失败，提取失败消息
//                            if (!pushResult.isSuccess()) {
//                                String errMessage = pushResult.getMessage();    // 错误信息
//                                for (SourceBillReport billReport : pushResult.getBillReports()) {
//                                    // 提取各单错误报告
//                                    if (!billReport.isSuccess()) {
//                                        String billMessage = billReport.getFailMessage();
//                                    }
//                                }
//                                throw new KDBizException("下推失败:" + errMessage);
//                            }
                    }
                }

            }else{
                this.operationResult.setMessage("失败！！");
                return;
                //失败
            }
        }
    }
}
