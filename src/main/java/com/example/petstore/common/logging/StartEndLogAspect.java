package com.example.petstore.common.logging;

import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@AllArgsConstructor
public class StartEndLogAspect {

  private final PetStoreLogger petStoreLogger;

  @Around("@annotation(startEndLog)")
  public Object logExecution(ProceedingJoinPoint joinPoint, StartEndLog startEndLog)
      throws Throwable {
    String serviceName =
        startEndLog.value().isEmpty()
            ? joinPoint.getTarget().getClass().getSimpleName()
            : startEndLog.value();

    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String input = buildInputLog(signature.getParameterNames(), joinPoint.getArgs());

    petStoreLogger.info("start", serviceName, input);

    try {
      Object result = joinPoint.proceed();
      petStoreLogger.info("complete", serviceName, String.valueOf(result));
      return result;
    } catch (Exception e) {
      petStoreLogger.error("error", e, serviceName, e.getMessage());
      throw e;
    }
  }

  private String buildInputLog(String[] paramNames, Object[] args) {
    if (paramNames == null || args == null) return "{}";
    StringBuilder sb = new StringBuilder("{");
    for (int i = 0; i < args.length; i++) {
      if (i > 0) sb.append(", ");
      sb.append(paramNames[i] != null ? paramNames[i] : "arg" + i).append("=").append(args[i]);
    }
    sb.append("}");
    return sb.toString();
  }
}
