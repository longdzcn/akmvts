package kd.cosmic;

import com.alibaba.druid.util.StringUtils;
import kd.bos.data.BusinessDataReader;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.metadata.IDataEntityType;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.botp.runtime.ConvertOperationResult;
import kd.bos.entity.botp.runtime.PushArgs;
import kd.bos.entity.datamodel.IRefrencedataProvider;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.control.TreeView;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.control.events.TreeNodeCheckEvent;
import kd.bos.form.control.events.TreeNodeCheckListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.BillList;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.botp.ConvertServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * 标准单据列表
 * 分配（慧乾）
 */
public class BusinessInformationListPlugin extends AbstractFormPlugin implements Plugin, TreeNodeCheckListener {

    private final static String KEY_BARITEM = "btn_assign";
    private final static String KEY_TREEVIEW = "treeviewap";

    private static List<String> orgIDs =new ArrayList<>();


    private static Log log = LogFactory.getLog(BusinessInformationListPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        Button button = this.getView().getControl(KEY_BARITEM);
        button.addClickListener(this);
        TreeView treeView = this.getView().getControl(KEY_TREEVIEW);
        treeView.addTreeNodeCheckListener(this);

    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control)evt.getSource();
        List<DynamicObject> targetBillObjs = null;
        if (StringUtils.equals(KEY_BARITEM, source.getKey())){
            // 事件处理代码略过
            BillList billList = this.getView().getControl("billlistap");
            ListSelectedRowCollection selectedRows = billList.getSelectedRows();
//            选中的单据
            for (ListSelectedRow row :selectedRows)  {
//                选中的组织
                for (String orgID : orgIDs) {
                    if (Long.parseLong(orgID)==100000L) break;
                    List<String> sTargetEntity = new ArrayList<>(16);
                    sTargetEntity.add("bd_materialpurchaseinfo");  //物料采购信息//如果是mv的物料则不需要质检信息
                    sTargetEntity.add("bd_inspect_cfg");  //物料质检信息
                    sTargetEntity.add("bd_materialinventoryinfo");  //物料库存信息
                    sTargetEntity.add("bd_materialsalinfo");  //物料销售信息
//                    生成业务信息
                    for (String s : sTargetEntity) {
                        PushArgs pushArgs = new PushArgs();
                        pushArgs.setSourceEntityNumber("bd_material");
                        pushArgs.setTargetEntityNumber(s);
                        // 可选，传入true，不检查目标单新增权
                        pushArgs.setHasRight(false);
                        //注意一定要设置当前组织，不然下推会以安捷利美维集团组织下推
                        pushArgs.setCurrentOrgId(Long.valueOf(orgID));
                        // 可选，传入目标单主组织默认值
                        pushArgs.setDefOrgId(Long.valueOf(orgID));
                        // 生成转换结果报告
                        pushArgs.setBuildConvReport(true);
                        // 必填，设置需要下推的单据，或分录行
                        List<ListSelectedRow> rows = new ArrayList<>();
                        //第一行
                        ListSelectedRow row1 = new ListSelectedRow();
                        //必填，设置源单单据id
                        row1.setPrimaryKeyValue(row.getPrimaryKeyValue());
                        rows.add(row1);

                        pushArgs.setSelectedRows(rows);
                        // 执行下推服务
                        ConvertOperationResult pushResult = ConvertServiceHelper.push(pushArgs);
//自动提交审核
                        // 获取生成的目标单数据包
                        MainEntityType targetMainType = EntityMetadataCache.getDataEntityType(s);
                        targetBillObjs = pushResult.loadTargetDataObjects(
                                new IRefrencedataProvider() {
                                    @Override
                                    public void fillReferenceData(Object[] objs, IDataEntityType dType) {
                                        BusinessDataReader.loadRefence(objs, dType);
                                    }
                                }, targetMainType);
                        //下推保存
                        OperationResult saveResult = SaveServiceHelper.saveOperate(
                                s,
                                targetBillObjs.toArray(new DynamicObject[0]));
                        OperationServiceHelper.executeOperate("submit", s, targetBillObjs.toArray(new DynamicObject[0]), OperateOption.create());
                        OperationServiceHelper.executeOperate("audit", s, targetBillObjs.toArray(new DynamicObject[0]), OperateOption.create());
                    }
                }
            }
        }
    }
    @Override
    public void treeNodeCheck(TreeNodeCheckEvent treeNodeCheckEvent) {
        TreeView treeView = (TreeView)treeNodeCheckEvent.getSource();
        if (StringUtils.equals(treeView.getKey(), KEY_TREEVIEW)){
            orgIDs = treeView.getTreeState().getCheckedNodeIds();
        }
    }

//    public
    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);

        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {

            BillList billList = this.getView().getControl("billlistap");
            ListSelectedRowCollection selectedRows = billList.getSelectedRows();
            int size = selectedRows.size();
        }
    }


}