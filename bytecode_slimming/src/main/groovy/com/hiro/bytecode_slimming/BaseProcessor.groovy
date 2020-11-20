package com.hiro.bytecode_slimming

/**
 * 基础的 class 处理器
 *
 * @author hongweiqiu
 */
abstract class BaseProcessor {
    protected static final String TAG = "BaseProcessor"

    /* 优化的 item 项数目（具体代表什么项取决于具体的 Processor） */
    protected int optimizeCount = 0
    /* 不做优化改动的类名列表 */
    private final List<String> keepClassNameList = new LinkedList<>()

    /**
     * 判断某个类名是否在 keepClassNameList 白名单中
     */
    boolean inKeepClassList(String className) {
        if (Utils.isEmpty(className)) {
            return false
        }
        return keepClassNameList.contains(className)
    }

    void addInKeepClassList(List<String> addedClassNameList) {
        if (addedClassNameList == null) {
            return
        }
        addedClassNameList.each { className ->
            addInKeepClassList(className)
        }
    }

    /**
     * 添加参数指定的类名到 keep 类白名单列表中
     */
    void addInKeepClassList(String className) {
        if (Utils.isEmpty(className) || inKeepClassList(className)) {
            return
        }
        keepClassNameList.add(className)
    }

    /**
     * 处理器开始时回调的方法，将 optimizeCount 清 0
     */
    final void optimizeStart() {
        Logger.d3(TAG, "处理器 [$this] 优化开始")
        optimizeCount = 0
        onOptimizeStart()
    }

    void onOptimizeStart() {
    }

    /**
     * 在这个方法里面进行具体的 class 文件处理逻辑，处理完成后，
     * 需要将处理后的数据覆盖写入同一个的 class 文件中（处理的哪个文件就写入哪个文件），否则处理不会生效
     *
     * @param classDataList class 文件列表
     */
    final void accept(List<SingleClassData> classDataList) {
        if (classDataList == null || classDataList.isEmpty()) {
            return
        }
        onAccept(classDataList)
    }

    void onAccept(List<SingleClassData> classDataList) {
        // do nothing
    }

    /**
     * 在 jar 和 class 文件都处理完了之后会回调的方法
     */
    final void optimizeEnd() {
        Logger.d3(TAG, "处理器：[$this] 运行结束，共优化了: [$optimizeCount] 项")
        onOptimizeEnd()
    }

    void onOptimizeEnd() {
        // do nothing
    }

    /**
     * 指示优化的 item 数加一
     */
    final void increaseOptimizeCount() {
        optimizeCount++
    }
}