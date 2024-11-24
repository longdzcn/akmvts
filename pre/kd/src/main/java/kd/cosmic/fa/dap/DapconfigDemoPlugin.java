package kd.cosmic.fa.dap;

import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.ext.fi.ai.AbstractDapWriteBackImpl;
import kd.bos.ext.fi.ai.Voucher;
import kd.bos.ext.fi.ai.VoucherOperation;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 描述: 在费用研发分摊生成凭证时，反写生成的凭证号给源单表头字段凭证号，复选框选中,删除凭证时，反之
 * 开发者: 易佳伟
 * 创建日期:二期
 * 关键客户：黄淑玲
 * 已部署正式：?
 * 备注：
 */
public class DapconfigDemoPlugin extends AbstractDapWriteBackImpl {
	public static final Log log = LogFactory.getLog(DapconfigDemoPlugin.class);
	//采购申请单上，要反写的凭证号字段名
	public static final String voucherNoFile="fk_ezob_pzh";
	@Override
	protected DBRoute getDBRoute() {
		// 扩展开发库的分库标识，实际值可以查看mc分库标识
		DBRoute dbr = DBRoute.of("fi");
		return dbr;
	}

	// 删除凭证写入的值这里是复选框- 布尔false
	@Override
	protected Object getVchDisableStatus() {
		Boolean disRchValue = false;
		return disRchValue;
	}

	// 生成凭证时写入的值，这里是复选框- 布尔 true。
	@Override
	protected Object getVchEnableStatus() {
		Boolean rchValue = true;
		return rchValue;
	}

	// 要反写的字段,反写的是常量值，反写凭证的一些其他信息回单据只能重写writeBack方法
	@Override
	protected String getVchStatusField() {
		String vchField = "fk_ezob_sfscpz";
		return vchField;
	}

	@Override
	public void writeBack(VoucherOperation operation, String billEntityNumber, Map<Long, Voucher> billToVch) {
		MainEntityType mt = EntityMetadataCache.getDataEntityType(billEntityNumber);
		Set<Long> keySet = billToVch.keySet();
		if (VoucherOperation.Create.equals(operation)) {
			log.info("AbstractDapWriteBackImpl#开始执行生成凭证反写:" + operation);
			Object vchStatusField = getVchEnableStatus();
			List<Object[]> params = new ArrayList<>();
			for (Long me : keySet) {
				log.info("AbstractDapWriteBackImpl#生成凭证的单据ID：" + me);
				Voucher voucher=billToVch.get(me);//凭证
				String voucherNo = voucher.getVoucherNo();
				//这里处理的是一个单据只有一个凭证的时候，如果是多个凭证，可以先获取已经生成的凭证号，再拼接上字符串。
				Object[] param = new Object[] { vchStatusField,voucherNo, me };//
				params.add(param);
				if (params.size() >= 10000) {

					DB.executeBatch(getDBRoute(),
							"update " + mt.getAlias() + " set " + getVchStatusField() + " = ? , "+voucherNoFile+" = ? where fid = ?", params);

					params.clear();
				}
			}

			if (params.size() > 0) {

				DB.executeBatch(getDBRoute(),
						"update " + mt.getAlias() + " set " + getVchStatusField() + " = ?  , "+voucherNoFile+" = ? where fid = ?", params);
			}

		} else if (VoucherOperation.Delete.equals(operation)) {
			log.info("AbstractDapWriteBackImpl#开始执行删除凭证反写:" + operation);
			Object vchDisableStatus = getVchDisableStatus();
			List<Object[]> params = new ArrayList<>();
			for (Long me : keySet) {
				log.info("AbstractDapWriteBackImpl#删除凭证的单据ID：" + me);
				//这里处理的是一个单据只有一个凭证的时候，如果是多个凭证，可以先获取已经生成的凭证号，再去掉本凭证之后的凭证号
				Object[] param = new Object[] {vchDisableStatus, "",me };// getVchDisableStatus
				params.add(param);
				if (params.size() >= 10000) {
					DB.executeBatch(getDBRoute(),
							"update " + mt.getAlias() + " set " + getVchStatusField() + " = ?  , "+voucherNoFile+" = ? where fid = ?", params);
					params.clear();
				}
			}

			if (params.size() > 0) {
				DB.executeBatch(getDBRoute(),
						"update " + mt.getAlias() + " set " + getVchStatusField() + " = ? , "+voucherNoFile+" = ? where fid = ?", params);
			}
		}
	}
}
