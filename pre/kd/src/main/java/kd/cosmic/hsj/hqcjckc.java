package kd.cosmic.hsj;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.AfterDeleteRowEventArgs;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListFilterParameter;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.math.BigDecimal;
import java.util.*;

/**
 * 单据界面：获取车间仓库存kd.cosmic.hqcjckc
 */
/**
 * 描述: 批量发料插件——领料申请单页面
 * 开发者: 胡思江
 * 创建日期: 2024/05/05
 * 关键客户：马丙丙
 * 已部署正式：ture
 * 备注：已经部署
 */
public class hqcjckc extends AbstractBillPlugIn implements BeforeF7SelectListener {
    private static final String KEY_MAINBAR = "advcontoolbarap";  //advcontoolbarap
    private static final String KEY_BARITEM_NEW = "ezob_hqcjckc";  //ezob_hqcjckc

    private static final String KEY_BARITEM_DEL = "ezob_delrow";  //条码主档-删除分录
    private static Map<String,Integer > tmzdtm = new HashMap<String,Integer >(); //条码所出现的行数

    private static final List<String> fbarcodeList = new ArrayList<>();

    @Override
    public void afterLoadData(EventObject e) {
        super.afterLoadData(e);
        //获取单据体分录行数量
        tmzdtm.clear();
        fbarcodeList.clear();
        System.out.println("1111111111111111");
        int irowcount = getModel().getEntryRowCount("billentry");
        System.err.println("我选择了第几行呀？"+irowcount);
        for (int parentRow = 0; parentRow < irowcount; parentRow++) {
            IDataModel iDataModel = getModel();
            // 指定父单据体行号 (必须)
            iDataModel.setEntryCurrentRowIndex("billentry", parentRow);
            iDataModel.deleteEntryData("ezob_subentryentity");      //重新点击获取车间仓库存，要先清空子单据体
        }

        calcQty();  //重新计算本单数量和尚欠数量
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 侦听主菜单按钮点击事件
        System.out.println("22222222222222222");
        addItemClickListeners(new String[] { "advcontoolbarap" });
        BasedataEdit bEdit = this.getView().getControl("ezob_tmzd");
        bEdit.addBeforeF7SelectListener(this);
    }

    public void beforeF7Select(BeforeF7SelectEvent evt) {
        System.out.println("333333333333333333");
        ListShowParameter formShowParameter = (ListShowParameter) evt.getFormShowParameter();
        List<QFilter> qFilters = new ArrayList<>();
        //获取组织
        DynamicObject dorg = (DynamicObject)getModel().getValue("org", this.getModel().getEntryCurrentRowIndex("billentry"));
        //获取选择的父单据体的物料
        DynamicObject dmaterial = (DynamicObject)getModel().getValue("material", this.getModel().getEntryCurrentRowIndex("billentry"));
        qFilters.add(new QFilter("fstockorgid", QCP.equals, dorg.getLong("id")));  //条码主档的物料kdxt_materialid
        qFilters.add(new QFilter("fmaterialid", QCP.equals, dmaterial.getLong("masterid.id")));
        qFilters.add(new QFilter("ezob_rkkczt", QCP.not_equals, 691930349319792640L));   //库存状态等于
        qFilters.add(new QFilter("fiqty", QCP.large_than, 0));   //结余数量
        qFilters.add(new QFilter("fbarcode", QCP.not_in,fbarcodeList));   //排除已经发完的
        formShowParameter.getListFilterParameter().setQFilters(qFilters);
        ListFilterParameter listFilterParameter = formShowParameter.getListFilterParameter();
        listFilterParameter.setOrderBy("fproductiondate,fiqty,fbarcode");
    }

    @Override
    public void afterDeleteRow(AfterDeleteRowEventArgs e) {
        super.afterDeleteRow(e);
        System.out.println("444444444444444444444");
        //当点击条码主档的删除分录时，要重新计算一次
        System.out.println("444444444444444444444"+e);
        System.out.println("444444444444444444444"+ e.getEntryProp().getName());
        if (StringUtils.equals("ezob_subentryentity", e.getEntryProp().getName())){
            calcQty();  //重新计算本单数量和尚欠数量
        }
    }

