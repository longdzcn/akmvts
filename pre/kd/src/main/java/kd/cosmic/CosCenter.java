package kd.cosmic;

import com.alibaba.druid.util.StringUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;

/**
 * 描述: 与myhr同步成本中心，标准单据列表
 * 开发者: 易佳伟
 * 创建日期: 1期完成
 * 关键客户：马衍浩
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

//AbstractOperationServicePlugIn
//AbstractListPlugin
public class CosCenter extends AbstractListPlugin implements Plugin {

    private final static String KEY_BARITEM = "ezob_baritemap";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
      //  e.getSource();

        this.addItemClickListeners(KEY_BARITEM);
    }


//bos_costcenter_ext

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        Control source = (Control) evt.getSource();

        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {
            DynamicObject [] dynamicObject = BusinessDataServiceHelper.load("bos_costcenter", "enable,name,orgduty,createtime,parent,accountorg,description,modifytime",null);
           for(int i=0;i<dynamicObject.length;i++) {
               String description="";
               String parent="";

               DynamicObject dynamicObject1 = dynamicObject[i];
               String number = dynamicObject1.get("number").toString();//编码
               String name = dynamicObject1.get("name").toString();//名称
               String  orgduty = ((DynamicObject) dynamicObject1.get("orgduty")).get("name").toString();//类型
               String enable  = dynamicObject1.get("enable").toString();
               if(enable.equals("0")){
                   enable="I";
               }
               if(enable.equals("1")){
                   enable="A";
               }

               try {
                   description = dynamicObject1.get("description").toString();//描述
               }catch (Exception e){
                   e.printStackTrace();
               }

               try {
                    parent = ((DynamicObject) dynamicObject1.get("parent")).get("number").toString();//上级
               }catch (Exception e){
                   e.printStackTrace();
               }
               String createtime = dynamicObject1.get("createtime").toString().replace(" ","T");//启用时间
               String modifytime = dynamicObject1.get("modifytime").toString().replace(" ","T");//修改时间
               if (orgduty.equals("管理")) {
                   orgduty = "M";
               }
               if (orgduty.equals("销售")) {
                   orgduty = "S";
               }
               if (orgduty.equals("研发")) {
                   orgduty = "D";
               } else {
               }
               String remeg=  CostCenterUnit.postmessage(orgduty,enable,number,name,description,parent,modifytime);
               String remeg1=  CostCenterUnit.postmessagede(orgduty,enable,number,name,description,parent,modifytime);
           }

                this.getView().showMessage("推送完成");



        }

    }

}