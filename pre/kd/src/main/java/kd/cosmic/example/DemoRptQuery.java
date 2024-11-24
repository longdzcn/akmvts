package kd.cosmic.example;

import kd.bos.entity.report.ReportQueryParam;
import kd.bplat.scmc.report.core.tpl.AbstractReportQuery;

import java.util.Map;

/**
 *   描述: 报表查询插件样例
 *   开发者:
 *   创建日期:
 *   关键客户：
 *   已部署正式：
 *   备注：
 */

public final class DemoRptQuery extends AbstractReportQuery{
	
	@Override
	public ReportQueryParam getQueryParam() {
		ReportQueryParam queryParam = super.getQueryParam();
		// 从ReportQueryParam获取漏斗过滤条件设置到自定义参数中
		Map<String, Object> customParam = queryParam.getCustomParam();
		customParam.put(RptConst.KEY_HEAD_FILTERS, queryParam.getFilter().getHeadFilters());		
		return queryParam;
	}
}
