package kd.cosmic;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

/**
 * 描述: 物料供应商版本
 * 开发者: 李四辉
 * 创建日期:2024-04-01
 * 关键客户：仓库
 * 已部署正式：true
 * 备注：已投入正式环境使用，无问题
 */
public class wlgysbb extends AbstractFormPlugin implements Plugin {

    private static final String KEY_MAINBAR = "tbmain";


    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(KEY_MAINBAR);
    }


    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String key = e.getProperty().getName();
        if (key.equals("ezob_gys")) {
            String number = this.getModel().getValue("number").toString();
            StringBuffer builder = new StringBuffer(number);
            String gys = this.getModel().getValue("ezob_gys").toString();
            //修改供应商版本标识
            int ezobXggysbs = (int) this.getModel().getValue("ezob_xggysbs");
            int ezobBcbs = (int) this.getModel().getValue("ezob_bcbs");
            //首次新增物料供应商填写未填写的值
//            if(gys != "" && ezob_xggysbs == 0){
//                builder.setCharAt(number.indexOf("-")+2, 'A');
//                number = builder.toString();
//                this.getModel().setValue("number", number);
//            }else if(gys == "" && ezob_xggysbs == 0){
//                builder.setCharAt(number.indexOf("-")+2, '0');
//                number = builder.toString();
//                this.getModel().setValue("number", number);
//            }
//            下推过后ezob_xggysbs=1，如果物料还没点击过保存则执行下面的逻辑
//            取最后+1版本，且第16码会重新从A开始
            if (ezobXggysbs == 1 && ezobBcbs == 0) {
                int index = number.indexOf("-");
                String wlbm = number.substring(0, index + 2);

                String sql = "/*dialect*/ select fnumber,fk_ezob_gys from akmmv_prd_eip_test.t_bd_material \n" +
                        "WHERE fnumber like'" + wlbm + "__" + "'";
                DataSet ds = DB.queryDataSet(wlgysbb.class.getName(), DBRoute.of("eip"), sql);


                String ezobGys = this.getModel().getValue("ezob_gys").toString();
                Set<String> set = new HashSet<>();
                if (!ds.isEmpty()) {
                    //遍历
                    while (ds.hasNext()) {
                        Row row = ds.next();
                        if (row.getString(1).equals(ezobGys)) {
                            this.getModel().setValue("number", row.getString(0));
                            return;
                        }
                        String str = row.getString(0);
                        set.add(str.substring(str.length() - 2, str.length() - 1));
                    }
                }
                char a = 'A';
                a += (char) set.size();
                builder.setCharAt(index + 2, a);
                builder.setCharAt(index + 3, 'A');
                number = builder.toString();
                this.getModel().setValue("number", number);
            }
        }
        if (key.equals("ezob_bsykgldbs")) {
//            修改保税标识
                String number = this.getModel().getValue("number").toString();
                String ezobBsykgldbs = this.getModel().getValue("ezob_bsykgldbs").toString();
                int index = number.indexOf("-");
                StringBuffer builder = new StringBuffer(number);
                builder.setCharAt(index + 1, ezobBsykgldbs.toCharArray()[0]);
                number = builder.toString();
                this.getModel().setValue("number", number);
        }
        if (key.equals("modelnum") || key.equals("ezob_gg")) {
            //修改版本升级标识
//            单纯升级第16码一次
            int ezobBbsjbs = (int) this.getModel().getValue("ezob_bbsjbs");
            int ezobBcbs = (int) this.getModel().getValue("ezob_bcbs");
            if (ezobBbsjbs == 1 && ezobBcbs == 0) {
                String number = this.getModel().getValue("number").toString();
                int index = number.indexOf("-");
                String wlbm = number.substring(0, index + 3);

                String sql = "/*dialect*/ select fnumber,FModel,fk_ezob_gg from akmmv_prd_eip_test.t_bd_material \n" +
                        "WHERE fnumber like'" + wlbm + "_'";
                DataSet ds = DB.queryDataSet(wltbsf.class.getName(), DBRoute.of("eip"), sql);
                String modelnum = this.getModel().getValue("modelnum").toString();
                String ezobGg = this.getModel().getValue("ezob_gg").toString();
                int i = 0;
                if (!ds.isEmpty()) {
                    while (ds.hasNext()) {
                        Row row = ds.next();
                        if (row.getString(1).equals(modelnum)) {
                            if (row.getString(2).equals(ezobGg)) {
                                this.getModel().setValue("number", row.getString(0));
                                return;
                            }
                        }
                        i++;
                    }
                }
                char a = 'A';
                a += i;
                StringBuffer builder = new StringBuffer(number);
                builder.setCharAt(index + 3, a);
                number = builder.toString();
                this.getModel().setValue("number", number);
            }
        }
    }
}