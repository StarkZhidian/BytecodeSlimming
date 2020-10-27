package com.hiro.bytecode_slimming
/**
 * 基础的方法内联处理器
 * @author hongweiqiu
 */
class BaseMethodInlineProcessor extends BaseProcessor {

    Map<String, Object> methodInlineInfoMap = new HashMap<>()

    /**
     * 添加一个需要内联的方法信息
     * @param className 内联方法所属的类名
     * @param methodName 内联方法名
     * @param inlineMethodInfo 内联方法内部的信息
     */
    void appendInlineMethod(def className, def methodName, def inlineMethodInfo) {
        if (Utils.isEmpty(className) || Utils.isEmpty(methodName) || inlineMethodInfo == null) {
            return
        }
        methodInlineInfoMap.put(makeInlineMethodInfoKey(className, methodName), inlineMethodInfo)
    }

    static def makeInlineMethodInfoKey(def className, def methodName) {
        return className + "#" + methodName
    }


}