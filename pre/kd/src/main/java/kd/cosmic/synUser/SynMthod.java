package kd.cosmic.synUser;

import cn.hutool.http.HttpRequest;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.model.UserParam;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;

import java.util.*;


public class SynMthod {
    private static final Log log = LogFactory.getLog(SynMthod.class);

    public static int itv=0;
    public static int jbv=0;
    private static int error =1;
    private static String firstName;

    private static  String msg;
    public static void getmess(String url) {

        //将用户名密码：我用API测试工具翻译出来的加密后的密码直接添加到请求头
        HttpRequest request = HttpRequest.get(url).header("Authorization", "Basic SmluRGllQVBJQGFrbW12OkppbkRpZTEyMw==");
        //发起请求
        String bodys = request.execute().body();

        String url3 = "";
        //循环所有数据
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(bodys);
            JsonNode resultsNode = rootNode.path("d").path("results");
            int length = resultsNode.size();

            for (int i = 0; i < length; i++) {      //length


                try {
                    //法人
                    String zz = resultsNode.get(i).path("companyNav").path("externalCode").asText();
                    if(zz.equals("LE0008"))
                    {
                        continue;
                    }
                    itv++;


                    //取员工工号
                    JsonNode personIdExternalNode = resultsNode.get(i).path("employmentNav").path("personNav").path("personIdExternal");
                    String personIdExternal = personIdExternalNode.asText();

                    //取员旧工号
                    JsonNode customString1Node = resultsNode.get(i).path("employmentNav").path("personNav").path("customString1");
                    String customString = customString1Node.asText();

                    //取业务单元
                    String yydy = resultsNode.get(i).path("companyNav").path("externalCode").asText();


                    //中文姓名
                    JsonNode firstNameNode = resultsNode.get(i).path("employmentNav").path("personNav").path("personalInfoNav").path("results").get(0).path("firstName");
                    firstName = firstNameNode.asText();



                    log.info("Mytask头部信息,姓名:"+firstName+",人员插入:"+itv);





                    //手机
                    JsonNode userNavNode = resultsNode.path(i).path("userNav");
                    String cellPhone = userNavNode.path("cellPhone").asText().trim();

                    //邮箱
                    JsonNode useremail = resultsNode.path(i).path("userNav");
                    String email = useremail.path("email").asText();




                    //运营中心
                    String yyzx =  resultsNode.get(i).path("businessUnitNav").path("name_defaultValue").asText();

                    //成本中心
                    String costCenter = resultsNode.get(i).path("costCenter").asText();



                    //人员名称
                    JsonNode employeeClassNavNode = resultsNode.get(i).path("employeeClassNav");
                    String gz = employeeClassNavNode.path("externalCode").asText(); //工种

                    //上级工号uri
                    String managerId = resultsNode.get(i).path("managerId").asText();


                    //职位名称
                    JsonNode customString1NavNode = resultsNode.get(i).path("positionNav");
                    String externalNameDefaultValue = customString1NavNode.path("externalName_defaultValue").asText();

                    //业务单元
                    String businessUnit= resultsNode.get(i).path("businessUnit").asText();



                    //分部
                    //String division = resultsNode.get(i).path("division").asText();
                    String customString4= resultsNode.get(i).path("customString4").asText();

                    //L1部门
                    String department = resultsNode.get(i).path("department").asText();
                    //L2部门
                    String customString3= resultsNode.get(i).path("customString3").asText();

                    //级别
                    String customString19 = resultsNode.get(i).path("customString19Nav").path("externalCode").asText();







                    //人员集合
                    Map<String, Object> userdata = new HashMap<String, Object>();

                    //根据工号获取人员集合信息
                    QFilter qFilter = new QFilter("number", QCP.equals, personIdExternal);
                    DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("bos_user", "number，name", new QFilter[]{qFilter});

                    //根据组织编码获取组织ID
                    QFilter zzFilter = new QFilter("number", QCP.equals, yydy);

                    DynamicObject ywzz = BusinessDataServiceHelper.loadSingle("bos_org",  new QFilter[]{zzFilter});


                    /*
                        人员表头字段赋值
                     */

                    userdata.put("name", firstName); //姓名
                    userdata.put("number", personIdExternal); //编码
                    userdata.put("username", personIdExternal); //用户名
                    userdata.put("usertype", 1); //用户类型(默认职员)
                    userdata.put("phone", cellPhone); //电话
                    if(customString19!=null&&!customString19.equals(""))
                    {
                        userdata.put("ezob_bxzj",customString19); //报销职级

                    }else{
                        userdata.put("ezob_bxzj","G1"); //报销职级

                    }
                    //userdata.put("email", email); //邮件
                    userdata.put("ezob_jgh", customString);//旧工号
                    userdata.put("ezob_rzrq", getTimestamp(resultsNode,i)); //入职日期
                    userdata.put("ezob_assistantfield",getGz(gz)); //工种
                    userdata.put("ezob_cbzx",null); //成本中心
                    String dpt = getDept(department,customString4,customString3,businessUnit);
                    userdata.put("ezob_cbzx",getCentercostId(yyzx,costCenter,zz,dpt)); //成本中心
                    userdata.put("ezob_cyzw",getKey(resultsNode,i)); //是否重要职位
                    userdata.put("ezob_superior_number", managerId);  //上级工号
                    userdata.put("ezob_frzt",ywzz.getPkValue());  //法人主体


                    //明细
                    ArrayList<LinkedHashMap<String, Object>> entryentity = new ArrayList<>();
                    LinkedHashMap<String, Object> entryentitydata = new LinkedHashMap<>();

                    entryentitydata.put("superior",getSidByNumber(managerId));//直接上级
                    entryentitydata.put("dpt", getDept(department,customString4,customString3,businessUnit)); //部门
                    entryentitydata.put("position", externalNameDefaultValue); //职位
                    //将当前这一行添加到明细
                    entryentity.add(entryentitydata);
                    //将明细添加到人员集合
                    userdata.put("entryentity", entryentity);
                    List<UserParam> userList = new ArrayList<>();
                    UserParam uParam = new UserParam();
                    uParam.setDataMap(userdata);
                    if (dynamicObject != null) {

                        uParam.setId(Long.parseLong(dynamicObject.get(0).toString()));
                    }
                    userList.add(uParam);
                    // 新增人员

                    //判断系统是否已经有该人员,有则更新，没则新增人员
                    String synType ="";
                    if(dynamicObject != null)
                    {
                        UserServiceHelper.update(userList);
                        log.info("人员更新"+firstName);
                        synType ="更新";
                    }
                    else {
                        UserServiceHelper.addOrUpdate(userList);
                        UserServiceHelper.enable(userList);
                        log.info("人员新增"+firstName);
                        synType="新增";
                    }

                    int sucsessCount=1;
                    int failCount   =1;

                    msg = uParam.getMsg();
                    if(msg==null)
                    {

                        log.info("Mytask同步"+synType+"成功！人员:"+firstName+"人员插入:"+itv+", 同步后返回信息:"+msg+"第"+sucsessCount+"条成功");
                        sucsessCount++;
                    }else{

                        log.info("Mytask同步"+synType+"失败！人员:"+firstName+"人员插入:"+itv+", 失败原因:"+msg+"第"+failCount+"条失败");
                        failCount++;
                    }
                }
                catch (Exception e) {
                    StackTraceElement stackTranceElment=  e.getStackTrace()[0];
                    int lineNumber = stackTranceElment.getLineNumber();
                    log.info("Mytask同步报错！人员:"+firstName+"人员插入:"+itv+", 报错信息:"+e.getMessage()+"报错行"+lineNumber);
                    e.printStackTrace();
                    error++;
                }


            }
            //更新报销职级
            updateBxzj(length,resultsNode);


            url3 = rootNode.path("d").path("__next").asText();
            if (url3!=""){
                getmess(url3);


                log.info("Mytask:"+url3);
            }
            else{
                return;
            }
        } catch (Exception e) {
            StackTraceElement stackTranceElment=  e.getStackTrace()[0];
            int lineNumber = stackTranceElment.getLineNumber();
            e.printStackTrace();
            log.error("Mytask报销职级同步:"+e.getMessage()+"报错行"+lineNumber);
        }
    }

    /**
     *   以下方法根据相应的逻辑条件获取对应的ID或值
     */

    //获取成本中心id
    public static String  getCentercostId(String yyzx,String costCenter,String zz,String dept)
    {
        //08241
        QFilter filterOldcostCenter = new QFilter("ezob_jbm","=",costCenter);  //旧编码
        QFilter filterOldcostCenter2 = new QFilter("ezob_yyzx.name","=",yyzx); //运营中心
        QFilter filterOldcostCenter3 = new QFilter("accountorg.number","=",zz);//法人
        QFilter fs = filterOldcostCenter.and(filterOldcostCenter2).and(filterOldcostCenter3);


        String centerCostId = null;
        DynamicObject dynamicObjectnewcostCenter = null;
        if(yyzx.equals("总部")) //要总部才能走C00+旧编码后四位
        {
            //C00+后四位编码查询是否有对应的新编码
            String strNumber ="C00"+ costCenter.substring(costCenter.length() - 4);
            QFilter filtercode = new QFilter("number",QCP.equals,strNumber);
            dynamicObjectnewcostCenter = BusinessDataServiceHelper.loadSingle("bos_costcenter","id,number", new QFilter[]{filtercode});
        }else if(yyzx.equals("研发中心")) //要研发中心才能走C01+旧编码后四位
        {
            //C01+后四位编码查询是否有对应的新编码
            String strNumber ="C01"+ costCenter.substring(costCenter.length() - 4);
            QFilter filtercode = new QFilter("number",QCP.equals,strNumber);
            dynamicObjectnewcostCenter = BusinessDataServiceHelper.loadSingle("bos_costcenter","id,number", new QFilter[]{filtercode});
        }else if(yyzx.equals("mSAP运营中心")&&dept.equals("DV000015"))
        {
            //C03+后四位编码查询是否有对应的新编码
            String strNumber ="C03"+ costCenter.substring(costCenter.length() - 4);
            QFilter filtercode = new QFilter("number",QCP.equals,strNumber);
            dynamicObjectnewcostCenter = BusinessDataServiceHelper.loadSingle("bos_costcenter","id,number", new QFilter[]{filtercode});
        }


        if(dynamicObjectnewcostCenter==null)
        {
            //成本中心先用旧编码搜索，如果有旧编码，就是使用对应的新编码，否则还是使用旧编码
            DynamicObject dynamicObjectcostCenter = BusinessDataServiceHelper.loadSingle("bos_costcenter","id,number",new QFilter[] {fs});
            if(dynamicObjectcostCenter!=null) {
                centerCostId = dynamicObjectcostCenter.get("id").toString();
            }


            else {
                QFilter filterOldcostCenter4 = new QFilter("number",QCP.equals,costCenter);

                DynamicObject dynamicObjectcostCenter2 = BusinessDataServiceHelper.loadSingle("bos_costcenter","id,number", new QFilter[]{filterOldcostCenter4});
                if(dynamicObjectcostCenter2!=null)
                {
                    centerCostId = dynamicObjectcostCenter2.get("id").toString();

                }

            }
        }
        else{

            centerCostId = dynamicObjectnewcostCenter.get("id").toString();

        }
        return centerCostId;
    }

    //更新报销职级
    public static void updateBxzj(int length, JsonNode resultsNode)
    {
        for (int i1 = 0; i1 <length; i1++) {
            try {
                jbv++;
                System.out.print("级别更新" + jbv);

                JsonNode personIdExternalNode1 =resultsNode.get(i1).path("employmentNav").path("personNav").path("personIdExternal");
                String personIdExternal1 = personIdExternalNode1.asText();
                //级别
                String customString191 = resultsNode.get(i1).path("customString19Nav").path("externalCode").asText();

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
                            DynamicObject erReimbursesettingrel = BusinessDataServiceHelper.newDynamicObject("er_reimbursesetting_rel");
                            erReimbursesettingrel.set("user", dynamicObject.get(0));
                            // er_reimbursesetting_rel.set("reimburselevel_id", 1729271031845118976l);
                            qFilter = new QFilter("number", QCP.equals, personIdExternal1);
                            erReimbursesettingrel.set("reimburselevel", evelE.get(0));
                            erReimbursesettingrel.set("company", OrgUnitServiceHelper.getRootOrgId());
                            // er_reimbursesetting_rel.set("company_id", OrgUnitServiceHelper.getRootOrgId());
                            SaveServiceHelper.save(new DynamicObject[]{erReimbursesettingrel});

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

    }


    //获取直接上级id
    public static long getSidByNumber(String managerId)
    {
        //根据上级编号查询直接上级id
        QFilter qFilterNumber = new QFilter("number",QCP.equals,managerId);
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("bos_user","id",new QFilter[]{qFilterNumber});

        long sid;
        if(dynamicObject!=null)
        {
            sid = Long.parseLong(dynamicObject.get("id").toString());
            return sid ;
        }else {
            return 0;

        }

    }
    //获取工种id
    public static long getGz(String gzNumber)
    {
        QFilter f2 = new QFilter("number", "=", gzNumber);
        DynamicObject dynamicObjectGz = BusinessDataServiceHelper.loadSingle("bos_assistantdata_detail", "id", new QFilter[] {f2});
        long gzId = dynamicObjectGz.getLong("id");
        return  gzId;
    }

    //获取部门id
    public static String getDept(String department,String customString4,String customString3,String businessUnit)
    {
        String number ="";



        if(businessUnit.equals("BU000001"))
        {
            //部门为空就取customString4字段去查
            if(customString4.equals("null"))
            {
                //如果L2部门为空的话，就取L1，如果L1为空，就取dname本身的值，也就是department字段的值
                if(!customString3.equals("null")){
                    number = customString3;
                }else {
                    number = department;
                }
            }
            else{
                customString4 = selDeptByNumber(customString4);

                //如果L2查不到，再进入判断L1不为空，再拿L1查,能查到则直接赋值ID给number返回
                if(customString4.equals("100000"))
                {
                    //不为空则用L1查，为空等于100000
                    if(!department.equals("null"))
                    {
                         return selDeptByNumber(department);
                    }else {
                        return customString4;
                    }
                }else {
                    return  customString4;
                }
            }
        }else {
            if(!customString3.equals("null")){
                number = customString3;
            }else {
                number = department;
            }

        }



        return selDeptByNumber(number);
    }

    public static String selDeptByNumber(String dNumber)
    {
        String department ="";
        QFilter filter = new QFilter("number",QCP.equals,dNumber);
        DynamicObject dynamicObjecttt = BusinessDataServiceHelper.loadSingle("bos_org","id,name",new QFilter[]{filter});
        if(dynamicObjecttt!=null) {
            department  = dynamicObjecttt.get("id").toString();
        }
        else {
            department  = "100000";
        }
        return department;
    }

    //获取是否重要职位
    public static String getKey(JsonNode resultsNode,int i)
    {
        //是否次要职位
        String key = "";
        boolean isPrimary = resultsNode.get(i).path("isPrimary").asBoolean();
        if (isPrimary == true) {
            key = "1";
        } else {
            key = "0";
        }
        return key;
    }
    //最早入职日期
    public static long getTimestamp(JsonNode resultsNode,int i){
        String startDate = resultsNode.get(i).path("employmentNav").path("startDate").asText();
        String timestampString = startDate.replaceAll("[^0-9]", "");
        long timestamp = Long.parseLong(timestampString);
        return timestamp;
    }


}
