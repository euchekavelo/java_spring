package com.example.MyBookShopApp.logging;

import com.example.MyBookShopApp.logging.dto.ClassMethodInformation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.StringJoiner;
import java.util.logging.Logger;

@Component
@Aspect
public class ExceptionInterceptorAspect {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private ClassMethodInformation getClassMethodInformation(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String[] paramsNames = signature.getParameterNames();
        String outputParamLine;
        if (paramsNames.length != 0) {
            StringJoiner stringJoiner = new StringJoiner(", ");
            Object[] paramsValue = joinPoint.getArgs();
            for (int i = 0; i < paramsNames.length; i++) {
                stringJoiner.add(paramsNames[i] + " = " + paramsValue[i]);
            }
            outputParamLine = "(" + stringJoiner + ")";
        } else
            outputParamLine = "()";

        return new ClassMethodInformation(className, methodName, outputParamLine);
    }

    @Pointcut("execution(public * *.*(..))")
    public void getExecutionAllPublicMethods(){
    }

    @Pointcut("execution(public * com.example.MyBookShopApp.controller.GlobalExceptionHandlerController.*(..))")
    public void getExecutionOfClassMethodsGlobalExceptionHandlerController(){
    }

    @Pointcut("within(@com.example.MyBookShopApp.logging.annotation.InfoLogs *)")
    public void getLabeledInfoLogs(){
    }

    @Pointcut("getLabeledInfoLogs() && getExecutionAllPublicMethods()")
    public void getExecutionAllPublicMethodsControllersWithInfoLogsAnnotation() {
    }

    @Pointcut("within(@com.example.MyBookShopApp.logging.annotation.DebugLogs *)")
    public void getLabeledDebugLogs(){
    }

    @Pointcut("getLabeledDebugLogs() && getExecutionAllPublicMethods()")
    public void getExecutionAllPublicMethodsRepositoriesAndServicesWithDebugLogsAnnotation() {
    }

    @Around("getExecutionOfClassMethodsGlobalExceptionHandlerController()")
    public Object logExecutionAllPublicMethodsControllersWithInfoAnnotation(ProceedingJoinPoint joinPoint)
            throws Throwable {

        ClassMethodInformation classMethodInformation = getClassMethodInformation(joinPoint);
        logger.info("Controller advice method '" + classMethodInformation.getClassName() + "."
                + classMethodInformation.getMethodName() + classMethodInformation.getOutputParamLine()
                + "' started running...");

        Object result = joinPoint.proceed();

        logger.info("Controller advice method '" + classMethodInformation.getClassName() + "."
                + classMethodInformation.getMethodName() + classMethodInformation.getOutputParamLine() +
                "' finished with result: " + result);

        return result;
    }

    @Before("getExecutionAllPublicMethodsControllersWithInfoLogsAnnotation() " +
            "|| getExecutionAllPublicMethodsRepositoriesAndServicesWithDebugLogsAnnotation()")
    public void logBeforeExecutingPublicControllerMethods(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> currentClass = signature.getDeclaringType();
        String className = currentClass.getSimpleName();
        String methodName = signature.getName();

        if (currentClass.isAnnotationPresent(Controller.class)) {
            logger.info("Controller method '" + className + "." + methodName + "' started running...");
        } else if (currentClass.isAnnotationPresent(Service.class)) {
            logger.fine("Service method '" + className + "." + methodName + "' started running...");
        } else if (currentClass.isAnnotationPresent(Repository.class)) {
            logger.fine("Repository method '" + className + "." + methodName + "' started running...");
        }
    }

    @AfterReturning(pointcut = "getExecutionAllPublicMethodsControllersWithInfoLogsAnnotation() " +
            "|| getExecutionAllPublicMethodsRepositoriesAndServicesWithDebugLogsAnnotation()", returning = "result")
    public Object logAfterReturningControllerResult(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> currentClass = signature.getDeclaringType();
        String className = currentClass.getSimpleName();
        String methodName = signature.getName();

        if (currentClass.isAnnotationPresent(Controller.class)) {
            logger.info("Controller method '" + className + "." + methodName + "' finished.");
        } else if (currentClass.isAnnotationPresent(Service.class)) {
            logger.fine("Service method '" + className + "." + methodName + "' finished.");
        } else if (currentClass.isAnnotationPresent(Repository.class)) {
            logger.fine("Repository method '" + className + "." + methodName + "' finished.");
        }

        return result;
    }

    @AfterThrowing(pointcut = "getExecutionAllPublicMethodsControllersWithInfoLogsAnnotation() " +
            "|| getExecutionAllPublicMethodsRepositoriesAndServicesWithDebugLogsAnnotation()", throwing = "ex")
    public void logThrowable(JoinPoint joinPoint, Throwable ex) {

        Signature signature = joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        logger.severe("An exception of type '" + ex.getClass() + "' with wording '" + ex.getMessage()
                + "' occurred while executing method '" + className + "." + methodName + "'.");
    }
}
