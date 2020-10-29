package com.hiro.bytecode_slimming.accessinline
/**
 * 描述 access$xxx 方法信息的类
 */
class AccessMethodInfo {

    /* access$xxx 方法所在的类名 */
    def className
    /* access$xxx 的方法名 */
    def methodName
    /* 方法的描述（java 层的方法签名除去方法名） */
    def desc
    /* access$xxx 方法内部访问(通过 getfield 指令)的字段 */
    def operateFieldInfoList = new ArrayList()
    /* access$xxx 方法内部通过 invokespecial 指令调用方法信息 */
    def invokeMethodInfoList = new ArrayList()
    /* 当前 access$xxx 方法内部相关指令列表 */
    def instructions

    AccessMethodInfo(def className, def methodName, def desc) {
        this.className = className
        this.methodName = methodName
        this.desc = desc
    }

    void appendOperateFiledInfo(OperateFieldInfo operateFieldInfo) {
        if (operateFieldInfo == null) {
            return
        }
        operateFieldInfoList.add(operateFieldInfo)
    }

    void appendInvokeMethodInfo(InvokeMethodInfo invokeMethodInfo) {
        if (invokeMethodInfo == null) {
            return
        }
        invokeMethodInfoList.add(invokeMethodInfo)
    }

    @Override
    String toString() {
        return "{className: $className, methodName: $methodName, desc: $desc, operateFieldInfoList: " + operateFieldInfoList + ", invokeMethodInfoList: " + invokeMethodInfoList + "}"
    }

    /**
     * 记录 access$xxx 方法内部访问的字段信息
     */
    static class OperateFieldInfo {
        def opcode
        def fieldClassName
        def fieldName
        def desc

        OperateFieldInfo(def opcode, def fieldClassName, def fieldName, def desc) {
            this.opcode = opcode
            this.fieldClassName = fieldClassName
            this.fieldName = fieldName
            this.desc = desc
        }

        @Override
        String toString() {
            return "{opcode: $opcode, fieldClassName: $fieldClassName, fieldName: $fieldName, desc: $desc}"
        }
    }

    /**
     * 记录 access$xxx 方法内部调用的方法信息
     */
    static class InvokeMethodInfo {
        def opcode
        def methodClassName
        def methodName
        def desc

        InvokeMethodInfo(def opcode, def methodClassName, def methodName, def desc) {
            this.opcode = opcode
            this.methodClassName = methodClassName
            this.methodName = methodName
            this.desc = desc
        }

        @Override
        String toString() {
            return "{opcode: $opcode, methodClassName: $methodClassName, methodName: $methodName, desc: $desc}"
        }
    }

}