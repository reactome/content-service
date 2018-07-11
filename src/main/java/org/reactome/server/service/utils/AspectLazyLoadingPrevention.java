package org.reactome.server.service.utils;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.reactome.server.graph.domain.model.DatabaseObject;

import java.util.Collection;
import java.util.Map;

@Aspect
public class AspectLazyLoadingPrevention {

    //private static final List<Object> list = Collections.synchronizedList(new ArrayList<>());

    @AfterReturning(value = "controllerMethod()", returning = "response")
    public void afterControllerMethodReturning(Object response) {
        preventLazyLoading(response, true);
    }

    //This method needs to be called right after the serialisation happens. The problem is that it
    //cannot be intercepted with Aspect so the method will be called from the CustomRequestFilter
//    static void restoreObjectsLazyLoadingPrevention() {
//        synchronized (list) {
//            for (Object object : list) {
//               preventLazyLoading(object, false);
//            }
//            list.clear();
//        }
//    }

    @Pointcut(value = "execution(public * org.reactome.server.service.controller.graph.*.*(..)) " +
            "|| execution(public * org.reactome.server.service.controller.interactors.*.*(..))")
    public void controllerMethod() {}

    private static void preventLazyLoading(Object obj, boolean prevent) {
        if (obj == null) return;

        if (obj instanceof DatabaseObject) {
            ((DatabaseObject) obj).preventLazyLoading(prevent);
//            if (prevent) {
//                synchronized (list) {
//                    list.add(obj);
//                }
//            }
        } else if (obj instanceof Collection) {
            for (Object o : ((Collection) obj)) {
                preventLazyLoading(o, prevent);
            }
        } else if (obj instanceof Map) {
            Map map = (Map) obj;
            for (Object o : map.keySet()) {
                preventLazyLoading(o, prevent);
            }
            for (Object o : map.values()) {
                preventLazyLoading(o, prevent);
            }
        }
    }

}
