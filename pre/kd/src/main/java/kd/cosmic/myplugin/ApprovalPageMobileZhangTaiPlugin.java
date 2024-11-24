package kd.cosmic.myplugin;

import kd.bos.form.IClientViewProxy;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.workflow.taskcenter.plugin.ApprovalPageMobilePluginNew;

import java.util.HashMap;
import java.util.Map;

public class ApprovalPageMobileZhangTaiPlugin extends ApprovalPageMobilePluginNew {


    /**
     *  author：易佳伟
     *  createDate : 2023/05/07
     *  description: 用于解决移动端BUG审批完待办后，返回待办页面
     *  已部署到正式环境
     */

      @Override
      public void closedCallBack(ClosedCallBackEvent e) {

           String ss = System.getProperty("domain.contextUrl");

           String actionId = e.getActionId();
           if("btntransfer".equals(actionId) || "approvalBtn".equals(actionId)){
           //转交、同意or驳回操作，从成功页面回调后的情况 || 终止的情况
           String operation = (String) e.getReturnData();
           if(!"cancel".equals(operation)){
           IClientViewProxy proxy = this.getView().getService(IClientViewProxy.class);
           Map<String, String> mpURL = new HashMap<>(2);
           mpURL.put("url","https://kd.akmmv.com/ierp/mobile.html?form=er_mainpage_daily&formId=wf_mobilelist_mob"); // 重定向的页面地址，比如 https://www.yunzhijia.com/
           mpURL.put("noPushState", "true");
           proxy.addAction("openUrl", mpURL);
           }
            }else {
            super.closedCallBack(e);
            }
           }
      }