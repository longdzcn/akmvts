package kd.cosmic.rrfy;

import com.alibaba.nacos.api.utils.StringUtils;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.AfterAddRowEventArgs;
import kd.bos.entity.datamodel.events.AfterDeleteRowEventArgs;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述: 判断是否超标，带出成本中心，带出核销人。功能：1.判断是否超标 2.带出成本中心和核销人
 * 开发者: 钟有吉
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
public class Bill_fybxd2 extends AbstractFormPlugin implements Plugin {
  private final static String KEY_ENTRYENTITY = "expenseentryentity";

  @Override
  public void afterDeleteRow(AfterDeleteRowEventArgs e) {
    super.afterDeleteRow(e);
    String s = e.getEntryProp().getName();
    System.out.println(s);
    if (StringUtils.equals(e.getEntryProp().getName(), KEY_ENTRYENTITY)) {
      IDataModel model = getModel();
      boolean isValueOneFound = false;
      DynamicObjectCollection entryEntity = model.getEntryEntity("expenseentryentity");
      for (DynamicObject entry : entryEntity) {
        String value = entry.getString("ezob_bz");//value = "1"
        if ("1".equals(value)) {
          isValueOneFound = true; // 如果找到value等于"1"，设置标志为true
          break; // 找到后可以立即退出循环
        }
      }
      if (!isValueOneFound) {
        model.setValue("ezob_cbz", "2"); // 如果所有是否超标都不是，则设置为不超标
      }
      this.getView().updateView();
    }
  }

  /**
   * 在JW的判断超标类上加了一个增行带出成本中心以及核销人
   */

  public void propertyChanged(PropertyChangedArgs e) {
     String name = e.getProperty().getName();
    int yesorNo = 0;
    final String npn="1";
    if ("ezob_bz".equals(name)) {
      IDataModel model = getModel();
      DynamicObjectCollection entryEntity = model.getEntryEntity("expenseentryentity");
      for (DynamicObject entry : entryEntity) {
        String value = entry.getString("ezob_bz");//value = "1"
        if (value != null && value.equals(npn)) {
          yesorNo = 1; // 如果是否超标为是，则设置yesorNo为1
          break; // 一旦找到是否超标为是的记录，就可以退出循环
        }
      }
      if (yesorNo == 1) {
        model.setValue("ezob_cbz", "1"); // 如果至少有一个是否超标为是，则设置为超标
      } else {
        model.setValue("ezob_cbz", "2"); // 如果所有是否超标都不是，则设置为不超标
      }
      this.getView().updateView();
    }
  }
  @Override
  public void afterAddRow(AfterAddRowEventArgs e) {
    if (StringUtils.equals(KEY_ENTRYENTITY, e.getEntryProp().getName())){
      //    获取当前人id
        RequestContext requestContext = RequestContext.get();
      long currUserId = requestContext.getCurrUserId();
      //获取人员信息
      DynamicObject bosuser = BusinessDataServiceHelper.loadSingle(currUserId, "bos_user", "name,number,ezob_cbzx,ezob_cbzx.number");
      //    获取成本中心信息
      DynamicObject ezobCbzx = bosuser.getDynamicObject("ezob_cbzx");
//      获取人员编码
      String number = bosuser.getString("number");
      EntryGrid entryGrid = this.getControl(KEY_ENTRYENTITY);
      DynamicObjectCollection rows = entryGrid.getModel().getEntryEntity(KEY_ENTRYENTITY);
// 创建一个 HashMap
      Map<Integer,Object> map = new HashMap<>();
      for (DynamicObject row : rows) {
//        //        获取序列号
        int seq = Integer.parseInt(row.getString("seq"));
//        获取报销人
        String reimburser = row.getString("reimburser.number");
        QFilter q = new QFilter("number", QCP.equals, reimburser);
        DynamicObject[] ry = BusinessDataServiceHelper.load("bos_user", "name,number,ezob_cbzx,ezob_cbzx.number",new QFilter[]{q});

//        String cbzx = row.getString("std_entrycostcenter");
//        map.put(seq,cbzx);
        for (DynamicObject dynamicObject : ry) {
//          获取成本中心信息
          DynamicObject ezobCbzx1 = dynamicObject.getDynamicObject("ezob_cbzx");
          //        给成本中心赋值
          row.set("std_entrycostcenter",ezobCbzx1);
          //   给报销人赋值
          row.set("reimburser.number",reimburser);

        }
//        //        给成本中心赋值
//        row.set("std_entrycostcenter",ezobCbzx);
//        //   给报销人赋值
//        row.set("reimburser.number",number);

      }
//      for (DynamicObject row : rows) {
////        获取序列号
//        int seq = Integer.parseInt(row.getString("seq"));
//        if (seq<rows.size()){
//          //        给成本中心赋值
//          row.set("std_entrycostcenter",map.get(seq));
//        }else {
//          //        给成本中心赋值
//          row.set("std_entrycostcenter",ezobCbzx);
//        }
//
////   给报销人赋值
//        row.set("reimburser.number",number);
//      }
      getView().updateView();
    }

  }



}

