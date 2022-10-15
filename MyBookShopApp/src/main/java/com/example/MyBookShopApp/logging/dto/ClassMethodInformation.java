package com.example.MyBookShopApp.logging.dto;

public class ClassMethodInformation {

    private String methodName;
    private String outputParamLine;

    public ClassMethodInformation(String className, String methodName, String outputParamLine) {
        this.className = className;
        this.methodName = methodName;
        this.outputParamLine = outputParamLine;
    }

    private String className;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getOutputParamLine() {
        return outputParamLine;
    }

    public void setOutputParamLine(String outputLine) {
        this.outputParamLine = outputLine;
    }
}