    public void beforeItemClick(BeforeItemClickEvent evt) {
        System.out.println("看看信息是什么！beforeItemClick-===");
        if (StringUtils.equals(KEY_BARITEM_NEW, evt.getItemKey())) {
            Set<Long> masterIds = new HashSet<>(16);

            //获取单据体分录行数量
            int irowcount = getModel().getEntryRowCount("billentry");
            for (int parentRow = 0; parentRow < irowcount; parentRow++) {
                IDataModel iDataModel = getModel();
                // 指定父单据体行号 (必须)
                iDataModel.setEntryCurrentRowIndex("billentry", parentRow);
                iDataModel.deleteEntryData("ezob_subentryentity");      //重新点击获取车间仓库存，要先清空子单据体
                //获取选择行的未领数量
                BigDecimal dremainoutqty = (BigDecimal)getModel().getValue("remainoutqty", parentRow);
                BigDecimal doutqty = (BigDecimal)getModel().getValue("remainoutqty", parentRow);
                //行状态
                String sstatus = (String)getModel().getValue("rowstatus", parentRow);
                //如果未领数量为0或行状态为已关闭，则跳过循环
                if(dremainoutqty.compareTo(BigDecimal.ZERO)==0 || sstatus.equals("D"))
                {
                    continue;
                }
                //获取物料
                DynamicObject dmaterial = (DynamicObject)getModel().getValue("material", parentRow);
                //获取组织
                DynamicObject dorg = (DynamicObject)getModel().getValue("org");
                //获取成本中心
                DynamicObject dcbzx = (DynamicObject)getModel().getValue("ezob_sfcbzx");

                //清空本单数量和尚欠数量
                getModel().setValue("ezob_bdsl",0,parentRow);
                getModel().setValue("ezob_sqsl",dremainoutqty,parentRow);

                //根据物料代码和成本中心获取车间仓库存
                QFilter qmaterial=null;
                if(dmaterial!=null) {
                    qmaterial = new QFilter("billentry.material.id", "=", dmaterial.getLong("id"));
                }
                else
                {
                    qmaterial = new QFilter("billentry.material.id", "=", 0);
                }
                QFilter qcbzx=null;
                if(dcbzx!=null) {
                    qcbzx = new QFilter("ezob_sfcbzx.id", "=", dcbzx.getLong("id"));
                }
                else {
                    qcbzx = new QFilter("ezob_sfcbzx.id", "=", 0);
                }
                QFilter qorgnumber=null;
                if(dorg!=null) {
                    qorgnumber = new QFilter("outorg.id", "=", dorg.getLong("id"));
                }
                else {
                    qorgnumber = new QFilter("outorg.id", "=", "");
                }
                QFilter qwthsl = new QFilter("billentry.remainreturnqty", ">", 0);
                QFilter qtmzd = new QFilter("billentry.ezob_tmzd.id", "<>", 0);
                QFilter qbillstatus = new QFilter("billstatus", "=", "C");

                //排除已被使用过的条码主档
                QFilter qzytmzd=null;
                if(masterIds.size()>0) {
                    qzytmzd = new QFilter("billentry.ezob_tmzd.id", "not in", masterIds);
                }
                DynamicObject[] simpleBill = BusinessDataServiceHelper.load
                        ("im_transdirbill","ezob_sfcbzx,billentry.id,billentry.material,billentry.ezob_bdsl,billentry.ezob_sqsl,billentry.ezob_tmzd,billentry.ezob_tmzd.id,billentry.warehouse,billentry.location,billentry.unit,billentry.unit.id,billentry.ezob_tmzd.fbarcode,billentry.id,billentry.remainreturnqty",
                                new QFilter[]{qmaterial,qcbzx,qwthsl,qtmzd,qorgnumber,qbillstatus,qzytmzd});  //qmaterial,qcbzx,qwthsl,qtmzd,qorgnumber,qbillstatus,qzytmzd

                //遍历查询的直接调拨单信息。注意这种方式只能获取整张单据，没办法过滤表体
                BigDecimal dbdsl = new BigDecimal(0);    //本单数量
                BigDecimal dzysl = new BigDecimal(0);    //已占用数量
                for(DynamicObject zzdbd : simpleBill){
                    //如果未领数量为0，则中断循环
                    if(dremainoutqty.compareTo(BigDecimal.ZERO)==0 )
                    {
                        break;  //中断
                    }
                    //赋值
                    BigDecimal dremainreturnqty=new BigDecimal(0);
                    DynamicObjectCollection apEntry = zzdbd.getDynamicObjectCollection("billentry");
                    for (DynamicObject entry : apEntry) {
                        //物料ID
                        long tmpwlid = entry.getLong("material.id");
                        //未退回数量
                        BigDecimal tmpremainreturnqty = entry.getBigDecimal("remainreturnqty");
                        //条码主档ID
                        long tmzdid = entry.getLong("ezob_tmzd.id");
                        //因为以上im_transdirbill的过滤只能过滤出表头，所以表体还得再做一次判断
                        if(tmpwlid==dmaterial.getLong("id") && tmpremainreturnqty.compareTo(BigDecimal.ZERO)>0 && tmzdid!=0) {
                            //子单据体创建新一行数据
                            int subRow = iDataModel.createNewEntryRow("ezob_subentryentity");

                            getModel().setValue("ezob_tmzd", tmzdid, subRow, parentRow);
                            masterIds.add(Long.valueOf(tmzdid));  //把已占用的条码主档先记录下来以便在下次循环中排除
                            dremainreturnqty = entry.getBigDecimal("remainreturnqty");
                            //条形码
                            String txm = entry.getString("ezob_tmzd.fbarcode");
                            getModel().setValue("ezob_txm", txm, subRow, parentRow);
                            //计量单位
                            long unitid = entry.getLong("unit.id");
                            getModel().setValue("ezob_tmdw", unitid, subRow, parentRow);
                            //仓库
                            long stockid = entry.getLong("warehouse.id");
                            getModel().setValue("ezob_ck", stockid, subRow, parentRow);
                            //因为车间仓不需要仓位
//                        long cwid = entry.getLong("location.id");
//                        getModel().setValue("ezob_cw",0,subRow,parentRow);
                            //条码结余数量
                            getModel().setValue("ezob_iqty",dremainreturnqty,subRow,parentRow);
                            //本单数量
                            dbdsl=dbdsl.add(dremainreturnqty);
                            //尚欠数量
                            dremainoutqty=dremainoutqty.subtract(dremainreturnqty);
                            //条码出库数量
                            getModel().setValue("ezob_cksl",dremainreturnqty,subRow,parentRow);
                            getModel().setValue("ezob_cjcdbdnm", entry.getLong("id"), subRow, parentRow);

                            //当尚欠数量小于或者等于0时
                            if(dremainoutqty.compareTo(new BigDecimal(0))!=1)
                            {
                                //条码出库数量
                                getModel().setValue("ezob_cksl",doutqty.subtract(dzysl),subRow,parentRow);
                                dbdsl=doutqty;    //本单数量等于一开始的尚欠数量
                                dremainoutqty=new BigDecimal(0);   //尚欠数量为0
                                break;  //中断
                            }
                            else
                            {
                                //累计已占用数量
                                dzysl=dzysl.add(dremainreturnqty);
                            }
                        }
                    }
                    //因为在值更新事件中触发计算了，所以此处无需再赋值
//                    getModel().setValue("ezob_bdsl",dbdsl,parentRow);  //最后赋值本单数量
//                    getModel().setValue("ezob_sqsl",dremainoutqty,parentRow);  //最后赋值尚欠数量
                }
            }
            System.out.println("4444444");
            this.getView().updateView("ezob_subentryentity");//更新子单据体View
            this.getView().showMessage("获取车间仓库存成功！");
        }
    }

