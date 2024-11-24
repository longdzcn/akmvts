package kd.cosmic.synUser;

import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.CloseCallBack;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.schedule.api.JobInfo;
import kd.bos.schedule.api.JobType;
import kd.bos.schedule.api.TaskInfo;
import kd.bos.schedule.form.JobForm;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 描述: 任务发布界面插件
 * 演示如何发起一个任务，并显示任务进度界面，捕获任务结束事件
 * 通过点击人员列表的同步人员按钮，会读取Myhr人员接口，全量新增或更新人员信息
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：马衍浩
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

/**
 *  kd.cosmic.synUser.JobDispatchFormPlugin  *12月份新版本
 */
public class JobDispatchFormPlugin extends AbstractListPlugin implements Plugin {
    private static final String KEY_JOBDISPATCH = "ezob_baritemap";
    /**
     * 添加事件监听："启动任务"按钮点击事件
     */
//    @Override
//    public void registerListener(EventObject e) {
//        super.registerListener(e);
//        this.addClickListeners(KEY_JOBDISPATCH);
//    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(KEY_JOBDISPATCH);
    }
    /**
     * 事件处理："启动任务"按钮点击事件
     */
   // @Override
//    public void click(EventObject evt) {
//        Control source = (Control)evt.getSource();
//        if (StringUtils.equalsIgnoreCase(KEY_JOBDISPATCH, source.getKey())) {
//            this.dispatch();
//        }
//    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        if (StringUtils.equals(evt.getItemKey(), KEY_JOBDISPATCH)) {
            this.dispatch();
        }
    }


    /**
     * 回调事件，在任务处理完毕后继续后续处理
     */
    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        if (StringUtils.equals(closedCallBackEvent.getActionId(), "taskcloseback")) {
            this.taskCallBack(closedCallBackEvent.getReturnData());
        }
    }



    /**
     * 创建任务目标，发布新任务
     */
    private void dispatch() {
        // 创建任务目标
        JobInfo jobInfo = new JobInfo();
        jobInfo.setAppId("bos");                // 执行类所在的应用名
        jobInfo.setJobType(JobType.REALTIME);   // 即时执行
        jobInfo.setName("test job");
        jobInfo.setId(UUID.randomUUID().toString());        // 随机产生一个JobId (任务目标的标识)
        jobInfo.setTaskClassname("kd.cosmic.synUser.MyTaskUserAll");

        // 自定义参数
        Map<String,Object> params = new HashMap<>();
        params.put("time", 1);         // 自定义参数，示例任务执行循环次数，80次
        jobInfo.setParams(params);
        // 回调参数，设置一个回调处理标识(actionId)
        CloseCallBack closeCallBack = new CloseCallBack(this, "taskcloseback");
        // 发布任务，并显示进度
        JobForm.dispatch(jobInfo, this.getView(), closeCallBack);
    }
    /**
     * 任务完成后的回调处理
     *
     * @param returnData
     */
    private void taskCallBack(Object returnData) {
        if (returnData == null) {
            return;
        }
        if (returnData instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>)returnData;
            if (result.containsKey("taskinfo")) {
                String taskInfoStr = (String)result.get("taskinfo");
                if (StringUtils.isNotBlank(taskInfoStr)) {
                    TaskInfo taskInfo = SerializationUtils.fromJsonString(taskInfoStr, TaskInfo.class);
                    if (taskInfo.isTaskEnd()) {
                        // 获取任务执行完毕，生成的内容
                        String data = taskInfo.getData();
                        this.getView().showMessage(data);
                    }
                }
            }
        }
    }














}