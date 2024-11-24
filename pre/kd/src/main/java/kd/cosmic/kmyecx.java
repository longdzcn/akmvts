package kd.cosmic;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.epm.eb.ebBusiness.model.BalanceQueryParam;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 描述: 股东方科目余额查询同步中间库
 * 开发者: 李四辉
 * 创建日期:2024-08-01
 * 关键客户：股东方
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */

public class kmyecx extends AbstractOperationServicePlugIn {
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        String bbbm = "";
        String bbmc = "";
        Long billIds = Long.valueOf(String.valueOf(e.getDataEntities()[0].getPkValue()));
        DynamicObject aTBZJKBILL = BusinessDataServiceHelper.loadSingle(billIds,"ezob_tbzjkrq",
                "ezob_tbqj,ezob_tbzz");
        BalanceQueryParam param = new BalanceQueryParam();
        param.setOrgId(Long.parseLong(aTBZJKBILL.get("ezob_tbzz.id").toString()));//LE0006组织id
        if(aTBZJKBILL.get("ezob_tbzz.number").toString().equals("LE0012")){
            param.setBookTypeId(1725278244103544832L);//账簿T账id
        }else{
            param.setBookTypeId(237528347981256704L);//账簿Local账id
        }
        if(aTBZJKBILL.get("ezob_tbzz.number").toString().equals("LE0089")){
            //不传币别字段查询的就是综合本位币
            //param.setCurrencyId(22);//泰铢
            bbbm = "THB";
            bbmc = "泰铢";
        }else if(aTBZJKBILL.get("ezob_tbzz.number").toString().equals("LE0018")){
            //param.setCurrencyId(12);//韩元
            bbbm = "KRW";
            bbmc = "韩元";
        }else if(aTBZJKBILL.get("ezob_tbzz.number").toString().equals("LE0017")){
            //param.setCurrencyId(13);//印度卢比
            bbbm = "INR";
            bbmc = "印度卢比";
        }else if(aTBZJKBILL.get("ezob_tbzz.number").toString().equals("LE0016")){
            //param.setCurrencyId(2);//港币
            bbbm = "HKD";
            bbmc = "港币";
        }else if(aTBZJKBILL.get("ezob_tbzz.number").toString().equals("LE0002")){
            //param.setCurrencyId(2);//港币
            bbbm = "HKD";
            bbmc = "港币";
        }else{
            //param.setCurrencyId(1);//人民币
            bbbm = "CNY";
            bbmc = "人民币";
        }

        param.setAccountTableId(1725281024792880128L);//科目表
        param.setPeriodId(Integer.parseInt(aTBZJKBILL.get("ezob_tbqj.id").toString()));//期间120240030

//设置查询字段
//        param.addSelector("org.number");//组织编码
//        param.addSelector("org.name");//组织名称
//        param.addSelector("period.number");//期间编码
//        param.addSelector("booktype.number");//账簿类型编码
//        param.addSelector("booktype.name");//账簿类型名称
//        param.addSelector("assgrp");//核算维度编码
        param.addSelector("yeardebitlocal");//年初借方金额
        param.addSelector("yearcreditlocal");//年初贷方金额
        param.addSelector("beginlocal");//期初余额
        param.addSelector("beginlocal");//期初余额
        param.addSelector("debitlocal");//本期发生额（借方金额）
        param.addSelector("creditlocal");//本期发生额（贷方金额）
        param.addSelector("yeardebitlocal");//本年累计（借方金额）
        param.addSelector("yearcreditlocal");//本年累计（贷方金额）
        param.addSelector("endlocal");//期末余额（借方金额）
        param.addSelector("endlocal");//期末余额（贷方金额）
        param.setSpecialAccount(false);//是查指定科目，否查上下级科目
        param.setSubstractPL(true);//科目余额接口传这个参数会扣减损益
        param.addGroupBy("account");//根据科目汇总
        param.addGroupBy("assgrp");//根据核算维度汇总
        param.addGroupBy("org.number");//根据组织编码汇总
        param.addGroupBy("org.name");//根据组织名称汇总
        //param.addGroupBy("period.number");//根据期间编码汇总
        param.addGroupBy("booktype.number");//根据账簿类型编码汇总
        param.addGroupBy("booktype.name");//根据账簿类型名称汇总
//科目1121ID--1761662154211079168L
        //param.setAccountAssgrp(1769773638933823488L, null);
        //param.setAccountAssgrp(1761662032274273280L, null);//1002科目

