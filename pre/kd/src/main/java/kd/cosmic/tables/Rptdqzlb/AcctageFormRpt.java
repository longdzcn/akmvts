//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package kd.cosmic.tables.Rptdqzlb;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.ValueMapItem;
import kd.bos.entity.report.AbstractReportColumn;
import kd.bos.entity.report.ComboReportColumn;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.orm.query.QFilter;
import kd.bos.report.events.CreateColumnEvent;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 描述: 应付账龄表（新）,报表表体插件，新增了一些字段，具体字段可看下面代码标识
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：关敏婷
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

public class AcctageFormRpt extends AbstractReportFormPlugin {
    public AcctageFormRpt() {
    }


    public void processRowData(String gridPK, DynamicObjectCollection rowData, ReportQueryParam queryParam) {

        Iterator var4 = rowData.iterator();
        String pk = "";
        try {

            while (var4.hasNext()) {
                DynamicObject row = (DynamicObject) var4.next();
                pk = row.getString("id");
                String id = "";
                if (row.getDynamicObject("asstact") != null) {
                    id = row.getDynamicObject("asstact").getString("id");
                }
                if (pk.equals("0")) {
                    continue;
                }
                DynamicObject sup;
                if (!id.equals("")) {
                    sup = BusinessDataServiceHelper.loadSingle(id, "bd_supplier");
                    String bzfk = "";
                    if (sup.getString("ezob_bzfk") != null) {
                        bzfk = sup.getString("ezob_bzfk");
                        if (bzfk.equals("false")) {
                            bzfk = "非标准付款";
                        } else {
                            bzfk = "标准付款";
                        }
                        row.set("ezob_bzfk", bzfk);


                    }
                }


                QFilter qFilter = new QFilter("id", "=", pk);

                DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle(pk, "ap_finapbill", "detailentry,detailentry.e_materialname,detailentry.materialname,detailentry.ezob_hth,ezob_fphm,ezob_textfield3,ezob_cgzl,ezob_datefield2,paycond,settlementtype,exchangerate,settleamount,remark,receivingsupplierid,pricetaxtotalbase,unsettleamountbase,settleamountbase,ezob_datefield1,ezob_mverpzt");

                DynamicObjectCollection entrys = dynamicObject.getDynamicObjectCollection("detailentry");

                //mverp账套

                if (dynamicObject.getString("ezob_mverpzt") != null) {
                    row.set("ezob_mverpzt", dynamicObject.getDynamicObject("ezob_mverpzt").getString("number"));

                }


                //发票号码
                if (dynamicObject.get("ezob_fphm") != null && dynamicObject.get("ezob_fphm") != "") {
                    row.set("ezob_fphm", dynamicObject.get("ezob_fphm").toString());
                }

                //发票日期
                if (dynamicObject.get("ezob_datefield1") != null && dynamicObject.get("ezob_datefield1") != "") {
                    row.set("ezob_fprq", getTime(dynamicObject.getDate("ezob_datefield1")));
                }


                //项目说明
                if (dynamicObject.get("ezob_textfield3") != null && dynamicObject.get("ezob_textfield3") != "") {
                    row.set("ezob_textfield3", dynamicObject.get("ezob_textfield3").toString());
                }

                //采购种类
                if (dynamicObject.get("ezob_cgzl") != null && dynamicObject.get("ezob_cgzl") != "") {

                    row.set("ezob_cgzl", dynamicObject.getDynamicObject("ezob_cgzl").get("name").toString());
                }

                //验收日期 ezob_datefield2
                if (dynamicObject.get("ezob_datefield2") != null && dynamicObject.get("ezob_datefield2") != "") {
                    row.set("ezob_datefield2", dynamicObject.get("ezob_datefield2").toString());
                }

                //付款方式 paycond
                if (dynamicObject.get("paycond") != null && dynamicObject.get("paycond") != "") {
                    String paycond = dynamicObject.getDynamicObject("paycond").getString("name");
                    row.set("paycond", dynamicObject.getDynamicObject("paycond").getString("name"));
                }

                //结算方式 settlementtype
                if (dynamicObject.get("settlementtype") != null && dynamicObject.get("settlementtype") != "") {
                    DynamicObject settlementtypeDynamic = dynamicObject.getDynamicObject("settlementtype");
                    row.set("settlementtype", settlementtypeDynamic.getString("name"));
                }

                //汇率
                if (dynamicObject.get("exchangerate") != null && dynamicObject.get("exchangerate") != "") {
                    row.set("ezob_hl", dynamicObject.get("exchangerate").toString());
                }

                //已结算币别 settleamount
                if (dynamicObject.get("settleamount") != null && dynamicObject.get("settleamount") != "") {
                    row.set("settleamount", dynamicObject.get("settleamount").toString());
                }

                //备注 remark
                if (dynamicObject.get("remark") != null && dynamicObject.get("remark") != "") {
                    row.set("remark", dynamicObject.get("remark").toString());
                }

                //供应商编码  ezob_gysbm
                if (dynamicObject.get("receivingsupplierid") != null && dynamicObject.get("receivingsupplierid") != "") {
                    DynamicObject gys = dynamicObject.getDynamicObject("receivingsupplierid");
                    row.set("receivingsupplierid", gys.getString("number"));
                }
               /* //应付金额（本位币） pricetaxtotalbase
                if(dynamicObject.get("pricetaxtotalbase") != null && dynamicObject.get("pricetaxtotalbase") != "")
                {
                    row.set("pricetaxtotalbase", dynamicObject.get("pricetaxtotalbase").toString());
                }
                //未结算金额（本位币） unsettleamountbase
                if(dynamicObject.get("unsettleamountbase") != null && dynamicObject.get("unsettleamountbase") != "")
                {
                    row.set("unsettleamountbase", dynamicObject.get("unsettleamountbase").toString());
                }
                //已结算金额（本位币）settleamountbase
                if(dynamicObject.get("settleamountbase") != null && dynamicObject.get("settleamountbase") != "")
                {
                    row.set("settleamountbase", dynamicObject.get("settleamountbase").toString());
                }*/
                String hth = "";
                StringBuffer sb = new StringBuffer();
                for (DynamicObject entry : entrys) {
                    String str = "";
                    if (entry.get("ezob_hth") != null && entry.get("ezob_hth") != "") {
                        if (entrys.size() == 1) {
                            sb.append(entry.getString("ezob_hth.number"));
                        } else {
                            str = entry.getString("ezob_hth.number") + ",";
                            sb.append(str);
                        }

                    }

                }
                hth = sb.toString();
                row.set("ezob_hth", hth);

                String selsql = "/*dialect*/select fvoucherid from akmmv_prd_fi_test.t_ai_daptracker where Fsourcebillid=" + pk;
                DataSet selDs = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, selsql);
                long fvoucherid = 0;

                while (selDs.hasNext()) {
                    Row roww = selDs.next();
                    fvoucherid = roww.getLong(0);
                }

                String selsql2 = "/*dialect*/select fnumber from  akmmv_prd_fi_test.t_gl_voucher where fid=" + fvoucherid;
                DataSet selDs2 = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, selsql2);
                String fnumber = "";
                while (selDs2.hasNext()) {
                    Row roww2 = selDs2.next();
                    fnumber = roww2.getString(0);
                    if (fnumber != "") {
                        row.set("ezob_pzzh", fnumber);
                    }
                }


            }

        } catch (Exception e) {
            this.getView().showMessage("pk:" + pk + "错误:" + e.getMessage());
        }


    }

    public static String getTime(Date dateTimeString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(dateTimeString);
        return date;
    }


    @Override
    public void afterCreateColumn(CreateColumnEvent event) {
        //在这里试下给下这个字段添加下拉项目  comboitems
        //先按这种方式试下 我这边先退出远程了

        List<AbstractReportColumn> list= event.getColumns();

        List<String> list1 = new ArrayList<>();
        DynamicObject[] load = BusinessDataServiceHelper.load("bd_paycondition","name",new QFilter[]{});
        for (DynamicObject dynamicObject : load){
            String payCond = dynamicObject.getString("name");
            list1.add(payCond);
        }
        for (int i = 0; i < list.size(); i++) {
            AbstractReportColumn tempCol=list.get(i);
            if(tempCol instanceof ComboReportColumn&&"paycond".equals(((ComboReportColumn) tempCol).getFieldKey())){
                List<ValueMapItem> comboItems=((ComboReportColumn) tempCol).getComboItems();
                //构建下拉项
                for (int j = 0; j < list1.size(); j++) {
                    String payCond = list1.get(j);
                    ValueMapItem item = new ValueMapItem();
                    item.setValue(payCond);
                    LocaleString name = new LocaleString();
                    name.setLocaleValue_zh_TW(payCond);
                    name.setLocaleValue_en(payCond);
                    name.setLocaleValue_zh_CN(payCond);
                    item.setName(name);
                    item.setItemVisible(true);

                    // 把下拉项放入下拉列表
                    comboItems.add(item);
                }

            }
        }
        super.afterCreateColumn(event);
        System.out.println(event);
    }

}






