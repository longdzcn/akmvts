package kd.cosmic.lyj;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 描述: 根据财务应付单的发票明细反写表头的发票号码和发票日期
 * 开发者: 梁远健
 * 创建日期:
 * 关键客户：
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

public class xgfph extends AbstractFormPlugin implements Plugin {
  private static final String KEY_MAINBAR = "tbmain";
  
  private static final String KEY_BARITEM_BC = "bar_save";
  
  public void registerListener(EventObject e) {
    super.registerListener(e);
    addItemClickListeners(new String[] { "tbmain" });
  }
  
  public void itemClick(ItemClickEvent evt) {
    super.itemClick(evt);
    if (StringUtils.equals("bar_save", evt.getItemKey())||StringUtils.equals("bar_audit", evt.getItemKey())) {
      DynamicObject dataEntity = getModel().getDataEntity(true);
      DynamicObjectCollection goodsEntities = dataEntity.getDynamicObjectCollection("inventry");
      List<String> fphm = new ArrayList<>();
      List<Date> fprq = new ArrayList<>();
      for (DynamicObject entryObj : goodsEntities) {
        fphm.add((String)entryObj.get("i_invoiceno"));
        fprq.add((Date)entryObj.get("i_invoicedate"));
      } 
      if (!fphm.isEmpty()) {
        String str = fphm.stream().collect(Collectors.joining("/"));
        getModel().setValue("ezob_fphm", str);
      } 
      if (!fprq.isEmpty()) {
        Date maxDate = Collections.<Date>max(fprq);
        getModel().setValue("ezob_datefield1", maxDate);
      } 
      getView().invokeOperation("save");
      getView().updateView();
    } 
  }
}
