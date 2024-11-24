package kd.cosmic;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.ExtendedDataEntitySet;
import kd.bos.entity.botp.plugin.AbstractConvertPlugIn;
import kd.bos.entity.botp.plugin.args.AfterConvertEventArgs;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.basedata.BaseDataServiceHelper;
import kd.fi.fa.business.calc.FinCardCalc;
import kd.fi.fa.business.dao.impl.FaDaoOrmImpl;
import kd.fi.fa.business.utils.FaBizUtils;
import kd.fi.fa.business.utils.FaUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 描述: 财务应付单下推固定资产转移单，给下游字段赋值，插件位置：对应的转换规则
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：顾问夏晓君
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

public class aptofa_zy extends AbstractConvertPlugIn {
    private Map<String, BigDecimal> exchangeRateMapCache = new HashMap<>();

    public void afterConvert(AfterConvertEventArgs e) {
        ExtendedDataEntitySet targetExtDataEntitySet = e.getTargetExtDataEntitySet();
        ExtendedDataEntity[] extendedDataEntityArr = targetExtDataEntitySet.FindByEntityKey("fa_change_dept");
        List<DynamicObject> changeBillObjs = (List<DynamicObject>)Stream.<ExtendedDataEntity>of(extendedDataEntityArr).map(v -> v.getDataEntity()).collect(Collectors.toList());
        Set<Object> realCardIdSet = new HashSet(16);
        Set<Long> sourceids = new HashSet<>(16);
        Set<Object> newRealCard = new HashSet(16);
        List<DynamicObject> realcardDys = new ArrayList<>(16);
        int amtprecision = 2;
        Map<Long, DynamicObject> apBillMap = new HashMap<>();
        Map<Long, Set<Long>> engnnerRealMap = new HashMap<>();
        Set<Long> realSet = null;
        Map<Long, List<DynamicObject>> cardMap = new HashMap<>();
        Set<Long> existCards = new HashSet<>(1);



        for (DynamicObject changeBill : changeBillObjs) {
            Long sourceid = Long.valueOf(changeBill.getLong("sourceid"));
            realSet = engnnerRealMap.get(sourceid);
            if (realSet == null) {
                realSet = new HashSet<>(16);
                engnnerRealMap.put(sourceid, realSet);
            }
            sourceids.add(sourceid);
            DynamicObjectCollection fieldentry = changeBill.getDynamicObjectCollection("fieldentry");
            for (DynamicObject entry : fieldentry) {
                long realcard1 = entry.getLong("realcard1.masterid");
                realSet.add(Long.valueOf(realcard1));
                realcardDys.add(entry.getDynamicObject("realcard1"));
            }
            Date bizDate = setDateDifPeriod(changeBill.getDynamicObject("org"), Boolean.FALSE.booleanValue());
            Set<Long> curRealCard = FaUtils.getCurRealCard(bizDate, Long.valueOf(changeBill.getLong("org.id")), Boolean.valueOf(false), realSet);
            existCards.addAll(curRealCard);

        }
        //这里根据源单ID获取源单（财务应付单）信息

        Map<String, DynamicObject> entryMap = getApMap(sourceids, realCardIdSet, newRealCard, apBillMap);
        //根据实物卡片查找财务卡片

        List<DynamicObject> fincards = queryFinCardList(newRealCard, null);

        for (DynamicObject fin : fincards) {
            long realMasterid = fin.getLong("realcard.masterid");
            List<DynamicObject> fins = cardMap.get(Long.valueOf(realMasterid));
            if (fins == null) {
                fins = new ArrayList<>(2);
                cardMap.put(Long.valueOf(realMasterid), fins);
            }
            fins.add(fin);
        }
        for (DynamicObject changeBill : changeBillObjs) {
            DynamicObjectCollection fieldentrys = changeBill.getDynamicObjectCollection("fieldentry");
            DynamicObjectCollection changes = changeBill.getDynamicObjectCollection("main_changebillentry");


            Set<Long> realcards = new HashSet<>(fieldentrys.size());
            for (DynamicObject fieldentry : fieldentrys)

                realcards.add(Long.valueOf(fieldentry.getLong("realcard1.masterid")));
            fieldentrys.clear();
            long aPId = changeBill.getLong("sourceid");
            DynamicObject doAp = apBillMap.get(Long.valueOf(aPId));
            if (engnnerRealMap.containsKey(Long.valueOf(aPId)))
                for (Long real : realcards) {
                    List<DynamicObject> fincs = cardMap.get(real);
                    if (null == fincs)
                        continue;
                    for (DynamicObject fincard : fincs) {
                        Date bizDate = setDateDifPeriod(changeBill.getDynamicObject("org"), Boolean.FALSE.booleanValue());
                        changeBill.set("changedate", bizDate);


                        String billstatus = fincard.getString("billstatus");
                        String number = fincard.getString("number");
//            if (!"C".equals(billstatus))
//              throw new KDBizException(String.format(ResManager.loadKDString(", "EngineeringToChangeBillByCountPlugin_3", "fi-fa-opplugin", new Object[0]), new Object[] { number }));
                        BigDecimal netResidualValRate = FinCardCalc.getNetResidualValRate(fincard, true);
                        BigDecimal exchangeRate = getExchangeRate(fincard, doAp, this.exchangeRateMapCache);
//            if (exchangeRate == null)
//              throw new KDBizException(ResManager.loadKDString(", "EngineeringToFinCardGenerate_0", "fi-fa-business", new Object[0]));
                        DynamicObject preresidualvalDy = fieldentrys.addNew();
                        DynamicObject aPInfo = entryMap.get(aPId + "_" + fincard.getLong("realcard.masterid"));
                        long id = 0;
                        if(aPInfo.getDynamicObject("ezob_zrdm")!=null)
                        {
                            id = (long)aPInfo.getDynamicObject("ezob_zrdm").getPkValue();
                        }





                        DynamicObject inBaseCurrency = fincard.getDynamicObject("basecurrency");
                        if (inBaseCurrency != null)
                            amtprecision = inBaseCurrency.getInt("amtprecision");
                        //以下凡是涉及到ApInfo赋值的语句都要修改【ZOC：修改2】


                       // originalvalDy.set("field", "fa_card_fin.originalval");

//                        long id= (long)ApInfo.getDynamicObject("ezob_zrdm").getPkValue();


//                        originalvalDy.set("realcard1", fincard.get("realcard"));
//                        originalvalDy.set("fincard1", fincard);
//                        originalvalDy.set("reason", "资本化");
//                        originalvalDy.set("isadjustdepre1", "1");
//                        originalvalDy.set("assetnumber", ApInfo.getDynamicObject("ezob_basedatafield").getString("number"));
//                        originalvalDy.set("depreuse1", fincard.get("depreuse"));

//                        preresidualvalDy.set("field", "fa_card_fin.preresidualval");
                        preresidualvalDy.set("field", "fa_card_real.headusedept");
                        preresidualvalDy.set("aftervalue",id);


                        preresidualvalDy.set("realcard1", fincard.get("realcard"));
                        preresidualvalDy.set("fincard1", fincard);
                        preresidualvalDy.set("reason", "资本化");
                        preresidualvalDy.set("isadjustdepre1", "1");
                        preresidualvalDy.set("assetnumber", aPInfo.getDynamicObject("ezob_basedatafield").getString("number"));
                        preresidualvalDy.set("depreuse1", fincard.get("depreuse"));
                        preresidualvalDy.set("bizdate1",bizDate);
                        DynamicObjectCollection realentry = changeBill.getDynamicObjectCollection("realentry");
                        for (DynamicObject finentrys : realentry) {
                            DynamicObjectCollection finentry = finentrys.getDynamicObjectCollection("finentry");
                            DynamicObject fin = finentry.addNew();
                            fin.set("fincard", fincard);
                            fin.set("currency", fincard.getDynamicObject("basecurrency"));
                            fin.set("depreuse", fincard.get("depreuse"));
                            fin.set("isadjustdepre", "1");
                            fin.set("bizdate", bizDate);
                        }
                    }

                }
        }
    }

