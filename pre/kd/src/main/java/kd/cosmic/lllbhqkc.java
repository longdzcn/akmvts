package kd.cosmic;

import com.alibaba.druid.util.StringUtils;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.list.IListView;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.sdk.plugin.Plugin;

/**
 * 描述: 领料申请单列表更新库存车间仓
 * 开发者: 李四辉
 * 创建日期:2024-04-01
 * 关键客户：仓库
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */

public class lllbhqkc extends AbstractListPlugin implements Plugin{
    private final static String KEY_BARITEM = "ezob_hqkc";
    private final static String KEY_MAIN = "ezob_hqcjc";
    //获取时间戳
    String timestamp = String.valueOf(System.currentTimeMillis());
    /*@Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(KEY_BARITEM);
    }
*/

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        String rowid = "";
        ListSelectedRowCollection selectedRows = ((IListView) this.getView()).getSelectedRows();

        for (ListSelectedRow list : selectedRows) {
            rowid += list.getEntryPrimaryKeyValue()+",";
        }
        if(rowid.length()>0){
            //选中行ID
            rowid = rowid.substring(0, rowid.length() - 1);
        }
        //领料申请单点击获取库存按钮获取即时库存
        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {
//            String sql = "/*dialect*/ select fmaterialid as 物料ID from t_im_mreqbillentry where FEntryID in ("+rowid+");";
//            DataSet ds = DB.queryDataSet("selectwlid",DBRoute.of("scm"),sql);
//            if(!ds.isEmpty()){
//                //遍历
//                while(ds.hasNext()) {
//                    Row row = ds.next();
//                    wlid += row.get(0).toString()+",";//物料id
//                }
//                //选中物料ID
//                wlid = wlid.substring(0, wlid.length() - 1);
            if(rowid.length()>0){
                String sql = "/*dialect*/call akmmv_prd_scm_test.KDUpdateJSKC(1,'"+rowid+"')";
                DB.update(DBRoute.basedata, sql);
                this.getView().invokeOperation("refresh");
            }
//            }
//            //领料申请单点击获取库存按钮获取库存最早仓位
//            sql = "/*dialect*/ create table t_jskchzbcw"+timestamp+"\n" +
//                    "select tiir.forgid as 组织ID,tiir.fmaterialid as 物料ID,flocationid as 仓位,MIN(fproducedate) as 生产日期\n" +
//                    "from akmmv_prd_scm_test.t_im_inv_realbalance tiir\n" +
//                    "where tiir.FOWNERTYPE='bos_org' and tiir.fqty>0\n" +
//                    "group by tiir.forgid ,tiir.fmaterialid;\n" +
//                    "CREATE INDEX IDX_t_jskchzbcw ON t_jskchzbcw"+timestamp+" (组织ID,物料ID);\n" +
//                    "update akmmv_prd_scm_test.t_im_mreqbillentry te\n" +
//                    "left join akmmv_prd_scm_test.t_im_mreqbill t on te.fid = t.fid\n" +
//                    "left join akmmv_prd_eip_test.t_bd_materialinvinfo tbmi on te.fmaterialid = tbmi.FID\n" +
//                    "left join t_jskchzbcw"+timestamp+" wlkc on t.forgid = wlkc.组织ID and tbmi.FMASTERID = wlkc.物料ID\n" +
//                    "set te.fk_ezob_jskccw = wlkc.仓位;\n" +
//                    "drop table t_jskchzbcw"+timestamp+";";
//            DB.update(DBRoute.basedata, sql);
//            this.getView().invokeOperation("refresh");
        }

        if (StringUtils.equals(evt.getItemKey(), KEY_MAIN)) {
            String sql = "/*dialect*/call akmmv_prd_scm_test.KDUpdateJSKC(2,'"+rowid+"')";
            DB.update(DBRoute.basedata, sql);
            this.getView().invokeOperation("refresh");
        }

    }
}
