package org.reactome.server.service.controller.graph.util;

import org.jsoup.Jsoup;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.service.exception.NotFoundTextPlainException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

public class ControllerUtils {

    public static Collection<String> getProperties(Collection objects, String attributeName) throws InvocationTargetException, IllegalAccessException {
        Collection<String> rtn = new ArrayList<>();
        for (Object object : objects) {
            rtn.add(getProperty(object, attributeName));
        }
        return rtn;
    }

    public static String getProperty(Object object, String attributeName) throws InvocationTargetException, IllegalAccessException {
        Object property = getPropertyObject(object, attributeName);
        if (property != null) return toTSV(property);
        throw new NotFoundTextPlainException("Attribute: '" + attributeName + "' has not been found for object: " + object.getClass().getSimpleName());
    }

    private static Object getPropertyObject(Object object, String attributeName) throws InvocationTargetException, IllegalAccessException {
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().toLowerCase().equals("get" + attributeName.toLowerCase())) {
                return method.invoke(object);
            }
        }
        return null;
    }

    private static String toTSV(Object object) {
        if (object instanceof DatabaseObject){
            DatabaseObject dbo = (DatabaseObject) object;
            String display;
            try {
                display = Jsoup.parse(((String) dbo.getClass().getMethod("getText").invoke(dbo))).text();
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                display = dbo.getDisplayName();
            }
            String id = dbo.getStId();
            return ((id!=null && !id.isEmpty()) ? id : dbo.getDbId()) + "\t" + display + "\t" + dbo.getSchemaClass();
        }
        if (object instanceof Collection){
            Collection<?> list = (Collection<?>) object;
            StringBuilder stringBuilder = new StringBuilder();
            for (Object o : list) {
                stringBuilder.append(toTSV(o)).append("\n");
            }
            return stringBuilder.toString();
        }
        return object.toString();
    }
}
