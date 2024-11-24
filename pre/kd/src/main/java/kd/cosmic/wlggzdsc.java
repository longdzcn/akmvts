package kd.cosmic;

import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * 描述: 物料规格自动生成  由物料分类属性构成
 * 开发者: 李四辉
 * 创建日期:2024-04-01
 * 关键客户：仓库
 * 已部署正式：true
 * 备注：已投入正式环境使用，无问题
 */

public class wlggzdsc extends AbstractFormPlugin implements Plugin {

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String key = e.getProperty().getName();
        int index = key.indexOf("_");
        if (index == -1) {
            return;
        }
        String pre = key.substring(0, index);
        //不是规格字段更新时更新规格
        if (pre.equals("ezob") && !key.equals("ezob_gg")) {
            changedGg();
        }
        if (key.equals("ezob_gcscxh")){
            DynamicObject fl = (DynamicObject) this.getModel().getValue("ezob_wlfl");
            if (fl == null) {
                this.getView().showTipNotification("表头物料分类为空！");
                return;
            }
            Long masterid = fl.getLong("masterid");
            String ezobgcscxh = this.getModel().getValue("ezob_gcscxh").toString();
            String sql = "/*dialect*/ select fk_ezob_gcscxh from akmmv_prd_eip_test.t_bd_material \n" +
                    "WHERE fk_ezob_wlfl ='" + masterid +  "' and fk_ezob_gcscxh = '"+ezobgcscxh+"';";
            DataSet ds = DB.queryDataSet(wlggzdsc.class.getName(), DBRoute.of("eip"), sql);

            if (!ds.isEmpty()) {
                this.getView().showTipNotification("该生产型号已有编码！");
            }
        }
    }

    public void changedGg() {
        StringBuilder gg = new StringBuilder();//规格
        //名称
//        DynamicObject ezob_wlname = (DynamicObject)this.getModel().getValue("ezob_wlname");
//        if (ezob_wlname != null) {
//            String ezob_wlnamename = ezob_wlname.get("name").toString();
//            gg.append(ezob_wlnamename).append(" ");
//        }
        //型号
//        DynamicObject ezob_kings = (DynamicObject)this.getModel().getValue("ezob_kings");
//        if (ezob_kings != null) {
//            String ezob_kingsname = ezob_kings.get("name").toString();
//            gg.append(ezob_kingsname).append(" ");
//        }
        //图纸版本
        String ezobtzbb = this.getModel().getValue("ezob_tzbb").toString();
        if (!Objects.equals(ezobtzbb, "")) {
            gg.append(ezobtzbb).append(" ");
        }
        //品牌(制造商)
        DynamicObject ezobppmake = (DynamicObject)this.getModel().getValue("ezob_ppmake");
        if (ezobppmake != null) {
            String ezobppmakename = ezobppmake.get("name").toString();
            gg.append(ezobppmakename).append(" ");
        }
        //设备名称
        String ezobsbmc = this.getModel().getValue("ezob_sbmc").toString();
        if (!ezobsbmc.isEmpty()) {
            gg.append(ezobsbmc).append(" ");
        }
        //FCCL规格
        DynamicObject ezobfcclgg = (DynamicObject)this.getModel().getValue("ezob_fcclgg");
        if (ezobfcclgg != null) {
            String ezobfcclggname = ezobfcclgg.get("name").toString();
            gg.append(ezobfcclggname).append(" ");
        }
        //P片规格
        DynamicObject ezobppgg = (DynamicObject)this.getModel().getValue("ezob_ppgg");
        if (ezobppgg != null) {
            String ezobppggname = ezobppgg.get("name").toString();
            gg.append(ezobppggname).append(" ");
        }
        //含胶量
        String ezobhjl = this.getModel().getValue("ezob_hjl").toString();
        if (!Objects.equals(ezobhjl, "")) {
            gg.append("RC"+ezobhjl);
            //含胶量公差
            String ezobhjlgc = this.getModel().getValue("ezob_hjlgc").toString();
            //如果含胶量有值含胶量公差为空则加%
            if (!Objects.equals(ezobhjl, "") && Objects.equals(ezobhjlgc, "")) {
                gg.append("%").append(" ");
            }
            if (!Objects.equals(ezobhjlgc, "")) {
                gg.append("±"+ezobhjlgc+"%").append(" ");
            }

        }


        //客户系列号
        String ezobkhxlh = this.getModel().getValue("ezob_khxlh").toString();
        if (!Objects.equals(ezobkhxlh, "")) {
            gg.append(ezobkhxlh).append(" ");
        }
        //工厂生产型号
        String ezobgcscxh = this.getModel().getValue("ezob_gcscxh").toString();
        if (!Objects.equals(ezobgcscxh, "")) {
            gg.append(ezobgcscxh).append(" ");
        }
        //层别
        String ezobcb = this.getModel().getValue("ezob_cb").toString();
        if (!Objects.equals(ezobcb, "")) {
            gg.append(ezobcb).append(" ");
        }
        //序列号
        String ezobxlh = this.getModel().getValue("ezob_xlh").toString();
        if (!Objects.equals(ezobxlh, "")) {
            gg.append(ezobxlh).append(" ");
        }
        //测试单元
        String ezobcsdy = this.getModel().getValue("ezob_csdy").toString();
        if (!Objects.equals(ezobcsdy, "")) {
            gg.append(ezobcsdy).append(" ");
        }
        //纯胶类型
        DynamicObject ezobcsunit = (DynamicObject)this.getModel().getValue("ezob_csunit");
        if (ezobcsunit != null) {
            String ezobcsunitname = ezobcsunit.get("name").toString();
            gg.append(ezobcsunitname).append(" ");
        }
        DynamicObject ezobwlfl = (DynamicObject)this.getModel().getValue("ezob_wlfl");
        if(ezobwlfl != null){
            String ezobwlfznumber = ezobwlfl.get("number").toString();
            int wlfzlength = ezobwlfznumber.length();//获取字符串长度防止超出下标
            //这两类分组需要W:改成经向:
            if(wlfzlength >= 5){
                if(ezobwlfznumber.substring(0, 5).equals("M1.09") || ezobwlfznumber.substring(0, 5).equals("M1.15") || ezobwlfznumber.substring(0, 5).equals("A1.21")){
                    //宽度(经向)/mm
                    BigDecimal ezobkdjx = (BigDecimal) this.getModel().getValue("ezob_kdjx");
                    if (ezobkdjx != null) {
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String data = format.format(ezobkdjx);
                        if (!data.equals("0")) {
                            gg.append("经向:").append(data);
                        }
                    }
                }else{
                    //宽度(经向)/mm
                    BigDecimal ezobkdjx = (BigDecimal) this.getModel().getValue("ezob_kdjx");
                    if (ezobkdjx != null) {
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String data = format.format(ezobkdjx);
                        if (!data.equals("0")) {
                            gg.append("W:").append(data);
                        }
                    }
                }
            }
        }
        //宽度(经向)单位
        if (this.getModel().getValue("ezob_kddw") != null) {
            String ezobkddw = this.getModel().getValue("ezob_kddw").toString();
            gg.append(ezobkddw).append(" ");
        }
        //宽度(经向)/inch
//        BigDecimal ezob_kdwidh = (BigDecimal) this.getModel().getValue("ezob_kdwidh");
//        if (ezob_kdwidh != null) {
//            DecimalFormat format = new DecimalFormat("#.##########");
//            String data = format.format(ezob_kdwidh);
//            if (!data.equals("0")) {
//                gg.append("W:").append(data).append("inch*");
//            }
//        }


        if(ezobwlfl != null){
            String ezobwlfznumber = ezobwlfl.get("number").toString();
            int wlfzlength = ezobwlfznumber.length();//获取字符串长度防止超出下标
            //这两类分组需要W:改成经向:
            if(wlfzlength >= 5){
                if(ezobwlfznumber.substring(0, 5).equals("M1.09") || ezobwlfznumber.substring(0, 5).equals("M1.15") || ezobwlfznumber.substring(0, 5).equals("A1.21")){
                    //长度(纬向)/mm
                    BigDecimal cdwx = (BigDecimal) this.getModel().getValue("ezob_cdwx");
                    if (cdwx != null) {
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String data = format.format(cdwx);
                        if (!data.equals("0")) {
                            gg.append("纬向:").append(data);
                        }
                    }
                }else{
                    //长度(纬向)/mm
                    BigDecimal ezobCdwx = (BigDecimal) this.getModel().getValue("ezob_cdwx");
                    if (ezobCdwx != null) {
                        DecimalFormat format = new DecimalFormat("#.##########");
                        String data = format.format(ezobCdwx);
                        if (!data.equals("0")) {
                            gg.append("L:").append(data);
                        }
                    }
                }
            }
        }
        //长度(纬向)单位
        if (this.getModel().getValue("ezob_cddw") != null) {
            String cddw = this.getModel().getValue("ezob_cddw").toString();
            gg.append(cddw).append(" ");
        }
        //长度(纬向)/inch
//        BigDecimal ezob_length = (BigDecimal) this.getModel().getValue("ezob_length");
//        if (ezob_length != null) {
//            DecimalFormat format = new DecimalFormat("#.##########");
//            String data = format.format(ezob_length);
//            if (!data.equals("0")) {
//                gg.append("L:").append(data).append("inch ");
//            }
//        }
        //ezob_cc
        String ezobCc = this.getModel().getValue("ezob_cc").toString();
        if (!Objects.equals(ezobCc, "")) {
            gg.append(ezobCc).append(" ");
        }
        //厚度/um
        BigDecimal ezobHd = (BigDecimal) this.getModel().getValue("ezob_hd");
        if (ezobHd != null) {
            DecimalFormat format = new DecimalFormat("#.##########");
            String data = format.format(ezobHd);
            if (!data.equals("0")) {
                gg.append("H:").append(data);
            }
        }
        //厚度单位
        if (this.getModel().getValue("ezob_hddw") != null) {
            String ezobHddw = this.getModel().getValue("ezob_hddw").toString();
            gg.append(ezobHddw).append(" ");
        }
        //覆盖膜油厚/um
        BigDecimal ezobFgm = (BigDecimal) this.getModel().getValue("ezob_fgm");
        if (ezobFgm != null) {
            DecimalFormat format = new DecimalFormat("#.##########");
            String data = format.format(ezobFgm);
            if (!data.equals("0")) {
                gg.append("SM:").append(data).append("um ");
            }
        }
        //类型
        String ezobLx = this.getModel().getValue("ezob_lx").toString();
        if (!Objects.equals(ezobLx, "")) {
            gg.append(ezobLx).append(" ");
        }

        //颜色
        DynamicObject ezobYs = (DynamicObject)this.getModel().getValue("ezob_ys");
        if (ezobYs != null) {
            String ezobYsname = ezobYs.get("name").toString();
            gg.append(ezobYsname).append(" ");
        }
        //含量/浓度
        String ezobHlnd = this.getModel().getValue("ezob_hlnd").toString();
        if (!Objects.equals(ezobHlnd, "")) {
            gg.append(ezobHlnd).append(" ");
        }
        //级别
        DynamicObject ezobLevel = (DynamicObject)this.getModel().getValue("ezob_level");
        if (ezobLevel != null) {
            String ezobLevelname = ezobLevel.get("name").toString();
            gg.append(ezobLevelname).append(" ");
        }
        //纯度
        String ezobCdu = this.getModel().getValue("ezob_cdu").toString();
        if (!Objects.equals(ezobCdu, "")) {
            gg.append(ezobCdu).append(" ");
        }

        //芯板厚度/mm
        BigDecimal ezobXbhd = (BigDecimal) this.getModel().getValue("ezob_xbhd");
        if (ezobXbhd != null) {
            DecimalFormat format = new DecimalFormat("#.##########");
            String data = format.format(ezobXbhd);
            if (!data.equals("0")) {
                gg.append(data);
            }
        }
        //芯板厚度单位
        if (this.getModel().getValue("ezob_xbhddw") != null) {
            String xbhddw = this.getModel().getValue("ezob_xbhddw").toString();
            gg.append(xbhddw).append(" ");
        }
        //芯板厚度/mil
//        BigDecimal ezob_xbhdml = (BigDecimal) this.getModel().getValue("ezob_xbhdml");
//        if (ezob_xbhdml != null) {
//            DecimalFormat format = new DecimalFormat("#.##########");
//            String data = format.format(ezob_xbhdml);
//            if (!data.equals("0")) {
//                gg.append(data).append("mil ");
//            }
//        }
        //芯板厚度公差
        if (this.getModel().getValue("ezob_xbhddw") != null) {
            String ezobXbhddw = this.getModel().getValue("ezob_xbhddw").toString();
            String xbhdtbhd = this.getModel().getValue("ezob_tbhd").toString();
            if (ezobXbhddw.equals("mil") && !xbhdtbhd.isEmpty()) {
                String xbhdgc = this.getModel().getValue("ezob_xbhdgc").toString();
                if(!xbhdgc.isEmpty()){
                    double xbhd = Double.parseDouble(xbhdgc.replace("±",""))/0.0254;
                    gg.append("±"+String.format("%.1f", xbhd)).append(" ");
                }
            }if (ezobXbhddw.equals("inch") && !xbhdtbhd.isEmpty()) {
                String ezobXbhdgc = this.getModel().getValue("ezob_xbhdgc").toString();
                if(!ezobXbhdgc.isEmpty()){
                    double xbhd = Double.parseDouble(ezobXbhdgc.replace("±",""))/0.0254/1000;
                    gg.append("±"+String.format("%.4f", xbhd)).append(" ");
                }
            }else if (ezobXbhddw.equals("mm") && !xbhdtbhd.isEmpty()){
                String ezobxbhdgc = this.getModel().getValue("ezob_xbhdgc").toString();
                if (!Objects.equals(ezobxbhdgc, "")) {
                    gg.append(ezobxbhdgc).append(" ");
                }
            }
        }
        //芯板厚度是否含铜
        String ezobXbhdsfht = this.getModel().getValue("ezob_xbhdsfht").toString();
        if (ezobXbhdsfht.equals("true")) {
            gg.append("含铜").append(" ");
        }
        //PI品牌
        DynamicObject ezobPipp = (DynamicObject)this.getModel().getValue("ezob_pipp");
        if (ezobPipp != null) {
            String ezobPippname = ezobPipp.get("name").toString();
            gg.append(ezobPippname).append(" ");
        }
        //单面/双面
        DynamicObject ezobDsm = (DynamicObject)this.getModel().getValue("ezob_dsm");
        if (ezobDsm != null) {
            String ezobDsmname = ezobDsm.get("name").toString();
            gg.append(ezobDsmname).append(" ");
        }
        //压延/电解
        DynamicObject ezobYydj = (DynamicObject)this.getModel().getValue("ezob_yydj");
        if (ezobYydj != null) {
            String ezobYydjname = ezobYydj.get("name").toString();
            gg.append(ezobYydjname).append(" ");
        }
        //铜箔品牌
        DynamicObject ezobTbpp = (DynamicObject)this.getModel().getValue("ezob_tbpp");
        if (ezobTbpp != null) {
            String ezobTbppname = ezobTbpp.get("name").toString();
            gg.append(ezobTbppname).append(" ");
        }
        //铜箔型号
        DynamicObject ezobTbxh = (DynamicObject)this.getModel().getValue("ezob_tbxh");
        if (ezobTbxh != null) {
            String ezobTbxhname = ezobTbxh.get("name").toString();
            gg.append(ezobTbxhname).append(" ");
        }
        //铜箔类型
        DynamicObject ezobTblx = (DynamicObject)this.getModel().getValue("ezob_tblx");
        if (ezobTblx != null) {
            String ezobTblxname = ezobTblx.get("name").toString();
            gg.append(ezobTblxname).append(" ");
        }
        //铜箔厚度(顶/底)/um
        String ezobTbhd = this.getModel().getValue("ezob_tbhd").toString();
        if (!ezobTbhd.isEmpty()) {
            gg.append("Cu:").append(ezobTbhd);
        }
        //铜箔厚度单位
        if (this.getModel().getValue("ezob_dbhddw") != null) {
            String ezobDbhddw = this.getModel().getValue("ezob_dbhddw").toString();
            gg.append(ezobDbhddw).append(" ");
        }
        //PI厚度/um
        BigDecimal ezobPihd = (BigDecimal) this.getModel().getValue("ezob_pihd");
        if (ezobPihd != null) {
            DecimalFormat format = new DecimalFormat("#.##########");
            String data = format.format(ezobPihd);
            if (!data.equals("0")) {
                gg.append("PI:").append(data).append("um ");
            }
        }
        //纯胶型号
        DynamicObject ezobCjxh = (DynamicObject)this.getModel().getValue("ezob_cjxh");
        if (ezobCjxh != null) {
            String ezobCjxhname = ezobCjxh.get("name").toString();
            gg.append(ezobCjxhname).append(" ");
        }
        //胶厚度/um
        BigDecimal ezobJhd = (BigDecimal) this.getModel().getValue("ezob_jhd");
        if (ezobJhd != null) {
            DecimalFormat format = new DecimalFormat("#.##########");
            String data = format.format(ezobJhd);
            if (!data.equals("0")) {
                gg.append("AD:").append(data).append("um ");
            }
        }
        //保护膜型号
        DynamicObject ezobBhmxh = (DynamicObject)this.getModel().getValue("ezob_bhmxh");
        if (ezobBhmxh != null) {
            String ezobBhmxhname = ezobBhmxh.get("name").toString();
            gg.append(ezobBhmxhname).append(" ");
        }
        //保护膜厚度/um
        BigDecimal ezobBhmhd = (BigDecimal) this.getModel().getValue("ezob_bhmhd");
        if (ezobBhmhd != null) {
            DecimalFormat format = new DecimalFormat("#.##########");
            String data = format.format(ezobBhmhd);
            if (!data.equals("0")) {
                gg.append("保护膜:").append(data).append("um ");
            }
        }
        //叠构
        DynamicObject ezobDg = (DynamicObject)this.getModel().getValue("ezob_dg");
        if (ezobDg != null) {
            String ezobDgname = ezobDg.get("name").toString();
            gg.append(ezobDgname).append(" ");
        }
        //物料名称
        String ezobWlmc = this.getModel().getValue("ezob_wlmc").toString();
        if (!Objects.equals(ezobWlmc, "")) {
            gg.append(ezobWlmc).append(" ");
        }
        //供应商型号
        String ezobGysxh = this.getModel().getValue("ezob_gysxh").toString();
        if (!Objects.equals(ezobGysxh, "")) {
            gg.append(ezobGysxh).append(" ");
        }
        //原材型号规格
        String ezobYcxhgg = this.getModel().getValue("ezob_ycxhgg").toString();
        if (!Objects.equals(ezobYcxhgg, "")) {
            gg.append(ezobYcxhgg).append(" ");
        }
        //材质
        String ezobCz = this.getModel().getValue("ezob_cz").toString();
        if (!Objects.equals(ezobCz, "")) {
            gg.append(ezobCz).append(" ");
        }
        //色块
        String ezobSk = this.getModel().getValue("ezob_sk").toString();
        if (!Objects.equals(ezobSk, "")) {
            gg.append(ezobSk).append(" ");
        }
        //亮度
        String ezobLight = this.getModel().getValue("ezob_light").toString();
        if (!Objects.equals(ezobLight, "")) {
            gg.append(ezobLight).append(" ");
        }
        //电阻值
        String ezobDzz = this.getModel().getValue("ezob_dzz").toString();
        if (!Objects.equals(ezobDzz, "")) {
            gg.append(ezobDzz).append(" ");
        }
        //盘数
        String ezobPs = this.getModel().getValue("ezob_ps").toString();
        if (!Objects.equals(ezobPs, "")) {
            gg.append(ezobPs).append(" ");
        }
        //物料要求
        String ezobWlyq = this.getModel().getValue("ezob_wlyq").toString();
        if (!Objects.equals(ezobWlyq, "")) {
            gg.append(ezobWlyq).append(" ");
        }
        //流胶度
        DynamicObject ezobLjd = (DynamicObject)this.getModel().getValue("ezob_ljd");
        if (ezobLjd != null) {
            String ezobLjdname = ezobLjd.get("name").toString();
            gg.append(ezobLjdname).append(" ");
        }

        //Type
        DynamicObject ezobType = (DynamicObject)this.getModel().getValue("ezob_type");
        if (ezobType != null) {
            String ezobTypename = ezobType.get("name").toString();
            gg.append(ezobTypename).append(" ");
        }
        //玻布产地
        DynamicObject ezobBbcd = (DynamicObject)this.getModel().getValue("ezob_bbcd");
        if (ezobBbcd != null) {
            String ezobBbcdname = ezobBbcd.get("name").toString();
            gg.append(ezobBbcdname).append("");
        }
        //玻布类别
        DynamicObject ezobBblb = (DynamicObject)this.getModel().getValue("ezob_bblb");
        if (ezobBblb != null) {
            String ezobBblbname = ezobBblb.get("name").toString();
            gg.append(ezobBblbname).append(" ");
        }
        //主剂单重/kg
        BigDecimal ezobZjdz = (BigDecimal) this.getModel().getValue("ezob_zjdz");
        if (ezobZjdz != null) {
            DecimalFormat format = new DecimalFormat("#.##########");
            String data = format.format(ezobZjdz);
            if (!data.equals("0")) {
                gg.append(data).append("KG ");
            }
        }
        //硬化剂型号 硬化剂单重/kg
        DynamicObject ezobYhjxh = (DynamicObject)this.getModel().getValue("ezob_yhjxh");
        BigDecimal ezobYhjdz = (BigDecimal) this.getModel().getValue("ezob_yhjdz");
        if (ezobYhjxh != null || ezobYhjdz != null) {
            gg.append("(");
            if (ezobYhjxh != null) {
                String ezobYhjxhname = ezobYhjxh.get("name").toString();
                gg.append(ezobYhjxhname).append(" ");
            }
            if (ezobYhjdz != null) {
                DecimalFormat format = new DecimalFormat("#.##########");
                String data = format.format(ezobYhjdz);
                if (!data.equals("0")) {
                    gg.append(data).append("KG");
                }
            }
            gg.append(")");
        }
        //产地
        DynamicObject ezobCd = (DynamicObject)this.getModel().getValue("ezob_cd");
        if (ezobCd != null) {
            String ezobCdname = ezobCd.get("name").toString();
            gg.append(ezobCdname).append(" ");
        }
        //丝网T数
        String ezobSwts = this.getModel().getValue("ezob_swts").toString();
        if (!ezobSwts.isEmpty()) {
            gg.append(ezobSwts).append(" ");
        }
        //硬度
        String ezobYd = this.getModel().getValue("ezob_yd").toString();
        if (!Objects.equals(ezobYd, "")) {
            gg.append(ezobYd).append(" ");
        }
        //模具分类
        String ezobMjfl = this.getModel().getValue("ezob_mjfl").toString();
        if (!Objects.equals(ezobMjfl, "")) {
            gg.append(ezobMjfl).append(" ");
        }
        //夹具类型
        String ezobJjlx = this.getModel().getValue("ezob_jjlx").toString();
        if (!Objects.equals(ezobJjlx, "")) {
            gg.append(ezobJjlx).append(" ");
        }
        //钢网类型
        String ezobGwlx = this.getModel().getValue("ezob_gwlx").toString();
        if (!Objects.equals(ezobGwlx, "")) {
            gg.append(ezobGwlx).append(" ");
        }
        //治具类型
        String ezobZjlx = this.getModel().getValue("ezob_zjlx").toString();
        if (!Objects.equals(ezobZjlx, "")) {
            gg.append(ezobZjlx).append(" ");
        }
        //涨缩系数X
        String ezobZsxsx = this.getModel().getValue("ezob_zsxsx").toString();
        if (!Objects.equals(ezobZsxsx, "")) {
            gg.append("X:").append(ezobZsxsx).append(" ");
        }
        //涨缩系数Y
        String ezobZsxsy = this.getModel().getValue("ezob_zsxsy").toString();
        if (!Objects.equals(ezobZsxsy, "")) {
            gg.append("Y:").append(ezobZsxsy).append(" ");
        }
        //配比/腔数
        String ezobPbqs = this.getModel().getValue("ezob_pbqs").toString();
        if (!Objects.equals(ezobPbqs, "")) {
            gg.append(ezobPbqs).append(" ");
        }
        //片料/卷料
        DynamicObject ezobPljl = (DynamicObject)this.getModel().getValue("ezob_pljl");
        if (ezobPljl != null) {
            String ezobPljlname = ezobPljl.get("name").toString();
            gg.append(ezobPljlname).append(" ");
        }
        //图纸日期
        String ezobTzrq = this.getModel().getValue("ezob_tzrq") == null ? "" : this.getModel().getValue("ezob_tzrq").toString();
        if (!Objects.equals(ezobTzrq, "")) {
            DateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = inputFormat.parse(ezobTzrq);
                ezobTzrq = outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            gg.append(ezobTzrq).append(" ");
        }
        //片料/卷料
        DynamicObject ezobYj = (DynamicObject)this.getModel().getValue("ezob_yj");
        if (ezobYj != null) {
            String ezobYjname = ezobYj.get("name").toString();
            gg.append(ezobYjname).append(" ");
        }
        //表面处理方式
        DynamicObject ezobBmclfs = (DynamicObject)this.getModel().getValue("ezob_bmclfs");
        if (ezobBmclfs != null) {
            String ezobBmclfsname = ezobBmclfs.get("name").toString();
            gg.append(ezobBmclfsname).append(" ");
        }
        //包装
        String ezobBz = this.getModel().getValue("ezob_bz").toString();
        if (!Objects.equals(ezobBz, "")) {
            gg.append(ezobBz).append(" ");
        }
        //酸碱类型
        DynamicObject ezobSjlx = (DynamicObject)this.getModel().getValue("ezob_sjlx");
        if (ezobSjlx != null) {
            String ezobSjlxname = ezobSjlx.get("name").toString();
            gg.append(ezobSjlxname).append(" ");
        }
        //钻嘴种类
        DynamicObject ezobZzzl = (DynamicObject)this.getModel().getValue("ezob_zzzl");
        if (ezobZzzl != null) {
            String ezobZzzlname = ezobZzzl.get("name").toString();
            gg.append(ezobZzzlname).append(" ");
        }
        //铣刀种类
        DynamicObject ezobXdzl = (DynamicObject)this.getModel().getValue("ezob_xdzl");
        if (ezobXdzl != null) {
            String ezobXdzlname = ezobXdzl.get("name").toString();
            gg.append(ezobXdzlname).append(" ");
        }
        //刀径/mm
        BigDecimal ezobDj = (BigDecimal) this.getModel().getValue("ezob_dj");
        if (ezobDj != null) {
            DecimalFormat format = new DecimalFormat("#.##########");
            String data = format.format(ezobDj);
            if (!data.equals("0")) {
                gg.append("Ф").append(data).append("mm ");
            }
        }
        //长度(刃长)/mm
        BigDecimal ezobCdrc = (BigDecimal) this.getModel().getValue("ezob_cdrc");
        if (ezobCdrc != null) {
            DecimalFormat format = new DecimalFormat("#.##########");
            String data = format.format(ezobCdrc);
            if (!data.equals("0")) {
                gg.append(data).append("mm ");
            }
        }
        //刀柄直径/mm
        BigDecimal ezobDbzj = (BigDecimal) this.getModel().getValue("ezob_dbzj");
        if (ezobDbzj != null) {
            DecimalFormat format = new DecimalFormat("#.##########");
            String data = format.format(ezobDbzj);
            if (!data.equals("0")) {
                gg.append(data).append("mm ");
            }
        }
        //钻尖角类型
        DynamicObject ezobZzjlx = (DynamicObject)this.getModel().getValue("ezob_zzjlx");
        if (ezobZzjlx != null) {
            String ezobZzjlxname = ezobZzjlx.get("name").toString();
            gg.append(ezobZzjlxname).append(" ");
        }
        //刀刃类型
        DynamicObject ezobDrlx = (DynamicObject)this.getModel().getValue("ezob_drlx");
        if (ezobDrlx != null) {
            String ezobDrlxname = ezobDrlx.get("name").toString();
            gg.append(ezobDrlxname).append(" ");
        }
        //刀刃类型
        DynamicObject ezobHtys = (DynamicObject)this.getModel().getValue("ezob_htys");
        if (ezobHtys != null) {
            String ezobHtysname = ezobHtys.get("name").toString();
            gg.append(ezobHtysname).append(" ");
        }
        //基重
        String ezobJz = this.getModel().getValue("ezob_jz").toString();
        if (!Objects.equals(ezobJz, "")) {
            gg.append(ezobJz).append(" ");
        }
        //直径/mm
        BigDecimal ezobZj = (BigDecimal) this.getModel().getValue("ezob_zj");
        if (ezobZj != null) {
            DecimalFormat format = new DecimalFormat("#.##########");
            String data = format.format(ezobZj);
            if (!data.equals("0")) {
                gg.append(data).append("mm ");
            }
        }
        //MPQ
        String ezobMpqq = this.getModel().getValue("ezob_mpqq").toString();
        if (!Objects.equals(ezobMpqq, "")) {
            gg.append("MPQ:").append(ezobMpqq).append(" ");
        }
        //厚度代码
        DynamicObject ezobTncode = (DynamicObject)this.getModel().getValue("ezob_tncode");
        if (ezobTncode != null) {
            String ezobTncodename = ezobTncode.get("name").toString();
            gg.append(ezobTncodename).append(" ");
        }
        //回收材料
        DynamicObject ezobSyhscl = (DynamicObject)this.getModel().getValue("ezob_syhscl");
        if (ezobSyhscl != null) {
            String ezobSyhsclname = ezobSyhscl.get("name").toString();
            gg.append(ezobSyhsclname).append(" ");
        }
        //规格
        this.getModel().setValue("ezob_gg", gg.toString().trim().replaceAll("     ", " ").replaceAll("    ", " ").replaceAll("   ", " ").replaceAll("  ", " "));
    }


}