package org.reactome.server.service.config;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.Aspects;
import org.gk.persistence.MySQLAdaptor;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.graph.aop.LazyFetchAspect;
import org.reactome.server.interactors.service.PsicquicService;
import org.reactome.server.service.utils.AspectLazyLoadingPrevention;
import org.reactome.server.service.utils.TupleManager;
import org.reactome.server.service.utils.TuplesFileCheckerController;
import org.reactome.server.tools.diagram.exporter.raster.RasterExporter;
import org.reactome.server.tools.event.exporter.EventExporter;
import org.reactome.server.tools.fireworks.exporter.FireworksExporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class WebConfig implements WebMvcConfigurer {

    @Value("${tuples.custom.folder}")
    String tuplesFolder;

    @Value("${analysis.token}")
    String analysisToken;

    @Value("${diagram.json.folder}")
    String diagramPath;

    @Value("${ehld.folder}")
    String ehldPath;

    @Value("${analysis.token}")
    String analysisPath;

    @Value("${svg.summary.file}")
    String svgSummary;

    @Value("${fireworks.json.folder}")
    String fireworksPath;

    @Value("${mysql.host}")
    String mysqlHost;

    @Value("${mysql.port}")
    Integer mysqlPort;

    @Value("${mysql.database}")
    String mysqlDatabase;

    @Value("${mysql.user}")
    String mysqlUser;

    @Value("${mysql.password}")
    String mysqlPassword;


    @Bean
    public FireworksExporter fireworksExporter(@Value("${fireworks.json.folder}") String fireworkPath, @Value("${analysis.token}") String analysisPath) {
        return new FireworksExporter(fireworkPath, analysisPath);
    }


    @Bean
    public RasterExporter rasterExporter(@Value("${diagram.json.folder}") String diagramPath,
                                         @Value("${ehld.folder}") String ehldPath,
                                         @Value("${analysis.token}") String analysisPath,
                                         @Value("${svg.summary.file}") String svgSummary) {
        return new RasterExporter(diagramPath, ehldPath, analysisPath, svgSummary);
    }

    @Bean
    public EventExporter eventExporter(@Value("${diagram.json.folder}") String diagramPath,
                                       @Value("${ehld.folder}") String ehldPath,
                                       @Value("${analysis.token}") String analysisToken,
                                       @Value("${fireworks.json.folder}") String fireworksPath,
                                       @Value("${svg.summary.file}") String svgSummary) {
        return new EventExporter(diagramPath, ehldPath, analysisToken, fireworksPath, svgSummary);
    }

    @Bean
    public TokenUtils tokenUtils() {
        TokenUtils tokenUtils = new TokenUtils();
        tokenUtils.setPathDirectory(analysisToken);
        return tokenUtils;
    }

    @Bean
    public PsicquicService psicquicService() {
        return new PsicquicService();
    }

    @Bean(destroyMethod = "interrupt", name = "FileCheckerController")
    public TuplesFileCheckerController fileCheckerController() {
        TuplesFileCheckerController fileCheckerController = new TuplesFileCheckerController();
        fileCheckerController.setPathDirectory(tuplesFolder);
        fileCheckerController.setMaxSize(2684354560L); // 2684354560 = 2.5GB // 5368709120 = 5 GB // 10737418240 = 10GB
        fileCheckerController.setThreshold(524288000L); //10485760 = 10MB // 524288000 = 500MB // 1073741824 = 1GB
        fileCheckerController.setTime(10000L); // 10 sec
        fileCheckerController.setTtl(604800000L); // 1 week (SAB suggestion)
        return fileCheckerController;
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(52428800); //10 MB  // 52428800 = 50 MB // 209715200 = 200MB
        return multipartResolver;
    }

    @Bean
    public CommonsMultipartResolver commonsMultipartResolver() {
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        commonsMultipartResolver.setMaxUploadSize(52428800);
        return commonsMultipartResolver;
    }

    @Bean
    public TupleManager tupleManager() {
        TupleManager tupleManager = new TupleManager();
        tupleManager.setPathDirectory(tuplesFolder);
        return tupleManager;
    }

    @Bean
    public LazyFetchAspect lazyFetchAspect() {
        return Aspects.aspectOf(LazyFetchAspect.class);
    }

    @Bean
    public AspectLazyLoadingPrevention actionResponseLazyLoading() {
        return new AspectLazyLoadingPrevention();
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper = new ObjectMapper();
        // properties with null value, or what is considered empty, are not to be included.
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(objectMapper);

        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.valueOf("text/plain; charset=UTF-8"));
        mediaTypes.add(MediaType.valueOf("application/json; charset=UTF-8"));
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        stringHttpMessageConverter.setWriteAcceptCharset(false);
        stringHttpMessageConverter.setSupportedMediaTypes(mediaTypes);

        converters.add(mappingJackson2HttpMessageConverter);
        converters.add(stringHttpMessageConverter);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("/resources/");

        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public MySQLAdaptor mysqlDba(@Value("${mysql.host}") String mysqlHost,
                                 @Value("${mysql.database}") String mysqlDatabase,
                                 @Value("${mysql.user}") String mysqlUser,
                                 @Value("${mysql.password}") String mysqlPassword,
                                 @Value("${mysql.port}") Integer mysqlPort) throws SQLException {
        return new MySQLAdaptor(mysqlHost, mysqlDatabase, mysqlUser, mysqlPassword, mysqlPort);
    }
}
