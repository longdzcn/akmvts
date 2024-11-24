package kd.cosmic.fa.report;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.text.SimpleDateFormat;
import java.util.Iterator;

/**
 * 描述: 给折旧表增加二开字段成本中心和启用日期,插件放在报表表单
 * 开发者: 易佳伟
 * 创建日期:二期
 * 关键客户：刘雨婕
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class Rpt_depreciat extends AbstractReportFormPlugin {
    private static Log log = LogFactory.getLog(Rpt_depreciat.class);
    public void processRowData(String gridPK, DynamicObjectCollection rowData, ReportQueryParam queryParam) {

        Iterator var4 = rowData.iterator();
        try {
            String cbzx = null;
            while (var4.hasNext()) {
                DynamicObject row = (DynamicObject) var4.next();
                String number = row.getString("number");
                QFilter qFilter = new QFilter("number", "=", number);
                DynamicObject aRealCardDynamic = BusinessDataServiceHelper.loadSingle("fa_card_real", "id,realcard,originalval,ezob_textfield4,number,ezob_kyxm,remark,costcentrer,ezob_hth,realaccountdate", new QFilter[]{qFilter})  ;
                DynamicObject costcenter =  aRealCardDynamic.getDynamicObject("costcentrer");
                if (costcenter != null) {
                    cbzx = costcenter.getString("number");
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String qyrq = sdf.format(aRealCardDynamic.getDate("realaccountdate"));

                row.set("ezob_cbzx",cbzx);
                row.set("ezob_qyrq",qyrq);


            }
        }catch (Exception e)
        {
           log.info("avc");
        }


    }
}
