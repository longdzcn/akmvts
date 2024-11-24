package kd.cosmic;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.fi.fa.business.constants.FaClearBill;
import kd.fi.fa.business.constants.FaRealCard;
import kd.fi.fa.business.utils.FaBizUtils;
import kd.fi.fa.business.utils.FaUtils;
import kd.sdk.plugin.Plugin;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 描述: 清理申请单带出财务卡片编码
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：顾问夏晓君
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class Bill_qlsqd2 extends AbstractFormPlugin implements Plugin {
    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        String name = e.getProperty().getName();
        IDataModel model = this.getModel();
        if(FaClearBill.REALCARD.equalsIgnoreCase(name)){//选择卡片后自动携带
            ChangeData changeData = e.getChangeSet()[0];
            int index = changeData.getRowIndex();
            DynamicObject card = (DynamicObject) model.getValue(FaClearBill.REALCARD,index);
            if(card!=null){

                //后校验选择的实物卡片是否合法，不合法的需要去掉，并提示
                Date date=(Date)model.getValue(FaClearBill.CLEARDATE);
                long orgId=(Long)model.getValue("org_id");
                Set<Long> cardIds = new HashSet<>(1);
                cardIds.add(card.getLong("id"));
                Set<Long> existCards = FaUtils.getCurRealCard(date, orgId, false, cardIds);
                if(!existCards.containsAll(cardIds)) {
                    model.setValue(FaClearBill.REALCARD, null, index);
                    model.setValue(FaClearBill.UNIT,null,index);
                    model.setValue(FaClearBill.CLEARQTY, null,index);
                    model.setValue(FaClearBill.ASSETQTY, null,index);
                    FaUtils.showErrorRealCardMsg(1, null, card, this.getView());
                    return;
                }

                model.setValue(FaClearBill.UNIT,card.getDynamicObject(FaRealCard.UNIT).get("id"),index);
                BigDecimal assetQty =card.getBigDecimal(FaRealCard.ASSETAMOUNT);
                model.setValue(FaClearBill.CLEARQTY, assetQty,index);
                model.setValue(FaClearBill.ASSETQTY, assetQty,index);
				/*if(assetQty.compareTo(BigDecimal.ONE)==0) {
					this.getView().setEnable(false,index, FaClearBill.CLEARQTY);
				}*/

            }else{
                model.setValue(FaClearBill.UNIT, null,index);
                model.setValue(FaClearBill.CLEARQTY, null,index);
            }
        }else if(FaClearBill.ORG.equals(name)) {
            //设置业务日期范围
            FaBizUtils.setDate(this.getView(), FaClearBill.CLEARDATE,getControl(FaClearBill.CLEARDATE),false,true);

        }else{

        }
        super.propertyChanged(e);
    }


}