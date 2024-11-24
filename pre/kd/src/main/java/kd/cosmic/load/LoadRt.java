package kd.cosmic.load;

import kd.bos.algo.DataSet;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.Uuid8;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.datamodel.events.BizDataEventArgs;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.fi.er.business.utils.ErEntityTypeUtils;
import kd.fi.er.model.FormModel;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;


/**
 *   description: 根据报销提醒设置，来判断费用申请单和出差申请单是否需要弹框，插件位置，在需要弹框的表单表体插件配置，目前已配置费用申请和出差申请
 *   author     : 易佳伟
 *   createDate : 2024/07/22
 *   关键客户 ： 黄淑玲
 *   islife ：ture
 *   备注 ： 已投入正式环境使用，无问题
 *
 *
 */
public class LoadRt extends AbstractFormPlugin implements Plugin {

    @Override
    public void createNewData(BizDataEventArgs e) {

        super.createNewData(e);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        String entity = String.valueOf(this.getModel().getDataEntityType());

        long userId = UserServiceHelper.getCurrentUserId();
        long orgId = RequestContext.get().getOrgId();
        long aWZ = 2012970605850146816L;
        long aYD = 1727016761368301568L;
        long aHG = 1727018577904587776L;
        long aHK = 1724819622093417472L;
        long aTL = 1838391834745816064L;

        String sql = "";
        e.getSource();
        if (orgId != aHK && orgId != aTL && orgId != aHG && orgId != aYD && orgId != aWZ) {


            if (entity.equals("er_tripreqbill"))   //出差申请
            {
                String selsql = "/*dialect*/select t2.fmulttipscontent from akmmv_prd_fi_test.t_er_tipsinfo t1 inner join akmmv_prd_fi_test.t_er_tipsinfo_l  t2 on t1.FENTRYID = t2.FENTRYID\n" +
                        "\n" +
                        "where t1.FBILLTYPE ='er_tripreqbill'  and t1.fisdefaultshow =1  and t2.FLOCALEID ='zh_CN'";
                sql = selsql;
                String content = this.getContent(sql);
                this.showPcTips(content);
            } else if (entity.equals("er_dailyapplybill")) {  //费用申请

                String selsql = "/*dialect*/select t2.fmulttipscontent from akmmv_prd_fi_test.t_er_tipsinfo t1 inner join akmmv_prd_fi_test.t_er_tipsinfo_l  t2 on t1.FENTRYID = t2.FENTRYID\n" +
                        "\n" +
                        "where t1.FBILLTYPE ='er_dailyapplybill'  and t1.fisdefaultshow =1  and t2.FLOCALEID ='zh_CN'";
                sql = selsql;
                String content = this.getContent(sql);
                this.showPcTips(content);
            } else if (entity.equals("er_dailyreimbursebill")) {
                String selsql = "/*dialect*/select t2.fmulttipscontent from akmmv_prd_fi_test.t_er_tipsinfo t1 inner join akmmv_prd_fi_test.t_er_tipsinfo_l  t2 on t1.FENTRYID = t2.FENTRYID\n" +
                        "\n" +
                        "where t1.FBILLTYPE ='er_dailyreimbursebill'  and t1.fisdefaultshow =1  and t2.FLOCALEID ='zh_CN'";
                sql = selsql;
                String content = this.getContent(sql);
                this.showFirstPC(content);
            } else if (entity.equals("er_dailyloanbill")) {
                String selsql = "/*dialect*/select t2.fmulttipscontent from akmmv_prd_fi_test.t_er_tipsinfo t1 inner join akmmv_prd_fi_test.t_er_tipsinfo_l  t2 on t1.FENTRYID = t2.FENTRYID\n" +
                        "\n" +
                        "where t1.FBILLTYPE ='er_dailyloanbill'  and t1.fisdefaultshow =1  and t2.FLOCALEID ='zh_CN'";
                sql = selsql;
                String content = this.getContent(sql);
                this.showFirstPC(content);
            } else if (entity.equals("er_tripreimbursebill")) {
                String selsql = "/*dialect*/select t2.fmulttipscontent from akmmv_prd_fi_test.t_er_tipsinfo t1 inner join akmmv_prd_fi_test.t_er_tipsinfo_l  t2 on t1.FENTRYID = t2.FENTRYID\n" +
                        "\n" +
                        "where t1.FBILLTYPE ='er_tripreimbursebill'  and t1.fisdefaultshow =1  and t2.FLOCALEID ='zh_CN'";
                sql = selsql;
                String content = this.getContent(sql);
                this.showFirstPC(content);
            }
        }

    }
    public String getContent(String selsql) {

        DataSet selDs = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, selsql);

