package com.logisticsapplication.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ServiceExecutionLoggingAspect {

    @Around("execution(* com.logisticsapplication.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsedMs = System.currentTimeMillis() - startedAt;
            log.info("Service method {} completed in {} ms", joinPoint.getSignature(), elapsedMs);
            return result;
        } catch (Throwable throwable) {
            long elapsedMs = System.currentTimeMillis() - startedAt;
            log.warn(
                    "Service method {} failed in {} ms: {}",
                    joinPoint.getSignature(),
                    elapsedMs,
                    throwable.getMessage()
            );
            throw throwable;
        }
    }
}
