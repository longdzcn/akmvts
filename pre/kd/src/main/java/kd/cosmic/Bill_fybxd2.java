package kd.cosmic;

import com.alibaba.nacos.api.utils.StringUtils;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.AfterAddRowEventArgs;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

/**
 * 描述: 费用报销单新增行根据报销人带出成本中心
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：赖雅瑜
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

public class Bill_fybxd2 extends AbstractFormPlugin implements Plugin {
//  费用明细单据体
  private final static String KEY_ENTRYENTITY = "expenseentryentity";
  //    获取当前人id
  RequestContext requestContext = RequestContext.get();
  long currUserId = requestContext.getCurrUserId();
  //
  DynamicObject bosuser = BusinessDataServiceHelper.loadSingle(currUserId, "bos_user", "name,ezob_cbzx,ezob_cbzx.number");
  //    获取成本中心信息
  DynamicObject ezobCbzx = bosuser.getDynamicObject("ezob_cbzx");
  public void propertyChanged(PropertyChangedArgs e) {
    String name = e.getProperty().getName();
    int yesorNo = 0;

    if (name.equals("ezob_bz")) {
      IDataModel model = getModel();
      DynamicObjectCollection entryEntity = model.getEntryEntity("expenseentryentity");
    for (DynamicObject entry : entryEntity) {

        String value = entry.getString("ezob_bz");
        getModel().setValue("ezob_cbz", "2");
        if (value.equals("1"))
          yesorNo = 1;

      } 
      if (yesorNo == 1)
        getModel().setValue("ezob_cbz", "1");
        getView().updateView();
    }

    }
  @Override
  public void afterAddRow(AfterAddRowEventArgs e) {
    if (StringUtils.equals(KEY_ENTRYENTITY, e.getEntryProp().getName())){
      // 在此添加业务逻辑
      EntryGrid entryGrid = this.getControl(KEY_ENTRYENTITY);
      // 获取新增行赋值
      DynamicObjectCollection rows = entryGrid.getModel().getEntryEntity(KEY_ENTRYENTITY);
      for (DynamicObject row : rows) {
        //根据报销人带出成本中心
        row.set("std_entrycostcenter",ezobCbzx);
      }
      getView().updateView();
    }
  }


}

