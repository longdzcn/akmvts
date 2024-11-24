package kd.cosmic;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.basedata.BaseDataServiceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 描述:审核后自动分配客户
 * 开发者: 李四辉
 * 创建日期:2024-04-01
 * 关键客户：
 * 已部署正式：true
 * 备注：已投入正式环境使用，无问题
 */

public class zdfpkh extends AbstractOperationServicePlugIn {
    @Override
    public void afterExecuteOperationTransaction(AfterOperationArgs  e) {
        for (DynamicObject dy : e.getDataEntities()){
            String entityID = "bd_customer";//客户实体
            //获取当前客户id
            Long billIds = Long.valueOf(String.valueOf(dy.getPkValue()));
            //获取客户对应组织ID
            DynamicObject khbill = BusinessDataServiceHelper.loadSingle(billIds,"bd_customer",
                    "ezob_khdyzz,ezob_khdyzz.id");

            if(khbill.get("ezob_khdyzz.id") != null){
                Long khflid = Long.valueOf(khbill.get("ezob_khdyzz.id").toString());
                if(khflid == 1724780663468483584L || khflid == 1724819622093417472L|| khflid == 1724819792365379584L|| khflid == 1724780663468483584L|| khflid == 1724820077292841984L|| khflid == 1724820279533791232L|| khflid == 1724820510136626176L|| khflid == 1724821433898526720L){//美维下级组织
                    Long assignOrgID = 100000L;//分配组织ID
                    List<Long> dataIDs = new ArrayList<Long>(16);
                    dataIDs.add(billIds);//待分配数据ID
                    List<Long> orgIDs = new ArrayList<Long>(16);
                    orgIDs.add(1724780663468483584L);//LE0001
                    orgIDs.add(1724819622093417472L);//LE0002
                    orgIDs.add(1724819792365379584L);//LE0003
                    orgIDs.add(1724820077292841984L);//LE0004
                    orgIDs.add(1724820279533791232L);//LE0005
                    orgIDs.add(1724820510136626176L);//LE0006
                    orgIDs.add(1724821433898526720L);//LE0009
                    Map<Long,Map<Long, String>> resultMap = BaseDataServiceHelper.
                            batchAssignWithDetail(entityID,assignOrgID,dataIDs,orgIDs);
                    if (resultMap.isEmpty()){
                        //成功
                    }else{
                        this.operationResult.setMessage("失败！！");
                        return;
                        //失败
                    }
                }else{//安捷利下级组织
                    Long assignOrgID = 100000L;//分配组织ID
                    List<Long> dataIDs = new ArrayList<Long>(16);
                    dataIDs.add(billIds);//待分配数据ID
                    List<Long> orgIDs = new ArrayList<Long>(16);
                    orgIDs.add(1724780663468483584L);//南沙新增客户需要分配LE0001，转单业务
                    orgIDs.add(1727018577904587776L);//BU-00012
                    orgIDs.add(1724821971004320768L);//LE0011
                    orgIDs.add(1724822236310824960L);//LE0012
                    orgIDs.add(1724822469556070400L);//LE0013
                    orgIDs.add(1724822759483141120L);//LE0014
                    orgIDs.add(1727015954182886400L);//LE0015
                    orgIDs.add(1726904003092075520L);//LE0016
                    orgIDs.add(1727016761368301568L);//LE0017
                    Map<Long,Map<Long, String>> resultMap = BaseDataServiceHelper.
                            batchAssignWithDetail(entityID,assignOrgID,dataIDs,orgIDs);
                    if (resultMap.isEmpty()){
                        //成功
                    }else{
                        this.operationResult.setMessage("失败！！");
                        return;
                        //失败
                    }
                }
            }else{
                //安捷利下级组织
                Long assignOrgID = 100000L;//分配组织ID
                List<Long> dataIDs = new ArrayList<Long>(16);
                dataIDs.add(billIds);//待分配数据ID
                List<Long> orgIDs = new ArrayList<Long>(16);
                orgIDs.add(1727018577904587776L);//BU-00012
                orgIDs.add(1724821971004320768L);//LE0011
                orgIDs.add(1724822236310824960L);//LE0012
                orgIDs.add(1724822469556070400L);//LE0013
                orgIDs.add(1724822759483141120L);//LE0014
                orgIDs.add(1727015954182886400L);//LE0015
                orgIDs.add(1726904003092075520L);//LE0016
                orgIDs.add(1727016761368301568L);//LE0017
                Map<Long,Map<Long, String>> resultMap = BaseDataServiceHelper.
                        batchAssignWithDetail(entityID,assignOrgID,dataIDs,orgIDs);
                if (resultMap.isEmpty()){
                    //成功
                }else{
                    this.operationResult.setMessage("失败！！");
                    return;
                    //失败
                }
            }
        }
    }
}
