package io.github.ardavanghaffari.mybank;

import io.github.ardavanghaffari.mybank.context.ApplicationConfiguration;
import io.github.ardavanghaffari.mybank.web.TransactionServlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationLauncher {

    public static void main(String[] args) throws LifecycleException {
        String portStr = System.getProperty("server.port", "8080");
        int port = Integer.parseInt(portStr);

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();

        var context = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
        var servlet = context.getBean(TransactionServlet.class);

        Context ctx = tomcat.addContext("", null);
        Wrapper wrapper = Tomcat.addServlet(ctx, "transactionServlet", servlet);
        wrapper.setLoadOnStartup(1);
        wrapper.addMapping("/*");

        tomcat.start();
    }

}
