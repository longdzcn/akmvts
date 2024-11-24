package kd.cosmic.fa;

import kd.bos.algo.DataSet;
import kd.bos.coderule.api.CodeRuleInfo;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.coderule.CodeRuleServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.sdk.plugin.Plugin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *   描述: 研发费用分摊插件,插件放在计提折旧列表。（ZOC：20241008：修正了每张卡片小数点差异的问题）
 *   开发者: 易佳伟
 *   创建日期: 2024/07/22
 *   关键客户：黄淑玲
 *   已部署正式：ture
 *   备注：已投入正式环境使用，无问题
 */
public class MakeCostBill extends AbstractFormPlugin implements Plugin {

    private final static String KEY_BARITEM = "ezob_pushinvoice";

    private static String yfxmtest ="";

    private static String zjlytest ="";

    private final static List<String> list = new ArrayList<>();



    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);

        this.addItemClickListeners("toolbarap");
    }
    //vatCode 明细dono,明细factory

    @Override
    public void itemClick(ItemClickEvent evt) {
        try {

        super.itemClick(evt);
        String key = evt.getItemKey();
        if (key.equals(KEY_BARITEM)){
            //获取单据体控件
            EntryGrid entryGrid = this.getControl("entryentity");
            //获取选中行，数组为行号，从0开始int[]
            int selectRows[] = entryGrid.getSelectRows();
            String org = "";
            String depreuse = "";
            String curperiod = "";
            //获取单据体数据集合
            DynamicObjectCollection entity=this.getModel().getEntryEntity("entryentity");
            if(selectRows!=null && selectRows.length>0){
                for(int selectRow :selectRows){
                    DynamicObject dynamicObject=entity.get(selectRow);  //获取选中行的单据体数据
                    org = dynamicObject.getDynamicObject("org").getString("id");
                    depreuse = dynamicObject.getDynamicObject("depreuse").getString("id");
                    curperiod = dynamicObject.getDynamicObject("curperiod").getString("id");
                }
            }

            QFilter[] qFilter = new QFilter[] { new QFilter("ezob_kjqj", QFilter.equals, curperiod),
                    new QFilter("createorg", QFilter.equals, org),
                    new QFilter("enable", QFilter.equals, "1")};

            //查询资产卡片研发费用比例表
            DynamicObject [] aCD = BusinessDataServiceHelper.load("ezob_zcpitem","number,ezob_entryentity,ezob_zckp,ezob_yffybl", qFilter);
            for (DynamicObject  dy:aCD) {
                //获取比例单的编号
                long ydbm = 0;
                ydbm = dy.getLong("id");

                //获取比例表明细
                DynamicObjectCollection dn = dy.getDynamicObjectCollection("ezob_entryentity");

                //循环费用比例表明细
                for (DynamicObject dt : dn) {
                    //资产卡片
                    String cardId = " ";
                    String realCard = " ";

                    if (dt.getDynamicObject("ezob_zckp") != null) {
                        cardId = dt.getDynamicObject("ezob_zckp").getString("id");
                        realCard = dt.getDynamicObject("ezob_zckp").getString("number");
                    }

                    //测试用的判断，正式必须删除此判断
                    //【20241008】注意下面的sql排除了折旧额为0的资产卡片了
                    //if (dt.getDynamicObject("ezob_zckp").getString("number").equals("L00011556")) {
                    String selsql = "/*dialect*/\n" +
                            "select sum(te.FSPLITAMOUNT) as 折旧总额,te.FREALCARDID as 实物卡片ID from akmmv_prd_fi_test.t_fa_depredetailentry te    \n" +
                            "left join akmmv_prd_fi_test.t_fa_depreuse t on te.FDEPREUSEID=t.fid   \n" +
                            "left join akmmv_prd_fi_test.t_bd_period tp on te.fperiodid=tp.fid \n" +
                            "where tp.fid=" + curperiod + " and te.FORGID=" + org + " and te.fdepreuseid=" + depreuse + " and te.FREALCARDID=" + cardId + "\n" +
                            "GROUP BY te.FREALCARDID";
                    DataSet aDs = DB.queryDataSet(this.getClass().getName(), DBRoute.basedata, selsql);
                    ORM orm = ORM.create();
                    DynamicObjectCollection rows = orm.toPlainDynamicObjectCollection(aDs);
                    BigDecimal zjAmount = BigDecimal.valueOf(0); //折旧总额
                    BigDecimal allzjAmount = BigDecimal.valueOf(0);//累计折旧

                    for (DynamicObject row : rows) {

                        QFilter[] aFilters = new QFilter[]{new QFilter("ezob_kjqj", QFilter.equals, curperiod),
                                new QFilter("createorg", QFilter.equals, org),
                                new QFilter("enable", QFilter.equals, "1")};
//                                new QFilter("ezob_yfxm.number", QCP.equals, "RDD022024000148")};  //测试

                        //查找研发项目比例分摊表
                        DynamicObject[] aTb = BusinessDataServiceHelper.load("ezob_yfxmblft", "createorg,number,ezob_kjqj,ezob_entryentity,ezob_yfxm,ezob_zjly,ezob_yfxmbl,ezob_gldh", aFilters);

                        //循环分摊表
                        for (int i = 0; i < aTb.length; i++) {
                            int js = 0;
                            DynamicObjectCollection dc = aTb[i].getDynamicObjectCollection("ezob_entryentity");//取明细集合
                            //循环取比重
                            double bz = 0;
                            HashMap<String, BigDecimal> map = new HashMap<>();

                            //循环分摊表明细取比重
                            for (DynamicObject dynamicBz : dc) {
                                if(dynamicBz.getDynamicObject("ezob_gldh").getLong("id")==ydbm) {
                                    bz += Double.parseDouble(dynamicBz.getString("ezob_yfxmbl"));
                                }
                            }
                            /**
                             *   一条分摊明细生成一张费用分摊表
                             */
                            //循环分摊表明细
                            for (DynamicObject dynamicObject : dc) {
                                if(dynamicObject.getDynamicObject("ezob_gldh").getLong("id")==ydbm) {
                                    ++js;

                                    //项目研发
                                    String xmyf = dynamicObject.getDynamicObject("ezob_yfxm").getString("id");
                                    yfxmtest = dynamicObject.getDynamicObject("ezob_yfxm").getString("id");
                                    //关联单号
                                    String gldj = dynamicObject.getDynamicObject("ezob_gldh").getString("number");

                                    //研发费用比重
                                    double yffybz = Double.parseDouble(dynamicObject.getString("ezob_yfxmbl"));

                                    double bl = yffybz / bz;

                                    //获取登录人的id
                                    long userId = UserServiceHelper.getCurrentUserId();

                                    //资金来源
                                    long ezobzjly = 0;
                                    if (dynamicObject.getDynamicObject("ezob_zjly") != null) {
                                        ezobzjly = dynamicObject.getDynamicObject("ezob_zjly").getLong("id");
                                    }

                                    //生成分摊表对象
                                    DynamicObject ezobyfxmft = BusinessDataServiceHelper.newDynamicObject("ezob_yfxmft");

                                    //获取生成单据对象的编码生成规则
                                    CodeRuleInfo codeRule = CodeRuleServiceHelper.getCodeRule(ezobyfxmft.getDataEntityType().getName(), ezobyfxmft, null);
                                    String no = CodeRuleServiceHelper.getNumber(codeRule, ezobyfxmft);

                                    ezobyfxmft.set("billno", no);
                                    ezobyfxmft.set("ezob_ydbm", ydbm);

                                    //获取单据体集合
                                    DynamicObjectCollection entrys = ezobyfxmft.getDynamicObjectCollection("ezob_entryentity");

                                    //折旧用途
                                    ezobyfxmft.set("ezob_zjyt", depreuse);

                                    ezobyfxmft.set("billstatus", "A");  //单据状态
                                    ezobyfxmft.set("creator", userId);  //创建人
                                    ezobyfxmft.set("ezob_cjsj", getCurrTime()); //创建时间
                                    ezobyfxmft.set("org", org); //组织

                                    double bl2 = Double.parseDouble(dt.getString("ezob_yffybl"));

                                    DynamicObject oderEntry = entrys.addNew();

                                    oderEntry.set("ezob_zckp", cardId);//资产卡片

                                    oderEntry.set("ezob_yfxm", xmyf);//研发项目

                                    oderEntry.set("ezob_zjly", ezobzjly);//资金来源

                                    String number = "";

                                    if (js == 1) {
                                        allzjAmount = parase(row.getBigDecimal("折旧总额").multiply(new BigDecimal(bl2)));
                                    }
                                    //如果是最后一行，就将最后的值赋予过去，免得出现小数位差异。
                                    if (dc.size() == js) {
                                        //有可能最后一行减出个负数出来。。。这种几率实在太低了，但是先不考虑了
                                        zjAmount = allzjAmount;
                                    } else {
                                        //总额*比例 = 累计折旧
                                        zjAmount = parase(parase(row.getBigDecimal("折旧总额")).multiply(new BigDecimal(bl))).multiply(new BigDecimal(bl2));
                                        allzjAmount = allzjAmount.subtract(zjAmount);  //allzjAmount=allzjAmount-zjAmount
                                    }

                                    oderEntry.set("ezob_ljzj", zjAmount);//累计折旧
                                    oderEntry.set("ezob_yffybl", bl);//研发费用比重
                                    //重新保存，注意要用saveOperate才会经过校验，用save就直接保存数据库了。
                                    OperationResult operationResult =
                                            SaveServiceHelper.saveOperate("ezob_yfxmft", new DynamicObject[]{ezobyfxmft},
                                                    OperateOption.create());
                                    //返回
                                    if(!operationResult.getSuccessPkIds().isEmpty())
                                    {
                                        //执行成功
                                    }
                                    else
                                    {
                                        //执行失败
                                    }
                                    //SaveServiceHelper.save(new DynamicObject[]{ezob_yfxmft});
                                }
                            }

                        }
                    }
                }
            }
            this.getView().showMessage("研发分摊表生成成功！");
        }
        }catch (Exception e)
        {
            StackTraceElement stackTraceElement=  e.getStackTrace()[0];
            this.getView().showMessage(e.getMessage()+yfxmtest+"异常发生在: " + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + " - " + stackTraceElement.getMethodName());
        }
    }
    public static Date getCurrTime() throws ParseException {
        Date date = new Date();
        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd :HH:mm:ss");
        String currTime = dateFormat.format(date);
        Date date1 = dateFormat.parse(currTime);
        return date1;
    }

    public BigDecimal parase(BigDecimal x)
    {

        BigDecimal newAmount = x.setScale(2, RoundingMode.HALF_UP);
        return  newAmount;
    }











}
