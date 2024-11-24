package kd.cosmic.synUser;

import cn.hutool.http.HttpRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDate;

public class Test {

    private  static  int i = 0;

    public static void main(String[] args) throws IOException {

        LocalDate currentDate = LocalDate.now();
        LocalDate result = currentDate.minusDays(2);
        String url ="https://api15.sapsf.cn/odata/v2/EmpJob?$select=employmentNav/personNav/personIdExternal,employmentNav/personNav/customString1,employmentNav/personNav/personalInfoNav/firstName,userNav/username,userNav/cellPhone,userNav/email,costCenter,employeeClassNav/externalCode,employeeClassNav/localeLabel,managerId,managerUserNav/firstName,customString1,customString1Nav/externalName_defaultValue,customString2,customString2Nav/externalName_defaultValue,companyNav/externalCode,businessUnit,businessUnitNav/name_defaultValue,division,divisionNav/name_defaultValue,department,departmentNav/name_defaultValue,customString3,customString3Nav/externalName_defaultValue,customString4,customString4Nav/externalName_defaultValue,customString15,customString15Nav/externalName_defaultValue,customString18Nav/externalCode,customString19Nav/externalCode,positionNav/externalName_defaultValue,employmentNav/startDate,emplStatusNav/externalCode,employmentNav/isPrimary&$format=json&$expand=employmentNav/personNav,employmentNav,userNav,emplStatusNav,employmentNav/personNav/personalInfoNav,employeeClassNav,managerUserNav,companyNav,customString1Nav,customString2Nav,businessUnitNav,divisionNav,departmentNav,customString3Nav,customString4Nav,customString15Nav,customString18Nav,customString19Nav,positionNav&$filter=(employmentNav/isPrimary eq false or employmentNav/isPrimary eq null) and lastModifiedDateTime ge datetime'2022-06-11T00:00:01'";

        fangfa(url);
    }


    public static void fangfa(String url) throws IOException {
        i++;
        //将用户名密码：我用API测试工具翻译出来的加密后的明明直接添加到请求头
        HttpRequest request = HttpRequest.get(url).header("Authorization", "Basic SmluRGllQVBJQGFrbW12OkppbkRpZTEyMw==");
        //发起请求
        String bodys = request.execute().body();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(bodys);
        JsonNode resultsNode = rootNode.path("d").path("results");
        int length = resultsNode.size();

        System.out.println("长度："+length);
        String url3 = rootNode.path("d").path("__next").asText();
        fangfa(url3);
    }
}
