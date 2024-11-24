package kd.cosmic;

import kd.bos.context.RequestContext;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.exception.KDException;
import kd.bos.schedule.executor.AbstractTask;
import kd.sdk.plugin.Plugin;

import java.util.Map;

/**
 * 描述: 定时执行存储过程akmmv_prd_eip_test.KDUpdateSomeThing
 * 开发者: 李四辉
 * 创建日期:2024-08-01
 * 关键客户：仓库
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
public class dszxccgc extends AbstractTask implements Plugin {

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        String sql = "/*dialect*/call akmmv_prd_eip_test.KDUpdateSomeThing(1)";
        DB.update(DBRoute.basedata, sql);   
    }
}