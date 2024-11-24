package kd.cosmic.tables.yszltables;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.sdk.plugin.Plugin;

import java.util.Iterator;

/**
 * 报表界面插件
 */
public class YsAcctageFormRpt extends AbstractReportFormPlugin implements Plugin {
    public YsAcctageFormRpt() {

    }

    @Override
    public void processRowData(String gridPK, DynamicObjectCollection rowData, ReportQueryParam queryParam) {
        super.processRowData(gridPK, rowData, queryParam);
        Iterator var4 = rowData.iterator();
        String pk = "";

        while (var4.hasNext()) {
            DynamicObject a;
        }
    }
}