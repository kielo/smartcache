package org.kielo.smartcache.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.SoftException;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.kielo.smartcache.SmartCache;
import org.kielo.smartcache.action.ActionResult;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
public class SmartCacheAspect {

    private static final String SEPARATOR = "#";

    private final SmartCache smartCache;

    public SmartCacheAspect(SmartCache smartCache) {
        this.smartCache = smartCache;
    }

    @Around("@annotation(org.kielo.smartcache.spring.SmartCached)")
    public Object execute(ProceedingJoinPoint pjp) throws Throwable {

        SmartCached annotation = extractMethod(pjp).getAnnotation(SmartCached.class);
        String region = annotation.region();
        String key = createKey(pjp);

        ActionResult<Object> result = smartCache.get(region, key, () -> callOriginalMethod(pjp));

        if (result.failedWithoutCacheHit()) {
            throw result.caughtException();
        }
        return result.result();
    }

    private Method extractMethod(ProceedingJoinPoint pjp) {
        return ((MethodSignature) pjp.getSignature()).getMethod();
    }

    private String createKey(ProceedingJoinPoint pjp) {
        String className = pjp.getStaticPart().getSignature().getDeclaringType().getCanonicalName();
        String methodName = extractMethod(pjp).getName();

        return className + SEPARATOR +
                methodName + SEPARATOR +
                Arrays.stream(pjp.getArgs()).map(Object::toString).collect(Collectors.joining(SEPARATOR));
    }

    public Object callOriginalMethod(ProceedingJoinPoint pjp) throws Exception {
        try {
            return pjp.proceed();
        } catch (Exception exception) {
            throw exception;
        } catch (Throwable throwable) {
            throw new SoftException(throwable);
        }
    }
}
