package org.reactome.server.service.controller.graph.util;

import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.service.exception.newExceptions.NotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by flo on 06/06/16.
 */
public class ControllerUtils {

    public static Collection<Object> getProperties(Collection<? extends Object> objects, String attributeName) throws InvocationTargetException, IllegalAccessException {
        Collection<Object> properties = new ArrayList<>();
        for (Object object : objects) {
            Object property = ControllerUtils.getPropertyObject(object, attributeName);
            if (property != null) properties.add(property);
        }
        if (!properties.isEmpty()) return properties;
        throw new NotFoundException("Attribute: " + attributeName + " has not been found in the System");
    }

    public static Object getProperty(Object object, String attributeName) throws InvocationTargetException, IllegalAccessException {
        Object property = getPropertyObject(object, attributeName);
        if (property != null) return property;
        throw new NotFoundException("Attribute: " + attributeName + " has not been found in the System");
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
}
