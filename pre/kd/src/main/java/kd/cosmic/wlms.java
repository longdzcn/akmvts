package kd.cosmic;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;

/**
 * 描述: 物料描述构成=物料名称+型号+规格
 * 开发者: 李四辉
 * 创建日期:2024-04-01
 * 关键客户：仓库
 * 已部署正式：true
 * 备注：已投入正式环境使用，无问题
 */

public class wlms extends AbstractFormPlugin implements Plugin {

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String key = e.getProperty().getName();
        switch (key) {
            case "name":
                this.changed();
                break;
            case "modelnum":
                this.changed();
                break;
            case "ezob_gg":
                this.changed();
                break;
            case "ezob_wlname":
                this.changed();
                break;
            case "ezob_kings":
                this.changed();
                break;
            case "ezob_wlfl":
                //更新物料分组，清空字段
                DynamicObject fl = (DynamicObject) this.getModel().getValue("ezob_wlfl");
                if(fl != null){
                    Long masterid = fl.getLong("masterid");
                    this.getModel().setValue("groupid", masterid);
                }else{
                    this.getModel().setValue("groupid", null);
                }
                this.clean();
                break;
        }
    }

//拼接物料描述
    public void changed() {
        //物料规格
        String ezobGg = this.getModel().getValue("ezob_gg").toString();
        //供应商
        String ezobGys = this.getModel().getValue("ezob_gys").toString();
        //保税/非保税/客供
        String ezobBs = "";
        String bsvalue = "";
        if(this.getModel().getValue("ezob_bsykgldbs") != null){
            ezobBs = this.getModel().getValue("ezob_bsykgldbs").toString();
        }
        if(ezobBs.equals("0")){
            bsvalue = "";
        }else if(ezobBs.equals("1")){
            bsvalue = "保税";
        }else if(ezobBs.equals("2")){
            bsvalue = "客供";
        }else if(ezobBs.equals( "4")){
            bsvalue = "客供(样品)";
        }
        DynamicObject ezobWlfl = (DynamicObject)this.getModel().getValue("ezob_wlfl");
        if(ezobWlfl != null){
            String ezobWlfznumber = ezobWlfl.get("number").toString();
            int wlfzlength = ezobWlfznumber.length();//获取字符串长度防止超出下标
            //这两类分组才需要取物料参数库型号参数库值拼接
            if(wlfzlength >= 5){
                if(ezobWlfznumber.substring(0, 2).equals("M1") || ezobWlfznumber.substring(0, 5).equals("A1.02")){
                    StringBuilder ms = new StringBuilder();//描述
                    //供应商
                    if(!ezobGys.equals("")){
                        ms.append(ezobGys).append(" ");
                    }
                    //名称
                    DynamicObject ezobWlname = (DynamicObject)this.getModel().getValue("ezob_wlname");
                    if (ezobWlname != null) {
                        String ezobWlnamename = ezobWlname.get("name").toString();
                        ms.append(ezobWlnamename).append(" ");
                    }
                    //保税/非保税/客供
                    ms.append(bsvalue).append(" ");
                    //型号
                    DynamicObject ezobKings = (DynamicObject)this.getModel().getValue("ezob_kings");
                    if (ezobKings != null) {
                        String ezobKingsname = ezobKings.get("name").toString();
                        ms.append(ezobKingsname).append(" ");
                    }
                    //规格
                    if(ezobGg != null){
                        this.getModel().setValue("ezob_wlms",(ms + " " + ezobGg).trim());
                    }else{
                        this.getModel().setValue("ezob_wlms",(ms + " ").trim());
                    }
                }else{
                    String name = this.getModel().getValue("name").toString();//物料名称
                    String modelnum = this.getModel().getValue("modelnum").toString();//物料型号
                    this.getModel().setValue("ezob_wlms", (ezobGys + " " + name + " " + bsvalue + " " + modelnum + " " + ezobGg).trim());
                }
            }
        }
    }

    public void clean() {
        this.getModel().setValue("ezob_wlname",null);//参数库名称
        this.getModel().setValue("ezob_kings",null);//参数库型号
        this.getModel().setValue("ezob_ppmake",null);//品牌(制造商)
        this.getModel().setValue("ezob_sbmc",null);//设备名称
        this.getModel().setValue("ezob_fcclgg",null);//FCCL规格
        this.getModel().setValue("ezob_ppgg",null);//P片规格
        this.getModel().setValue("ezob_khxlh",null);//客户系列号
        this.getModel().setValue("ezob_gcscxh",null);//工厂生产型号
        this.getModel().setValue("ezob_cb",null);//层别
        this.getModel().setValue("ezob_xlh",null);//序列号
        this.getModel().setValue("ezob_csdy",null);//测试单元
        this.getModel().setValue("ezob_csunit",null);//纯胶类型
        this.getModel().setValue("ezob_kdjx",null);//宽度(经向)/mm
        this.getModel().setValue("ezob_kdwidh",null);//宽度(经向)/inch
        this.getModel().setValue("ezob_cdwx",null);//长度(纬向)/mm
        this.getModel().setValue("ezob_length",null);//长度(纬向)/inch
        this.getModel().setValue("ezob_cc",null);//尺寸
        this.getModel().setValue("ezob_hd",null);//厚度/um
        this.getModel().setValue("ezob_fgm",null);//覆盖膜油厚/um
        this.getModel().setValue("ezob_lx",null);//类型
        this.getModel().setValue("ezob_ys",null);//颜色
        this.getModel().setValue("ezob_hlnd",null);//含量/浓度
        this.getModel().setValue("ezob_level",null);//级别
        this.getModel().setValue("ezob_cdu",null);//纯度
        this.getModel().setValue("ezob_tncode",null);//厚度代码
        this.getModel().setValue("ezob_xbhd",null);//芯板厚度/mm
        this.getModel().setValue("ezob_xbhdml",null);//芯板厚度/mil
        this.getModel().setValue("ezob_xbhdgc",null);//芯板厚度公差
        this.getModel().setValue("ezob_xbhdsfht",false);//芯板厚度是否含铜
        this.getModel().setValue("ezob_pipp",null);//PI品牌
        this.getModel().setValue("ezob_dsm",null);//单面/双面
        this.getModel().setValue("ezob_yydj",null);//压延/电解
        this.getModel().setValue("ezob_tbpp",null);//铜箔品牌
        this.getModel().setValue("ezob_tbxh",null);//铜箔型号
        this.getModel().setValue("ezob_tblx",null);//铜箔类型
        this.getModel().setValue("ezob_tbhd",null);//铜箔厚度(顶/底)/um
        this.getModel().setValue("ezob_pihd",null);//PI厚度/um
        this.getModel().setValue("ezob_cjxh",null);//纯胶型号
        this.getModel().setValue("ezob_jhd",null);//胶厚度/um
        this.getModel().setValue("ezob_bhmxh",null);//保护膜型号
        this.getModel().setValue("ezob_bhmhd",null);//保护膜厚度/um
        this.getModel().setValue("ezob_dg",null);//叠构
        this.getModel().setValue("ezob_wlmc",null);//物料名称
        this.getModel().setValue("ezob_gysxh",null);//供应商型号
        this.getModel().setValue("ezob_ycxhgg",null);//原材型号规格
        this.getModel().setValue("ezob_cz",null);//材质
        this.getModel().setValue("ezob_sk",null);//色块
        this.getModel().setValue("ezob_light",null);//亮度
        this.getModel().setValue("ezob_dzz",null);//电阻值
        this.getModel().setValue("ezob_ps",null);//盘数
        this.getModel().setValue("ezob_wlyq",null);//物料要求
        this.getModel().setValue("ezob_ljd",null);//流胶度
        this.getModel().setValue("ezob_hjl",null);//含胶量
        this.getModel().setValue("ezob_hjlgc",null);//含胶量公差
        this.getModel().setValue("ezob_type",null);//Type
        this.getModel().setValue("ezob_bblb",null);//玻布类别
        this.getModel().setValue("ezob_bbcd",null);//玻布产地
        this.getModel().setValue("ezob_zjdz",null);//主剂单重/kg
        this.getModel().setValue("ezob_yhjxh",null);//硬化剂型号
        this.getModel().setValue("ezob_yhjdz",null);//硬化剂单重/kg
        this.getModel().setValue("ezob_cd",null);//产地
        this.getModel().setValue("ezob_swts",null);//丝网T数
        this.getModel().setValue("ezob_yd",null);//硬度
        this.getModel().setValue("ezob_mjfl",null);//模具分类
        this.getModel().setValue("ezob_jjlx",null);//夹具类型
        this.getModel().setValue("ezob_gwlx",null);//钢网类型
        this.getModel().setValue("ezob_zjlx",null);//治具类型
        this.getModel().setValue("ezob_zsxsx",null);//涨缩系数X
        this.getModel().setValue("ezob_zsxsy",null);//涨缩系数Y
        this.getModel().setValue("ezob_pbqs",null);//配比/腔数
        this.getModel().setValue("ezob_pljl",null);//片料/卷料
        this.getModel().setValue("ezob_tzrq",null);//图纸日期
        this.getModel().setValue("ezob_yj",null);//圆角
        this.getModel().setValue("ezob_bmclfs",null);//表面处理方式
        this.getModel().setValue("ezob_bz",null);//包装
        this.getModel().setValue("ezob_sjlx",null);//酸碱类型
        this.getModel().setValue("ezob_zzzl",null);//钻嘴种类
        this.getModel().setValue("ezob_xdzl",null);//铣刀种类
        this.getModel().setValue("ezob_dj",null);//刀径/mm
        this.getModel().setValue("ezob_cdrc",null);//长度(刃长)/mm
        this.getModel().setValue("ezob_dbzj",null);//刀柄直径/mm
        this.getModel().setValue("ezob_zzjlx",null);//钻尖角类型
        this.getModel().setValue("ezob_drlx",null);//刀刃类型
        this.getModel().setValue("ezob_htys",null);//套环颜色
        this.getModel().setValue("ezob_jz",null);//基重
        this.getModel().setValue("ezob_zj",null);//直径/mm
        this.getModel().setValue("ezob_tzbb",null);//图纸版本
        this.getModel().setValue("ezob_ll",null);//频率
        this.getModel().setValue("ezob_dk",null);//DK
        this.getModel().setValue("ezob_df",null);//DF
        this.getModel().setValue("ezob_tg",null);//TG
        this.getModel().setValue("ezob_gg",null);//物料规格
        this.getModel().setValue("ezob_mpqq",null);//MPQ
    }
}