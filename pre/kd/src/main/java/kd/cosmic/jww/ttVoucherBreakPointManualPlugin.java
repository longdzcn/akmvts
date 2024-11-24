//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package kd.cosmic.jww;

import com.alibaba.fastjson.JSONArray;
import kd.bos.coderule.api.CodeRuleInfo;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.FormShowParameter;
import kd.bos.form.control.events.ClickListener;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QFilter;
import kd.bos.util.StringUtils;
import kd.fi.gl.closeperiod.breakpoint.BreakPointCommonUtil;
import kd.fi.gl.util.GLBreakPointUtil;

import java.util.*;


/**
 * 描述: 在人工调整界面中接收自定义参数，按照这个参数重新排序凭证
 * 开发者: 江伟维
 * 创建日期:
 * 关键客户：
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class ttVoucherBreakPointManualPlugin extends AbstractFormPlugin implements ClickListener {
    public ttVoucherBreakPointManualPlugin() {
    }

    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        this.updateView();
    }

    private void updateView() {
        IDataModel model = this.getModel();
        model.deleteEntryData("entryentity");
        FormShowParameter parame = this.getView().getFormShowParameter();
        Map<String, Object> map = parame.getCustomParams();
        if (map != null && map.size() != 0) {
            String orgCol = String.valueOf(map.get("orgCol"));
            String periodCol = String.valueOf(map.get("periodCol"));
            if (StringUtils.isNotEmpty(orgCol)) {
                String[] orgs = orgCol.split(",");
                String[] periods = periodCol.split(",");

                for(int i = 0; i < orgs.length; ++i) {
                    Map<String, Object> m = new HashMap();
                    m.put("orgId", orgs[i]);
                    m.put("booktype", map.get("booktype"));
                    m.put("period", periods[i]);
                    CodeRuleInfo coderule = BreakPointCommonUtil.getCodeRuleInfo(orgs[i]);
                    m.put("condition", !coderule.getConditionEntry().isEmpty());
                    DynamicObjectCollection ruleEntry = GLBreakPointUtil.getCodeRuleEntry(coderule);
                    List<HashMap<String, String>> codeRuleEntryAttributes = GLBreakPointUtil.getAttributeFromCodeRuleEntry(ruleEntry);
                    DynamicObject[] voucherList = new DynamicObject[0];
                    JSONArray jarrayVoucherIds = (JSONArray)map.get("voucherids");
                    if (jarrayVoucherIds != null) {
                        Object[] voucherIds = jarrayVoucherIds.toArray();
                        if (voucherIds != null && voucherIds.length != 0) {
                            QFilter qf = new QFilter("id", "in", voucherIds);
                            voucherList = GLBreakPointUtil.queryVoucher(m, codeRuleEntryAttributes, qf);
                        }
                    }

                    if (voucherList.length != 0) {
                        List<List<DynamicObject>> issortitemvouchers = GLBreakPointUtil.getIssortitemvouchers(codeRuleEntryAttributes, voucherList);
                        model.beginInit();
                        Iterator var22 = issortitemvouchers.iterator();

                        while(var22.hasNext()) {
                            List<DynamicObject> vos = (List)var22.next();

                            //【ZOC】重新按正确的顺序排序
                            Object[] vosIds = jarrayVoucherIds.toArray();
                            List<DynamicObject> vos2=new ArrayList<DynamicObject>();
                            for(int v = 0; v < vosIds.length; ++v) {
                                for (int j = 0; j < vos.size(); ++j) {
                                    DynamicObject vo = (DynamicObject) vos.get(j);
                                    if (((Long) vo.get("id")).equals((Long) vosIds[v])) {
                                        vos2.add(vo);
                                    }
                                }
                            }

                            for(int j = 0; j < vos2.size(); ++j) {
                                DynamicObject vo = (DynamicObject)vos2.get(j);
                                int rowKey = model.createNewEntryRow("entryentity");
                                model.setValue("org", vo.get("org"), rowKey);
                                model.setValue("vouchertype", vo.get("vouchertype"), rowKey);
                                model.setValue("voucherno", vo.get("billno"), rowKey);
                                model.setValue("newvoucherno", "", rowKey);
                                model.setValue("voucherid", vo.get("id"), rowKey);
                                model.setValue("bizdate", vo.get("bizdate"), rowKey);
                                model.setValue("bookeddate", vo.get("bookeddate"), rowKey);
                            }
                        }

                        model.endInit();
                    }
                }
            }
        }

        this.getView().updateView("entryentity");
    }
}
