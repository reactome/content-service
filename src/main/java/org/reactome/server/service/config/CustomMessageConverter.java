package org.reactome.server.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Autowired
    public CustomMessageConverter(ObjectMapper defaultObjectMapper, @Qualifier("jsogObjectMapper") ObjectMapper jsogObjectMapper) {
        super(defaultObjectMapper);
        this.defaultObjectMapper = defaultObjectMapper;
        this.jsogObjectMapper = jsogObjectMapper;
    }

    public ObjectMapper getObjectMapper(HttpServletRequest request) {
        boolean useJsog = Boolean.parseBoolean(request.getParameter("includeRef"));
        return useJsog ? jsogObjectMapper : defaultObjectMapper;
    }

    // serializing an object to JSON, keeping type for method override
    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        ObjectMapper objectMapper = this.getObjectMapper(request);
        objectMapper.writeValue(outputMessage.getBody(), object);
    }

    // deserializing JSON
    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        ObjectMapper objectMapper = this.getObjectMapper(request);
        return objectMapper.readValue(inputMessage.getBody(), clazz);
    }
}
