package com.hiro.bytecode_slimming

/**
 * 基础的 class/jar 文件处理器
 * @author hongweiqiu
 */
abstract class BaseProcessor {
    private static final def TAG = "BaseProcessor"

    /* 优化的 item 项数目（具体代表什么项取决于具体的 Processor） */
    protected def optimizeCount = 0

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
     * @param classList class 文件列表
     */
    final void accept(List<SingleClassData> classModelList) {
        if (classModelList == null || classModelList.isEmpty()) {
            return
        }
        onAccept(classModelList)
    }

    void onAccept(List<SingleClassData> classModelList) {
        // do nothing
    }

    /**
     * 在 jar 和 class 文件都处理完了之后会回调的方法
     */
    final void optimizeEnd() {
        Logger.d3(TAG, "处理器：[$this] 运行结束，共优化了: $optimizeCount 项")
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