package kd.cosmic.synUser.theSecond;


import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.exception.KDBizException;
import kd.bos.exception.KDException;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.model.UserParam;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DBServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * kd.cosmic.synUser.theSecond.MyTaskUser  * 11月份老版本
 */
//257 378
public class MyTaskUser extends AbstractTask {
    private static final Log log = LogFactory.getLog(MyTaskUser.class);

    private static int ns =0;
    @Override
    public void execute(RequestContext rc, Map<String, Object> params) throws KDException {
        // 任务开始，输出当前进度及提示
        feedbackProgress(0, "已经进入任务执行环节，开始执行任务", null);
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
                String url ="https://api15.sapsf.cn/odata/v2/EmpJob?$select=employmentNav/personNav/personIdExternal,employmentNav/personNav/customString1,employmentNav/personNav/personalInfoNav/firstName,userNav/username,userNav/cellPhone,userNav/email,costCenter,employeeClassNav/externalCode,employeeClassNav/localeLabel,managerId,managerUserNav/firstName,customString1,customString1Nav/externalName_defaultValue,customString2,customString2Nav/externalName_defaultValue,companyNav/externalCode,businessUnit,businessUnitNav/name_defaultValue,division,divisionNav/name_defaultValue,department,departmentNav/name_defaultValue,customString3,customString3Nav/externalName_defaultValue,customString4,customString4Nav/externalName_defaultValue,customString15,customString15Nav/externalName_defaultValue,customString18Nav/externalCode,customString19Nav/externalCode,positionNav/externalName_defaultValue,employmentNav/startDate,emplStatusNav/externalCode,employmentNav/isPrimary&$format=json&$expand=employmentNav/personNav,employmentNav,userNav,emplStatusNav,employmentNav/personNav/personalInfoNav,employeeClassNav,managerUserNav,companyNav,customString1Nav,customString2Nav,businessUnitNav,divisionNav,departmentNav,customString3Nav,customString4Nav,customString15Nav,customString18Nav,customString19Nav,positionNav&$filter=(employmentNav/isPrimary eq false or employmentNav/isPrimary eq null) and lastModifiedDateTime ge datetime'2022-06-11T00:00:01'";
                //String  url="https://api15.sapsf.cn/odata/v2/EmpJob?$select=employmentNav/personNav/personIdExternal,employmentNav/personNav/customString1,employmentNav/personNav/personalInfoNav/firstName,userNav/username,userNav/cellPhone,userNav/email,costCenter,employeeClassNav/externalCode,employeeClassNav/localeLabel,managerId,managerUserNav/firstName,customString1,customString1Nav/externalName_defaultValue,customString2,customString2Nav/externalName_defaultValue,companyNav/externalCode,businessUnit,businessUnitNav/name_defaultValue,division,divisionNav/name_defaultValue,department,departmentNav/name_defaultValue,customString3,customString3Nav/externalName_defaultValue,customString4,customString4Nav/externalName_defaultValue,customString15,customString15Nav/externalName_defaultValue,customString18Nav/externalCode,customString19Nav/externalCode,positionNav/externalName_defaultValue,employmentNav/startDate,emplStatusNav/externalCode,employmentNav/isPrimary&$format=json&$expand=employmentNav/personNav,employmentNav,userNav,emplStatusNav,employmentNav/personNav/personalInfoNav,employeeClassNav,managerUserNav,companyNav,customString1Nav,customString2Nav,businessUnitNav,divisionNav,departmentNav,customString3Nav,customString4Nav,customString15Nav,customString18Nav,customString19Nav,positionNav&$filter=(employmentNav/isPrimary%20eq%20false%20or%20employmentNav/isPrimary%20eq%20null)%20and%20lastModifiedDateTime%20ge%20datetime%272022-06-11T00:00:01%27%20and%20userId%20eq%20%27E0009191%27";
                //getmess(url);   //同步人员和报销级别
                update();       //更新上级
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

    public static int itv=0;
    public static int jbv=0;
    public static void getmess(String url) {

        String uri = url;
        //提供的接口Params 参数用map组装起来
        Map<String, Object> map = new HashMap<>();

        //将用户名密码：我用API测试工具翻译出来的加密后的明明直接添加到请求头
        HttpRequest request = HttpRequest.get(url).header("Authorization", "Basic SmluRGllQVBJQGFrbW12OkppbkRpZTEyMw==");
        //发起请求
        String bodys = request.execute().body();
        //将json字符串转成JSONOBJECT对象，方便迭代对象判断人员是否已纯在
        JSONObject jsonObject = JSONUtil.parseObj(bodys);
        // System.out.println(jsonObject);
        String url3 = "";
        //循环所有数据
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(bodys);
            JsonNode resultsNode = rootNode.path("d").path("results");
            int length = resultsNode.size();

            for (int i = 0; i < length; i++) {      //length


                try {
                    itv++;
                    System.out.print("人员插入" + itv);

                    //查询用户是否存在
                    //UserServiceHelper.
                    //取员工工号
                    //UserServiceHelper.
                    //取员工工号
                    JsonNode personIdExternalNode = rootNode.path("d").path("results").get(i).path("employmentNav").path("personNav").path("personIdExternal");
                    String personIdExternal = personIdExternalNode.asText();

                    //取员旧工号
                    JsonNode customString1Node = rootNode.path("d").path("results").get(i).path("employmentNav").path("personNav").path("customString1");
                    String customString = customString1Node.asText();

                    //中文姓名
                    JsonNode firstNameNode = rootNode.path("d").path("results").get(i).path("employmentNav").path("personNav").path("personalInfoNav").path("results").get(0).path("firstName");
//                    JsonNode firstNameNode = rootNode.path("d").path("results").path(i).path("userNav").path("firstName");
                    String firstName = firstNameNode.asText();

                    int ns1000 =ns *1000+i;

                    System.out.println("Mytask:"+firstName+"人员插入:"+i);
                    log.info("Mytask:"+firstName+"人员插入:"+ns1000);



                    //用户名
                    //JsonNode usernameNode = rootNode.path("d").path("results").path(i).path("userNav").path("username");
                    //String username = usernameNode.asText();
                    //String entityName = "bos_user";

                    //手机
                    JsonNode userNavNode = rootNode.path("d").path("results").path(i).path("userNav");
                    String cellPhone = userNavNode.path("cellPhone").asText().trim();

                    //邮箱
                    JsonNode useremail = rootNode.path("d").path("results").path(i).path("userNav");
                    String email = useremail.path("email").asText();



                    //法人
                    String zz = rootNode.path("d").path("results").get(i).path("companyNav").path("externalCode").asText();
                    //运营中心
                    String yyzx =  rootNode.path("d").path("results").get(i).path("businessUnitNav").path("name_defaultValue").asText();

                    //成本中心
                    String costCenter = rootNode.path("d").path("results").get(i).path("costCenter").asText();

                    //人员类型编码
                    //JsonNode customString18NavNode = rootNode.path("d").path("results").get(i).path("customString18Nav");
                    //String externalCode = customString18NavNode.path("externalCode").asText();

                    //人员名称
                    JsonNode employeeClassNavNode = rootNode.path("d").path("results").get(i).path("employeeClassNav");
                    String localeLabel = employeeClassNavNode.path("localeLabel").asText();
                    String gz = employeeClassNavNode.path("externalCode").asText(); //工种

                    //上级工号uri
                    String managerId = rootNode.path("d").path("results").get(i).path("managerId").asText();

                    //上级姓名
                    //String managefirstName = rootNode.path("d").path("results").get(i).path("managerUserNav").path("firstName").asText();

                    //职位名称
                    JsonNode customString1NavNode = rootNode.path("d").path("results").get(i).path("positionNav");
                    String externalNamedefaultValue = customString1NavNode.path("externalName_defaultValue").asText();

                    //总公司
                    // String company = rootNode.path("d").path("results").get(i).path("customString1").asText();


                    //分公司
                    //String customString2 = rootNode.path("d").path("results").get(i).path("customString2").asText();

                    //业务单元
                    //String businessUnit = rootNode.path("d").path("results").get(i).path("businessUnit").asText();

                    //分部
                    String division = rootNode.path("d").path("results").get(i).path("division").asText();

                    //L1部门
                    String department = rootNode.path("d").path("results").get(i).path("department").asText();

                    //L2部门
                    //String customString3 = rootNode.path("d").path("results").get(i).path("customString3").asText();

                    //工序小组
                    //String customString4 = rootNode.path("d").path("results").get(i).path("customString4").asText();

                    //岗位
                    //String customString15 = rootNode.path("d").path("results").get(i).path("customString15").path("externalName_defaultValue").asText();

                    //职级
                    //String customString18 = rootNode.path("d").path("results").get(i).path("customString18Nav").path("externalCode").asText();

                    //级别
                    String customString19 = rootNode.path("d").path("results").get(i).path("customString19Nav").path("externalCode").asText();

                    //最早入职日期
                    String startDate = rootNode.path("d").path("results").get(i).path("employmentNav").path("startDate").asText();
                    String timestampString = startDate.replaceAll("[^0-9]", "");
                    long timestamp = Long.parseLong(timestampString);
                    Date date = new Date(timestamp);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String formattedDate = dateFormat.format(date);
                    String ldate = formattedDate.replace("-", "");
                    DynamicObject[] dynamicObject4 = BusinessDataServiceHelper.load("bos_user", QCP.equals, null);

                    //获取入职状态
                    String emplStatusNavexternalCode = rootNode.path("d").path("results").get(i).path("emplStatusNav").path("externalCode").asText();

                    //是否次要职位
                    String key = "";
                    boolean isPrimary = rootNode.path("d").path("results").get(i).path("isPrimary").asBoolean();
                    if (isPrimary == true) {
                        key = "1";
                    } else {
                        key = "0";
                    }
                    //获取新的uri

                    Map<String, Object> userdata = new HashMap<String, Object>();
                    QFilter qFilter = new QFilter("number", QCP.equals, personIdExternal);

                    DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("bos_user", "number，name", new QFilter[]{qFilter});


                    //  dynamicObject = BusinessDataServiceHelper.loadSingle(dynamicObject.get(0),"bos_user");

                    long masterid= DBServiceHelper.genGlobalLongId();

                    userdata.put("name", firstName);
                    userdata.put("number", personIdExternal);
                    userdata.put("username", personIdExternal);
                    userdata.put("usertype", 1);
                    userdata.put("phone", cellPhone);
                    userdata.put("ezob_bxzj", customString19);

                    if(customString=="null") {
                        userdata.put("ezob_jgh", "");//旧工号
                    }
                    else
                    {
                        userdata.put("ezob_jgh", customString);//旧工号
                    }
                    userdata.put("ezob_rzrq", timestamp); //入职日期

//                    QFilter f1 = new QFilter("group.name", "=", "工种");
                    QFilter f2 = new QFilter("number", "=", gz);
//                    QFilter f=f1.and(f2);
                     DynamicObject dynamicObjectGz = BusinessDataServiceHelper.loadSingle("bos_assistantdata_detail", "id, number, name", new QFilter[] {f2});
                    userdata.put("ezob_assistantfield", dynamicObjectGz.get("id").toString()); //工种

                    //当C00+编码后四位查不到,就根据旧编码,运营中心,法人组织查询
                    QFilter[] qFilters = new QFilter[3];
                    QFilter filterOldcostCenter = new QFilter("ezob_jbm","=",costCenter);
                    QFilter filterOldcostCenter2 = new QFilter("ezob_yyzx.name","=",yyzx);
                    QFilter filterOldcostCenter3 = new QFilter("accountorg.number","=",zz);
                    qFilters[0] = filterOldcostCenter;
                    qFilters[1] = filterOldcostCenter2;
                    qFilters[2] = filterOldcostCenter3;
                    QFilter fs = filterOldcostCenter.and(filterOldcostCenter2).and(filterOldcostCenter3);
                    //先赋值为空
                    userdata.put("ezob_cbzx",null); //成本中心

                    DynamicObject dynamicObjectnewcostCenter = null;
                    if(yyzx.equals("总部")) //要总部才能走C00+旧编码后四位
                    {
                        //C00+后四位编码查询是否有对应的新编码
                        String strNumber ="C00"+ costCenter.substring(costCenter.length() - 4);
                        QFilter filtercode = new QFilter("number",QCP.equals,strNumber);
                         dynamicObjectnewcostCenter = BusinessDataServiceHelper.loadSingle("bos_costcenter","id,number", new QFilter[]{filtercode});
                    }


                    if(dynamicObjectnewcostCenter==null)
                    {
                        //成本中心先用旧编码搜索，如果有旧编码，就是使用对应的新编码，否则还是使用旧编码
                        DynamicObject dynamicObjectcostCenter = BusinessDataServiceHelper.loadSingle("bos_costcenter","id,number",new QFilter[] {fs});
                        if(dynamicObjectcostCenter!=null) {
                            String newcostCenter = dynamicObjectcostCenter.get("id").toString();
                            userdata.put("ezob_cbzx", newcostCenter); //成本中心
                        }


                        else {
                            QFilter filterOldcostCenter4 = new QFilter("number",QCP.equals,costCenter);

                            DynamicObject dynamicObjectcostCenter2 = BusinessDataServiceHelper.loadSingle("bos_costcenter","id,number", new QFilter[]{filterOldcostCenter4});
                            String newcostCenter2 = dynamicObjectcostCenter2.get("id").toString();
                            userdata.put("ezob_cbzx", newcostCenter2); //成本中心
                        }
                    }
                else{

                        String newcostCenter = dynamicObjectnewcostCenter.get("id").toString();
                        userdata.put("ezob_cbzx", newcostCenter); //成本中心
                    }
                    userdata.put("ezob_cyzw", key); //是否重要职位
                    userdata.put("ezob_superior_number", managerId);  //上级工号
                    // userdata.put("reimburselevel.name", customString19);
                    // userdata.put("fuid", masterid);

                    long c = DBServiceHelper.genGlobalLongId();
                    ArrayList<LinkedHashMap<String, Object>> entryentity = new ArrayList<>();
                    LinkedHashMap<String, Object> entryentitydata = new LinkedHashMap<>();
                    if(department.equals("null")){
                        department = division;
                    }
                    QFilter filter = new QFilter("number",QCP.equals,department);
                    DynamicObject dynamicObjecttt = BusinessDataServiceHelper.loadSingle("bos_org","id,name",new QFilter[]{filter});
                    if(dynamicObjecttt!=null) {
                        String department22 = dynamicObjecttt.get("id").toString();
                        entryentitydata.put("dpt", department22);
                    }
                    else {
                        entryentitydata.put("dpt", "100000");
                    }
                    entryentitydata.put("position", externalNamedefaultValue);
                    entryentity.add(entryentitydata);
                    userdata.put("entryentity", entryentity);
                    List<UserParam> userList = new ArrayList<>();
                    UserParam uParam = new UserParam();
                    uParam.setDataMap(userdata);
                    if (dynamicObject != null) {

                        uParam.setId(Long.parseLong(dynamicObject.get(0).toString()));
                        // uParam.setCustomUserId((long));
                    }
                    userList.add(uParam);
                    // 新增人员

                    //判断系统是否已经有该人员,有则更新，没则新增人员
                    if(dynamicObject != null)
                    {
                        UserServiceHelper.update(userList);
                        log.info("人员更新"+firstName);
                    }
                    else {
                        UserServiceHelper.addOrUpdate(userList);
                        UserServiceHelper.enable(userList);
                        log.info("人员新增"+firstName);
                    }


                    String msg = uParam.getMsg();

                    //ApiResult data = new ApiResult();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    //System.out.print(e.getMessage());

                }
                //如果人员创建成功
                //System.out.print(url3);
                //DeleteServiceHelper.delete("er_reimbursesetting_rel",null);

            }

            for (int i1 = 0; i1 <length; i1++) {
                try {
                    jbv++;
                    System.out.print("级别更新" + jbv);

                    JsonNode personIdExternalNode1 =rootNode.path("d").path("results").get(i1).path("employmentNav").path("personNav").path("personIdExternal");
                    String personIdExternal1 = personIdExternalNode1.asText();
                    //级别
                    String customString191 = rootNode.path("d").path("results").get(i1).path("customString19Nav").path("externalCode").asText();

                    QFilter qFilter = new QFilter("number", QCP.equals, personIdExternal1);
                    DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("bos_user", "number，name", new QFilter[]{qFilter});
                    String id = dynamicObject.getString("id");
                    DynamicObject dynamicObject1 = BusinessDataServiceHelper.loadSingle(id,"bos_user");

                    if (dynamicObject != null) {

                        qFilter = new QFilter("number", QCP.equals, customString191);
                        // DynamicObject [] listA=   BusinessDataServiceHelper.load("er_reimbursesetting_rel","id,user,reimburselevel,company,user_id,reimburselevel_id,company_id",null);

                        DynamicObject evelE = BusinessDataServiceHelper.loadSingle("er_reimburselevel", "masterid", new QFilter[]{qFilter});
                        if (evelE != null && evelE.get(0) != null) {


                            qFilter = new QFilter("user_id", QCP.equals, dynamicObject.get(0));
                            // DynamicObject [] listA=   BusinessDataServiceHelper.load("er_reimbursesetting_rel","id,user,reimburselevel,company,user_id,reimburselevel_id,company_id",null);
                            DynamicObject setE = BusinessDataServiceHelper.loadSingle("er_reimbursesetting_rel", "masterid,reimburselevel", new QFilter[]{qFilter});
                            if (setE != null) {
                                setE.set("reimburselevel", evelE.get(0));
                                SaveServiceHelper.update(setE);

                                dynamicObject1.set("ezob_bxzj",evelE.get(0));
                                SaveServiceHelper.update(dynamicObject1);

                            } else {
                                DynamicObject erreimbursesettingrel = BusinessDataServiceHelper.newDynamicObject("er_reimbursesetting_rel");
                                erreimbursesettingrel.set("user", dynamicObject.get(0));
                                // er_reimbursesetting_rel.set("reimburselevel_id", 1729271031845118976l);
                                qFilter = new QFilter("number", QCP.equals, personIdExternal1);
                                erreimbursesettingrel.set("reimburselevel", evelE.get(0));
                                erreimbursesettingrel.set("company", OrgUnitServiceHelper.getRootOrgId());
                                // er_reimbursesetting_rel.set("company_id", OrgUnitServiceHelper.getRootOrgId());
                                SaveServiceHelper.save(new DynamicObject[]{erreimbursesettingrel});

                                dynamicObject1.set("ezob_bxzj",evelE.get(0));
                                SaveServiceHelper.update(dynamicObject1);


                            }


                        }


                    } else {

                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    //System.out.print(e.getMessage());

                }
            }

            url3 = rootNode.path("d").path("__next").asText();
            if (url3!=""){
                getmess(url3);
                ns=ns+1;

                log.info("Mytask:"+url3);
            }
            else{
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print(e.getMessage());
            log.error("Mytask:"+e.getMessage());
        }
    }



    public static void update(){

        DynamicObject []  dynamicObjectccc= BusinessDataServiceHelper.load("bos_user","*,ezob_superior_number,dpt.name,dpt.position,position,number",null);
        //DynamicObject []  dynamicObjectbbb =BusinessDataServiceHelper.load("bos_user","*,position",null);
        int lenth = dynamicObjectccc.length;

//        for(int i = 0;i<lenth;i++) {
//            System.out.println(i);
//            if(dynamicObjectccc[i].getString("number").equals("E0000023")){
//                System.out.println(i+"xxxxx");
//            }
//        }

        for(int i = 0;i<lenth;i++){
            System.out.println(i);
            try{
                //整张单据
                DynamicObject dynamicObjectqwe =dynamicObjectccc[i];

                DynamicObjectCollection entrys = dynamicObjectqwe.getDynamicObjectCollection("entryentity");
                String positions="";
                for (DynamicObject entry:entrys)
                {
                     positions = entry.get("position").toString();
                }
                //dynamicObjectccc[9].getDynamicObjectCollection("entryentity").get(0).get("position").toString()

                //工号
                String supernumer =dynamicObjectqwe.get("ezob_superior_number").toString();
                //人员id
                long id =Long.parseLong(dynamicObjectqwe.get("id").toString());
                //明细 cols
                DynamicObjectCollection cols = dynamicObjectqwe.getDynamicObjectCollection("entryentity");
                //部门
                DynamicObject org=(DynamicObject)cols.get(0).get("dpt");
                //部门id
                long or = Long.parseLong(org.get("id").toString());
                //职位
                String position =(dynamicObjectccc[i].getDynamicObjectCollection("entryentity")).get(0).get("position").toString();
                //查询条件==工号
                QFilter qFilter = new QFilter("number",QCP.equals,supernumer);
                //
                long id1 = Long.parseLong(BusinessDataServiceHelper.loadSingle("bos_user","id",new QFilter[]{qFilter}).get("id").toString());
                Map<String, Object> userdata = new HashMap<String, Object>();
                ArrayList<LinkedHashMap<String, Object>> entryentity = new ArrayList<>();//用于传明细
                //部门分录
                LinkedHashMap<String, Object> entryentitydata = new LinkedHashMap<>();
                entryentitydata.put("dpt",or);//部门
                entryentitydata.put("superior",id1);//直接上级
                entryentitydata.put("position",position);//职位
                entryentity.add(entryentitydata);
                userdata.put("entryentity", entryentity);
                List<UserParam> userList = new ArrayList<>();
                UserParam uParam = new UserParam();
                uParam.setId(id);
                uParam.setDataMap(userdata);
                userList.add(uParam);
                UserServiceHelper.update(userList);
                //long id = Long.parseLong(dynamicObjectqwe.get("id").toString);
            } catch (Exception e){
                e.printStackTrace();

            }
        }
    }













}
