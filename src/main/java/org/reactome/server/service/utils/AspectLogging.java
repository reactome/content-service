package org.reactome.server.service.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Aspect
public class AspectLogging {

    private static final Logger callLogger = LoggerFactory.getLogger("callLogger");

    /**
     * Logging around all Service Methods to see execution times
     * @param joinPoint loggingPointcut
     * @return Object returned by the method currently logged around
     * @throws Throwable exception when executing service methods
     */
    @Around("execution(public * org.reactome.server.service.controller..*(..))")
    public Object monitorExecutionTimes(ProceedingJoinPoint joinPoint) throws Throwable {
        String id = UUID.randomUUID().toString();
        String method = joinPoint.getSignature().getDeclaringType().getSimpleName() + ":" + joinPoint.getSignature().getName();
        callLogger.trace(id + ": before " + method);
        Object result = joinPoint.proceed();
        callLogger.trace(id + ": after " + method);
        return result;
    }

}
