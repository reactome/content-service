package org.reactome.server.service.utils;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.graph.domain.annotations.ReactomeAllowedClasses;
import org.reactome.server.graph.domain.annotations.ReactomeSchemaIgnore;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.helper.AttributeProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static org.reactome.server.graph.service.util.DatabaseObjectUtils.lowerFirst;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources-test/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class NewInstancesTest extends BaseTest{

    @Autowired
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;



    private String query =  "MATCH (d:DatabaseObject)<-[:created]-(i:InstanceEdit) "+
            "WHERE d.stId is not null "+
            "AND   i.dateTime >= \"2020-03-22\" "+
            "AND   i.note is null "+
            "RETURN DISTINCT d "+
            "ORDER BY d.schemaClass, d.stId";

    @Test
    public void testAllNewInstances() throws Exception {

        Collection<DatabaseObject> allNewInstances =  advancedDatabaseObjectService.getCustomQueryResults(DatabaseObject.class, query);

        for (DatabaseObject databaseObject : allNewInstances) {
            Class<? extends DatabaseObject> clazz = databaseObject.getClass();

            Set<AttributeProperties> propertiesList = getAttributeTable(clazz.getName());
            Field[] fields = clazz.getDeclaredFields();


            ArrayList<AttributeProperties>  DeclaredFields = new ArrayList<>();
            for(AttributeProperties temp: propertiesList){
                if (temp.getOrigin().getSimpleName().equals(clazz.getSimpleName())){
                    DeclaredFields.add(temp);
                }
            }

            if (databaseObject instanceof Event) {
                mockMvcGetResult("/data/query/" + databaseObject.getStId());
              /*  for (Field field : fields) {
                   mockMvcGetResult("/data/query/" + databaseObject.getStId() + "/" + field.getName());
                }*/

                for (AttributeProperties field : DeclaredFields) {
                    String t = databaseObject.getStId();
                    String t2 = field.getName();
                    mockMvcGetResult("/data/query/" + databaseObject.getStId() + "/" + field.getName());
                }

                mockMvcGetResult("/data/participants/" + databaseObject.getStId());
                if (databaseObject instanceof Pathway) {
                    //pathways controller...
                    mockMvcGetResult("/pathway/" + databaseObject.getStId() + "/containedEvents");
                }
            } else if (databaseObject instanceof PhysicalEntity) {
                mockMvcGetResult("/data/entity/" + databaseObject.getStId() + "/otherForms");
            }
        }
    }

    @Test
    public void testNewClassSchemaPAge() throws Exception {
        // get All classes that extends DATABASEOBJECT NonsenseMutation
        // for with classname
        mockMvcGetResult("/schema/NonsenseMutation"); //ModifiedNucleotide // TranscriptionalModification
    }
    // TODO: Pay attention to InstanceEdit.note for values like "UniProt Update on 2020-02-16" 9676484 │"ENSEMBL:ENSG00000276011 KIR2DL2"│9676415 │"UniProt Update on 2020-02-16"


    public static Set<AttributeProperties> getAttributeTable(String className) throws ClassNotFoundException {
      //  String packageName = DatabaseObject.class.getPackage().getName() + ".";
      //  Class clazz = Class.forName(packageName + className);

        Class clazz = Class.forName(className);
        Set<AttributeProperties> propertiesList = new TreeSet<>();

        while (clazz != null && !clazz.getClass().equals(Object.class)) {
            for (Method method : clazz.getDeclaredMethods()) {
                String methodName = method.getName();

                if (method.getAnnotation(ReactomeSchemaIgnore.class) == null
                        && methodName.startsWith("get")
                        && !methodName.equals("getClass")
                        && !methodName.startsWith("getSuperclass")
                        && !methodName.contains("_aroundBody")) { // aspectj injected methods

                    AttributeProperties properties = getAttributeProperties(method);
                    properties.setOrigin(clazz);

                    propertiesList.add(properties);
                }
            }

            // Didn't find the field in the given class. Check the Superclass.
            clazz = clazz.getSuperclass();
        }
        return propertiesList;
    }

    private static AttributeProperties getAttributeProperties(Method method) {
        AttributeProperties properties = new AttributeProperties();
        properties.setName(lowerFirst(method.getName().substring(3)));
        Type returnType = method.getGenericReturnType();
        Annotation annotation = method.getAnnotation(ReactomeAllowedClasses.class);
        if (returnType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) returnType;
            Type[] typeArguments = type.getActualTypeArguments();
            properties.setCardinality("+");
            if (typeArguments.length > 0) {
                if (annotation == null) {
                    properties.addAttributeClass((Class) typeArguments[0]);
                } else {
                    for (Class<? extends DatabaseObject> clazz : ((ReactomeAllowedClasses) annotation).allowed()) {
                        properties.addAttributeClass(clazz);
                    }
                }
            }
        } else {
            properties.setCardinality("1");
            if (annotation == null) {
                properties.addAttributeClass((Class) returnType);
            } else {
                for (Class<? extends DatabaseObject> clazz : ((ReactomeAllowedClasses) annotation).allowed()) {
                    properties.addAttributeClass(clazz);
                }
            }
        }
        return properties;
    }

}

