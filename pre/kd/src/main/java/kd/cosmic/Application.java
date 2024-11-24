package kd.cosmic;

import kd.cosmic.server.Launcher;

/**
 * 启动本地应用程序(微服务节点)
 */
public class Application {

    public static void main(String[] args) {
        Launcher cosmic = new Launcher();
        String ip = "10.22.237.70";
        cosmic.setClusterNumber("ierp");
        cosmic.setTenantNumber("10001");
        cosmic.setServerIP("10.22.237.71");
        cosmic.setAppName("cosmic-jian-oOo3WO8s");
        //cosmic-jian-60h1JJiH
        cosmic.setWebPath("D:/IntelliJIDEA/kd-server/webapp");
        cosmic.setFsServerUrl(ip , 30003);
        cosmic.setImageServerUrl(ip, 30003);
        cosmic.setStartWithQing(false);
        cosmic.start();
    }
}