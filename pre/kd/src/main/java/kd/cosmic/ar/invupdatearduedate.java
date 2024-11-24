package kd.cosmic.ar;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.fi.arapcommon.dev.BeanFactory;
import kd.fi.arapcommon.service.plan.PlanRow;
import kd.fi.arapcommon.service.plan.PlanRowDataCalculator;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 描述: 销售发票开票后反写应收单到期日
 * 开发者: 江伟维
 * 创建日期: 2期
 * 关键客户：关敏婷
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
public class invupdatearduedate extends AbstractOperationServicePlugIn {
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        try {
            for (DynamicObject dy : e.getDataEntities()) {
                Long billId = Long.valueOf(String.valueOf(dy.getPkValue()));
                DynamicObject aBILL = BusinessDataServiceHelper.loadSingle(billId, "sim_original_bill", "billno,billdate,billsourceid");
                DynamicObjectCollection dynamicObjectCollection = aBILL.getDynamicObjectCollection("sim_original_bill_item");
                for (DynamicObject dynamicObject : dynamicObjectCollection) {
                    String sourceBillIds = dynamicObject.getString("billsourceid"); //源单id
                    Date ddate = aBILL.getDate("billdate"); //发票日期

                    //根据源单id查找
                    DynamicObject dyo= BusinessDataServiceHelper.loadSingle(sourceBillIds,"ar_finarbill");
                    DynamicObjectCollection dyoc = dyo.getDynamicObjectCollection("planentity");
                    DynamicObject payCondition = this.getCondition(dyo); //获取源单收款条件
                    PlanRowDataCalculator planRowDataCalculator = (PlanRowDataCalculator) BeanFactory.getBean(PlanRowDataCalculator.class, new Object[0]);
                    //重新计算到期日
                    List<Map<String, Object>> planData = planRowDataCalculator.getPayPlanData(payCondition, ddate, BigDecimal.ZERO, 0);
                    if (planData == null) {
                        System.out.println("这里没有任何数据");
                    } else {
                        Iterator var8 = planData.iterator();

                        while(var8.hasNext()) {
                            Map<String, Object> rowData = (Map)var8.next();
                            PlanRow planRow = new PlanRow();
                            Date dueDate = (Date)rowData.get("date");
                            System.out.println(dueDate);
                            dyo.set("invoicedate",ddate); //赋值源单发票日
                            dyo.set("duedate",dueDate); //赋值源单到期日
                            for (DynamicObject doe : dyoc) {
                                doe.set("planduedate",dueDate); //赋值源单收款计划的到期日
                            }
                            SaveServiceHelper.update(dyo); //更新源单数据
                        }
                    }

                }
                String sql = "/*dialect*/call akmmv_prd_fi_test.KDUpdateInvoiceNo(" + String.valueOf(billId) + ")";
                DB.update(DBRoute.basedata, sql);
            }
        } catch (Exception e1) {
            this.operationResult.setMessage(e1.toString());
            return;
        }
    }
    protected DynamicObject getCondition(DynamicObject bill) {
        return bill.getDynamicObject("paycond");
    }
}