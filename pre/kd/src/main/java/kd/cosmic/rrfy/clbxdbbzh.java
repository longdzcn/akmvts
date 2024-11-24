package kd.cosmic.rrfy;


import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

/**
 * 描述: 差旅报销单，带出“本位币对人民币”的汇率和人民币金额
 * 开发者: 钟有吉、梁远健
 * 关键客户：宋诗丹
 */

public class clbxdbbzh extends AbstractFormPlugin implements Plugin {
//    final static String RMB="CNY";
    public void propertyChanged(PropertyChangedArgs e) {

        String name = e.getProperty().getName();

        if (name.equals("orientryamount")) {
            IDataModel model = getModel();
            DynamicObjectCollection entryEntity = model.getEntryEntity("entryentity");
//            查询组织编码
            DynamicObject costcompany = getModel().getDataEntity().getDynamicObject("costcompany");
            Long pkValue = (Long) costcompany.getPkValue();
            if (pkValue == 1838391834745816064L || pkValue == 1727016761368301568L || pkValue == 2012970605850146816L || pkValue ==1724819622093417472L || pkValue == 1727018577904587776L){
//            查询对应本位币
                QFilter q = new QFilter("baseacctorg", QCP.equals, pkValue);
                DynamicObject load = BusinessDataServiceHelper.loadSingle("bd_accountingsys_base", "basecurrrency", new QFilter[]{q});
//            当本位币不为空时进入
                if (load != null) {
                    String basecurrrency = load.getDynamicObject("basecurrrency").getPkValue().toString();


//            获取本位币
//            String basecurrrency = load.getString("basecurrrency");
                    QFilter bwb = new QFilter("orgcur", QCP.equals, basecurrrency);
                    String date = this.getDate();
                    QFilter rq = new QFilter("effectdate",QCP.equals,date);
//            获取汇率
                    DynamicObject hl = BusinessDataServiceHelper.loadSingle("bd_exrate_tree", "cur,orgcur,excval,effectdate", new QFilter[]{bwb});
                    String cur = hl.getDynamicObject("cur").getString("id");
                    QFilter mbb = new QFilter("cur",QCP.equals,cur);
                    DynamicObject h2 = BusinessDataServiceHelper.loadSingle("bd_exrate_tree", "cur,orgcur,excval,effectdate", new QFilter[]{bwb,rq,mbb});
                    String cxrq = h2.getString("effectdate");
                    String xmbb = h2.getDynamicObject("cur").getString("number");
                    String xyb = h2.getDynamicObject("orgcur").getString("number");
//            获取汇率
                    String excval = h2.getString("excval");
                    //            costdept   flexpanelap12
//            费用承担公司
                    Object o = model.getDataEntity().get("costcompany");
                    Double hjjey = 0.0;
                    for (DynamicObject entry : entryEntity) {
                        String cot = entry.getString("entryamount");
                        double coti = Double.parseDouble(cot);
                        double exc = Double.parseDouble(excval);

                        // 进行加法运算
                        double sum = coti * exc;

                        // 将结果转换回字符串
                        String result = Double.toString(sum);
//                给人名币汇率赋值
                        entry.set("ezob_rmbhl", excval);
//                给人名币金额赋值
                        entry.set("ezob_rmbje", sum);
                        //人民币金额约
                        hjjey += sum;
                        this.getModel().setValue("ezob_rmbjey", hjjey);
                    }
                    getView().updateView();

                }
            }

        }

    }
    public String getDate() {
        String selsql = "/*dialect*/select FEffectDate from t_bd_exrate " +
                "WHERE FOrgCurID = 22 and FCurID = 1 ORDER BY FEFFECTDATE desc limit 1";

        DataSet selDs = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata,selsql);
        ORM orm = ORM.create();

        DynamicObjectCollection rows = orm.toPlainDynamicObjectCollection(selDs);
        String date = "";
        for (DynamicObject row : rows) {
            date = row.getString("feffectdate");
        }
        return date;
    }

}



