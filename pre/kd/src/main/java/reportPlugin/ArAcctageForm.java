package reportPlugin;

import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.report.plugin.AbstractReportFormPlugin;

import java.util.Iterator;

public class ArAcctageForm extends AbstractReportFormPlugin {

    @Override
    public void processRowData(String gridPK, DynamicObjectCollection rowData, ReportQueryParam queryParam) {
        // TODO Auto-generated method stub
        super.processRowData(gridPK, rowData, queryParam);
        Iterator iterator = rowData.iterator();
        String pk = "";
        while (iterator.hasNext()) {
            }
        }
    }
