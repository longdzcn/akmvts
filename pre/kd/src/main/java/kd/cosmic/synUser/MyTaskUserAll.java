package kd.cosmic.synUser;


import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import kd.bos.context.RequestContext;
import kd.bos.exception.KDBizException;
import kd.bos.exception.KDException;
import kd.bos.schedule.executor.AbstractTask;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述: 全量点击同步
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：马衍浩
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */

//257 378
public class MyTaskUserAll extends AbstractTask {
    private static final Log log = LogFactory.getLog(MyTaskUser.class);


    @Override
    public void execute(RequestContext rc, Map<String, Object> params) throws KDException {
        // 任务开始，输出当前进度及提示
        // 从输入参数中，获取输入的循环次数 (默认20次)
        int time = 1;
        if (params.containsKey("time")) {
            time = (int)params.get("time");
        }
        if (time > 100 || time <= 0) {
            throw new KDBizException(String.format("设置的次数不合理，合理范围是[1,100]", time));
        }
        try {
            int progress = 0;
            for (int i = 0; i < time; i++) {
                // 反馈进度
                String desc = String.format("开始进行第  %s / %s 次循环", i+1, time);
                LocalDateTime currentDate =LocalDateTime.now();
//                String  url="https://api15.sapsf.cn/odata/v2/EmpJob?$select=employmentNav/personNav/personIdExternal,employmentNav/personNav/customString1,employmentNav/personNav/personalInfoNav/firstName,userNav/username,userNav/cellPhone,userNav/email,costCenter,employeeClassNav/externalCode,employeeClassNav/localeLabel,managerId,managerUserNav/firstName,customString1,customString1Nav/externalName_defaultValue,customString2,customString2Nav/externalName_defaultValue,companyNav/externalCode,businessUnit,businessUnitNav/name_defaultValue,division,divisionNav/name_defaultValue,department,departmentNav/name_defaultValue,customString3,customString3Nav/externalName_defaultValue,customString4,customString4Nav/externalName_defaultValue,customString15,customString15Nav/externalName_defaultValue,customString18Nav/externalCode,customString19Nav/externalCode,positionNav/externalName_defaultValue,employmentNav/startDate,emplStatusNav/externalCode,employmentNav/isPrimary&$format=json&$expand=employmentNav/personNav,employmentNav,userNav,emplStatusNav,employmentNav/personNav/personalInfoNav,employeeClassNav,managerUserNav,companyNav,customString1Nav,customString2Nav,businessUnitNav,divisionNav,departmentNav,customString3Nav,customString4Nav,customString15Nav,customString18Nav,customString19Nav,positionNav&$filter=(employmentNav/isPrimary eq false or employmentNav/isPrimary eq null) and lastModifiedDateTime ge datetime'2022-06-11T00:00:01' and userId eq 'E0000009'";
                String  url="https://api15.sapsf.cn/odata/v2/EmpJob?$select=employmentNav/personNav/personIdExternal,employmentNav/personNav/customString1,employmentNav/personNav/personalInfoNav/firstName,userNav/username,userNav/cellPhone,userNav/email,costCenter,employeeClassNav/externalCode,employeeClassNav/localeLabel,managerId,managerUserNav/firstName,customString1,customString1Nav/externalName_defaultValue,customString2,customString2Nav/externalName_defaultValue,companyNav/externalCode,businessUnit,businessUnitNav/name_defaultValue,division,divisionNav/name_defaultValue,department,departmentNav/name_defaultValue,customString3,customString3Nav/externalName_defaultValue,customString4,customString4Nav/externalName_defaultValue,customString15,customString15Nav/externalName_defaultValue,customString18Nav/externalCode,customString19Nav/externalCode,positionNav/externalName_defaultValue,employmentNav/startDate,emplStatusNav/externalCode,employmentNav/isPrimary&$format=json&$expand=employmentNav/personNav,employmentNav,userNav,emplStatusNav,employmentNav/personNav/personalInfoNav,employeeClassNav,managerUserNav,companyNav,customString1Nav,customString2Nav,businessUnitNav,divisionNav,departmentNav,customString3Nav,customString4Nav,customString15Nav,customString18Nav,customString19Nav,positionNav&$filter=(employmentNav/isPrimary eq false or employmentNav/isPrimary eq null) and lastModifiedDateTime ge datetime'2020-01-01T00:00:01'";

                SynMthod.getmess(url);   //同步人员和报销级别
                LocalDateTime endDate =LocalDateTime.now();
                log.info("Mytask全量同步开始到结束时间:"+currentDate+"~"+endDate);
                progress = (100 * i) / time;
                feedbackProgress(progress, desc, null);
                // 判断前端是否下达了终止任务的指令
                if (isStop()) {
                    stop();
                }
                // 执行业务逻辑，此处假设需要耗时500ms，暂停5000ms
                Thread.sleep(5);
            }
        } catch (InterruptedException e) {
            // 输出monitor日志
            log.error(e.toString());
        }
        // 任务执行完毕，生成执行结果输出
        HashMap<String, Object> result = new HashMap<>();
        result.put("success", "true");
        // 输出定制结果
        feedbackCustomdata(result);
    }




}
