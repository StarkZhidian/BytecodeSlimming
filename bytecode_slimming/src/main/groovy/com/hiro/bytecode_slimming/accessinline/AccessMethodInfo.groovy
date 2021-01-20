package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.tree.AbstractInsnNode

/**
 * 描述 access$xxx 方法信息的类
 */
class AccessMethodInfo {

    /* access$xxx 方法所在的类名 */
    final String className
    /* access$xxx 的方法名 */
    final String methodName
    /* 方法的描述（java 层的方法签名除去方法名） */
    final String desc
    /* access$xxx 方法内部访问(通过 getfield/putfield 指令)的字段 */
    List<OperateFieldInfo> operateFieldInfoList = new LinkedList<>()
    /* access$xxx 方法内部通过 invokespecial 指令调用方法信息 */
    List<InvokeMethodInfo> invokeMethodInfoList = new LinkedList()
    /* 当前 access$xxx 方法内部相关指令列表 */
    List<AbstractInsnNode> instructions = new LinkedList<>()
    /* 关联的类名列表 */
    List<String> relatedClassName = new LinkedList<>()

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

    void setInstructions(List<AbstractInsnNode> instructions) {
        if (instructions != null) {
            this.instructions = instructions
        }
    }

    void appendRelatedClassName(String className) {
        if (Utils.isEmpty(className) || relatedClassName.contains(className)) {
            return
        }
        relatedClassName.add(className)
    }

    @Override
    String toString() {
        return "{className: $className, methodName: $methodName, desc: $desc, operateFieldInfoList: $operateFieldInfoList, invokeMethodInfoList: $invokeMethodInfoList, relatedClassName : $relatedClassName}"
    }

    /**
     * 记录 access$xxx 方法内部访问的字段信息
     */
    static class OperateFieldInfo {
        int opcode
        String fieldClassName
        String fieldName
        String desc

        OperateFieldInfo(int opcode, String fieldClassName, String fieldName, String desc) {
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
        int opcode
        String methodClassName
        String methodName
        String desc

        InvokeMethodInfo(int opcode, String methodClassName, String methodName, String desc) {
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