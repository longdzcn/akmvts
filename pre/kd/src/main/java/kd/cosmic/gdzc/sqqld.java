package kd.cosmic.gdzc;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.fi.fa.common.util.FaCurrencyUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EventObject;
import java.util.logging.LogRecord;

/**
 * 描述: 资产清理单携带上游单据的比例，并进行计算残值
 * 开发者: 钟有吉
 * 关键客户：黄晓文
 * 已部署正式：false
 * 备注：目前只在测试环境
 */

public class sqqld extends AbstractFormPlugin {
	private LogRecord operationResult;

	@Override
	public void afterBindData(EventObject e) {
		try {
			super.afterBindData(e);
			//		// 获取当前表单的主实体模型
			DynamicObject dataEntity = this.getModel().getDataEntity();
			DynamicObjectCollection detailentry = dataEntity.getDynamicObjectCollection("detail_entry");

//		Map<String, Object> map = new HashMap<>();
			for (DynamicObject dynamicObject : detailentry) {
//			获取行序号
				String seq = dynamicObject.get("seq").toString();
				int i = Integer.parseInt(seq);
				i = i - 1;
//			获取资产清理原值
				String assetvalue = dynamicObject.getString("assetvalue");
				BigDecimal asset = new BigDecimal(assetvalue);

//			获取资产编码
				String number = dynamicObject.getString("number");
				QFilter q = new QFilter("number", QCP.equals, number);
				String depreuse = dynamicObject.getString("depreuse.id");
				QFilter d = new QFilter("depreuse", QCP.equals, depreuse);
				DynamicObject dynamicObject1 = BusinessDataServiceHelper.loadSingle("fa_card_fin", "originalval,accumdepre,decval,netamount,preresidualval,clearrate", new QFilter[]{q, d});
				if (dynamicObject1 != null) {
					String string = dynamicObject1.getString("originalval");
					BigDecimal origin = new BigDecimal(string);
					BigDecimal rate = asset.divide(origin, 10 ,4);
					setPartValue(rate, dynamicObject1, Boolean.TRUE, i, true,asset);
				}
				}
				OperationResult rnt = OperationServiceHelper.executeOperate("save", "fa_clearbill", new DynamicObject[]{dataEntity}, OperateOption.create());

			}catch(Exception e1){
				this.operationResult.setMessage(e1.getMessage());
				return;
			}
		}
	void setPartValue(BigDecimal rate, DynamicObject fincard, Boolean assetVlue, int rowIndex, boolean isAssetValueChange,BigDecimal asset) {
		IDataModel model = this.getModel();
		model.beginInit();
		model.setValue("clearrate", rate, rowIndex);
		if (assetVlue) {
			model.setValue("isclearall", rate.compareTo(BigDecimal.ONE) == 0 ? "1" : "0", rowIndex);
		} else {
			model.setValue("isclearall", rate.compareTo(BigDecimal.ONE) == 0 ? "1" : "2", rowIndex);
		}

		DynamicObject basecurrency = (DynamicObject)model.getValue("basecurrency", rowIndex);
		BigDecimal clearAcuumDepre;

		if (!isAssetValueChange) {
			clearAcuumDepre = fincard.getBigDecimal("originalval").multiply(rate);
			clearAcuumDepre = FaCurrencyUtil.parseByCurAmtPrecision(basecurrency, clearAcuumDepre);
			model.setValue("assetvalue", clearAcuumDepre, rowIndex);
		}
		BigDecimal originalval = fincard.getBigDecimal("originalval");
		BigDecimal accumdepre = fincard.getBigDecimal("accumdepre");
		BigDecimal divide = accumdepre.divide(originalval, 7, RoundingMode.DOWN);
		BigDecimal bfaccumdepre = asset.divide(divide, 7, RoundingMode.HALF_UP);
		bfaccumdepre = FaCurrencyUtil.parseByCurAmtPrecision(basecurrency, bfaccumdepre);
		model.setValue("addupdepre", bfaccumdepre, rowIndex);
		BigDecimal cleardecval = fincard.getBigDecimal("decval").multiply(rate);
		cleardecval = FaCurrencyUtil.parseByCurAmtPrecision(basecurrency, cleardecval);
		model.setValue("decval", cleardecval, rowIndex);
		BigDecimal clearnetamount = fincard.getBigDecimal("netamount").multiply(rate);
		clearnetamount = FaCurrencyUtil.parseByCurAmtPrecision(basecurrency, clearnetamount);
		model.setValue("netamount", clearnetamount, rowIndex);
		BigDecimal clearpreresidualval = fincard.getBigDecimal("preresidualval").multiply(rate);
		clearpreresidualval = FaCurrencyUtil.parseByCurAmtPrecision(basecurrency, clearpreresidualval);
		model.setValue("preresidualval", clearpreresidualval, rowIndex);
		model.endInit();
		this.getView().updateView("detail_entry");
		model.setDataChanged(false);
	}
}