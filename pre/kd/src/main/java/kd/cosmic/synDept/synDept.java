package kd.cosmic.synDept;

import cn.hutool.http.HttpRequest;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.org.model.OrgParam;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.bos.servicehelper.org.OrgViewType;
import kd.sdk.plugin.Plugin;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * 描述: 通过点击行政组织列表的同步部门按钮，会读取Myh组织接口，全量新增或更新部门信息
 * 开发者: 易佳伟
 * 创建日期: 1期
 * 关键客户：马衍浩
 * 已部署正式：ture
 * 备注：已投入正式环境使用
 */
public class synDept extends AbstractListPlugin implements Plugin {


    private static final Log log = LogFactory.getLog(synDept.class);
    private final static String KEY_BARITEM = "syn_dept";

    private static int  sumCount =0;
    private static int succeseCount = 0;
    private static int failCount = 0 ;

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(KEY_BARITEM);
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        if (StringUtils.equals(evt.getItemKey(), KEY_BARITEM)) {


        try {
            LocalDateTime currentDate = LocalDateTime.now();

            //总公司
            String custGroupurl ="https://api15.sapsf.cn/odata/v2/cust_Group?$format=json&$filter=lastModifiedDateTime ge datetime'2021-08-20T13:50:41'&$select=externalCode,externalName_zh_CN,cust_description_zh_CN,mdfSystemStatus,cust_headOfUnit";
            //公司
            String custCompanyurl ="https://api15.sapsf.cn/odata/v2/cust_Company?$format=json&$filter=lastModifiedDateTime ge datetime'2021-08-20T13:50:41'&$select=externalCode,externalName_zh_CN,cust_description_zh_CN,mdfSystemStatus,cust_headOfUnit,cust_toGroup/externalCode&$expand=cust_toGroup";
            //业务单元
            String fOBusinessUniturl ="https://api15.sapsf.cn/odata/v2/FOBusinessUnit?$format=json&$filter=lastModifiedDateTime ge datetime'2021-08-20T13:50:41' and status eq 'A'&$select=externalCode,name_zh_CN,description_zh_CN,status,headOfUnit,cust_toCompany/externalCode&$expand=cust_toCompany";
            //分部
            String fODivisionurl ="https://api15.sapsf.cn/odata/v2/FODivision?$format=json&$filter=lastModifiedDateTime ge datetime'2021-08-20T13:50:41' and  status eq 'A' &$select=externalCode,name_zh_CN,description_zh_CN,status,headOfUnit,cust_toBusinesUnit/externalCode&$expand=cust_toBusinesUnit";
            //L1部门
            String fODepartmenturl ="https://api15.sapsf.cn/odata/v2/FODepartment?$format=json&$filter=lastModifiedDateTime ge datetime'2021-08-20T13:50:41' and  status eq 'A'&$select=externalCode,name_zh_CN,description_zh_CN,status,headOfUnit,cust_toDivision/externalCode&$expand=cust_toDivision";
            //L2部门
            String custDep2url    ="https://api15.sapsf.cn/odata/v2/cust_Dep2?$format=json&$filter=lastModifiedDateTime ge datetime'2021-08-20T13:50:41' and  mdfSystemStatus eq  'A'&$select=externalCode,externalName_zh_CN,cust_description_zh_CN,mdfSystemStatus,cust_headOfUnit,cust_toDepartment/externalCode&$expand=cust_toDepartment";
            //工序/小组
            String custProcessOrTeamurl ="https://api15.sapsf.cn/odata/v2/cust_ProcessOrTeam?$format=json&$filter=lastModifiedDateTime ge datetime'2021-08-20T13:50:41' and cust_Division ne null and  mdfSystemStatus eq  'A'&$select=externalCode,externalName_zh_CN,cust_description_zh_CN,mdfSystemStatus,cust_headOfUnit,cust_DivisionNav/externalCode,cust_DepartmentNav/externalCode,cust_toDep2/externalCode&$expand=cust_DivisionNav,cust_DepartmentNav,cust_toDep2";
            //岗位
            String custPosturl="https://api15.sapsf.cn/odata/v2/cust_Post?$format=json&$filter=lastModifiedDateTime ge datetime'2021-08-20T13:50:41'&$select=externalCode,externalName_zh_CN,cust_description_zh_CN,mdfSystemStatus,cust_headOfUnit,cust_toProcessOrTeam/externalCode&$expand=cust_toProcessOrTeam";


            //将接口地址加入数组,循环调用
            String[] sumUrl = new String[7];
            sumUrl[0] =custGroupurl;
            sumUrl[1] =custCompanyurl;
            sumUrl[2] =fOBusinessUniturl;
            sumUrl[3] =fODivisionurl;
            sumUrl[4] =fODepartmenturl;
            sumUrl[5] =custProcessOrTeamurl;
            sumUrl[6] =custDep2url;
            //sumUrl[7] =cust_Post_url;1

            for (int y=2;y<sumUrl.length;y++) //上三级已经有了，直接从业务单元下级开始
            {
                if(y==4)
                {
                    System.out.println("1111");
                }


                String bodys = fangfa(sumUrl[y]);
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(bodys);
                JsonNode resultsNode = rootNode.path("d").path("results");
                int length = resultsNode.size();

                for (int i = 0; i < length; i++) {
                    //计算部门数量
                    sumCount++;
                    //根节点
                    JsonNode personIdExternalNode = rootNode.path("d").path("results").get(i);
                    //中文名称
                    String externalNamezhCN = getdName(personIdExternalNode,y);
                    //编码
                    String externalCode = personIdExternalNode.path("externalCode").asText();
                    //获取上级部门
                    String sPartment=getdNumber(personIdExternalNode,y);
                    //组织形态
                    long   dStatus = getdStatus(y);






                    //根据编码先取到当前组织id和上级id
                    long id = 0;
                         id = byNumberGetId(externalCode); //组织id
                    long sid =0;
                         sid = byNumberGetId(sPartment); //上级组织id
                    //行政组织集合
                    List<OrgParam> paramList = new ArrayList<>();
                    //创建行政组织对象
                    OrgParam orgParam = new OrgParam();
                    orgParam.setNumber(externalCode); //赋予编码
                    orgParam.setName(externalNamezhCN);//名称
                    orgParam.setAutoMoveSubOrg(false);
                    orgParam.setDuty(OrgViewType.Admin);//视图类型
                    orgParam.setOrgPatternId(dStatus);//形态
                    orgParam.setParentId(sid); //设置上级部门


                    paramList.add(orgParam);



                    //判断是否系统有该部门,无则新增有则修改
                    if(id==0)
                    {
                        OrgUnitServiceHelper.add(paramList);
                    }else {
                        orgParam.setId(id);
                        OrgUnitServiceHelper.update(paramList);
                    }

                    //获取返回的信息
                    String rMessge= orgParam.getMsg();

                    if(rMessge==null)
                    {
                        log.info("returnMessage成功:"+"同步组织信息: 编码:"+externalCode+",名称:"+externalNamezhCN+",上级编码:"+sPartment);
                         succeseCount++;
                    }else {
                        log.info("returnMessage失败:"+rMessge+"同步组织信息: 编码:"+externalCode+",名称:"+externalNamezhCN+",上级编码:"+sPartment);
                        failCount++;

                    }




                }

            }
            LocalDateTime endDate = LocalDateTime.now();
            log.info("行政组织部门同步-开始时间~结束时间:"+currentDate+"~"+endDate+"同步部门数量"+sumCount
                    +"同步成功数量"+succeseCount+"同步失败数量"+failCount);



        }catch (Exception e)
        {
            e.printStackTrace();
            System.out.print(e.getMessage());

        }
        }
    }

    //获得部门形态
    public static  long getdStatus(int y)
    {


        long dStatus=4;



        if (y==2) { //业务单元

            dStatus = 4;
        }else if (y==5||y==3||y==4||y==6) { // 分部 L1部门 工序小组
            dStatus = 4;
        }


        return dStatus;
    }

    //获得上级编码
    public static  String getdNumber(JsonNode personIdExternalNode,int y)
    {


        String sPartment="";
     /*   if(y==1) //公司的上级
        {
            sPartment = personIdExternalNode.path("cust_toGroup").path("results").get(0).get("externalCode").asText();

        } else */

        if (y==2) { //业务单元
            sPartment = personIdExternalNode.path("cust_toCompany").path("results").get(0).get("externalCode").asText();

        }else if (y==3) { //分部
            sPartment = personIdExternalNode.path("cust_toBusinesUnit").path("results").get(0).get("externalCode").asText();

        }else if (y==4) { //L1部门
            sPartment = personIdExternalNode.path("cust_toDivision").path("results").get(0).get("externalCode").asText();

        }
        else if (y==6) { //L2部门
            sPartment = personIdExternalNode.path("cust_toDepartment").path("externalCode").asText();

        }
        else if (y==5) { //工序/小组
            sPartment = personIdExternalNode.path("cust_DivisionNav").path("externalCode").asText();

        }

                /*    else if (y==6) { //工序/小组
                        String  cust_DivisionNav = personIdExternalNode.path("cust_DivisionNav").path("externalCode").asText();
                        String  cust_toDep2      = personIdExternalNode.path("cust_toDep2").path("externalCode").asText();
                        String  cust_DepartmentNav= personIdExternalNode.path("cust_DepartmentNav").path("externalCode").asText();


                        if(cust_DivisionNav!=null&&cust_DivisionNav!="")
                        {
                            sPartment = cust_DivisionNav;

                        }else  if(cust_toDep2!=null&&cust_toDep2!="")
                        {
                            sPartment = cust_toDep2;

                        }else if(cust_DepartmentNav!=null&&cust_DepartmentNav!=""){
                            sPartment = cust_DepartmentNav;

                        }*/

                   /* }else if (y==7) { //工序/小组

                        sPartment = personIdExternalNode.path("cust_toProcessOrTeam").path("results").get(0).get("externalCode").asText();


                    }//取值完毕-->后续赋值*/
        return sPartment;
    }
    //获得部门名称
    public static  String getdName(JsonNode personIdExternalNode,int y)
    {


        String dName="";
      /*  if(y==1) //公司的上级
        {
            dName = personIdExternalNode.path("cust_toGroup").path("results").get(0).get("externalCode").asText();

        } else */


        if (y==2||y==3||y==4) { //业务单元  //分部 //L1部门
            dName = personIdExternalNode.path("name_zh_CN").asText();

        }
        else if (y==6) { //L2部门
            dName = personIdExternalNode.path("externalName_zh_CN").asText();
        }
        else if (y==5) { //工序/小组

            dName = personIdExternalNode.path("externalName_zh_CN").asText();
        }
                /*    else if (y==6) { //工序/小组
                        String  cust_DivisionNav = personIdExternalNode.path("cust_DivisionNav").path("externalCode").asText();
                        String  cust_toDep2      = personIdExternalNode.path("cust_toDep2").path("externalCode").asText();
                        String  cust_DepartmentNav= personIdExternalNode.path("cust_DepartmentNav").path("externalCode").asText();


                        if(cust_DivisionNav!=null&&cust_DivisionNav!="")
                        {
                            sPartment = cust_DivisionNav;

                        }else  if(cust_toDep2!=null&&cust_toDep2!="")
                        {
                            sPartment = cust_toDep2;

                        }else if(cust_DepartmentNav!=null&&cust_DepartmentNav!=""){
                            sPartment = cust_DepartmentNav;

                        }*/

                   /* }else if (y==7) { //工序/小组

                        sPartment = personIdExternalNode.path("cust_toProcessOrTeam").path("results").get(0).get("externalCode").asText();


                    }//取值完毕-->后续赋值*/

        dName = dName.replace("_","-");

        return dName;
    }
    
    
    //返回接口的信息
    public static String fangfa(String url)
    {
        HttpRequest request = HttpRequest.get(url).header("Authorization", "Basic SmluRGllQVBJQGFrbW12OkppbkRpZTEyMw==");
        String bodys = request.execute().body();
        return bodys;
    }
    //根据编码获得行政组织的id
    public static long byNumberGetId(String number)
    {
        if(number.equals("C0001"))
        {
            number ="AKMMV";
        }
        QFilter qFilter =new QFilter("number","=",number);
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("bos_adminorg", new QFilter[]{qFilter});
        long id = 0;
        if(dynamicObject!=null)
        {
            id = (long) dynamicObject.getPkValue();
        }
        return id;
    }
}
