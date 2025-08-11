package io.github.ardavanghaffari.mybank;

import io.github.ardavanghaffari.mybank.web.TransactionServlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

public class ApplicationLauncher {

    public static void main(String[] args) throws LifecycleException {
        String portStr = System.getProperty("server.port", "8080");
        int port = Integer.parseInt(portStr);

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();

        Context ctx = tomcat.addContext("", null);
        TransactionServlet servlet = new TransactionServlet();
        Wrapper wrapper = Tomcat.addServlet(ctx, "transactionServlet", servlet);
        wrapper.setLoadOnStartup(1);
        wrapper.addMapping("/*");

        tomcat.start();
    }

}
