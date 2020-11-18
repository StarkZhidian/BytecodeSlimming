package com.hiro.bytecode_slimming

import com.hiro.bytecode_slimming.accessinline.AccessMethodInfo

/**
 * 基础的方法内联处理器
 */
class BaseMethodInlineProcessor<T> extends BaseProcessor {

    final Map<String, T> methodInlineInfoMap = new HashMap<>()

    /**
     * 添加一个需要内联的方法描述对象信息
     *
     * @param className 内联方法所属的类名
     * @param methodName 内联方法名
     * @param desc 内联方法在 JVM 层的描述
     * @param inlineMethodInfo 内联方法内部的信息
     */
    void appendInlineMethod(String className, String methodName, String desc, T inlineMethodInfo) {
        if (Utils.isEmpty(className) || Utils.isEmpty(methodName) || inlineMethodInfo == null) {
            return
        }
        methodInlineInfoMap.put(makeInlineMethodInfoKey(className, methodName, desc), inlineMethodInfo)
    }

    /**
     * 通过类名、方法名作为 key 获取对应的内联方法描述对象信息
     *
     * @param className 内联方法所在类名
     * @param methodName 内联方法名
     * @param desc 内联方法在 JVM 层的描述
     * @return 内联方法描述对象信息
     */
    T getInlineMethod(String className, String methodName, String desc) {
        if (Utils.isEmpty(className) || Utils.isEmpty(methodName) || Utils.isEmpty(desc)) {
            return null
        }
        return methodInlineInfoMap.get(makeInlineMethodInfoKey(className, methodName, desc))
    }

    protected static def makeInlineMethodInfoKey(String className, String methodName, String desc) {
        return className + "#" + methodName + "|" + desc
    }

    @Override
    void onOptimizeEnd() {
        super.onOptimizeEnd()
        // 优化结束，清除数据，触发内存回收
        methodInlineInfoMap.clear()
        System.gc()
    }
}