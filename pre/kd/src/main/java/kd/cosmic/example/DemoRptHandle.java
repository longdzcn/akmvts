package kd.cosmic.example;

import kd.bos.orm.query.QFilter;
import kd.bplat.scmc.report.core.ReportDataCtx;
import kd.bplat.scmc.report.core.tpl.IDataTransform;
import kd.bplat.scmc.report.core.tpl.IReportDataHandle;

import java.util.LinkedList;
import java.util.List;

/**
 *   描述: 报表表单样例
 *   开发者:
 *   创建日期:
 *   关键客户：
 *   已部署正式：
 *   备注：
 */
public final class DemoRptHandle implements IReportDataHandle {

	@Override
	public void setupCtx(ReportDataCtx ctx) {
		// 设置漏斗过滤条件供查询使用，场景1：文本类型字段过滤
		setTextHeadFilter(ctx);
		// 设置漏斗过滤条件供查询使用，场景2：基础资料类型字段过滤
		setBaseDataHeadFilter(ctx);
	}

	/**
	 * 设置漏斗过滤条件供查询使用，场景1：文本类型字段过滤
	 * @param ctx	报表框架上下文参数
	 */
	private void setTextHeadFilter(ReportDataCtx ctx) {
		// 从自定义参数中获取漏斗原始过滤条件
		List<QFilter> headFilters = ctx.getParam(RptConst.KEY_HEAD_FILTERS);
		if (headFilters == null || headFilters.isEmpty()) {
			return;
		}

		// 这里假设文本字段为name，根据标识获取其漏斗过滤QFilter
		List<QFilter> billHeadFilters = new LinkedList<>();
		for (QFilter headFilter : headFilters) {
			if("name".equals(headFilter.getProperty())){
				billHeadFilters.add(headFilter);
			}
		}
		
		// 将漏斗过滤条件设置到pageFs中供查询使用
		ctx.getPageFs().addAll(billHeadFilters);
	}
	
	/**
	 * 设置漏斗过滤条件供查询使用，场景2：基础资料类型字段过滤
	 * @param ctx	报表框架上下文参数
	 */
	private void setBaseDataHeadFilter(ReportDataCtx ctx) {
		// 从自定义参数中获取漏斗原始过滤条件
		List<QFilter> headFilters = ctx.getParam(RptConst.KEY_HEAD_FILTERS);
		if (headFilters == null || headFilters.isEmpty()) {
			return;
		}

		// 这里假设基础资料字段为material，根据标识获取其漏斗过滤QFilter，转换成编码过滤
		List<QFilter> billHeadFilters = new LinkedList<>();
		for (QFilter headFilter : headFilters) {
			if("material".equals(headFilter.getProperty())){
				headFilter.__setProperty("material.number");
				billHeadFilters.add(headFilter);
			}
		}
		
		// 将漏斗过滤条件设置到pageFs中供查询使用
		ctx.getPageFs().addAll(billHeadFilters);
	}
	
	@Override
	public void transformResult(List<IDataTransform> transCollector, ReportDataCtx ctx) {
		// 漏斗过滤，场景3：数量类型字段过滤
		List<QFilter> headFilters = ctx.getParam(RptConst.KEY_HEAD_FILTERS);
		transCollector.add(new QtyHeadFilterTransform(headFilters));
	}
	
}
