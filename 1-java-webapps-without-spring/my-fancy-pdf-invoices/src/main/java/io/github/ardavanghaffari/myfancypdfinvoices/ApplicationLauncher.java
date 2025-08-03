package io.github.ardavanghaffari.myfancypdfinvoices;

import io.github.ardavanghaffari.myfancypdfinvoices.web.PdfInvoicesServlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

public class ApplicationLauncher {

    /**
     * This isn't necessarily what you would do in real-life, though frameworks like Spring Security use
     * this approach a lot.
     *
     * @see <a href="https://github.com/spring-projects/spring-security/blob/main/web/src/main/java/org/springframework/security/web/authentication/ui/DefaultLoginPageGeneratingFilter.java">Spring Security DefaultLoginPageGeneratingFilter</a>
     */
    public static void main(String[] args) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();

        Context ctx = tomcat.addContext("", null);
        Wrapper servlet = Tomcat.addServlet(ctx, "myFirstServlet", new PdfInvoicesServlet());
        servlet.setLoadOnStartup(1);
        servlet.addMapping("/*");

        tomcat.start();
    }
}