        String result = DispatchServiceHelper.invokeBizService("fi", "gl", "BalanceService", "getBalance", param.toString());
        JSONArray jsonArray = new JSONArray(result);
        jsonArray.length();
        if(jsonArray.length()>0){
            int year = Integer.parseInt(aTBZJKBILL.get("ezob_tbqj.number").toString().substring(0, 4));//年份
            int month = Integer.parseInt(aTBZJKBILL.get("ezob_tbqj.number").toString().substring(aTBZJKBILL.get("ezob_tbqj.number").toString().length() - 2));//月份
            if(month != 12){
                month = month+1;
            }else{
                month = 1;
                year = year+1;
            }
            String qj = String.valueOf(year) + String.format("%02d", month);//同步数据库期间
            Date currentDateTime = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //插入数据库
//            String url = "jdbc:mysql://10.22.237.72:3306/kd_tran";
//            String user = "cosmic";
//            String password = "2024@Cosmic";
            String url = "jdbc:mysql://10.26.238.123:3306/kd_tran";
            String user = "kd_tran";
            String password = "kd2024transfer";
            String insertSQL = "INSERT INTO t_gl_balancerept (FHSZZBM,FHSZZMC,FQJ,FZBLXBM,FZBLXMC,FZHBWBBM,FZHBWBMC,FKMBM,FKMMC,FKHDM,FKHMC,FGYSDM,FGYSMC,FZL,FJRJGDM,FJRJGMC,FXMDM,FXMMC,FNCYEJFJE,FNCYEDFJE,FNCYEHBJE,FQCYEJFJE,FQCYEDFJE,FBQFSEJFJE,FBQFSEDFJE,FBQFSEHBJE,FBNLJJFJE,FBNLJDFJE,FBNLJHBJE,FQMYEJFJE,FQMYEDFJE,FQMYEHBJE,FUser,FInsertTime,FSyncQty,FSyncTime) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//            String deleteSQL = "delete from t_gl_balancerept where FHSZZBM = ? and FQJ = ?";
            String deleteSQL = "delete from t_gl_balancerept where FHSZZBM = ? and FQJ = ?";
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                // 设置要删除的条件的值
                pstmt.setString(1, aTBZJKBILL.get("ezob_tbzz.number").toString());
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
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    // 设置参数
                    pstmt.setString(1, jsonObject.get("org.number").toString());
                    pstmt.setString(2, jsonObject.get("org.name").toString());
                    pstmt.setInt(3, Integer.parseInt(qj));
                    pstmt.setString(4, jsonObject.get("booktype.number").toString());
                    pstmt.setString(5, jsonObject.get("booktype.name").toString());
                    pstmt.setString(6, bbbm);
                    pstmt.setString(7, bbmc);
                    //科目
                    DynamicObject aKMBILL = BusinessDataServiceHelper.loadSingle(jsonObject.get("account").toString(),"bd_accountview",
                            "number,name,dc");
                    pstmt.setString(8, aKMBILL.get("number").toString());
                    pstmt.setString(9, aKMBILL.get("name").toString());
                    //核算维度
                    if(jsonObject.get("assgrp")!=null && !jsonObject.get("assgrp").toString().equals("0")){
                        DynamicObject aHSWDBILL = BusinessDataServiceHelper.loadSingle(jsonObject.get("assgrp").toString(),"gl_assist",
                                "assvals");
                        String hswdresult = aHSWDBILL.get("assvals").toString();
                        if(hswdresult.length()>0){
                            JSONObject hswdjsonObject = new JSONObject(hswdresult);
                            //客户
                            if(hswdresult.contains("f0001")){
                                DynamicObject aKHBILL = BusinessDataServiceHelper.loadSingle(hswdjsonObject.get("f0001").toString(),"bd_customer",
                                        "number,name");
                                pstmt.setString(10, aKHBILL.get("number").toString());
                                pstmt.setString(11, aKHBILL.get("name").toString());
                            }else{
                                pstmt.setString(10, "");
                                pstmt.setString(11, "");
                            }
                            //供应商
                            if(hswdresult.contains("f000005")){
                                DynamicObject aGYSBILL = BusinessDataServiceHelper.loadSingle(hswdjsonObject.get("f000005").toString(),"bd_supplier",
                                        "number,name");
                                pstmt.setString(12, aGYSBILL.get("number").toString());
                                pstmt.setString(13, aGYSBILL.get("name").toString());
                            }else{
                                pstmt.setString(12, "");
                                pstmt.setString(13, "");
                            }
                            //金融机构
                            if(hswdresult.contains("f000002")){
                                DynamicObject aJRJGBILL = BusinessDataServiceHelper.loadSingle(hswdjsonObject.get("f000002").toString(),"bd_bebank",
                                        "number,name");
                                pstmt.setString(15, aJRJGBILL.get("number").toString());
                                pstmt.setString(16, aJRJGBILL.get("name").toString());
                            }else{
                                pstmt.setString(15, "");
                                pstmt.setString(16, "");
                            }
                            //项目
                            if(hswdresult.contains("f000011")){
                                DynamicObject aXMBILL = BusinessDataServiceHelper.loadSingle(hswdjsonObject.get("f000011").toString(),"bd_project",
                                        "number,name");
                                pstmt.setString(17, aXMBILL.get("number").toString());
                                pstmt.setString(18, aXMBILL.get("name").toString());
                            }else{
                                pstmt.setString(17, "");
                                pstmt.setString(18, "");
                            }
                        }
                    }else{
                        pstmt.setString(10, "");
                        pstmt.setString(11, "");
                        pstmt.setString(12, "");
                        pstmt.setString(13, "");
                        pstmt.setString(15, "");
                        pstmt.setString(16, "");
                        pstmt.setString(17, "");
                        pstmt.setString(18, "");
                    }

//                    pstmt.setString(8, jsonObject.get("assgrp").toString());
//                    pstmt.setString(9, "核算维度（客户名称)");
//                    pstmt.setString(10, "核算维度（供应商代码)");
//                    pstmt.setString(11, "核算维度（供应商代码)");
                    pstmt.setInt(14, 0);//账龄
//                    pstmt.setString(13, "核算维度（金融机构代码)");
//                    pstmt.setString(14, "核算维度（金融机构名称)");
//                    pstmt.setString(15, "核算维度（项目代码)");
//                    pstmt.setString(16, "核算维度（项目名称)");
                    if(aKMBILL.get("dc").toString().equals("1")){
                        pstmt.setDouble(19, Double.parseDouble(jsonObject.get("endlocal").toString())-(Double.parseDouble(jsonObject.get("yeardebitlocal").toString())-Double.parseDouble(jsonObject.get("yearcreditlocal").toString())));
                        pstmt.setDouble(20, 0);
                        pstmt.setDouble(21, Double.parseDouble(jsonObject.get("endlocal").toString())-(Double.parseDouble(jsonObject.get("yeardebitlocal").toString())-Double.parseDouble(jsonObject.get("yearcreditlocal").toString())));
                    }else{
                        pstmt.setDouble(19, 0);
                        pstmt.setDouble(20, (Double.parseDouble(jsonObject.get("endlocal").toString())-(Double.parseDouble(jsonObject.get("yeardebitlocal").toString())-Double.parseDouble(jsonObject.get("yearcreditlocal").toString())))*-1);
                        pstmt.setDouble(21, Double.parseDouble(jsonObject.get("endlocal").toString())-(Double.parseDouble(jsonObject.get("yeardebitlocal").toString())-Double.parseDouble(jsonObject.get("yearcreditlocal").toString())));
                    }
                    pstmt.setDouble(22, Double.parseDouble(jsonObject.get("beginlocal").toString()));
                    pstmt.setDouble(23, Double.parseDouble(jsonObject.get("beginlocal").toString()));
                    pstmt.setDouble(24, Double.parseDouble(jsonObject.get("debitlocal").toString()));
                    pstmt.setDouble(25, Double.parseDouble(jsonObject.get("creditlocal").toString()));
                    if(aKMBILL.get("dc").toString().equals("1")){
                        pstmt.setDouble(26, Double.parseDouble(jsonObject.get("debitlocal").toString())-Double.parseDouble(jsonObject.get("creditlocal").toString()));
                    }else{
                        pstmt.setDouble(26, Double.parseDouble(jsonObject.get("creditlocal").toString())-Double.parseDouble(jsonObject.get("debitlocal").toString()));
                    }
                    pstmt.setDouble(27, Double.parseDouble(jsonObject.get("yeardebitlocal").toString()));
                    pstmt.setDouble(28, Double.parseDouble(jsonObject.get("yearcreditlocal").toString()));
                    if(aKMBILL.get("dc").toString().equals("1")){
                        pstmt.setDouble(29, Double.parseDouble(jsonObject.get("yeardebitlocal").toString())-Double.parseDouble(jsonObject.get("yearcreditlocal").toString()));
                    }else{
                        pstmt.setDouble(29, Double.parseDouble(jsonObject.get("yearcreditlocal").toString())-Double.parseDouble(jsonObject.get("yeardebitlocal").toString()));
                    }
                    if(aKMBILL.get("dc").toString().equals("1")){
                        pstmt.setDouble(30, Double.parseDouble(jsonObject.get("endlocal").toString()));
                        pstmt.setDouble(31, 0);
                    }else{
                        pstmt.setDouble(30, 0);
                        pstmt.setDouble(31, Double.parseDouble(jsonObject.get("endlocal").toString()));
                    }
                    pstmt.setDouble(32, Double.parseDouble(jsonObject.get("endlocal").toString()));
                    pstmt.setString(33, "WEBAPI");
                    pstmt.setString(34, sdf.format(currentDateTime).toString());
                    pstmt.setInt(35, 0);
                    pstmt.setString(36, sdf.format(currentDateTime).toString());
                    pstmt.addBatch();
                }
                // 执行批处理
                pstmt.executeBatch();
                // 提交事务
                conn.commit();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        }


    }
}
