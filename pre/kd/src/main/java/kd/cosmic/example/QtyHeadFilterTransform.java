package kd.cosmic.example;

import kd.bos.algo.DataSet;
import kd.bos.orm.query.QFilter;
import kd.bplat.scmc.report.core.tpl.IDataTransform;

import java.util.List;
import java.util.StringJoiner;

/**
 *   描述: 数量类型字段漏斗过滤Transform样例
 *   开发者:
 *   创建日期:
 *   关键客户：
 *   已部署正式：
 *   备注：
 */

public class QtyHeadFilterTransform implements IDataTransform{
	
	private List<QFilter> headFilters;
	
	public QtyHeadFilterTransform(List<QFilter> headFilters) {
		this.headFilters = headFilters;
	}
	
	@Override
	public DataSet doTransform(DataSet srcData) {
		if(headFilters.isEmpty()){
			return srcData;
		}
		
		// 这里假设数量字段为qty, 收集数量字段漏斗过滤条件
		StringJoiner filterExprs = new StringJoiner(" and ");
		for(QFilter filter : headFilters){
			String qtyField = filter.getProperty();
			if("qty".equals(qtyField)){
				String property = filter.getProperty();
				String cp = filter.getCP();
				Object value = filter.getValue();
				String filterStr = property + cp + value;
				filterExprs.add(filterStr);
			}
		}
		if(filterExprs.length() == 0){
			return srcData;
		}
		
		// 过滤结果集
		return srcData.filter(filterExprs.toString());
	}
	
}
