package kd.cosmic.Test;

import kd.bos.dataentity.resource.ResManager;
import kd.bos.exception.ErrorCode;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.workflow.engine.WfConfigurationUtil;
import kd.bos.workflow.engine.msg.AbstractMessageServiceHandler;
import kd.bos.workflow.engine.msg.ctx.MessageContext;
import kd.bos.workflow.engine.msg.info.MessageInfo;
import kd.bos.workflow.engine.msg.info.ToDoInfo;
import kd.bos.workflow.engine.msg.model.yzj.YzjToDoState;
import kd.bos.workflow.exception.WFMessageServiceException;


/**
 *  author：易佳伟
 *  createDate : 2023/05/07
 *  description: 继承蓝凌OA待办的插件，插件放在消息渠道中配置（测试环境使用）
 *  关键客户：马衍浩
 */
public class LanglingServiceHandler extends AbstractMessageServiceHandler {
    private static Log logger = LogFactory.getLog(LanglingServiceHandler.class);

    public LanglingServiceHandler() {
    }





    private static final String CHANNEL_TYPE ="LangLing";

    @Override
    public void createToDo(MessageContext ctx, ToDoInfo toDoInfo) {

        logger.error("蓝凌待办测试任务createToDo:"+WfConfigurationUtil.isEnabled(CHANNEL_TYPE));


            try{
                LanglingOATaskUtilsTest.pushTodo(toDoInfo,ctx);
            }catch (Exception e)
            {
                throw new WFMessageServiceException(e,new ErrorCode("bos.wf.msg.LanglingnCreatTodoError","蓝凌创建待办任务失败"),e.getMessage());
            }


    }

    @Override
    public void dealToDo(MessageContext messageContext, ToDoInfo toDoInfo) {

            try{
                LanglingOATaskUtilsTest.pushTodoDone(toDoInfo,messageContext);
            }catch (Exception e)
            {
                throw new WFMessageServiceException(e,new ErrorCode("bos.wf.msg.LanglingnsetTodoDoneError","蓝凌设为已办任务失败"),e.getMessage());
            }



    }

    @Override
    public void deleteToDo(MessageContext messageContext, ToDoInfo toDoInfo) {

            try{
                LanglingOATaskUtilsTest.pushDelTodo(toDoInfo,messageContext);
            }catch (Exception e)
            {
                throw new WFMessageServiceException(e,new ErrorCode("bos.wf.msg.LanglingnsetdelTodoError","蓝凌删除待办任务失败"),e.getMessage());
            }



    }

    @Override
    public void sendMessage(MessageContext ctx, MessageInfo message) {
        super.sendMessage(ctx, message);
    }

    @Override
    public void checkTodo(MessageContext ctx, ToDoInfo info) {

        super.checkTodo(ctx, info);
    }

    private void logLlError(Exception e, int times, YzjToDoState todostate) {
        StringBuilder errorInfo = new StringBuilder();
        if (1 == times) {
            errorInfo.append(ResManager.loadKDString("[第一次重试]", "LanglingServiceHandler_0", "bos-wf-engine", new Object[0]));
        } else if (2 == times) {
            errorInfo.append(ResManager.loadKDString("[第二次重试]", "LanglingServiceHandler_1", "bos-wf-engine", new Object[0]));
        } else if (3 == times) {
            errorInfo.append(ResManager.loadKDString("[第三次重试]", "LanglingServiceHandler_2", "bos-wf-engine", new Object[0]));
        }

        if (YzjToDoState.DELETE == todostate) {
            errorInfo.append(ResManager.loadKDString("删除蓝凌待办失败!", "LanglingServiceHandler_3", "bos-wf-engine", new Object[0]));
        } else {
            errorInfo.append(ResManager.loadKDString("蓝凌更改为已办状态时失败!", "LanglingServiceHandler_4", "bos-wf-engine", new Object[0]));
        }

        errorInfo.append(e.getMessage());
        logger.info(errorInfo.toString());
    }

}
