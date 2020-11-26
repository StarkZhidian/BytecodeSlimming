package com.hiro.bytecode_slimming.r_slimming

import com.hiro.bytecode_slimming.BaseProcessor
import com.hiro.bytecode_slimming.ClassDataManager
import com.hiro.bytecode_slimming.Constants
import com.hiro.bytecode_slimming.Logger
import com.hiro.bytecode_slimming.SingleClassData
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

/**
 * R 文件瘦身处理器
 */
class RSlimmingProcessor extends BaseProcessor {

    private static final String[] R_CLASS_PURE_NAME = ['R$anim',
                                                       'R$array',
                                                       'R$attr',
                                                       'R$bool',
                                                       'R$color',
                                                       'R$dimen',
                                                       'R$drawable',
                                                       'R$id',
                                                       'R$integer',
                                                       'R$layout',
                                                       'R$mipmap',
                                                       'R$raw',
                                                       'R$string',
                                                       'R$style',
                                                       'R$styleable',
                                                       'R$xml']

    /* 记录删除的 R 类文件列表 */
    private List<String> deletedClassFilePathList = new LinkedList<>()

    private RSlimmingProcessor() {
    }

    static RSlimmingProcessor getInstance() {
        return InstanceHolder.INSTANCE
    }

    @Override
    void onAccept(List<SingleClassData> classDataList) {
        super.onAccept(classDataList)
        // first traversal
        classDataList.each { singleClassData ->
            String rClassName = singleClassData.className
            File rClassFile
            if (isRClass(rClassName)
                    && (!inKeepClassList(rClassName))
                    && Utils.isValidFile((rClassFile = ClassDataManager.getClassFile(rClassName)))) {
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
                RSlimmingScanClassVisitor rSlimmingClassVisitor =
                        new RSlimmingScanClassVisitor(Constants.ASM_VERSION, classWriter)
                new ClassReader(rClassFile.bytes).accept(rSlimmingClassVisitor, ClassReader.EXPAND_FRAMES)
                // 当前 R 类文件可以删除（里面的字段全部被移除了），执行文件删除逻辑，并且记录删除的文件路径
                if (rSlimmingClassVisitor.canDeleteClassFile && Utils.deleteFile(rClassFile)) {
                    deletedClassFilePathList.add(rClassFile.getAbsolutePath())
                } else if (rSlimmingClassVisitor.dataIsChanged) {
                    // 如果类访问器内部发生了数据改变（移除了某些常量字段），则需要将修改后的类数据写回对应的文件中
                    Utils.write2File(classWriter.toByteArray(), rClassFile)
                }
            }
        }
        // second traversal
        classDataList.each { singleClassData ->
            String rClassName = singleClassData.className
            File rClassFile
            if (!isRClass(rClassName)
                    && Utils.isValidFile((rClassFile = ClassDataManager.getClassFile(rClassName)))) {
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
                RFieldInlineClassVisitor rFieldInlineClassVisitor =
                        new RFieldInlineClassVisitor(Constants.ASM_VERSION, classWriter)
                new ClassReader(rClassFile.bytes).accept(rFieldInlineClassVisitor, ClassReader.EXPAND_FRAMES)
                // 如果内部数据改变了，则需要将修改后的类数据写回对应的文件中
                if (rFieldInlineClassVisitor.dataIsChanged) {
                    Utils.write2File(classWriter.toByteArray(), rClassFile)
                }
            }
        }
    }

    @Override
    void onOptimizeEnd() {
        super.onOptimizeEnd()
        // recycler memory
        deletedClassFilePathList.clear()
        Logger.d3(TAG, "被删除的 R 文件路径：$deletedClassFilePathList, " +
                "删除的 R 文件数: ${deletedClassFilePathList.size()}")
    }

    private static boolean isRClass(String className) {
        if (Utils.isEmpty(className)) {
            return false
        }
        String pureClassName = Utils.getPureClassName(className)
        for (String rClassName : R_CLASS_PURE_NAME) {
            if (Utils.textEquals(pureClassName, rClassName)) {
                return true
            }
        }
        return false
    }

    private static class InstanceHolder {
        static final RSlimmingProcessor INSTANCE = new RSlimmingProcessor()
    }
}