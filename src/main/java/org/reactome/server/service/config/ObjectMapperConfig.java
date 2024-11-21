
package org.reactome.server.service.config;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voodoodyne.jackson.jsog.JSOGGenerator;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ObjectMapperConfig {


    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "dbId")
    public abstract static class DatabaseObjectMixin {
    }

    @JsonIdentityInfo(generator = JSOGGenerator.class)
    public abstract static class DatabaseObjectJSOGMixin {
    }


    /**
     * This default mapper works great to resolve cyclic dependency in an object, however, it's not
     * that friendly on the client side, the client side has to deal with different data types and needs full JSON
     * object.
     * @return ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.addMixIn(DatabaseObject.class, DatabaseObjectMixin.class);
        return mapper;
    }

    /**
     * This mapper use a plugin for Jackson which can serialize cyclic object in the JSOG format and also annotate any class which may contain references.
     * In the JSOG response,each time a new object is encountered, give it a unique string @id.
     * Each time a repeated object is encountered, serialize as a @ref to the existing @id.
     * @return ObjectMapper
     */
    @Bean
    @Qualifier("jsogObjectMapper")
    public ObjectMapper jsogObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.addMixIn(DatabaseObject.class, DatabaseObjectJSOGMixin.class);
        return mapper;
    }

    /*
     *Global value for including @Ref in JSOG response
     */

//    @Value("${includeRef}")
//    Boolean includeRef;
//
//   @Bean
//    public ObjectMapper objectMapper() {
//        ObjectMapper mapper = new ObjectMapper();
//       mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
//        if (includeRef) {
//            mapper.addMixIn(DatabaseObject.class, DatabaseObjectJSOGMixin.class);
//       } else {
//            mapper.addMixIn(DatabaseObject.class, DatabaseObjectMixin.class);
//        }
//        return mapper;
//   }
}


