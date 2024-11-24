package kd.cosmic;

import com.alibaba.druid.util.StringUtils;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.BeforeClickEvent;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.sdk.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.Locale;

/**
 * 描述: 推荐时数
 * 开发者: 易佳伟
 * 创建日期:
 * 关键客户：
 * 已部署正式：ture
 * 备注：已投入正式环境使用，无问题
 */
public class recommendE extends AbstractBillPlugIn implements Plugin {

    private final static String KEY_BARITEM = "ezob_btnok";


    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners(KEY_BARITEM);
        // 按钮点击
        //Button button = this.getView().getControl(KEY_BUTTON1);
        // button.addClickListener(this);
    }

    @Override
    public void beforeClick(BeforeClickEvent evt) {
        super.beforeClick(evt);
        Control source = (Control) evt.getSource();
        if (StringUtils.equals(source.getKey(), KEY_BARITEM)) {
            try {


                String aUSERID= UserServiceHelper.getCurrentUser("number").get("number").toString();
                //String USER_ID="E0001979";
                Date day=new Date();
                SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String aSTARTTIME=sdf.format(day);
                String  inputDateS =this.getModel().getValue("startdate").toString();
                String inputDateE = this.getModel().getValue("enddate").toString();
                // String inputTimeS = this.getModel().getValue("ezob_kssj").toString();
                //  String inputTimeE = this.getModel().getValue("ezob_jssj").toString();
                 /*long timestamp = Long.parseLong(inputDateS);
                Date dateS = new Date(timestamp);
                timestamp = Long.parseLong(inputDateE);
                Date dateE = new Date(timestamp);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String outputDateS = dateFormat.format(dateS);
                String outputDateE = dateFormat.format(dateE);*/
                String outputDateS = recommendE.gettime(inputDateS);         //sdf.format(inputDateS);
                String outputDateE = recommendE.gettime(inputDateE);
                //String outputDateS ="2023-07-16";
                // String outputDateE ="2023-07-20";
                String inputTime=  formatSecondsToHHmmss(Integer.parseInt(this.getModel().getValue("ezob_kssj").toString()));
                String inputTimeE=  formatSecondsToHHmmss(Integer.parseInt(this.getModel().getValue("ezob_jssj").toString()));
                String aSTime=outputDateS+" "+inputTime;
                String aETime=outputDateE+" "+inputTimeE;
                String aShowT=   getMsg(aUSERID,aSTime,aETime);
                this.getView().showMessage(aShowT);
                //textEdit.setText(ShowT);
                //textEdit.setText();
                // this.View.Model.SetValue("ezob_textfield", 默认值);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static String gettime(String time1)
    {


        //格式化CST时间
        String time2 ="";
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        //CST时间转字符串
        String dateStr = time1;
        //CST时间字符串转Date类型
        try{Date date = (Date) sdf.parse(dateStr);
            String formatStr = new SimpleDateFormat("yyyy-MM-dd").format(date);
            time2=formatStr;
            //System.out.println(formatStr);
        }  catch (Exception e){
            e.printStackTrace();
        }

        return time2;
    }
    private  String formatSecondsToHHmmss(int seconds) {
        int hours = seconds / 3600; // 计算小时数
        int minutes = (seconds % 3600) / 60; // 计算分钟数
        int remainingSeconds = seconds % 60; // 计算剩余的秒数

        // 使用String.format()方法将时间格式化为HH:mm:ss格式
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }
    public  String getMsg(String userId, String sTime, String eTime) {
        return   verficationHR.post(userId,sTime,eTime);

    }




/*
    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);

        Control source = (Control) evt.getSource();


        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {

            this.getView().showMessage("显示完成2 ");
        }
   }
   */




}