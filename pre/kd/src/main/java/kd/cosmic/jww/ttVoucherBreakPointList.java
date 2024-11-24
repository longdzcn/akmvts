package kd.cosmic.jww;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.list.BillList;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.*;
import java.util.Map.Entry;


/**
 * 描述: [ZOC]通过另外创建的按钮【人工调整】，如果点击此按钮则通过自定义传递的参数打开人工调整界面
 * 开发者: 江伟维
 * 创建日期:
 * 关键客户：
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class ttVoucherBreakPointList extends AbstractListPlugin implements Plugin {

    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        switch (evt.getItemKey()) {
            case "ezob_btnvap":
                this.doManualAdjustOperation();
        }
    }

    private void doManualAdjustOperation() {
        String curorg = this.getPageCache().get("curOrg");
        String curBooktype = this.getPageCache().get("curBooktype");
        String curperiod = this.getPageCache().get("curPeriod");
        int permission = PermissionServiceHelper.checkPermission(RequestContext.get().getCurrUserId(), Long.parseLong(curorg), "gl", "gl_voucherbreakpoint", "0KWFI5FXP6GK");
        if (permission == 0) {
            this.getView().showErrorNotification(ResManager.loadKDString("无“断号调整”的”人工调整“权限，请联系管理员。", "VchBreakPointList_8", "fi-gl-formplugin", new Object[0]));
        } else {
            BillList billList = (BillList)this.getControl("billlistap");
            //这里选中的凭证号顺序会被打乱，需要重新找出来，但是扯淡的是，金蝶没有判断被选中状态的方法（或者我没有找到？），所以只能重新遍历排序
            ListSelectedRowCollection listSelect = billList.getSelectedRows();
            ListSelectedRowCollection listAll = billList.getCurrentListAllRowCollection();

            // 创建一个映射来存储listAll中行的主键值和它们的索引
            Map<Object, Integer> primaryKeyIndexMap = new HashMap<>();
            for (int i = 0; i < listSelect.size(); i++) {
                ListSelectedRow row = listSelect.get(i);
                Object primaryKeyValue = row.getPrimaryKeyValue();
                if (primaryKeyValue != null) { // 确保不添加null值到映射中
                    primaryKeyIndexMap.put(primaryKeyValue, row.getRowKey());
                }
            }
            // 创建一个List来保存Map的Entry对象
            List<Entry<Object, Integer>> entries = new ArrayList<>(primaryKeyIndexMap.entrySet());

            // 使用自定义比较器对List进行排序
            Collections.sort(entries, new Comparator<Entry<Object, Integer>>() {
                @Override
                public int compare(Map.Entry<Object, Integer> o1, Map.Entry<Object, Integer> o2) {
                    // 根据Integer值进行升序排序
                    return o1.getValue().compareTo(o2.getValue());
                }
            });

            // 创建一个Object数组来保存排序后的键
            Object[] ids = new Object[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                ids[i] = entries.get(i).getKey();
            }

            if (listSelect.isEmpty()) {
                this.getView().showMessage(ResManager.loadKDString("请选择要人工调整的数据", "VchBreakPointList_6", "fi-gl-formplugin", new Object[0]));
            } else if (listSelect.size() > 10000) {
                this.getView().showMessage(ResManager.loadKDString("人工调整不支持超过10000条以上的数据", "VchBreakPointList_5", "fi-gl-formplugin", new Object[0]));
            } else if (!listSelect.isEmpty() && listSelect.size() <= 10000) {
                this.showManualForm(curorg, curBooktype, curperiod, ids);
            }

        }
    }

    private void showManualForm(String curorg, String curBooktype, String curperiod, Object[] ids) {
        FormShowParameter currFormShowParameter = this.getView().getFormShowParameter();
        Map<String, Object> customParams = currFormShowParameter.getCustomParams();
        FormShowParameter show = new FormShowParameter();
        CloseCallBack closeCallBack = new CloseCallBack(this, "btnmanual");
        show.setCloseCallBack(closeCallBack);
        show.setFormId("gl_manualbreakpoint");
        show.getOpenStyle().setShowType(ShowType.Modal);
        customParams.put("voucherids", ids);
        Map<String, Object> params = new HashMap();
        params.putAll(customParams);
        params.put("orgCol", curorg);
        params.put("booktype", curBooktype);
        params.put("periodCol", curperiod);
        show.setCustomParams(params);
        this.getView().showForm(show);
    }

}