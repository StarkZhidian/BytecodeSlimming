package com.hiro.bytecode_slimming

/**
 * BytecodeSlimming 插件扩展选项
 */
class BytecodeSlimmingExtension {
    private static final String TAG = "BytecodeSlimmingExtension"
    static final String NAME = "bytecodeSlimming"

    /* 总开关，默认启用 */
    boolean enable = true
    /* 是否开启 access$xxx 方法内联，默认开启 */
    boolean slimmingAccessInline = true
    /* 是否开启运行时不可见的注解去除，默认开启 */
    boolean slimmingNonRuntimeAnnotation = true
    /* 是否开启 R 文件瘦身，默认开启 */
    boolean slimmingR = true
    /* log 过滤级别，默认为只能输出级别为 LOG_LEVEL_2 及以上的 log 信息 */
    int logLevel = Logger.LOG_LEVEL_2

    /* 不进行 access$xxx 方法内联的类名列表 */
    final List<String> keepAccessClass = new LinkedList<>()
    /* 要保留的非运行时注解类名列表 */
    final List<String> keepAnnotationClass = new LinkedList<>()
    /* 不进行瘦身的 R 类名列表 */
    final List<String> keepRClass = new LinkedList<>()

    BytecodeSlimmingExtension keepAccessClass(String... classNames) {
        if (classNames != null) {
            keepAccessClass(Arrays.asList(classNames))
        }
        return this
    }

    BytecodeSlimmingExtension keepAccessClass(List<String> classNameList) {
        Logger.d3(TAG, "keepAccessClass = $classNameList")
        if (classNameList != null) {
            classNameList.each { className ->
                if (Utils.isEmpty(className)
                        || keepAccessClass.contains(className = Utils.getJVMClassName(className))) {
                    return
                }
                keepAccessClass.add(className)
            }
        }
        return this
    }

    BytecodeSlimmingExtension keepAnnotationClass(String... classNames) {
        if (classNames != null) {
            keepAnnotationClass(Arrays.asList(classNames))
        }
        return this
    }

    BytecodeSlimmingExtension keepAnnotationClass(List<String> classNameList) {
        if (classNameList != null) {
            classNameList.each { className ->
                if (Utils.isEmpty(className)
                        || keepAnnotationClass.contains(className = Utils.getJVMClassName(className))) {
                    return
                }
                keepAnnotationClass.add(className)
            }
        }
        return this
    }

    BytecodeSlimmingExtension keepRClass(String... classNames) {
        if (classNames != null) {
            keepRClass(Arrays.asList(classNames))
        }
        return this;
    }

    BytecodeSlimmingExtension keepRClass(List<String> classNameList) {
        Logger.d3(TAG, "keepRClass = $classNameList")
        if (classNameList != null) {
            classNameList.each { className ->
                if (Utils.isEmpty(className)
                        || keepRClass.contains(className = Utils.getJVMClassName(className))) {
                    return
                }
                keepRClass.add(className)
            }
        }
        return this
    }

    @Override
    String toString() {
        return "{enable = [$enable], slimmingAccessInline = [$slimmingAccessInline], slimmingNonRuntimeAnnotation = [$slimmingNonRuntimeAnnotation], slimmingR = [$slimmingR], logLevel = [$logLevel], \n" +
                "keepAccessClass = $keepAccessClass, keepRClass = $keepRClass, keepAnnotationClass = $keepAnnotationClass}"
    }
}