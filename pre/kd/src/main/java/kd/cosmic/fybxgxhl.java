package kd.cosmic;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.plugin.AbstractFormPlugin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 描述: 单据界面：费用报销单LE0002更新汇率
 * 开发者: 李四辉
 * 创建日期:2024-08-01
 * 关键客户：赖雅瑜
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */

//kd.cosmic.fybxgxhl
public class fybxgxhl extends AbstractFormPlugin{

    public void propertyChanged(PropertyChangedArgs e) {
        String key = e.getProperty().getName();
        switch (key) {
            case "iscurrency":
            DynamicObject fycdgs = (DynamicObject)this.getModel().getValue("costcompany",0);//费用承担公司
            if (fycdgs != null) {
                String fycdgsnumber = fycdgs.get("number").toString();
                if(fycdgsnumber.equals("LE0002")){
                    int i = 0;
                    DynamicObjectCollection entryEntity = getModel().getEntryEntity("expenseentryentity");
                    for (DynamicObject entry : entryEntity) {
                        SimpleDateFormat originalFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            if(this.getModel().getValue("happendate",i) != null){
                                Date date = originalFormat.parse(this.getModel().getValue("happendate",i).toString());
                                String formattedDate = targetFormat.format(date);
                                String jsrq = formattedDate.substring(0, 7);//结束日期
                                DynamicObject bb = (DynamicObject)this.getModel().getValue("entrycurrency",i);
                                if(bb != null){
                                    String bbid = bb.get("id").toString();
                                    String sql = "/*dialect*/ select FExRate from t_bd_exrate " +
                                            "where fgroupid = 1729923235958537216 and DATE_FORMAT(FEffectDate,'%Y-%m') " +
                                            "= '"+jsrq+"' and FOrgCurID = '"+bbid+"' limit 1;";
                                    DataSet ds = DB.queryDataSet("selecthl", DBRoute.of("eip"),sql);
                                    if(!ds.isEmpty()){
                                        while(ds.hasNext()) {
                                            Row row = ds.next();
                                            this.getModel().setValue("exchangerate", row.get(0).toString());
                                        }
                                    }else{
                                        this.getModel().setValue("exchangerate", 0,i);
                                    }
                                }
                            }
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }
                        i++;
                    }
                }
            }
            break;
        }
        switch (key) {
            case "entrycurrency":
                DynamicObject fycdgs = (DynamicObject)this.getModel().getValue("costcompany",0);//费用承担公司
                if (fycdgs != null) {
                    String fycdgsnumber = fycdgs.get("number").toString();
                    if(fycdgsnumber.equals("LE0002")){
                        int i = 0;
                        DynamicObjectCollection entryEntity = getModel().getEntryEntity("expenseentryentity");
                        for (DynamicObject entry : entryEntity) {
                            SimpleDateFormat originalFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                            SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                if(this.getModel().getValue("happendate",i) != null){
                                    Date date = originalFormat.parse(this.getModel().getValue("happendate",i).toString());
                                    String formattedDate = targetFormat.format(date);
                                    String jsrq = formattedDate.substring(0, 7);//结束日期
                                    DynamicObject bb = (DynamicObject)this.getModel().getValue("entrycurrency",i);
                                    if(bb != null){
                                        String bbid = bb.get("id").toString();
                                        String sql = "/*dialect*/ select FExRate from t_bd_exrate " +
                                                "where fgroupid = 1729923235958537216 and DATE_FORMAT(FEffectDate,'%Y-%m') " +
                                                "= '"+jsrq+"' and FOrgCurID = '"+bbid+"' limit 1;";
                                        DataSet ds = DB.queryDataSet("selecthl", DBRoute.of("eip"),sql);
                                        if(!ds.isEmpty()){
                                            while(ds.hasNext()) {
                                                Row row = ds.next();
                                                this.getModel().setValue("exchangerate", row.get(0).toString(),i);
                                            }
                                        }else{
                                                this.getModel().setValue("exchangerate", 0,i);
                                             }
                                    }
                                }
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
                            i++;
                        }
                    }
                }
            break;
        }
        switch (key) {
            case "happendate":
                DynamicObject fycdgs = (DynamicObject)this.getModel().getValue("costcompany",0);//费用承担公司
                if (fycdgs != null) {
                    String fycdgsnumber = fycdgs.get("number").toString();
                    if(fycdgsnumber.equals("LE0002")){
                        int i = 0;
                        DynamicObjectCollection entryEntity = getModel().getEntryEntity("expenseentryentity");
                        for (DynamicObject entry : entryEntity) {
                            SimpleDateFormat originalFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                            SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                if(this.getModel().getValue("happendate",i) != null){
                                    Date date = originalFormat.parse(this.getModel().getValue("happendate",i).toString());
                                    String formattedDate = targetFormat.format(date);
                                    String jsrq = formattedDate.substring(0, 7);//结束日期
                                    DynamicObject bb = (DynamicObject)this.getModel().getValue("entrycurrency",i);
                                    if(bb != null){
                                        String bbid = bb.get("id").toString();
                                        String sql = "/*dialect*/ select FExRate from t_bd_exrate " +
                                                "where fgroupid = 1729923235958537216 and DATE_FORMAT(FEffectDate,'%Y-%m') " +
                                                "= '"+jsrq+"' and FOrgCurID = '"+bbid+"' limit 1;";
                                        DataSet ds = DB.queryDataSet("selecthl", DBRoute.of("eip"),sql);
                                        if(!ds.isEmpty()){
                                            while(ds.hasNext()) {
                                                Row row = ds.next();
                                                this.getModel().setValue("exchangerate", row.get(0).toString());
                                            }
                                        }else{
                                            this.getModel().setValue("exchangerate", 0,i);
                                        }
                                    }
                                }
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
                            i++;
                        }
                    }
                }
            break;
        }
    }
//    @Override
//    public void afterBindData(EventObject e) {
//        super.afterBindData(e);
//        int i = 0;
//        String qsrq = this.getModel().getValue("trip2startdate",0).toString();//起始日期
//        String jsrq = this.getModel().getValue("trip2enddate",0).toString();//结束日期
//        DynamicObjectCollection entryEntity = getModel().getEntryEntity("entryentity");
//        for (DynamicObject entry : entryEntity) {
//            this.getModel().setValue("taxrate",11, i);
//            i++;
//        }
//    }
}
