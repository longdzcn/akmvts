package kd.cosmic.myplugin;



import com.alibaba.fastjson.JSONObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.login.thirdauth.ThirdSSOAuthHandler;
import kd.bos.login.thirdauth.UserAuthResult;
import kd.bos.login.thirdauth.UserProperType;
import kd.bos.util.HttpUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
/**
 *  author：易佳伟
 *  createDate : 2023/05/07
 *  description: 当时用于UAT测试，后来改为了正式账套的插件
 *  ！！！这是正式环境的单点登录插件
 */

public class MyPluginSsoUat implements ThirdSSOAuthHandler {

    private static Log log = LogFactory.getLog(MyPluginSsoUat.class);

    private static final String systemId ="jindie";
    private static final String clientid ="jindie";
    private static final String secret   ="Dpub92wrus57";

    @Override
    public UserAuthResult getTrdSSOAuth(HttpServletRequest request, HttpServletResponse response) {

        UserAuthResult result = new UserAuthResult();
        result.setSucess(false);
        //接收code
        String code =null;
        String userNumber =null;
        try {
            String form = request.getParameter("form");
            userNumber=request.getParameter("userNumber");

            if(form.contains("code="))
            {
                int stratIndex = form.indexOf("code=");
                code = form.substring(stratIndex+5);
            }

        }catch (Exception e)
        {
            code  = request.getParameter("code");
            userNumber=request.getParameter("userNumber");
        }


        if(code!=""&&code!=null)
        {


            //uatUrl
            String accessTokenUrl = "http://oa.akmmv.com:8080/mv/oauth/mv_oauth_main/mvOauthToken.do?method=getToken" +
                    "&systemId="+systemId+
                    "&clientid="+clientid+
                    "&secret="  +secret;
            log.info("开始获取第三方认证token");
            JSONObject parse = (JSONObject)JSONObject.parse(HttpUtils.post(accessTokenUrl));

            String kdUserToken = parse.getString("access_token");
            log.info("开始获取第三方认证token："+kdUserToken);
            //调用getUser接口，获取用户信息
            String userUrl = "http://oa.akmmv.com:8080/mv/oauth/mv_oauth_main/mvOauthToken.do?method=getUserid" +
                    "&systemId="+systemId+
                    "&code="+code+
                    "&token="+kdUserToken;

            log.info("获取系统用户信息：");
            JSONObject  userInfo = (JSONObject)JSONObject.parse(HttpUtils.post(userUrl));
            //获得用户ID
            String userNo = userInfo.getString("userNo");
            log.info("获取系统用户信息："+userInfo);
            if(userNo!=null&&userNo!="")
            {
                result.setUser(userNo);
                result.setUserType(UserProperType.WorkerNumber);
                result.setSucess(true);

            }
        }else {
            if(userNumber!=null&&userNumber!="")
            {
                result.setUser(userNumber);
                result.setUserType(UserProperType.WorkerNumber);
                result.setSucess(true);

            }
        }
        return result;
    }






    @Override
    public void callTrdSSOLogin(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String s) {
        try {
            httpServletResponse.sendRedirect("https://kd.akmmv.com/ierp/login.html");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
