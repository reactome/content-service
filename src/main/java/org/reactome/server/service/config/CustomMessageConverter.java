package org.reactome.server.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.reactome.server.graph.aop.LazyFetchAspect;
import org.reactome.server.graph.domain.annotations.StoichiometryView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;

/*
  CustomJackson2HttpMessageConverter
 */

@Component
public class CustomMessageConverter extends MappingJackson2HttpMessageConverter {

    private final ObjectMapper defaultObjectMapper;
    private final ObjectMapper jsogObjectMapper;
    private final LazyFetchAspect lazyFetchAspect;


    @Autowired
    public CustomMessageConverter(ObjectMapper defaultObjectMapper, @Qualifier("jsogObjectMapper") ObjectMapper jsogObjectMapper, LazyFetchAspect lazyFetchAspect) {
        super(defaultObjectMapper);
        this.defaultObjectMapper = defaultObjectMapper;
        this.jsogObjectMapper = jsogObjectMapper;
        this.lazyFetchAspect = lazyFetchAspect;
    }

    private ObjectMapper getMapper(HttpServletRequest request) {
        boolean useJsog = Boolean.parseBoolean(request.getParameter("includeRef"));
        ObjectMapper mapper = useJsog ? jsogObjectMapper : defaultObjectMapper;
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        return mapper;
    }

    public ObjectWriter getObjectWriter(HttpServletRequest request) {
        ObjectMapper mapper = getMapper(request);
        String view = request.getParameter("view");
        if (view == null) return mapper.writerWithView(StoichiometryView.Flatten.class);
        switch (view) {
            case "nested":
                return mapper.writerWithView(StoichiometryView.Nested.class);
            case "nested-aggregated":
                return mapper.writerWithView(StoichiometryView.NestedAggregated.class);
            case "flatten":
            default:
                return mapper.writerWithView(StoichiometryView.Flatten.class);
        }
    }

    public ObjectReader getObjectReader(HttpServletRequest request) {
        ObjectMapper mapper = getMapper(request);
        String view = request.getParameter("view");
        if (view == null) return mapper.readerWithView(StoichiometryView.Flatten.class);
        switch (view) {
            case "nested":
                return mapper.readerWithView(StoichiometryView.Nested.class);
            case "nested-aggregated":
                return mapper.readerWithView(StoichiometryView.NestedAggregated.class);
            case "flatten":
            default:
                return mapper.readerWithView(StoichiometryView.Flatten.class);
        }
    }


    // serializing an object to JSON, keeping type for method override
    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        ObjectWriter writer = this.getObjectWriter(request);
        this.lazyFetchAspect.setEnableAOP(false);
        writer.writeValue(outputMessage.getBody(), object);
        this.lazyFetchAspect.setEnableAOP(true);
    }

    // deserializing JSON
    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        ObjectReader reader = this.getObjectReader(request);
        return reader.readValue(inputMessage.getBody(), clazz);
    }
}
