package kd.cosmic;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.control.events.BeforeItemClickEvent;

import java.math.BigDecimal;
import java.util.EventObject;

/**
 * 描述:逐行生成条码
 * 开发者: 李四辉
 * 创建日期:2024-04-01
 * 关键客户：仓库
 * 已部署正式：true
 * 备注：已投入正式环境使用，无问题
 */

//kd.cosmic.zhsctm
public class zhsctm extends AbstractBillPlugIn {
    private static final String KEY_MAINBAR = "advcontoolbarap";

    private static final String KEY_BARITEM_NEW = "ezob_zhsctm";

    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 侦听主菜单按钮点击事件
        addItemClickListeners(new String[] { "advcontoolbarap" });
    }

    public void beforeItemClick(BeforeItemClickEvent evt) {
        if (StringUtils.equals("ezob_zhsctm", evt.getItemKey())) {
            // TODO 在此添加业务逻辑
            //this.getView().showMessage("测试");
            //获取单据体分录行数量
            int irowcount = getModel().getEntryRowCount("billentry");//明细
            int izmxrowcount = getModel().getEntryRowCount("ezob_subentryentity");//子明细
            for (int parentRow = 0; parentRow < irowcount; parentRow++) {
                if(izmxrowcount > 0){
                    String lotnumberpd = getModel().getValue("lotnumber", parentRow).toString();
                    if(lotnumberpd == "" || lotnumberpd == null){
                        this.getView().showMessage("请先获取批号后再生成条码主档！");
                        return;
                    };
                }
                BigDecimal tmscs = (BigDecimal)getModel().getValue("ezob_tmscs", parentRow);
                BigDecimal zxbzs = (BigDecimal)getModel().getValue("ezob_zxbzs", parentRow);
                if(zxbzs.compareTo(BigDecimal.ZERO) == 0){
                    this.getView().showMessage("最小包装数不允许为0！");
                    return;
                }
                //强制转换为整型，因为条码生成数不可能为小数型
                int t = tmscs.intValue();
                IDataModel iDataModel = getModel();
                // 指定父单据体行号 (必须)
                iDataModel.setEntryCurrentRowIndex("billentry", parentRow);
                iDataModel.deleteEntryData("ezob_subentryentity");
                String lotnumber = getModel().getValue("lotnumber", parentRow).toString();
                BigDecimal zmxqty = (BigDecimal)getModel().getValue("ezob_zxbzs", parentRow);
                BigDecimal qty = (BigDecimal)getModel().getValue("qty", parentRow);
                BigDecimal q = qty.divideAndRemainder(zmxqty)[1];
                //int q = (int)Math.ceil(q3);
                for (int j = 0; j < t; j++) {
                    //子单据体创建新一行数据
                    int subRow = iDataModel.createNewEntryRow("ezob_subentryentity");
                    //赋值
                    getModel().setValue("ezob_tbarcode", lotnumber + "-" + String.format("%03d", new Object[] { Integer.valueOf(j + 1) }), subRow, parentRow);
                    //getModel().setValue("ezob_tbarcode", "A123343-001", subRow, parentRow);
                    getModel().setValue("ezob_xxs", Integer.valueOf(j + 1), subRow, parentRow);//箱系数
                    if (j < t - 1) {
                        getModel().setValue("ezob_sl", zmxqty, subRow, parentRow);//数量
                    } else if (BigDecimal.ZERO.compareTo(q) != 0) {
                        getModel().setValue("ezob_sl", q, subRow, parentRow);//数量
                    } else {
                        getModel().setValue("ezob_sl", zmxqty, subRow, parentRow);//数量
                    }
                    getModel().setValue("ezob_yszq", "", subRow, parentRow);//原始周期
                }
//                this.getView().setEnable(false, parentRow, "producedate");//锁定生产日期
//                this.getView().setEnable(false, parentRow, "ezob_bzq");//锁定保质期
//                this.getView().setEnable(false, parentRow, "expirydate");//锁定到期日
            }
        }
    }
}
