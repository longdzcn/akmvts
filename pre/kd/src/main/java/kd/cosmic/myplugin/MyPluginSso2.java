package kd.cosmic.myplugin;

import kd.bos.login.thirdauth.ThirdSSOAuthHandler;
import kd.bos.login.thirdauth.UserAuthResult;
import kd.bos.login.thirdauth.UserProperType;
import kd.bos.util.HttpUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//kd.cosmic.myplugin.MyPluginSso

/**
 *  author：易佳伟
 *  createDate : 2023/05/07
 *  description: 单点登录插件，部署位置：MC数据中心里头
 *  ！！！这是正式环境的单点登录插件
 */
public class MyPluginSso2 implements ThirdSSOAuthHandler {

    @Override
    public UserAuthResult getTrdSSOAuth(HttpServletRequest request, HttpServletResponse response) {
        // 现在可以了 写你的逻辑吧
        UserAuthResult result = new UserAuthResult();
        result.setSucess(false);
        //接收code
        String code =null;
        String userNumber =null;
        try {
            String form = request.getParameter("form");
            userNumber =request.getParameter("userNumber");
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
                //testUrl
                String accessTokenUrl = "https://oatest.akmmv.com/mv/oauth/mv_oauth_main/mvOauthToken.do?method=getToken" +
                        "&systemId=jindie&clientid=jindie&secret=jindie123456";

                String test = HttpUtils.post(accessTokenUrl);

                String []parts =test.split(",");
                String accessToken ="";
                for (String part:parts)
                {
                    if(part.contains("access_token"))
                    {
                        String[] keyValue = part.split(":");
                        accessToken = keyValue[1].replace("}","").trim();
                        accessToken = accessToken.replace("\"","");
                        break;
                    }
                }


                String kdUserToken = accessToken;
                //调用getUser接口，获取用户信息
                String userUrl = "https://oatest.akmmv.com/mv/oauth/mv_oauth_main/mvOauthToken.do?method=getUserid"
                        + "&systemId=jindie&code="+code+"&token="+kdUserToken;



                String  userInfo = HttpUtils.post(userUrl);

                String [] list =userInfo.split(",");
                String userNo ="";
                for (String part:list)
                {
                    if(part.contains("userNo"))
                    {
                        String[] keyValue = part.split(":");
                        userNo = keyValue[1].replace("}","").trim();
                        userNo = userNo.replace("\"","");
                        break;
                    }
                }

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
            httpServletResponse.sendRedirect("http://10.22.237.71:8020/ierp/login.html");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