    //当用户录入条码出库数量时自动更新本单数量和尚欠数量
    public void propertyChanged(PropertyChangedArgs e) {
        System.out.println("重新计算本单数量和尚欠数量5555555555555propertyChanged");
        super.propertyChanged(e);
        System.err.println("e.getProperty().getName();"+e);
        String name = e.getProperty().getName();
        System.err.println("e.getProperty().getName();"+name);
        if ("ezob_cksl".equals(name)) {
            calcQty();  //重新计算本单数量和尚欠数量
        }
    }


    public void calcQty(){
        System.err.println("-----------调用方法CalcQty-----------");
        System.err.println("tmzd"+tmzdtm);
        Map<String, BigDecimal> tmzd = new HashMap<String, BigDecimal>(); //条形码+数量
        DynamicObjectCollection entryEntity = getModel().getEntryEntity("billentry");
        int i = 0;
        //System.err.println("查看entryEntity查看entryEntity查看entryEntity："+entryEntity);

        // 步骤3.从单据体的每一行中获取gai行的子单据体对象，这里通过遍历的方式展现该效果
        for (DynamicObject entry : entryEntity) {
            System.err.println("检查空转1");
            //System.err.println("我看看我选了多少呀！！=-="+entry);
            System.err.println("我是最外层的信息当前次数"+i);
            BigDecimal qtyhz = new BigDecimal(0);
            // 获取当前单据体行的子单据体
            //System.err.println("查看qtyhz："+qtyhz);
            DynamicObjectCollection subEntryEntity = entry.getDynamicObjectCollection("ezob_subentryentity");
            System.err.println("查看subEntryEntity长度："+subEntryEntity.toArray().length);
            //获取当前选中物料行的收料数量
            BigDecimal dremainoutqty = (BigDecimal) entry.get("remainoutqty");
            // 从子单据体的每一行中获取当前行的子单据体的字段值，这里通过遍历的方式展现该效果

            int k  = 0 ;
            Boolean sf = false;
            for (DynamicObject subEntry : subEntryEntity) {
                k++;
                System.err.println("我看看长度"+subEntry.getPropertyChangeListeners().length);
                System.err.println("我看看长度"+subEntry.getLastDirty().length());
                System.err.println("检查空转2");
                System.err.println("本次勾选的所有数据我是明细计算的循环次数"+k);
                System.err.println("本单条码累积数量："+qtyhz);
                System.err.println("本单剩余应收数量"+dremainoutqty);
                BigDecimal zrcjcs = (BigDecimal)subEntry.get("ezob_drcjcsl");
                System.err.println("是不是存在了转入车间仓的数量了！！如果存在了就不进行循环了呀！"+zrcjcs);
                if(zrcjcs.compareTo(BigDecimal.ZERO)==0){
                    System.err.println("条码已经发放完毕1");
                }else{
                    System.err.println("条码已经发放完毕2");
                    sf = true;
                }

                if(subEntry.get("ezob_tmzd")==null){
                    System.err.println("检查空转3");
                    continue;
                }
                String fbarcode = (String)subEntry.get("ezob_tmzd.fbarcode").toString();//条形码
                System.err.println("+++++++++++++++++++++++++++CCCCCCCCCCCC"+subEntry);
                System.err.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+subEntry.get("ezob_tmzd"));
                System.err.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"+subEntry.get("ezob_tmzd.fstockid"));

                String  ck  = (String)subEntry.get("ezob_tmzd.fstockid.number").toString();//仓库
                System.err.println("条形码"+fbarcode);
                System.err.println("仓库"+ck);
                BigDecimal fiqty = (BigDecimal)subEntry.get("ezob_tmzd.fiqty");//数量

                //System.err.println("数量fiqty"+fiqty);
                BigDecimal outqty = (BigDecimal) subEntry.get("ezob_cksl");     //条码出库数量
                System.err.println("条码出库数量outqty："+outqty);
                System.err.println("条码出库数量fiqty："+fiqty);
                //需要区分是否点击获取车间仓
                System.err.println("outqty："+outqty!=null);
                System.err.println("fiqty："+fiqty.compareTo(BigDecimal.ZERO));
                if(fiqty.compareTo(BigDecimal.ZERO)==0&&outqty!=null){
                    System.err.println(" qtyhz = qtyhz.add(outqty);//累计数量？当前条码结余数量吧"+outqty);
                    qtyhz = qtyhz.add(outqty);//累计数量
                    fiqty = outqty;
                    //证明是点击获取车间仓后获取的到数值
                }else {
                    System.err.println(" qtyhz = qtyhz.add(fiqty);//累计数量？当前条码结余数量吧=："+fiqty);
                    qtyhz = qtyhz.add(fiqty);//累计数量
                }
                //System.err.println("数量outqty"+outqty);
                BigDecimal iqty = (BigDecimal) subEntry.get("ezob_iqty");     //条码结余数量
               // zcksl = zcksl.add(outqty);
                //检查是否有扫描过当前条码
                if(!tmzd.containsKey(fbarcode)){
                    System.err.println("检查空转4");
                    //第一次扫描则添加进记录最后还会更新一次数量
                    //System.err.println("本条码一次都没有扫过");
                    tmzd.put(fbarcode,fiqty);
                    System.err.println("查看当前的条码是："+fbarcode);
                    System.err.println("查看当前的条码是所在的行数是："+i);
                    if(!tmzdtm.containsKey(fbarcode)){
                        System.err.println("查看当前的条码是所在的行数是："+fbarcode);
                        tmzdtm.put(fbarcode,i);
                    }

                    if(i>tmzdtm.get(fbarcode)){
                        tmzdtm.replace(fbarcode,i);
                    }
                    System.err.println("无记录新条码加入"+fbarcode);
                    System.err.println("tmzdtmtmzdtmtmzdtm："+tmzdtm.get(fbarcode));
                    //qtyhz = qtyhz.add(outqty);
                    System.err.println("条码出库数量qtyhz："+qtyhz);
                    System.err.println("本单最大条码出库数量dremainoutqty："+dremainoutqty);

                    //用来对比那个数量大-出库数量和当前条码数量
                    int flag = dremainoutqty.compareTo(qtyhz);
                    System.err.println("对比的结果是"+flag);
                    System.err.println("当前物料最大出库数量："+dremainoutqty+"当前选择条码总数量"+qtyhz);
                    System.err.println("ubEntry.get(\"ezob_drcjcsl\")"+subEntry.get("ezob_drcjcsl"));

                    System.err.println("查看转入车间仓状态："+zrcjcs);
                    if(flag!=1 ){
                        System.err.println("检查空转5");
                        //bo = true;本条码的数量大于本行物料的数量
                        //BigDecimal sb = (qtyhz.add(outqty)).subtract(dremainoutqty);
                        System.err.println("正在进行操作条码："+fbarcode);
                        System.err.println("查看转入车间仓状态："+zrcjcs);
                        if(!zrcjcs.equals(BigDecimal.ZERO)){
                            System.err.println("检查空转6");
                            System.err.println("检测到有转入车间仓数量："+fbarcode+":"+zrcjcs);
                            subEntry.set("ezob_cksl",fiqty.subtract(qtyhz.subtract(dremainoutqty)));
                            //subEntryEntity.remove(subEntry);
                            //this.getModel().deleteEntryRow("ezob_subentryentity", k);
                            //this.getView().updateView("ezob_subentryentity");//更新子单据体View
                            subEntry.set("ezob_drcjcsl", qtyhz.subtract(dremainoutqty));      //计算调拨车间仓数量
                        }else{
                            System.err.println("检查空转7");
                            System.err.println("无转入车间仓数量："+fbarcode);
                            subEntry.set("ezob_cksl",fiqty.subtract(qtyhz.subtract(dremainoutqty)));
                            subEntry.set("ezob_drcjcsl", (qtyhz.add(zrcjcs)).subtract(dremainoutqty));      //计算调拨车间仓数量
                        }

                        System.err.println("检测到有转入车间仓数量："+fbarcode);
                        System.err.println("查看fiqtyfiqtyfiqtyfiqty数量"+fiqty);
                        //subEntry.set("ezob_iqty",qtyhz);
                        subEntry.set("ezob_iqty",fiqty);
                        subEntry.set("ezob_cksl",fiqty.subtract(qtyhz.subtract(dremainoutqty)));
                        //System.err.println("查看查看仓库是多少呀！！！！！"+subEntry.get("ezob_tmzd.fstockid"));
                        //System.err.println("查看查看仓库是多少呀！！！！！1111111"+subEntry.get("ezob_tmzd.fstockid.number"));

                        //String  ck  = (String)subEntry.get("ezob_tmzd.fstockid.number").toString();//仓库

                        if((ck =="100")||(ck =="AB100")||(ck =="FZ100")||(ck =="01.18")){
                            subEntry.set("ezob_cksl",dremainoutqty);
                        }
                        subEntry.set("ezob_drcjcsl", qtyhz.subtract(dremainoutqty));      //计算调拨车间仓数量
                        System.err.println("查看本单数量剩余"+dremainoutqty);
                        System.err.println("查看本单条码累计数量"+qtyhz);
                        System.err.println("查看条码数量"+iqty);
                        tmzd.replace(fbarcode,qtyhz.subtract(dremainoutqty));
                        System.err.println("当前条码余量："+tmzd);
                        System.err.println("当前的IIIIIIIIIIIIIII："+i);
                        System.err.println("当前的条码出现最后的位置："+tmzdtm);
                        subEntry.set("ezob_drcjcsl", qtyhz.subtract(dremainoutqty));      //计算调拨车间仓数量
                        System.err.println("当前的条码出现最后的位置i======："+tmzdtm.get(fbarcode));
                        if(tmzdtm.get(fbarcode)>i){
                            subEntry.set("ezob_drcjcsl",0);      //计算调拨车间仓数量
                        }
                        System.err.println("当前物料最大出库数量："+dremainoutqty+"——小于当前选择条码总数量"+qtyhz);
                        qtyhz = dremainoutqty;//本单数量最大等于当前物料最大出库数量
                        if(qtyhz.subtract(dremainoutqty).equals(iqty)){
                            subEntry.set("ezob_cksl",0);
                        }
//                        DynamicObjectCollection ezobSubentryentity = dynamicObject.getDynamicObjectCollection("ezob_subentryentity");
                        //subEntry.remove(2)
                        //zcksl = dremainoutqty;
                        //break;
                    }else {
                        System.err.println("检查空转8");
                        //本条码的数量不足够扣减本行物料的数量

                        System.err.println("本条码的数量不足够扣减本行物料的数量"+tmzd);
                        tmzd.replace(fbarcode,BigDecimal.valueOf(0));
                        System.err.println("当前条码余量："+tmzd);
                        subEntry.set("ezob_cksl",outqty);
                        //qtyhz =qtyhz.add(iqty);//
                        //zcksl = zcksl.add(iqty);
                        fbarcodeList.add(fbarcode);
                        subEntry.set("ezob_drcjcsl",0);      //计算调拨车间仓数量
                        System.err.println("当前物料最大出库数量："+dremainoutqty+"——大于当前选择条码总数量"+qtyhz);
                        //break;
                    }
                }else {
                    //dremainoutqty=dremainoutqty.subtract(qtyhz);
                    //不是第一次扫描已经有出库的数量了
                    System.err.println("检查空转9");
                    BigDecimal yfsl = tmzd.get(fbarcode);
                    System.err.println("条码剩余数量yfsl："+yfsl);
                    System.err.println("条码结余数量iqty："+iqty);
                    if(yfsl.equals(BigDecimal.ZERO)){
//                        bo = true;
//                        System.err.println("当前条码已经无余量");
                        System.err.println("检查空转10");
                        this.getView().showMessage("当前条码:"+fbarcode+"已经无余量");
//                        //System.err.println("当前条码已经无余量");
//                        break;
                    }else if (dremainoutqty.compareTo(yfsl)==1){
                        //当本单数量超过条码剩余数量时
                        System.err.println("检查空转11");
                        System.err.println("当前dremainoutqty"+dremainoutqty);
                        System.err.println("当前yfsl"+yfsl);
                        System.err.println("当前qtyhz"+qtyhz);

                        subEntry.set("ezob_cksl",yfsl);
                        subEntry.set("ezob_iqty",yfsl);
                        qtyhz = yfsl;
                        //zcksl = qtyhz.add(yfsl);
                        fbarcodeList.add(fbarcode);
                        tmzd.replace(fbarcode,BigDecimal.valueOf(0));
                        System.err.println("当前的IIIIIIIIIIIIIII："+i);
                        System.err.println("当前的条码出现最后的位置："+tmzdtm);
                        //当当前条码的i大于记录中出
                        tmzdtm.replace(fbarcode,i);
//                        if(i>tmzdtm.get(fbarcode)){
//
//                        }

                        subEntry.set("ezob_drcjcsl", 0);      //计算调拨车间仓数量
                        System.err.println("当前qtyhz"+qtyhz);
                        //break;
                    }else if(dremainoutqty.compareTo(yfsl)==-1){
                        //当本单数量小于条码数量时候
                        System.err.println("当前dremainoutqty"+dremainoutqty);
                        System.err.println("当前yfsl"+yfsl);
                        subEntry.set("ezob_iqty",yfsl);
                        subEntry.set("ezob_cksl",dremainoutqty);
                        //qtyhz = qtyhz.add(dremainoutqty);
                        //zcksl = qtyhz.add(dremainoutqty);
                        tmzd.replace(fbarcode,yfsl.subtract(dremainoutqty));
                        iqty = yfsl.subtract(dremainoutqty);
                        qtyhz = dremainoutqty;//应发数量=本单最大数量
                        System.err.println("当前条码余量："+tmzd);
                        System.err.println("当前qtyhz"+qtyhz);
                        System.err.println("当前qtyhz"+qtyhz);
                        subEntry.set("ezob_drcjcsl", iqty);      //计算调拨车间仓数量
                        System.err.println("我执行了删除操作"+fbarcode);
                        //subEntryEntity.remove(subEntry);
                        //this.getModel().deleteEntryRow("ezob_subentryentity", k);
                        System.err.println("条码主档tm最后出现的次数-"+tmzdtm);
                        //this.getView().updateView("ezob_subentryentity");//更新子单据体View
                        if(tmzdtm.get(fbarcode)>i){
                            subEntry.set("ezob_drcjcsl", 0);      //计算调拨车间仓数量
                        }else {
                            tmzdtm.replace(fbarcode,i);
                        }

                        System.err.println("我是多余的条码检查空转11"+fbarcode);
                        //break;
                    }else{
                        //刚刚剩余的数量=这个物料所需要的
                        iqty = yfsl;
                        tmzdtm.replace(fbarcode,i);
                        qtyhz = dremainoutqty;
                        System.err.println("我是多余的条码检查空转11"+fbarcode);
                        System.err.println("当前条码剩余数量："+iqty);
                        System.err.println("我执行了删除操作"+fbarcode);
                        subEntry.set("ezob_iqty",yfsl);
                        subEntry.set("ezob_cksl",dremainoutqty);
                        fbarcodeList.add(fbarcode);
                        //subEntryEntity.remove(subEntry);
                        //this.getModel().deleteEntryRow("ezob_subentryentity", k);
                        System.err.println("我执行完毕删除操作"+fbarcode);
                        //this.getView().updateView("ezob_subentryentity");//更新子单据体View
                    }
                }
            }
            System.err.println("ubEntry.get(\"(BigDecimal) entry.get(\"remainoutqty\")\")"+dremainoutqty);
            getModel().setValue("ezob_bdsl", qtyhz, i);
            getModel().setValue("ezob_sqsl", dremainoutqty.subtract(qtyhz), i);
            i++;

        }

        this.getView().updateView("ezob_subentryentity");//更新子单据体View
    }

}
