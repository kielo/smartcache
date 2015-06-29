package org.kielo.smartcache.spring;

import com.codahale.metrics.MetricRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.kielo.smartcache.action.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class SmartCacheAspect {

    private final org.kielo.smartcache.SmartCache smartCache;
    private final MetricRegistry metricRegistry;

    @Autowired
    public SmartCacheAspect(org.kielo.smartcache.SmartCache smartCache, MetricRegistry metricRegistry) {
        this.smartCache = smartCache;
        this.metricRegistry = metricRegistry;
    }

    @Around("@annotation(org.kielo.smartcache.spring.SmartCached)")
    public Object execute(ProceedingJoinPoint pjp) throws Throwable {
        SmartCached smartCacheAnnotation = getCachedAnnotation(pjp);
        String region = smartCacheAnnotation.region();
        String className = pjp.getStaticPart().getSignature().getDeclaringType().getCanonicalName();
        String methodName = ((MethodSignature) pjp.getSignature()).getMethod().getName();
        String key = className + "|" + methodName + "|" + Arrays.stream(pjp.getArgs()).map(arg -> arg.toString()).collect(Collectors.joining("|"));
        String metricPrefix = smartCacheAnnotation.metricPrefix();

        ActionResult<Object> categoryActionResult = smartCache.get(region, key, () -> callOriginalMethod(pjp));

        if (categoryActionResult.failed() && !categoryActionResult.isFromCache()) {
            metricRegistry.meter(metricPrefix + ".error").mark();
            throw categoryActionResult.caughtException();
        }
        if (categoryActionResult.failed() && categoryActionResult.isFromCache()) {
            metricRegistry.meter(metricPrefix + ".stale-cache-hit").mark();
        } else if (categoryActionResult.isFromCache()) {
            metricRegistry.meter(metricPrefix + ".cache-hit").mark();
        } else {
            metricRegistry.meter(metricPrefix + ".service-request").mark();
        }
        return categoryActionResult.result();
    }

    private SmartCached getCachedAnnotation(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(SmartCached.class);
    }

    public Object callOriginalMethod(ProceedingJoinPoint pjp) throws Exception {
        try {
            return pjp.proceed();
        } catch (Exception ex) {
            throw ex;
        } catch (Throwable throwable) {
            throw new Exception(throwable);
        }
    }

}
