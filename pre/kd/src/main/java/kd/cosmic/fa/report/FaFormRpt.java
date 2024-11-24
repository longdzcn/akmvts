//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package kd.cosmic.fa.report;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.entity.validate.BillStatus;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.report.events.SortAndFilterEvent;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 描述: 资产清单二开带出字段 , 插件放在报表表单里头
 * 开发者: 易佳伟
 * 创建日期:二期
 * 关键客户：黄淑玲,张薇
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class FaFormRpt extends AbstractReportFormPlugin {
    public FaFormRpt() {
    }


    public static String cerxmsm="";
    public static String cerxmdm="";

    public static String fa_fphm="";

    public static String zjfyNumber = "";

    public void processRowData(String gridPK, DynamicObjectCollection rowData, ReportQueryParam queryParam) {
        String  queryDate =  queryParam.getFilter().getDynamicObject("q_period").getString("id");

        Iterator var4 = rowData.iterator();
        String pk = "";
        try {

            while (var4.hasNext()) {
                DynamicObject row = (DynamicObject) var4.next();
                pk = row.getString("number");
                if(pk.equals("0")||pk.equals("合计"))
                {
                    continue;
                }
                QFilter qFilter = new QFilter("number", "=", pk);
                DynamicObject finCardDynamic = BusinessDataServiceHelper.loadSingle("fa_card_fin", "id,depreuse,realcard,originalval,ezob_textfield4,number,ezob_sbwxbm,vounchernumber,currency,depre_entry,org,assetcat,finaccountdate,preusingamount,monthdepre,addidepreamount,realcard", new QFilter[]{qFilter})  ;
                DynamicObject realCardDynamic = BusinessDataServiceHelper.loadSingle("fa_card_real", "id,realcard,originalval,ezob_textfield4,number,ezob_kyxm,remark,costcentrer,ezob_hth", new QFilter[]{qFilter})  ;


                //DynamicObjectCollection entrys = dynamicObject.getDynamicObjectCollection("detailentry");
                //财务卡片原值
                BigDecimal yz = BigDecimal.valueOf(0);

                if(!finCardDynamic.getBigDecimal("originalval").equals(BigDecimal.valueOf(0)))
                {
                     yz = finCardDynamic.getBigDecimal("originalval");
                }
                //财务卡片设备维修编码
                String sbwxbh =  finCardDynamic.getString("ezob_textfield4");

                //财务卡片合同号
                String hth ="";
                if(realCardDynamic.getDynamicObject("ezob_hth")!=null)
                {
                    hth = realCardDynamic.getDynamicObject("ezob_hth").getString("number");
                }
                //财务卡片入账日期
                Date rzDate = finCardDynamic.getDate("finaccountdate");

                //预计使用期间
                int qj = finCardDynamic.getInt("preusingamount");



                String realCardId = realCardDynamic.getString("id");  //实物卡片ID

                //实物卡片表头成本中心 costcentre
                String costcentreName ="";
                String costcentreNumber ="";


                if(realCardDynamic.getDynamicObject("costcentrer")!=null) {
                    costcentreName = realCardDynamic.getDynamicObject("costcentrer").getString("name");
                    costcentreNumber = realCardDynamic.getDynamicObject("costcentrer").getString("number");

                }else if(realCardDynamic.getDynamicObject("ezob_basedatafield1")!=null){
                    costcentreName = realCardDynamic.getDynamicObject("ezob_basedatafield1").getString("name");
                    costcentreNumber = realCardDynamic.getDynamicObject("ezob_basedatafield1").getString("number");
                }



                //折旧费用科目     规则：组织代码+标识符（YX/WX）
                zjfyNumber ="";
                String zjfyName = getzjfyNumber(finCardDynamic,costcentreNumber);








                Long depreuseId =   finCardDynamic.getDynamicObject("depreuse").getLong("id");                     //折旧用途ID






                /**
                 * 通过折旧途径id和实物卡片id和查询时间过滤的期间日期去查询财务卡片表（t_fa_card_fin）中的
                 * 期折旧额和本年累计旧额
                 */
                int zjnumber = 0;
                String selZJ = "/*dialect*/select  FMONTHDEPRE,FACCUMDEPRE from  akmmv_prd_fi_test.t_fa_card_fin WHERE FREALCARDID="+realCardId +" and FDEPREUSEID="+depreuseId+" and FBIZPERIODID="+queryDate+" and faddidepreamount <> "+zjnumber+" and fmonthdepre <> "+zjnumber;
                DataSet selDs3 = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, selZJ);
                String byzj="";
                String bnzj="";
                while (selDs3.hasNext()) {
                    Row roww2 = selDs3.next();
                    //财务卡片本月折旧（期折旧额）
                    byzj =  roww2.getString("FMONTHDEPRE");
                    //财务卡片本年折旧（本年累计折旧）
                    bnzj =  roww2.getString("FACCUMDEPRE");
                }


                //财务卡片ID
                String fcid =  finCardDynamic.getString("id");


                //原币币别
                String ybbz = finCardDynamic.getDynamicObject("currency").getString("name");


                //实物卡片编码
                String number = realCardDynamic.getString("number");

                //实物卡片备注
                String remark = realCardDynamic.getString("remark");

                //实物卡片科研项目
                String kyxm ="";
                if(realCardDynamic.getDynamicObject("ezob_kyxm")!=null)

                {
                   DynamicObject kyxmDynamic = realCardDynamic.getDynamicObject("ezob_kyxm");
                    kyxm = kyxmDynamic.getString("name")+"，"+kyxmDynamic.getString("number");

                }


                //核心单据号  ：根据相同资产编码的变更单，上查财务应付单中表头的【发票号】的编码（如果有多张财务应付单的话，则通过分隔符组合起来，比如发票号1/发票号2/发票号3……）
                String hxdjh = getCoreBillNo(number);


                //cer项目说明和CER项目代码:根据相同核心单据号找到采购订单，取表头的项目说明和项目代码
                String getchangehxdjh = getchangehxdjh(number,hth);


                //只有是外币才显示
                if(ybbz.equals("人民币"))
                {
                    yz=BigDecimal.valueOf(0);
                }

                //赋值
//                row.set("ezob_sbwxbm",sbwxbh);
//                row.set("ezob_hth",hth);
//                row.set("ezob_yz",yz);
                row.set("ezob_hxdjh",hxdjh);
//                row.set("ezob_remark",remark);
//                row.set("ezob_kyxm",kyxm);
                row.set("ezob_pzh",getpzhNumber(fcid));
                row.set("ezob_cerxmsm",cerxmsm);
                cerxmsm ="";
                row.set("ezob_cerxmdm",cerxmdm);
                cerxmdm="";
                row.set("ezob_fahxdjh",getchangehxdjh);
//                row.set("ezob_byzj",byzj);
//                row.set("ezob_bnzj",bnzj);
//                row.set("ezob_costcentreName",costcentreName);
//                row.set("ezob_costcentreNumber",costcentreNumber);
                row.set("ezob_fafphm",fa_fphm);
//                row.set("ezob_bfsj",getbfTime(realCardId,depreuseId));
                row.set("ezob_zjfykm",zjfyName);
//                row.set("ezob_ybbz",ybbz);
                row.set("ezob_fphm",getfphm(hxdjh));
                row.set("ezob_jzsj",getjzTime(realCardId,depreuseId));
//                row.set("ezob_zjdqr",getzjTime(rzDate,qj));
                row.set("ezob_zjfykmbm",zjfyNumber);






















            }

            }catch(Exception e)
            {
                StackTraceElement stackTrace=  e.getStackTrace()[0];
                this.getView().showMessage("异常发生在: " + stackTrace.getFileName() + ":" + stackTrace.getLineNumber() + " - " + stackTrace.getMethodName()+"  错误"+e.getMessage());
                System.out.println();

            }


        }

        public String getzjfyNumber(DynamicObject finCardDynamic,String costcentreNumber)
        {
            String zjfyName = "";
            String hszz = "";
            String assetcat= "";

            if(finCardDynamic.getDynamicObject("org")!=null)
            {
                hszz =  finCardDynamic.getDynamicObject("org").getString("number");
            }
            if(finCardDynamic.getDynamicObject("assetcat")!=null)
            {
                assetcat =  finCardDynamic.getDynamicObject("assetcat").getString("number");
                if(assetcat.equals("F06"))
                {
                    hszz=hszz+"-WX";
                }else{
                    hszz=hszz+"-YX";
                }
            }
            if(!hszz.equals(""))
            {

                QFilter qFilterSelKM = new QFilter("number", "=", hszz);

                DynamicObject aiaccountmaptype = BusinessDataServiceHelper.loadSingle("ai_accountmaptype", new QFilter[]{qFilterSelKM})  ;
                if(aiaccountmaptype!=null)
                {
                    DynamicObjectCollection srcaccttable =  aiaccountmaptype.getDynamicObjectCollection("entryentity");
                    for (DynamicObject dynamicObject:srcaccttable)
                    {
                        String input = dynamicObject.getString("bdinfoimport");
                        String[] parts = input.split(":", 2); // 使用限定符2来确保只分割一次
                        if (parts.length > 1) {
                            String afterColon = parts[1]; // 获取冒号后的字符串
                            if(costcentreNumber.equals(afterColon))
                            {
                                DynamicObject accfiledDynamic =dynamicObject.getDynamicObject("accfield");
                                zjfyName = accfiledDynamic.get(6).toString();
                                zjfyNumber    =accfiledDynamic.getString("number");
                            }
                        }
                    }
                }


            }
            return zjfyName;
        }


        //获取凭证号
        public String getpzhNumber(String fcid)
        {
            String number ="";
            String selsql = "/*dialect*/select fvoucherid from akmmv_prd_fi_test.t_ai_daptracker where Fsourcebillid=" + fcid;
            DataSet selDs = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, selsql);
            long fvoucherid = 0;

            while (selDs.hasNext()) {
                Row roww = selDs.next();
                fvoucherid = roww.getLong(0);
            }

            String selsql2 = "/*dialect*/select t1.fnumber as number,t2.fname as name from akmmv_prd_fi_test.t_gl_voucher  t1 inner join akmmv_prd_fi_test.t_bd_period t2  on t1.FPERIODID = t2.fid where t1.fid=" + fvoucherid;
            DataSet selDs2 = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, selsql2);
            String fnumber = "";
            String fname = "";
            while (selDs2.hasNext()) {
                Row roww2 = selDs2.next();
                fnumber = roww2.getString("number");
                fname = roww2.getString("name");

                if(fnumber!=""&&fname!="")
                {
                    number = fname+"#"+fnumber;
                }
            }
            return number;
        }

        //获取核心单据号
        public String getCoreBillNo(String number)
            {

                cerxmdm="";
                  cerxmsm="";
                //根据相同资产编码的实物卡片，上查采购转固单，再上查固定资产验收合格报告表体中的核心单据编号，如果有多张，则显示为核心单据号1/核心单据号2……
                //先拿到实物卡片的ID
                String selsql2 = "/*dialect*/select fid from  akmmv_prd_fi_test.T_FA_CARD_REAL where fnumber= ?"  ;
                Object[] params =new Object[1];
                params[0]= number;
                DataSet selDs2 = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, selsql2, new Object[]{params[0]});


                long cARDREALID = 0;
                while (selDs2.hasNext()) {
                    Row roww2 = selDs2.next();
                    cARDREALID = roww2.getLong(0);
                }
               /*

                */
                //再根据关联追踪表拿到采购转固单的FID
                String selsql3 = "/*dialect*/select FSBILLID from  akmmv_prd_fi_test.t_fa_card_real_tc WHERE FTBILLID="+cARDREALID ;
                DataSet selDs3 = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, selsql3);
                long fAPURCHASEBILLID = 0;

                    while (selDs3.hasNext()) {
                        Row roww2 = selDs3.next();
                        fAPURCHASEBILLID = roww2.getLong(0);
                    }
                    if(fAPURCHASEBILLID==0)
                    {
                        return "";
                    }



                //再根据另一张关联追踪表拿到上游验收单的ID
                String selsql4= "/*dialect*/select FSBILLID from  akmmv_prd_fi_test.t_fa_purchasebill_tc WHERE FTBILLID="+fAPURCHASEBILLID ;
                DataSet selDs4 = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, selsql4);
                long ezobgdzcyshgbg = 0;
                while (selDs4.hasNext()) {
                    Row roww2 = selDs4.next();
                    ezobgdzcyshgbg = roww2.getLong(0);
                }
                //查询验收报告的核心单据号
                String selsql5= "/*dialect*/select fbillno from  akmmv_prd_fi_test.tk_ezob_gdzcyshgbg WHERE FID="+ezobgdzcyshgbg ;
                DataSet selDs5 = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, selsql5);
                String coreId = "";
                while (selDs5.hasNext()) {
                    Row roww2 = selDs5.next();
                    coreId = roww2.getString(0);
                }

                QFilter qFilter = new QFilter("billno", "=",  coreId);
                DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("ezob_gdzcyshgbg", "entryentity,ezob_hxdh1", new QFilter[]{qFilter})  ;
                DynamicObjectCollection  dY = dynamicObject.getDynamicObjectCollection("entryentity");

                StringBuffer stringBuffer = new StringBuffer();
                StringBuffer sbXmdm = new StringBuffer();
                StringBuffer sbXmsm = new StringBuffer();

                for (DynamicObject dynamicObject1:dY)
                {

                    String  hxdh =  dynamicObject1.getString("ezob_hxdh1");
                    if (!hxdh.equals(""))
                    {
                        if(!stringBuffer.toString().contains(hxdh))
                        {
                            stringBuffer.append(hxdh+"/");
                        }
                        DynamicObject cGDDBill = null;
                        QFilter qFilter2 = new QFilter("billno", "=", hxdh);
                        cGDDBill = BusinessDataServiceHelper.loadSingle("pm_purorderbill", "ezob_textfield2,ezob_textfield1", new QFilter[]{qFilter2})  ;


                        if(cGDDBill!=null)
                        {

                            if(!cGDDBill.getString("ezob_textfield2").equals(""))
                            {
                                sbXmsm.append(cGDDBill.getString("ezob_textfield2")+"/") ;

                            }
                            if(!cGDDBill.getString("ezob_textfield1").equals(""))
                            {
                                sbXmsm.append(cGDDBill.getString("ezob_textfield1")+"/") ;

                            }

                        }

                    }

                }

                cerxmsm = clear(sbXmsm.toString());
                cerxmdm = clear(sbXmdm.toString());


                return clear(stringBuffer.toString());
            }


        //获取当前时间
        public static  String  getTime(Date dateTimeString)
        {
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
            String date =simpleDateFormat.format(dateTimeString);
            return date;
        }


        //去除最后一行 /
        public static String clear(String zifu)
        {
            int lastIndex = zifu.lastIndexOf("/");
            if (lastIndex != -1) {

                String output = zifu.substring(0, lastIndex);
                return output;
            }

            else {
                return zifu;
            }
        }

        //获取升级改造后核心订单号
        public String getchangehxdjh(String number,String hth) {



            QFilter qFilter = new QFilter("fieldentry.assetnumber", "=", number);
            DynamicObject[] dynamicObject = BusinessDataServiceHelper.load("fa_change_dept", "main_changebillentry,realcard1,fieldentry,fieldentry.assetnumber,billno", new QFilter[]{qFilter});
            String corebillno = "";
            for (int y = 0; y < dynamicObject.length; y++) {
                if (dynamicObject[y] != null) {
                    String id = dynamicObject[y].getString("id");
                        String selsql3 = "/*dialect*/select FSBILLID from  akmmv_prd_fi_test.t_fa_changebill_tc WHERE FTBILLID=" + id;
                    DataSet selDs3 = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, selsql3);

                    List<Long> fAPURCHASEBILLID = new ArrayList<Long>();


                    while (selDs3.hasNext()) {
                        Row roww2 = selDs3.next();
                        String contains = roww2.getString("FSBILLID");
                                if(!fAPURCHASEBILLID.toString().contains(contains))
                                {
                                    fAPURCHASEBILLID.add(roww2.getLong("FSBILLID"));
                                }
                    }

                    StringBuffer stringBuffer = new StringBuffer();
                    StringBuffer stringBuffer2 = new StringBuffer();


                    QFilter qFilter2 = new QFilter("id", "in", fAPURCHASEBILLID);
                    DynamicObject[] cWYFBill = BusinessDataServiceHelper.load("ap_finapbill", "detailentry,corebillno,ezob_fphm", new QFilter[]{qFilter2});
                    Map map = new HashMap<>();
                    for (int i = 0; i < cWYFBill.length; i++) {
                        fa_fphm = cWYFBill[i].getString("ezob_fphm");
                        stringBuffer2.append(fa_fphm + "/");
                        DynamicObjectCollection dynamicObjectCollection = cWYFBill[i].getDynamicObjectCollection("detailentry");
                        for (DynamicObject dynamicObject1 : dynamicObjectCollection) {

                            corebillno = dynamicObject1.getString("corebillno");
                            if (!stringBuffer.toString().contains(corebillno) && !corebillno.equals("")) {
                                stringBuffer.append(corebillno + "/");

                            }
                        }

                    }

                    corebillno = clear(stringBuffer.toString());
                    fa_fphm = clear(stringBuffer2.toString());



                } else {
                    return "";
                }
            }
                return corebillno ;
        }



        //获取发票号码 根据相同核心单据号的财务应付单中表头的【发票号】的编码（如果有多张财务应付单的话，则通过分隔符组合起来，比如发票号1/发票号2/发票号3……）
        public String getfphm(String number)
        {


            QFilter qFilter2 = new QFilter("detailentry.corebillno", "=", number);

            StringBuffer stringBuffer = new StringBuffer();
            String fphm="";

            DynamicObject [] cWYFBill = BusinessDataServiceHelper.load("ap_finapbill", "detailentry,corebillno,ezob_fphm", new QFilter[]{qFilter2})  ;
            for (int i=0 ;i<cWYFBill.length;i++)
            {
                if(cWYFBill[i].getString("ezob_fphm")!=null)
                {
                    stringBuffer.append(cWYFBill[i].getString("ezob_fphm")+"/");
                }
            }
            fphm = clear(stringBuffer.toString());

            return fphm;
        }


        //获取清理明细报废时间
        public String getbfTime(String realCardId,Long depreuseId)
        {

            String selZJ = "/*dialect*/select  fbizdate from  akmmv_prd_fi_test.T_FA_CLRBILLENTRY_D WHERE FREALCARDID="+realCardId +" and FDEPREUSEID="+depreuseId ;
            DataSet selDs3 = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, selZJ);
            String date="";
            StringBuffer sb = new StringBuffer();
            while (selDs3.hasNext()) {
                Row roww2 = selDs3.next();
                //财务卡片本月折旧（期折旧额）
                date = parseDate2(roww2.getString("fbizdate"));
                //财务卡片本年折旧（本年累计折旧）
                if(!date.equals(""))
                {
                    sb.append(date+"/");
                }
            }
            return clear(sb.toString());
        }
        //减值时间
        public String getjzTime(String realCardId,Long depreuseId)
        {
            StringBuffer stringBuffer = new StringBuffer();

            QFilter[] cardFilters = new QFilter[] { new QFilter("changebillentry.realcard", QCP.in,realCardId),
                    new QFilter("billstatus", QCP.equals, BillStatus.C.name()),
                    new QFilter("depreuse", QCP.equals, depreuseId) };
            DynamicObject [] assetDynamicObject = BusinessDataServiceHelper.load("fa_asset_devalue", "changebillentry,changebillentry.bizdate,depreuse", cardFilters)  ;
            for (int i=0;i<assetDynamicObject.length;i++)
            {
                if(!assetDynamicObject[i].getString("bizdate").equals(""))
                {
                    stringBuffer.append(parseDate(assetDynamicObject[i].getString("bizdate")));

                }
            }

            return clear(stringBuffer.toString());
        }

        public Date getzjTime(Date date, int number) throws ParseException {
            //创建Calendar实例
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);   //设置当前时间
            cal.add(Calendar.MONTH, number);  //在当前时间基础上加一个月

            //将时间格式化成yyyy-MM-dd的格式
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String zjDate =  format.format(cal.getTime());
            Date   date1 = format.parse(zjDate);
            return date1;
        }


        public String parseDate(String date)
        {


            // 使用LocalDateTime解析长日期时间字符串
            LocalDateTime dateTime = LocalDateTime.parse(date);

            // 使用DateTimeFormatter转换为短日期格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // 转换并输出
            String shortDateString = dateTime.format(formatter);

            return shortDateString;


        }

    public String parseDate2(String date)
    {


        // 使用LocalDateTime解析长日期时间字符串
        LocalDateTime dateTime = LocalDateTime.parse(date);

        // 使用DateTimeFormatter转换为短日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 转换并输出
        String shortDateString = dateTime.format(formatter);

        return shortDateString;


    }



    @Override

    public void setSortAndFilter(List<SortAndFilterEvent> allColumns) {

        super.setSortAndFilter(allColumns);

        for(SortAndFilterEvent event : allColumns){

            event.setSort(true);

            event.setFilter(true);

        }

    }




}









