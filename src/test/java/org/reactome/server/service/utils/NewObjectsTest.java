package org.reactome.server.service.utils;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.graph.domain.annotations.ReactomeAllowedClasses;
import org.reactome.server.graph.domain.annotations.ReactomeSchemaIgnore;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.helper.AttributeProperties;
import org.reactome.server.graph.service.helper.RelationshipDirection;
import org.reactome.server.service.exception.NotFoundTextPlainException;
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
import java.util.*;

import static org.reactome.server.graph.service.util.DatabaseObjectUtils.lowerFirst;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources-test/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class NewObjectsTest {

    private static final Logger logger = LoggerFactory.getLogger("errorLogger");

    @Value("${release.date}")
    private String releaseDate;

    @Autowired
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;

    //private String releaseDate = "2020-03-22";

    @Test
    public void testNewObjectWithAttributes() throws CustomQueryException, ClassNotFoundException {
        String query = String.format("MATCH (d:DatabaseObject)<-[:created]-(i:InstanceEdit) " +
                "WHERE d.stId is not null " +
                "AND   i.dateTime >=\"%s\" " +
                "AND   i.note is null " +
                "RETURN DISTINCT d " +
                "ORDER BY d.schemaClass, d.stId", releaseDate);

        Collection<DatabaseObject> allNewObjects = advancedDatabaseObjectService.getCustomQueryResults(DatabaseObject.class, query);

        for (DatabaseObject newObject : allNewObjects) {
            Class<? extends DatabaseObject> clazz = newObject.getClass();
            //get declared fields
            Set<AttributeProperties> propertiesList = getAttributeTable(clazz.getName());
            ArrayList<AttributeProperties> DeclaredFields = new ArrayList<>();
            for (AttributeProperties temp : propertiesList) {
                if (temp.getOrigin().getSimpleName().equals(clazz.getSimpleName())) {
                    DeclaredFields.add(temp);
                }
            }
            //Query for a specific property which are not inherited from a parent class of a new lasted recently created instances
            if (newObject instanceof Event) {
                DatabaseObject databaseObject = advancedDatabaseObjectService.findById(newObject.getStId(), RelationshipDirection.OUTGOING);
                if (databaseObject == null)
                    throw new NotFoundTextPlainException("Id: " + databaseObject.getStId() + " has not been found in the System");
                for (AttributeProperties field : DeclaredFields) {
                    getProperty(newObject, field.getName());
                }
            }
            if (newObject instanceof PhysicalEntity) {
                DatabaseObject databaseObject = advancedDatabaseObjectService.findById(newObject.getStId(), RelationshipDirection.OUTGOING);
                if (databaseObject == null)
                    throw new NotFoundTextPlainException("Id: " + databaseObject.getStId() + " has not been found in the System");
                for (AttributeProperties field : DeclaredFields) {
                    getProperty(newObject, field.getName());
                }
            }
        }
    }


    @Test
    public void testClassName() throws CustomQueryException {
        String query = String.format("MATCH (d:DatabaseObject)<-[:created]-(i:InstanceEdit) " +
                "WHERE d.dbId is not null " +
                "AND   not exists(d.stId) " +
                "AND   i.dateTime >= \"%s\" " +
                "AND   i.note is null " +
                "WITH  collect( distinct d.schemaClass) as clazz " +
                "UNWIND clazz as clazzCollection " +
                "RETURN clazzCollection", releaseDate);

        Collection<String> allNewClass = advancedDatabaseObjectService.getCustomQueryResults(String.class, query);
        for (String className : allNewClass) {
            getClassForName(className);
        }
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

    public static Collection<String> getProperties(Collection objects, String attributeName) {
        Collection<String> rtn = new ArrayList<>();
        for (Object object : objects) {
            rtn.add(getProperty(object, attributeName));
        }
        return rtn;
    }

    public static String getProperty(Object object, String attributeName) {
        Object property = getPropertyObject(object, attributeName);
        if (property != null) return "Found";
        // throw new NotFoundTextPlainException("Attribute: '" + attributeName + "' has not been found for object: " + object.getClass().getSimpleName());
        return null;
    }

    private static Object getPropertyObject(Object object, String attributeName) {
        try {
            Method[] methods = object.getClass().getMethods();
            for (Method method : methods) {
                String methodName = "getStId";
                Method getStIdMethod = object.getClass().getMethod(methodName);
                String stId = (String) getStIdMethod.invoke(object);
                try {
                    if (method.getName().toLowerCase().equals("get" + attributeName.toLowerCase())) {
                        return method.invoke(object);
                    }
                } catch (InvocationTargetException | IllegalAccessException ex) {
                    //  Throwable cause = ex.getCause();
                    System.out.format("Stable Id is %s Invocation of %s failed because of: %s%n", stId, method.getName(), ex.getCause().getMessage());
                }
            }
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            System.out.println("The following exception was thrown:");
            ex.printStackTrace();
        }
        return null;
    }

    public static Class getClassForName(String className) {
        String packageName = DatabaseObject.class.getPackage().getName() + ".";
        Class clazz;
        try {
            clazz = Class.forName(packageName + className);
            return clazz;
        } catch (ClassNotFoundException ex) {
            System.out.format("ClassNotFoundException: %s%n", ex.getMessage());
        }
        return null;
    }
}
