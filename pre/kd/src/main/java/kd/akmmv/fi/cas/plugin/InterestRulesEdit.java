package kd.akmmv.fi.cas.plugin;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class InterestRulesEdit extends AbstractBillPlugIn {
    String payerProvince = "";

    String payeeProvince = "";

    String payerBankName = "";

    String payeeBankName = "";

    public void propertyChanged(PropertyChangedArgs e) {
        addClickListeners(new String[] { "" });
        DynamicObject paymenttype = (DynamicObject)getModel().getValue("paymenttype");
        if (paymenttype != null) {
            String name = paymenttype.getString("name");
            if ("采购付款".equals(name) || "其他应付".equals(name)) {
                QFilter q;
                DynamicObject payerCurrency = (DynamicObject)getModel().getValue("currency");
                String currencyR = payerCurrency.getString("name");
                QFilter currencyQfilter = new QFilter("name", "=", currencyR);
                DynamicObject bdCurrency = BusinessDataServiceHelper.loadSingle("bd_currency", new QFilter[] { currencyQfilter });
                String bdCurrencyId = "";
                if (bdCurrency != null)
                    bdCurrencyId = bdCurrency.getPkValue().toString();
                DynamicObject payeeCurrency = (DynamicObject)getModel().getValue("dpcurrency");
                String currencyE = "";
                if (payeeCurrency != null)
                    currencyE = payeeCurrency.getString("name");
                QFilter currencyQfilterE = new QFilter("name", "=", currencyE);
                DynamicObject bdCurrencyE = BusinessDataServiceHelper.loadSingle("bd_currency", new QFilter[] { currencyQfilterE });
                String bdCurrencyIdE = "";
                if (bdCurrencyE != null)
                    bdCurrencyIdE = bdCurrencyE.getPkValue().toString();
                DynamicObject payerbank = (DynamicObject)getModel().getValue("payerbank");
                if (payerbank != null) {
                    Object payerPkValue = payerbank.getPkValue();
                    DynamicObject bdFinorginfo = BusinessDataServiceHelper.loadSingle(payerPkValue, "bd_finorginfo");
                    DynamicObject bankCate = bdFinorginfo.getDynamicObject("bank_cate");
                    if (bankCate != null)
                        this.payerBankName = bankCate.getString("name");
                    DynamicObject province = bdFinorginfo.getDynamicObject("province");
                    if (province != null)
                        this.payerProvince = province.getString("name");
                }
                DynamicObject payeebank = (DynamicObject)getModel().getValue("payeebank");
                if (payeebank != null) {
                    Object payeePkValue = payeebank.getPkValue();
                    DynamicObject bdBebank = BusinessDataServiceHelper.loadSingle(payeePkValue, "bd_bebank");
                    if (bdBebank != null) {
                        Object bankPkValue = bdBebank.getPkValue();
                        DynamicObject province = bdBebank.getDynamicObject("province");
                        if (province != null)
                            this.payeeProvince = province.getString("name");
                        QFilter bankQfiter = new QFilter("bebank.id", "=", bankPkValue);
                        DynamicObject bdFinorginfo = BusinessDataServiceHelper.loadSingle("bd_finorginfo", new QFilter[] { bankQfiter });
                        if (bdFinorginfo != null) {
                            DynamicObject bankCate = bdFinorginfo.getDynamicObject("bank_cate");
                            if (bankCate != null)
                                this.payeeBankName = bankCate.getString("name");
                        }
                    }
                }
                String paytype = "1";
                //付款处理 是否为异币别付款
                Boolean isdiffcur = (Boolean)getModel().getValue("isdiffcur");
                if (!currencyE.equals(currencyR) && isdiffcur.booleanValue())
                    paytype = "2";
                DynamicObject payeracctbank = (DynamicObject)getModel().getValue("payeracctbank");
                String bankId = "";
                if (payeracctbank != null)
                    bankId = payeracctbank.getPkValue().toString();
                QFilter q1 = new QFilter("ezob_billingstatus", "=", paytype);
                QFilter q2 = new QFilter("ezob_currency.id", "=", bdCurrencyId);
                QFilter q4 = new QFilter("ezob_currency.id", "=", bdCurrencyIdE);
                QFilter q3 = new QFilter("ezob_bank.id", "=", bankId);
                if (isdiffcur.booleanValue()) {
                    q = q1.and(q4).and(q3);
                } else {
                    q = q1.and(q2).and(q3);
                }
                DynamicObject ezobInterestrule = BusinessDataServiceHelper.loadSingle("ezob_interestrule", new QFilter[] { q });
                String rule = "";
                BigDecimal bigDecimal = BigDecimal.ZERO;
                BigDecimal hundred = BigDecimal.valueOf(100L);
                if (ezobInterestrule == null) {
                    rule = "1";
                } else {
                    rule = ezobInterestrule.get("ezob_rulestatus").toString();
                }
                if ("1".equals(rule)) {
                    free(bigDecimal);
                } else if ("2".equals(rule)) {
                    //isdiffcur异币别付款 hundred100 bigDecimal0 curr
                    perpen(ezobInterestrule, isdiffcur, hundred, bigDecimal, currencyE, currencyR);
                } else if ("3".equals(rule)) {
                    straddle(ezobInterestrule, this.payeeBankName, this.payerBankName, bigDecimal);
                } else if ("4".equals(rule)) {
                    increasing(hundred, ezobInterestrule, this.payeeBankName, this.payerBankName, bigDecimal, this.payerProvince, this.payeeProvince, paytype);
                } else if ("5".equals(rule)) {
                    my(ezobInterestrule, isdiffcur);
                }
            }
        }
    }

    public void free(BigDecimal bigDecimal) {
        getModel().setValue("ezob_bankcharges", bigDecimal);
    }

    public void perpen(DynamicObject ezobInterestrule, Boolean isdiffcur, BigDecimal hundred, BigDecimal bigDecimal, String currencyE, String currencyR) {
        BigDecimal multiply, taxSize = ezobInterestrule.getBigDecimal("ezob_taxsize");
        BigDecimal minamount = ezobInterestrule.getBigDecimal("ezob_minamount");
        BigDecimal maxamount = ezobInterestrule.getBigDecimal("ezob_maxamount");
        BigDecimal amount = ezobInterestrule.getBigDecimal("ezob_amount");
        BigDecimal amountHk = ezobInterestrule.getBigDecimal("ezob_amount_hk");
        BigDecimal actpayamt = (BigDecimal)getModel().getValue("actpayamt");
        BigDecimal dpamt = (BigDecimal)getModel().getValue("dpamt");
        BigDecimal localamt = (BigDecimal)getModel().getValue("localamt");
        BigDecimal dplocalamt = (BigDecimal)getModel().getValue("dplocalamt");
        Boolean ezobIsdianbao = (Boolean)getModel().getValue("ezob_isdianbao");
        if (!isdiffcur.booleanValue()) {
            multiply = taxSize.multiply(actpayamt.divide(hundred, 3, RoundingMode.DOWN));
        } else if ("人民币".equals(currencyE)) {
            multiply = taxSize.multiply(dpamt.divide(hundred, 3, RoundingMode.DOWN));
        } else if ("人民币".equals(currencyR)) {
            multiply = taxSize.multiply(localamt.divide(hundred, 3, RoundingMode.DOWN));
        } else {
            multiply = taxSize.multiply(dplocalamt.divide(hundred, 3, RoundingMode.DOWN));
        }
        if (ezobIsdianbao.booleanValue()) {
            Boolean ishk = (Boolean)getModel().getValue("ezob_ishk");
            if (ishk.booleanValue()) {
                getModel().setValue("ezob_bankcharges", amountHk);
            } else {
                getModel().setValue("ezob_bankcharges", amount);
            }
        } else if (maxamount.compareTo(bigDecimal) == 0) {
            getModel().setValue("ezob_bankcharges", multiply);
        } else {
            if (multiply.compareTo(minamount) == -1) {
                multiply = minamount;
            } else if (multiply.compareTo(maxamount) == 1) {
                multiply = maxamount;
            }
            getModel().setValue("ezob_bankcharges", multiply);
        }
    }

    public void straddle(DynamicObject ezobInterestrule, String payeeBankName, String payerBankName, BigDecimal bigDecimal) {
        BigDecimal amountanyone = ezobInterestrule.getBigDecimal("ezob_amountanyone");
        Boolean ezobIsdianbao = (Boolean)getModel().getValue("ezob_isdianbao");
        boolean isstraddle = ezobInterestrule.getBoolean("ezob_isstraddle");
        if (isstraddle) {
            if (payeeBankName.equals(payerBankName)) {
                getModel().setValue("ezob_bankcharges", bigDecimal);
            } else {
                int entry = getModel().getEntryRowCount("entry");
                BigDecimal multiply = BigDecimal.valueOf(entry).multiply(amountanyone);
                getModel().setValue("ezob_bankcharges", multiply);
            }
        } else if (ezobIsdianbao.booleanValue()) {
            getModel().setValue("ezob_bankcharges", bigDecimal);
        } else if (amountanyone.compareTo(bigDecimal) == 0) {
            getModel().setValue("ezob_bankcharges", bigDecimal);
        } else {
            int entry = getModel().getEntryRowCount("entry");
            BigDecimal multiply = BigDecimal.valueOf(entry).multiply(amountanyone);
            getModel().setValue("ezob_bankcharges", multiply);
        }
    }

    public void increasing(BigDecimal hundred, DynamicObject ezobInterestrule, String payeeBankName, String payerBankName, BigDecimal bigDecimal, String payerProvince, String payeeProvince, String paytype) {
        BigDecimal count = BigDecimal.ZERO;
        boolean isstraddle = ezobInterestrule.getBoolean("ezob_isstraddle");
        if (isstraddle) {
            if (payeeBankName.equals(payerBankName)) {
                getModel().setValue("ezob_bankcharges", bigDecimal);
            } else if (!payerProvince.equals(payeeProvince)) {
                for (DynamicObject entry : getModel().getEntryEntity("entry")) {
                    BigDecimal ePayableamt;
                    if ("1".equals(paytype)) {
                        ePayableamt = entry.getBigDecimal("e_actamt");
                    } else {
                        ePayableamt = entry.getBigDecimal("e_localamt");
                    }
                    for (DynamicObject entryentity : ezobInterestrule.getDynamicObjectCollection("ezob_entryentity")) {
                        BigDecimal ezobAmonts = entryentity.getBigDecimal("ezob_amonts");
                        String ezobSymbol = entryentity.getString("ezob_symbol");
                        if ("2".equals(ezobSymbol) || ePayableamt.compareTo(ezobAmonts) < 1) {
                            BigDecimal ezobAnyone = entryentity.getBigDecimal("ezob_anyone");
                            count = count.add(ezobAnyone);
                            break;
                        }
                        if ("3".equals(ezobSymbol) || ePayableamt.compareTo(ezobAmonts) == 1) {
                            BigDecimal ezobTax = entryentity.getBigDecimal("ezob_tax");
                            BigDecimal ezobMax = entryentity.getBigDecimal("ezob_max");
                            BigDecimal multiply = ePayableamt.multiply(ezobTax.divide(hundred, 3, RoundingMode.DOWN));
                            if (ezobMax.compareTo(bigDecimal) == 0) {
                                count = count.add(multiply);
                                continue;
                            }
                            if (multiply.compareTo(ezobMax) == 1) {
                                count = count.add(ezobMax);
                                continue;
                            }
                            count = count.add(multiply);
                        }
                    }
                }
                getModel().setValue("ezob_bankcharges", count);
            }
        } else {
            for (DynamicObject entry : getModel().getEntryEntity("entry")) {
                BigDecimal ePayableamt;
                if ("1".equals(paytype)) {
                    ePayableamt = entry.getBigDecimal("e_actamt");
                } else {
                    ePayableamt = entry.getBigDecimal("e_localamt");
                }
                for (DynamicObject entryentity : ezobInterestrule.getDynamicObjectCollection("ezob_entryentity")) {
                    BigDecimal ezobAmonts = entryentity.getBigDecimal("ezob_amonts");
                    String ezobSymbol = entryentity.getString("ezob_symbol");
                    if ("2".equals(ezobSymbol) && ePayableamt.compareTo(ezobAmonts) < 1) {
                        BigDecimal ezobAnyone = entryentity.getBigDecimal("ezob_anyone");
                        count = count.add(ezobAnyone);
                        break;
                    }
                    if ("3".equals(ezobSymbol) && ePayableamt.compareTo(ezobAmonts) == 1) {
                        BigDecimal ezobTax = entryentity.getBigDecimal("ezob_tax");
                        BigDecimal ezobMax = entryentity.getBigDecimal("ezob_max");
                        BigDecimal multiply = ePayableamt.multiply(ezobTax.divide(hundred, 3, RoundingMode.DOWN));
                        if (ezobMax.compareTo(bigDecimal) == 0) {
                            count = count.add(multiply);
                            continue;
                        }
                        if (multiply.compareTo(ezobMax) == 1) {
                            count = count.add(ezobMax);
                            continue;
                        }
                        count = count.add(multiply);
                    }
                }
            }
            getModel().setValue("ezob_bankcharges", count);
        }
    }

    public void my(DynamicObject ezobInterestrule, Boolean isdiffcur) {
        BigDecimal exchangerate, usdcurrency = ezobInterestrule.getBigDecimal("ezob_usdamount");
        int entry = getModel().getEntryRowCount("entry");
        if (!isdiffcur.booleanValue()) {
            exchangerate = (BigDecimal)getModel().getValue("exchangerate");
        } else {
            exchangerate = (BigDecimal)getModel().getValue("dpexchangerate");
        }
        BigDecimal multiply = BigDecimal.valueOf(entry).multiply(usdcurrency).multiply(exchangerate);
        getModel().setValue("ezob_bankcharges", multiply);
    }
}
