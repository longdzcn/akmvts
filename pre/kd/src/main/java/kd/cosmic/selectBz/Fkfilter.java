package kd.cosmic.selectBz;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.field.RefBillEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.epm.eb.formplugin.AbstractListPlugin;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
/**
 * 描述: 付款关联过滤
 * 开发者: 钟有吉
 * 关键客户：熊艳菲
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
public class Fkfilter extends AbstractListPlugin implements BeforeF7SelectListener {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        //给物品基础资料添加beforef7Select事件监听
        //对（企业单号）ezob_qytkdh进行监听
        RefBillEdit ezobQytkdh = this.getControl("ezob_qytkdh");
        RefBillEdit ezobYhtkdh = this.getControl("ezob_yhtkdh");
        ezobQytkdh.addBeforeF7SelectListener(this);
        ezobYhtkdh.addBeforeF7SelectListener(this);
    }
    @Override
    public void beforeF7Select(BeforeF7SelectEvent evt) {
        ListShowParameter formShowParameter = (ListShowParameter) evt.getFormShowParameter();
        List<QFilter> qFilters = new ArrayList<>();
        //获取当前组织的信息
        DynamicObject org = (DynamicObject) this.getModel().getValue("org");
        //如果当前组织的值于父组织的值相等则添加到过滤器中
        qFilters.add(new QFilter("registorg", QCP.equals, org.getPkValue()));
        formShowParameter.getListFilterParameter().setQFilters(qFilters);
    }
}

