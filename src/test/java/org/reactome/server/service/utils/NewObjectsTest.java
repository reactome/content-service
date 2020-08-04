package org.reactome.server.service.utils;


import org.junit.*;

import org.junit.runner.RunWith;
import org.reactome.server.graph.domain.annotations.ReactomeAllowedClasses;
import org.reactome.server.graph.domain.annotations.ReactomeSchemaIgnore;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.helper.AttributeProperties;
import org.reactome.server.graph.service.helper.RelationshipDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import static junit.framework.TestCase.assertNotNull;
import static org.reactome.server.graph.service.util.DatabaseObjectUtils.lowerFirst;


import static org.junit.Assert.*;

/**
 * Help to make sure the new lasted created objects don't break any pages
 * 1. test new objects with declared attributes
 * 2. test new Class exists or not in Graph core
 * <p>
 * Release date is needed for the query, it will be null and the test returns nothing
 * when the date is not provided in the maven command, add the release date to execute the
 * test properly.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class NewObjectsTest {

    private static final Logger infoLogger = LoggerFactory.getLogger("testLogger");

    @Value("${release.date:#{null}}")
    private String releaseDate;

    @Autowired
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;

    @Test
    public void testNewObjectWithAttributes() throws CustomQueryException, ClassNotFoundException {
        if (releaseDate == null) return;
        String query = String.format("MATCH (d:DatabaseObject)<-[:created]-(i:InstanceEdit) " +
                "WHERE d.stId is not null " +
                "AND   i.dateTime >=\"%s\" " +
                "AND   i.note is null " +
                "RETURN DISTINCT d " +
                "ORDER BY d.schemaClass, d.stId", releaseDate);
        Collection<DatabaseObject> allNewObjects = advancedDatabaseObjectService.getCustomQueryResults(DatabaseObject.class, query);

        int exceptionNum = 0;
        for (DatabaseObject newObject : allNewObjects) {
            Class<? extends DatabaseObject> clazz = newObject.getClass();
            //get declared attributes
            Set<AttributeProperties> propertiesList = getAttributeTable(clazz.getName());
            ArrayList<AttributeProperties> DeclaredFields = new ArrayList<>();
            for (AttributeProperties temp : propertiesList) {
                if (temp.getOrigin().getSimpleName().equals(clazz.getSimpleName())) {
                    DeclaredFields.add(temp);
                }
            }
            //query for a specific property which are not inherited from a parent class of a new lasted recently created instances
            DatabaseObject databaseObject = advancedDatabaseObjectService.findById(newObject.getStId(), RelationshipDirection.OUTGOING);
            assertNotNull(databaseObject);
            for (AttributeProperties field : DeclaredFields) {
                try {
                    Method[] methods = newObject.getClass().getMethods();
                    for (Method method : methods) {
                        String methodName = "getStId";
                        Method getStIdMethod = newObject.getClass().getMethod(methodName);
                        String stId = (String) getStIdMethod.invoke(newObject);
                        try {
                            if (method.getName().toLowerCase().equals("get" + field.getName().toLowerCase())) {
                                method.invoke(newObject);
                            }
                        } catch (InvocationTargetException | IllegalAccessException ex) {
                            exceptionNum++;
                            infoLogger.error("{}: Invocation of {} failed because of {}", stId, method.getName(), ex.getCause().getMessage());
                        }
                    }
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                    infoLogger.info("The following exception was thrown:");
                    ex.printStackTrace();
                }
            }
        }
        assertEquals("Expecting no exception was thrown, but InvocationTargetExceptions were caught!", 0, exceptionNum);
    }

    @Test
    public void testClassName() throws CustomQueryException {
        if (releaseDate == null) return;
        String query = String.format("MATCH (d:DatabaseObject)<-[:created]-(i:InstanceEdit) " +
                "WHERE d.dbId is not null " +
                "AND   not exists(d.stId) " +
                "AND   i.dateTime >= \"%s\" " +
                "AND   i.note is null " +
                "WITH  collect( distinct d.schemaClass) AS clazz " +
                "UNWIND clazz AS clazzCollection " +
                "RETURN clazzCollection", releaseDate);
        Collection<String> allNewClassName = advancedDatabaseObjectService.getCustomQueryResults(String.class, query);

        int exceptionNum = 0;
        String packageName = DatabaseObject.class.getPackage().getName() + ".";
        for (String className : allNewClassName) {
            try {
                Class.forName(packageName + className);
            } catch (ClassNotFoundException ex) {
                exceptionNum++;
                infoLogger.error("ClassNotFoundException: {}", ex.getMessage());
            }
        }
        assertEquals("Expecting no exception was thrown, but ClassNotFoundExceptions were caught!", 0, exceptionNum);
    }

    public static Set<AttributeProperties> getAttributeTable(String className) throws ClassNotFoundException {
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