        ORM orm = ORM.create();

        DynamicObjectCollection rows = orm.toPlainDynamicObjectCollection(selDs);

        String content = "";

        for (DynamicObject row : rows) {
            content = row.getString("fmulttipscontent");
        }

        return content;
    }
    public void showPcTips(String content){
        IFormView view = this.getView();
        FormModel formModel = new FormModel("er_reimtipsfirstshowpage", ResManager.loadKDString("温馨提示", "ReimburseTipsUtils_0", "fi-er-formplugin", new Object[0]), "1", true);
        formModel.setShowType(ShowType.Modal);
        Map<String, Object> customParam = new HashMap<>();
        customParam.put("tips", content);
        formModel.setCustomParam(customParam);
        FormShowParameter formShowParameter = formModel.getFormShowParameter();
        view.showForm(formShowParameter);
    }
    public void showFirstPC(String content){
        IFormView view = this.getView();
        String normalEntityId = view.getFormShowParameter().getFormId();
        String formId = checkLoan(normalEntityId, view);
        RequestContext requestContext = RequestContext.get();
        Long userId = Long.valueOf(requestContext.getUserId());
        if (isFirstLogin(userId, formId)) {
            FormModel formModel = new FormModel("er_reimtipsfirstshowpage", ResManager.loadKDString("温馨提示", "ReimburseTipsUtils_0", "fi-er-formplugin", new Object[0]), "1", true);
            formModel.setShowType(ShowType.Modal);
            Map<String, Object> customParam = new HashMap();
            customParam.put("formid", formId);
            customParam.put("tips", content);
            formModel.setCustomParam(customParam);
            FormShowParameter formShowParameter = formModel.getFormShowParameter();
            view.showForm(formShowParameter);
            updateFirstLoginFlag(userId, formId);
    }

//    private String checkLoan (String normalEntityId, IFormView view) {
//        if (ErEntityTypeUtils.isTripReqBill(normalEntityId)) {
//            boolean isloan = view.getModel().getDataEntity().getBoolean("isloan");
//            if (isloan) {
//                normalEntityId = normalEntityId + "_loan";
//            }
//        }
//        return normalEntityId;
//    }
}

    private void updateFirstLoginFlag(Long userId, String formId) {
        QFilter userFilter = new QFilter("userid", "=", userId);
        if (formId.contains("_mob")) {
            formId = formId.replace("_mob", "");
        }

        QFilter formIdFilter = new QFilter("pageid", "=", formId);
        QFilter[] filters = new QFilter[]{userFilter, formIdFilter};
        DynamicObject useInfo = BusinessDataServiceHelper.loadSingle("bos_devp_firstlogin", "isfirstlogin", filters);
        if (useInfo == null) {
            useInfo = BusinessDataServiceHelper.newDynamicObject("bos_devp_firstlogin");
            String id = Uuid8.generateShortUuid();
            useInfo.set("id", id);
            useInfo.set("userid", userId);
            useInfo.set("pageid", formId);
        }

        useInfo.set("isfirstlogin", "1");
        SaveServiceHelper.save(new DynamicObject[]{useInfo});
    }

    private boolean isFirstLogin(Long userId, String formId) {
        QFilter userFilter = new QFilter("userid", "=", userId);
        if (formId.contains("_mob")) {
            formId = formId.replace("_mob", "");
        }

        QFilter formIdFilter = new QFilter("pageid", "=", formId);
        QFilter[] filters = new QFilter[]{userFilter, formIdFilter};
        DynamicObject useInfo = QueryServiceHelper.queryOne("bos_devp_firstlogin", "isfirstlogin", filters);
        return useInfo == null || !useInfo.getBoolean("isfirstlogin");
    }

    private String checkLoan(String normalEntityId, IFormView view) {
                if (ErEntityTypeUtils.isTripReqBill(normalEntityId)) {
            boolean isloan = view.getModel().getDataEntity().getBoolean("isloan");
            if (isloan) {
                normalEntityId = normalEntityId + "_loan";
            }
        }
        return normalEntityId;
    }
    }