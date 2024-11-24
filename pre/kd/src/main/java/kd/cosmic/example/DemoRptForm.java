
package kd.cosmic.example;

import kd.bos.report.events.SortAndFilterEvent;
import kd.bos.report.plugin.AbstractReportFormPlugin;

import java.util.List;

/**
 *   描述: 报表表单样例
 *   开发者:
 *   创建日期:
 *   关键客户：
 *   已部署正式：
 *   备注：
 */
public final class DemoRptForm extends AbstractReportFormPlugin {
	
	/** 物料 **/
	private static final String F_material = "material";
	
	/**
	 * 设置报表表头的漏斗过滤
	 * 
	 */
	@Override
	public void setSortAndFilter(List<SortAndFilterEvent> list) {
		super.setSortAndFilter(list);
		for (SortAndFilterEvent event : list) {
			if (F_material.equals(event.getColumnName())) {
				event.setFilter(true);
			}
		}
	}
}
