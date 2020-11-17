package com.hiro.bytecode_slimming

/**
 * 基础的方法内联处理器
 */
class BaseMethodInlineProcessor extends BaseProcessor {

    final Map<String, Object> methodInlineInfoMap = new HashMap<>()

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

    protected static def makeInlineMethodInfoKey(def className, def methodName) {
        return className + "#" + methodName
    }

    @Override
    void onOptimizeEnd() {
        super.onOptimizeEnd()
        // 优化结束，清除数据，触发内存回收
        methodInlineInfoMap.clear()
        System.gc()
    }
}