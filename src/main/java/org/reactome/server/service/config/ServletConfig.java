package org.reactome.server.service.config;

import org.apache.catalina.Context;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.reactome.server.service.utils.CustomRequestFilter;
import org.reactome.server.utils.proxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

@Configuration
@PropertySource("classpath:service.properties")
public class ServletConfig {
    @Value("${proxy.host}")
    private String proxyHost;

    @Bean
    public ServletRegistrationBean<ProxyServlet> pluginsServletBean() {
        ServletRegistrationBean<ProxyServlet> bean = new ServletRegistrationBean<>(new ProxyServlet(), "/plugins/*");
        bean.setName("Plugins");
        bean.setInitParameters(Map.of(
                "proxyHost", this.proxyHost,
                "proxyPort", "80",
                "proxyPath", "/plugins")
        );
        return bean;
    }

    @Bean
    public ServletRegistrationBean<ProxyServlet> mediaServletBean() {
        ServletRegistrationBean<ProxyServlet> bean = new ServletRegistrationBean<>(new ProxyServlet(), "/media/*");
        bean.setName("Media");
        bean.setInitParameters(Map.of(
                "proxyHost", this.proxyHost,
                "proxyPort", "80",
                "proxyPath", "/media")
        );
        return bean;
    }

    @Bean
    public ServletRegistrationBean<ProxyServlet> templatesServletBean() {
        ServletRegistrationBean<ProxyServlet> bean = new ServletRegistrationBean<>(new ProxyServlet(), "/templates/*");
        bean.setName("Templates");
        bean.setInitParameters(Map.of(
                "proxyHost", this.proxyHost,
                "proxyPort", "80",
                "proxyPath", "/templates")
        );
        return bean;
    }

    @Bean
    public ServletRegistrationBean<ProxyServlet> modulesServletBean() {
        ServletRegistrationBean<ProxyServlet> bean = new ServletRegistrationBean<>(new ProxyServlet(), "/modules/*");
        bean.setName("Modules");
        bean.setInitParameters(Map.of(
                "proxyHost", this.proxyHost,
                "proxyPort", "80",
                "proxyPath", "/modules")
        );
        return bean;
    }

    @Bean
    public ServletRegistrationBean<ProxyServlet> contentServletBean() {
        ServletRegistrationBean<ProxyServlet> bean = new ServletRegistrationBean<>(new ProxyServlet(), "/content/*");
        bean.setName("Content");
        bean.setInitParameters(Map.of(
                "proxyHost", this.proxyHost,
                "proxyPort", "80",
                "proxyPath", "/content")
        );
        return bean;
    }

    @Bean
    public FilterRegistrationBean<CustomRequestFilter> CustomRequestFilter() {
        CustomRequestFilter customRequestFilter = new CustomRequestFilter();
        FilterRegistrationBean<CustomRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(customRequestFilter);
        registration.addUrlPatterns("/*");
        registration.setName("crs");
        return registration;
    }

    //FileNotFoundException warning during embedded Tomcat startup and try to scan jars from classloader, disable the StandardJarScanner for manifest files
    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                ((StandardJarScanner) context.getJarScanner()).setScanManifest(false);
            }
        };
    }
}