    private BigDecimal getExchangeRate(DynamicObject fincard, DynamicObject doAp, Map<String, BigDecimal> exchangeRateMapCache) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        DynamicObject assetBook = fincard.getDynamicObject("assetbook");
        DynamicObject exchangetable = null;
        long exchangetableId = 0L;
        long inBaseCurrencyId = 0L;
        BigDecimal exchangeRate = BigDecimal.ONE;
        if (assetBook != null)
            exchangetable = assetBook.getDynamicObject("exchangetable");
        if (exchangetable != null)
            exchangetableId = exchangetable.getLong("id");
        DynamicObject inBaseCurrency = fincard.getDynamicObject("basecurrency");
        if (inBaseCurrency != null)
            inBaseCurrencyId = inBaseCurrency.getLong("id");
        long currencyId = assetBook.getLong("basecurrency.id");
        String finaccountdate = sdf.format(fincard.getDate("finaccountdate"));
        String key = finaccountdate + exchangetableId + currencyId + inBaseCurrencyId;
        if (currencyId != inBaseCurrencyId) {
            if (!exchangeRateMapCache.containsKey(key)) {
                exchangeRate = (BigDecimal)getExchangeRate(Long.valueOf(exchangetableId), Long.valueOf(currencyId), Long.valueOf(inBaseCurrencyId), fincard.getDate("finaccountdate"));
                exchangeRateMapCache.put(key, exchangeRate);
            }
            exchangeRate = exchangeRateMapCache.get(key);
        }
        return exchangeRate;
    }

    private Object getExchangeRate(Long exchangeTableID, Long sourceCurrencyID, Long targetCurrencyID, Date date) {
        return BaseDataServiceHelper.getExchangeRate(exchangeTableID, sourceCurrencyID, targetCurrencyID, date);
    }

    //读取源单数据[ZOC：修改1]
    private static Map<String, DynamicObject> getApMap(Set<Long> sourceids, Set<Object> realCardIdSet, Set<Object> newRealCard, Map<Long, DynamicObject> enginnerBillMap) {
        QFilter qSourseid = new QFilter("id", "in", sourceids);
        Map<String, DynamicObject> entryMap = new HashMap<>(16);
        Set<Long> masterIds = new HashSet<>(16);
        //加载财务应付单元数据

        DynamicObject[] aPs = BusinessDataServiceHelper.load("ezob_gdzczysq", "entryentity,entryentity.creator,entryentity.ezob_basedatafield,entryentity.ezob_basedatafield.bizstatus,entryentity.ezob_basedatafield.masterid,entryentity.ezob_basedatafield.number,createtime,entryentity.ezob_zrdm,remark", new QFilter[] { qSourseid });
        for (DynamicObject doAp : aPs) {
            DynamicObjectCollection aPEntry = doAp.getDynamicObjectCollection("entryentity");

            for (DynamicObject entry : aPEntry) {
                long masterid = entry.getLong("ezob_basedatafield.masterid");
                masterIds.add(Long.valueOf(masterid));

            }
        }
        QFilter qMaster = new QFilter("masterid", "in", masterIds);
        QFilter qIsbak = new QFilter("isbak", "=", "0");
        DynamicObjectCollection realcards = QueryServiceHelper.query("fa_card_real", "id,masterid", new QFilter[] { qMaster, qIsbak });

        for (DynamicObject dy : realcards) {
            long  id = dy.getLong("id");
            newRealCard.add(Long.valueOf(id));
        }
        for (DynamicObject doAp : aPs) {
            DynamicObjectCollection aPEntry = doAp.getDynamicObjectCollection("entryentity");
            for (DynamicObject entry : aPEntry) {
                long realid = entry.getLong("ezob_basedatafield.masterid");
                String number = entry.getString("ezob_basedatafield.number");
                String bizstatus = entry.getString("ezob_basedatafield.bizstatus");
//        if (!"READY".equals(bizstatus))
//          throw new KDBizException(String.format(ResManager.loadKDString(", "EngineeringToChangeBillByCountPlugin_2", "fi-fa-opplugin", new Object[0]), new Object[] { number }));
                realCardIdSet.add(Long.valueOf(realid));
                entryMap.put(doAp.getLong("id") + "_" + entry.getLong("ezob_basedatafield.masterid"), entry);
                enginnerBillMap.put(Long.valueOf(doAp.getLong("id")), doAp);
            }
        }
        return entryMap;
    }

    private static List<DynamicObject> queryFinCardList(Set<Object> realCardPKSet, Set<String> curstoms) {
        Set<Long> realCardSet = new HashSet<>(realCardPKSet.size());
        realCardPKSet.forEach(r -> realCardSet.add(Long.valueOf(Long.parseLong(String.valueOf(r)))));
        QFilter[] filters = { new QFilter("realcard", "in", realCardSet) };
        String[] fieldArr = {
                "id", "realcard", "currency", "number", FaDaoOrmImpl.dot(new String[] { "realcard", "usestatus", "isdepre" }), "originalval", "accumdepre", "decval", "netamount", "preresidualval",
                "preusingamount", FaDaoOrmImpl.dot(new String[] { "assetbook", "basecurrency" }), FaDaoOrmImpl.dot(new String[] { "assetbook", "curperiod", "id" }), "depreuse", "isneeddepre", "bizperiod", "endperiod", "monthdepre", "networth", "addupyeardepre",
                "depremethod", "assetcat", "isdynamic", "monthorigvalchg", "monthdeprechg", "billstatus", "yearorigvalchg", "basecurrency", "finaccountdate" };
        Set<String> fieldSet = new HashSet<>();
        fieldSet.addAll(Arrays.asList(fieldArr));
        if (curstoms != null)
            fieldSet.addAll(curstoms);
        String selector = String.join(",", (Iterable)fieldSet);
        DynamicObject[] finCards = BusinessDataServiceHelper.load("fa_card_fin", selector, filters, "depreuse asc");
        Set<Object> bookIdSet = (Set<Object>)Stream.<DynamicObject>of(finCards).map(v -> v.get("assetbook_id")).collect(Collectors.toSet());
        selector = FaDaoOrmImpl.comma(new String[] { "id", "curperiod" });
        DynamicObjectCollection books = QueryServiceHelper.query("fa_assetbook", selector, (new QFilter("id", "in", bookIdSet)).toArray());
        Map<Object, Long> bookMap = (Map<Object, Long>)books.stream().collect(Collectors.toMap(v -> v.get("id"), v -> Long.valueOf(v.getLong("curperiod"))));
        List<DynamicObject> finCardLst = (List<DynamicObject>)Stream.<DynamicObject>of(finCards).filter(fincard -> filterFinCards(((Long)bookMap.get(fincard.get("assetbook_id"))).longValue(), fincard)).collect(Collectors.toList());
        return finCardLst;
    }

    private static boolean filterFinCards(long curPeriodId, DynamicObject fincard) {
        DynamicObject bizPeriodDyn = fincard.getDynamicObject("bizperiod");
        long bizPeriodId = 0L;
        if (bizPeriodDyn != null)
            bizPeriodId = fincard.getDynamicObject("bizperiod").getLong("id");
        long endPeriodId = 99999999999L;
        DynamicObject endPeriod = fincard.getDynamicObject("endperiod");
        if (endPeriod != null)
            endPeriodId = endPeriod.getLong("id");
        boolean isRight = (bizPeriodId <= curPeriodId && curPeriodId < endPeriodId);
        if (isRight) {
            if (bizPeriodId < curPeriodId) {
                fincard.set("monthorigvalchg", BigDecimal.ZERO);
                fincard.set("monthdeprechg", BigDecimal.ZERO);
            }
            if (bizPeriodId / FaBizUtils.YEAR_PERIOD_L.longValue() < curPeriodId / FaBizUtils.YEAR_PERIOD_L.longValue()) {
                fincard.set("yearorigvalchg", BigDecimal.ZERO);
                fincard.set("addupyeardepre", BigDecimal.ZERO);
            }
        }
        return isRight;
    }

    private static Date setDateDifPeriod(DynamicObject org, boolean futureBusiness) {
        Date now = new Date();
        if (org == null)
            return now;
        DynamicObject mainBook = FaBizUtils.getAsstBookByOrg(Long.valueOf(org.getLong("id")));
        if (mainBook != null) {
            Date begindate = (Date)mainBook.get("begindate");
            Date enddate = (Date)mainBook.get("enddate");
            if (begindate == null)
                return now;
            boolean flag = true;
            if (futureBusiness) {
                flag = now.after(begindate);
            } else {
                flag = (now.after(begindate) && now.before(enddate));
            }
            if (flag)
                return now;
            return enddate;
        }
        //throw new KDBizException(String.format(ResManager.loadKDString("%s, "EngineeringToFinCardGenerate_1", "fi-fa-opplugin", new Object[0]), new Object[] { org.getString("name") }));
        return now; 	//此处需修正
    }



}
