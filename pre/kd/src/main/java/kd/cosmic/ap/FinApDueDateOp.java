package kd.cosmic.ap;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.metadata.IMetadata;
import kd.bos.dataentity.metadata.clr.DataEntityPropertyCollection;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.BeginOperationTransactionArgs;
import kd.bos.entity.property.EntryProp;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.util.CollectionUtils;
import kd.fi.arapcommon.service.plan.FinPlanRowService;
import kd.fi.arapcommon.service.plan.PlanRow;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 描述: 财务应付单计算到期日，触发条件：目前是保存，提交，审核位置触发，只要代码插件有放置操作事件插件就会触发
 * 开发者: 易佳伟，李四辉（有总部老师参与提供代码）
 * 创建日期: 1期
 * 关键客户：李琼
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */

public class FinApDueDateOp extends AbstractOperationServicePlugIn {
    public void beginOperationTransaction(BeginOperationTransactionArgs e) {
        DynamicObject[] entities = e.getDataEntities();
        FinPlanRowService service = new FinPlanRowService();
        for (DynamicObject entity : entities) {
            List<Date> list = new ArrayList<>();
            Date maxDate = null;
            if (entity.getDate("ezob_datefield") != null)
                list.add(entity.getDate("ezob_datefield"));
            if (entity.getDate("ezob_datefield1") != null)
                list.add(entity.getDate("ezob_datefield1"));
            if (entity.getDate("ezob_datefield2") != null)
                list.add(entity.getDate("ezob_datefield2"));
            if (entity.getDate("ezob_datefield4") != null)
                list.add(entity.getDate("ezob_datefield4"));
            if (entity.getDate("ezob_datefield5") != null)
                list.add(entity.getDate("ezob_datefield5"));
            DynamicObjectCollection invEntries = entity.getDynamicObjectCollection("inventry");
            for (DynamicObject invEntry : invEntries) {
                if (invEntry.getDate("i_invoicedate") != null)
                    list.add(invEntry.getDate("i_invoicedate"));
            }
            //根据供应商id查供应商，再根据供应商查财务资金安排天数
            DynamicObject cwyfBill = entity.getDynamicObject("receivingsupplierid");
            String gysid = null;
            int score = 0;
            if (cwyfBill != null) {
                gysid = cwyfBill.get("id").toString();
                if (gysid != null) {
                    DynamicObject cwzjapts = BusinessDataServiceHelper.loadSingle(gysid, "bd_supplier", "ezob_cwzjapts,entry_bank," + "entry_bank.currency,entry_bank.bankaccount,entry_bank.bank");
                    score = cwzjapts.getInt("ezob_cwzjapts");
                    DynamicObjectCollection dobank = cwzjapts.getDynamicObjectCollection("entry_bank");
//            首先获取获取银行的账户信息
//            DynamicObject cwyfbank = entity.getDynamicObject("receivingsupplierid");
                    DynamicObject fcurrencyid = entity.getDynamicObject("currency");
                    for (DynamicObject doabankentry : dobank) {
//                //循环供应商银行分录
//                收款账户字段fpayeebanknum  收款银行字段bebank   币别字段fcurrencyid
//              若币别相同，则给账户赋值
                        if (fcurrencyid == doabankentry.getDynamicObject("currency")) {

                            entity.set("payeebanknum", doabankentry.get("bankaccount"));
                            entity.set("bebank", doabankentry.get("bank"));
                            }
                        }
                    } else {
                    this.operationResult.setMessage("结算供应商未填");
                }
            }



            if (!CollectionUtils.isEmpty(list))
                maxDate = Collections.<Date>max(list);
            entity.set("termsdate", maxDate);
            Date termDate = entity.getDate("termsdate");
            if (termDate != null) {
                List<PlanRow> planRows = service.calculatePlanRows(entity, termDate);
                DynamicObjectCollection planEntries = entity.getDynamicObjectCollection("planentity");
                if (planRows.size() == planEntries.size()) {
                    for (int i = 0, size = planEntries.size(); i < size; i++) {
                        DynamicObject planEntry = (DynamicObject)planEntries.get(i);
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.setTime(((PlanRow)planRows.get(i)).getDueDate());
                        calendar1.add(5, score);
                        planEntry.set("planduedate", calendar1.getTime());
                    }
                } else {DynamicObject splitScheme = entity.getDynamicObject("splitscheme");
                    if (splitScheme != null) {
                        int planRowSize = planRows.size();
                        int remainSize = 0;
                        for (DynamicObject planEntry : planEntries) {
                            Calendar calendar1 = Calendar.getInstance();
                            calendar1.setTime(((PlanRow)planRows.get(remainSize)).getDueDate());
                            calendar1.add(5, score);
                            planEntry.set("planduedate", calendar1.getTime());
                            remainSize++;
                            if (remainSize == planRowSize)
                                remainSize = 0;
                        }
                    }
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(((PlanRow)planRows.get(planRows.size() - 1)).getDueDate());
                calendar.add(5, score);
                entity.set("duedate", calendar.getTime());
            }
        }
    }

    public void onPreparePropertys(PreparePropertysEventArgs e) {
        List<String> fieldKeys = e.getFieldKeys();
        List<IDataEntityProperty> properties = getProperties(this.billEntityType);
        List<String> propertyKeys = (List<String>)properties.stream().map(IMetadata::getName).collect(Collectors.toList());
        fieldKeys.addAll(propertyKeys);
        fieldKeys.add("ezob_cwzjapts");
        fieldKeys.add("receivingsupplierid.ezob_cwzjapts");
        fieldKeys.add("receivingsupplierid_ezob_cwzjapts");
        fieldKeys.add("receivingsupplierid");
    }

    private static List<IDataEntityProperty> getProperties(MainEntityType entityType) {
        List<IDataEntityProperty> props = new ArrayList<>();
        DataEntityPropertyCollection properties = entityType.getProperties();
        for (IDataEntityProperty prop : properties) {
            if (prop instanceof EntryProp) {
                DataEntityPropertyCollection entryProperties = ((EntryProp)prop)._collectionItemPropertyType.getProperties();
                props.addAll((Collection<? extends IDataEntityProperty>)entryProperties);
                continue;
            }
            props.add(prop);
        }
        return props;
    }
}
