package kd.cosmic;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.fi.arapcommon.api.param.AssignBillPushParam;
import kd.fi.arapcommon.api.param.ServiceNameEnum;
import kd.fi.arapcommon.api.push.IPushBillService;
import kd.fi.arapcommon.factory.ArApServiceAPIFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述:在暂估应付单审核后，按照备注中的期间信息及规则查找到对应的暂估应付单自动全额冲回
 * 开发者: 李四辉
 * 创建日期:2024-08-01
 * 关键客户：财务
 * 已部署正式：true
 * 备注：已投入正式环境使用，无问题
 */

//kd.cosmic.zgyfdch
public class zgyfdch extends AbstractOperationServicePlugIn {
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        String nf = "";//年份
        String yf = "";//月份
        String hxdjh = "";//核心单据号
        String jtlx = "";//计提类型
        Long billpk = (Long)e.getDataEntities()[0].getPkValue();
        DynamicObject cgshBill = BusinessDataServiceHelper.loadSingle(billpk, "ap_busbill", "billtype,remark,asstact,ezob_zdmverpzt,entry,ezob_jtlx,e_corebillno");
        DynamicObjectCollection dynamicObjectCollection = cgshBill.getDynamicObjectCollection("entry");
        String djlx = cgshBill.get("billtype.id").toString();//单据类型
        String bz = cgshBill.get("remark").toString();//备注
        if(bz.length() == 6){
            nf = bz.substring(0,4);//年份
            yf = bz.substring(bz.length() - 2);//月份
        }else{
            return;//不用冲回
        }
        String gys = cgshBill.get("asstact.masterid").toString();//供应商
        String zdmverpzt = "";
        if(cgshBill.get("ezob_zdmverpzt.name") !=null ){
            zdmverpzt = cgshBill.get("ezob_zdmverpzt.name").toString();//终端mverp账套
        }
        for (DynamicObject dynamicObject : dynamicObjectCollection) {
            hxdjh = dynamicObject.get("e_corebillno").toString();//核心单据号
            jtlx = dynamicObject.get("ezob_jtlx.masterid").toString();//计提类型
        }
        //核心单据号不为空并且单据类型为费用暂估并且计提类型为废料并且mverp账套编码为GME/FPC
        if(!hxdjh.isEmpty() && djlx.equals("676547888708361216") && jtlx.equals("1737816695306727424") && (zdmverpzt.equals("GME") || zdmverpzt.equals("FPC"))){
            //查询需要冲回的暂估应付单：匹配日期+供应商+核心单据号为空+单据类型为费用暂估+计提类型为废料+创建人接口API
            String sqlxz = "/*dialect*/select DISTINCT t.FID from t_ap_busbill t " +
                    "left join t_ap_busbillentry te on t.FID = te.FID " +
                    "where t.fasstactid= '"+gys+"' and YEAR(t.fbizdate) = '"+nf+"' " +
                    "and DATE_FORMAT(t.fbizdate, '%m') = '"+yf+"'" +
                    "and t.fbilltype = '676547888708361216' and te.fcorebillno = '' " +
                    "and t.fcreatorid = '1723806756926133248' and t.FBILLNO like '"+zdmverpzt+"%';";
            DataSet ds = DB.queryDataSet("selectzgyfch", DBRoute.of("fi"),sqlxz);
            //String id = "";
            if(!ds.isEmpty()){
                //遍历
                try {
                    //暂估应付单全额冲回
                    List<AssignBillPushParam> paramList = new ArrayList<>();
                    while(ds.hasNext()) {
                        Row row = ds.next();
                        AssignBillPushParam param = new AssignBillPushParam(Long.parseLong(row.get(0).toString()));
                        paramList.add(param);
                    }
                    IPushBillService service = ArApServiceAPIFactory.getPushBillService(ServiceNameEnum.BUSAPWOFF.getValue());
                    List<Object> targetPKs = service.assignBillPush("ap_busbill", paramList);
                } catch (Exception ex) {
                }
            }
        }
    }
}
