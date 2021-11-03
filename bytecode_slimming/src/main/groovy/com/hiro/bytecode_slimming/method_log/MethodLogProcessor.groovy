package com.hiro.bytecode_slimming.method_log

import com.hiro.bytecode_slimming.BaseProcessor
import com.hiro.bytecode_slimming.ClassDataManager
import com.hiro.bytecode_slimming.Constants
import com.hiro.bytecode_slimming.SingleClassData
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

/**
 * 方法添加日志处理器
 */
class MethodLogProcessor extends BaseProcessor {

    private MethodLogProcessor() {
        super()
    }

    static MethodLogProcessor getInstance() {
        return InstanceHolder.INSTANCE
    }

    @Override
    void onAccept(List<SingleClassData> classDataList) {
        super.onAccept(classDataList)
        classDataList.each { singleClassData ->
            String slimmingClassName = singleClassData.className
            File slimmingClassFile
            if (!Utils.isValidFile((slimmingClassFile = ClassDataManager.getClassFile(slimmingClassName)))) {
                return
            }
            def cr = new ClassReader(singleClassData.fileBytes)
            def cw = new ClassWriter(ClassWriter.COMPUTE_MAXS)
            def methodLogClassVisitor = new MethodLogClassVisitor(Constants.ASM_VERSION, cw)
            cr.accept(methodLogClassVisitor, ClassReader.EXPAND_FRAMES)
            // 插入了方法进入的 log，需要将 class 数据写入文件
            if (methodLogClassVisitor.getInsertMethodEnterLog()) {
                Utils.write2File(cw.toByteArray(), slimmingClassFile)
            }
        }
    }

    private static class InstanceHolder {
        static final MethodLogProcessor INSTANCE = new MethodLogProcessor()
    }
}