package kd.cosmic;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.BillList;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.epm.eb.common.ebcommon.common.util.QFBuilder;
import kd.fi.bcm.common.FormConstant;
import kd.fi.bcm.spread.domain.view.JsonSerializerUtil;
import kd.fi.bcm.spread.domain.view.Sheet;
import kd.fi.bcm.spread.domain.view.SpreadManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;

import static kd.epm.eb.common.utils.base.KdUtils.billlistap;
/**
 * 描述: 同步现金流量表
 * 开发者: 李四辉
 * 创建日期:2024-08-01
 * 关键客户：股东方
 * 已部署正式：false
 * 备注：
 */

public class tbxjll extends AbstractFormPlugin {
    private final static String KEY_MAINBAR = "toolbarap";
    private final static String KEY_BARITEM_NEW = "ezob_tbxjllb";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 侦听主菜单按钮点击事件
        this.addItemClickListeners(KEY_MAINBAR);
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        if (StringUtils.equals(KEY_BARITEM_NEW, evt.getItemKey())){
            // TODO 在此添加业务逻辑
            BillList billList = this.getView().getControl(billlistap);
            ListSelectedRowCollection selectedRows= billList.getSelectedRows();
            if(selectedRows.size() == 1){
                for (ListSelectedRow list : selectedRows) {
                    DynamicObject finE = BusinessDataServiceHelper.loadSingle(list.getPrimaryKeyValue(), "bcm_reportlistentity");
                    //组织
                    String entity = "";
                    if(finE.get("entity") != null){
                        entity = finE.getString("entity.id");
                    }
                    //财年
                    String yearid = "";
                    DynamicObject year = (DynamicObject)this.getModel().getValue("year");
                    if (year != null) {
                        yearid = year.get("id").toString();
                    }
                    //期间
                    String periodid = "";
                    DynamicObject period = (DynamicObject)this.getModel().getValue("period");
                    if (period != null) {
                        periodid = period.get("id").toString();
                    }
                    //情景
                    String scenarioid = "";
                    DynamicObject scenario = (DynamicObject)this.getModel().getValue("scenario");
                    if (scenario != null) {
                        scenarioid = scenario.get("id").toString();
                    }
                    //币别
                    String currencyid = "";
                    DynamicObject currency = (DynamicObject)this.getModel().getValue("currency");
                    if (currency != null) {
                        currencyid = currency.get("id").toString();
                    }
                    //体系
                    String modelid = "";
                    DynamicObject model = (DynamicObject)this.getModel().getValue("model");
                    if (model != null) {
                        modelid = model.get("id").toString();
                    }
                    //模板编码
                    String templatenumber = "";
                    if(finE.get("template") != null){
                        templatenumber = finE.getString("template.number");
                    }
                    QFBuilder qf = new QFBuilder();
                    //qf.add ("entity","=",1981182481147126790L);//组织id
                    qf.add ("entity.number","=",finE.getString("entity.number"));//组织编码
                    qf.add ("fyear","=",yearid);//年id
                    qf.add ("period","=",periodid);//期间id
                    qf.add ("scene","=",scenarioid);//情景id
                    qf.add ("currency","=",currencyid);//币别id
                    qf.add ("template.number","=",templatenumber);//模板编码
                    qf.add ("model","=",modelid);//体系id
                    DynamicObject  report = QueryServiceHelper.queryOne(FormConstant.FORM_RPTRECORD_ENTITY, "data",qf.toArray());
                    if(report != null && report.getString("data") != null){
                        SpreadManager sm = JsonSerializerUtil.toSpreadManager(report.getString("data"));
                        Sheet sheet = sm.getBook().getSheet(0);//报表只支持单个页签，直接取第一个
                        //修改日期格式
                        int nf = Integer.parseInt(year.get("number").toString().substring(year.get("number").toString().length() - 4));//年份
                        int yf = Integer.parseInt(period.get("number").toString().substring(period.get("number").toString().length() - 2));//月份
                        String qj = String.valueOf(nf) + String.format("%02d", yf);//同步数据库期间
                if(sheet.getTable().size()>0){
                    Date currentDateTime = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    //插入数据库
                    String url = "jdbc:mysql://10.22.237.72:3306/kd_tran";
                    String user = "cosmic";
                    String password = "2024@Cosmic";
                    String insertSQL = "INSERT INTO t_gl_cslreportpro (FZZDM,FZZMC,FCNQJ,FBB,FXJLLXM,FBYJE,FBNLJJE,FHJXJ,FUser,FInsertTime,FSyncQty,FSyncTime) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
                    String deleteSQL = "delete from t_gl_cslreportpro where FZZDM = ? and FCNQJ = ?";
                    try (Connection conn = DriverManager.getConnection(url, user, password);
                         PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                        // 设置要删除的条件的值
                        pstmt.setString(1, finE.getString("entity.number"));
                        pstmt.setInt(2, Integer.parseInt(qj));
                        // 执行删除操作
                        int affectedRows = pstmt.executeUpdate();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    try (
                        Connection conn = DriverManager.getConnection(url, user, password);
                        PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                        // 开启批处理模式
                        conn.setAutoCommit(false);
                        for(int i = 0; i < sheet.getTable().size(); i++){
                            if(i>3){
                                // 设置参数
                                pstmt.setString(1, finE.getString("entity.number"));
                                pstmt.setString(2, finE.getString("entity.name"));
                                pstmt.setInt(3, Integer.parseInt(qj));
                                pstmt.setString(4, currency.get("name").toString());
                                if(sheet.getCell(i,0).getValue() != null){
                                    pstmt.setString(5, sheet.getCell(i,0).getValue().toString());
                                }else{
                                    pstmt.setString(5, "");
                                }
                                if(sheet.getCell(i,1).getValue() != null){
                                    pstmt.setDouble(6, Double.parseDouble(sheet.getCell(i,1).getValue().toString()));
                                }else{
                                    pstmt.setDouble(6, 0);
                                }
                                if(sheet.getCell(i,2).getValue() != null){
                                    pstmt.setDouble(7, Double.parseDouble(sheet.getCell(i,2).getValue().toString()));
                                }else{
                                    pstmt.setDouble(7, 0);
                                }
                                if(i==11||i==19||i==30||i==37||i==48||i==55){
                                    pstmt.setString(8, "小计");
                                }else if(i==21||i==39||i==57){
                                    pstmt.setString(8, "合计");
                                }
                                else{
                                    pstmt.setString(8, "");
                                }
                                pstmt.setString(9, "测试");
                                pstmt.setString(10, sdf.format(currentDateTime).toString());
                                pstmt.setInt(11, 0);
                                pstmt.setString(12, sdf.format(currentDateTime).toString());
                                pstmt.addBatch();
                            }
                        }
                        // 执行批处理
                        pstmt.executeBatch();
                        // 提交事务
                        conn.commit();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                this.getView().showMessage("同步成功！！");
                //this.getView().showMessage(sheet.getCell(5,1).getValue().toString());
                    }else{
                        this.getView().showMessage("未查询到数据，请检查选中内容！！");
                    }
                }
            }else{
                this.getView().showMessage("选择需要同步的报表，一次只能选一行！！");
            }
        }
    }
}
