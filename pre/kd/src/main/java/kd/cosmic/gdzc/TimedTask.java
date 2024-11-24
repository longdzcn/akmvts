package kd.cosmic.gdzc;

import kd.bos.context.RequestContext;
import kd.bos.exception.KDException;
import kd.bos.schedule.executor.AbstractTask;
import kd.sdk.plugin.Plugin;

import java.util.Map;

/**
 * 后台任务插件
 */
public class TimedTask extends AbstractTask implements Plugin {

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
//        String number1 = "LE0002";
//        String number2 = "LE0017";
//        String number3 = "LE0018";
//        String number4 = "LE0089";
//        String number5 = "LE0900";
//        List<String> abc = new ArrayList<>();
//        abc.add(number1);
//        abc.add(number2);
//        abc.add(number3);
//        abc.add(number4);
//        abc.add(number5);
//        QFilter q = new QFilter("number", QCP.in,abc);
//        DynamicObject[] bosUsers = BusinessDataServiceHelper.load("bos_user", "", new QFilter[]{q});
//        for (DynamicObject dynamicObject : bosUsers) {
//            String string = dynamicObject.getString("id");
//            String SQL = "/*dialect*/update t_meta_firstlogin set FISFIRSTLOGIN = 1 where FUSERID = "+ string + "and FPAGEID " +
//                    "in ('er_tripreqbill','er_tripreimbill_grid','er_dailyreimbursebill','er_dailyloanbill','er_dailyapplybill')";
//        }
//    }
        String zzxry = "/*dialect*/ select ";
    }
}