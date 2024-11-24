package kd.cosmic;

import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang.StringUtils;

import java.util.EventObject;

/**
 * 描述: 在凭证协同列表中添加清除凭证协同关联关系的功能
 * 开发者: 梁远健
 * 创建日期:
 * 关键客户：
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

public class qcpzgx extends AbstractListPlugin implements Plugin {
    private final static String KEY_BARITEM = "ezob_cqpzxtglgx";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
            this.addItemClickListeners(KEY_BARITEM);
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        if (StringUtils.equals(KEY_BARITEM,evt.getItemKey())){
            String delSql = "/*dialect*/call akmmv_prd_fi_test.KDDeleteVouLink";
            DB.update(DBRoute.basedata, delSql);
            this.getView().showMessage("清除凭证协调关联关系成功");
        }
    }
}