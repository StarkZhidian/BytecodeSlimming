package com.hiro.bytecode_slimming.constants_field_rm

import com.hiro.bytecode_slimming.BaseProcessor
import com.hiro.bytecode_slimming.ClassDataManager
import com.hiro.bytecode_slimming.Constants
import com.hiro.bytecode_slimming.Logger
import com.hiro.bytecode_slimming.SingleClassData
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

/**
 * 类常量字段移除处理器基类
 */
abstract class BaseConstantFieldSlimmingProcessor extends BaseProcessor {
    protected static final String TAG = "BaseConstantFieldSlimmingProcessor"

    /* 记录删除的常量类文件列表 */
    protected final List<String> deletedClassFilePathList = new LinkedList<>()

    @Override
    void onAccept(List<SingleClassData> classDataList) {
        super.onAccept(classDataList)
        final BaseConstantFieldSlimmingProcessor processor = this
        // first traversal
        classDataList.each { singleClassData ->
            String slimmingClassName = singleClassData.className
            File slimmingClassFile
            if (canRemoveConstantFields(slimmingClassName)
                    && Utils.isValidFile((slimmingClassFile = ClassDataManager.getClassFile(slimmingClassName)))) {
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
                ConstantFieldSlimmingScanClassVisitor constantFieldSlimmingScanClassVisitor =
                        new ConstantFieldSlimmingScanClassVisitor(
                                Constants.ASM_VERSION, classWriter, processor)
                new ClassReader(slimmingClassFile.bytes).accept(
                        constantFieldSlimmingScanClassVisitor, ClassReader.EXPAND_FRAMES)
                // 当前常量类文件可以删除（里面的字段全部被移除了），执行文件删除逻辑，并且记录删除的文件路径
                if (constantFieldSlimmingScanClassVisitor.canDeleteClassFile && Utils.deleteFile(slimmingClassFile)) {
                    deletedClassFilePathList.add(slimmingClassFile.getAbsolutePath())
                } else if (constantFieldSlimmingScanClassVisitor.dataIsChanged) {
                    // 如果类访问器内部发生了数据改变（移除了某些常量字段），则需要将修改后的类数据写回对应的文件中
                    Utils.write2File(classWriter.toByteArray(), slimmingClassFile)
                }
            }
        }
        // second traversal
        classDataList.each { singleClassData ->
            String fieldInlineClassName = singleClassData.className
            File fieldInlineClassFile
            if (!canRemoveConstantFields(fieldInlineClassName)
                    && Utils.isValidFile((fieldInlineClassFile = ClassDataManager.getClassFile(fieldInlineClassName)))) {
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
                ConstantFieldInlineClassVisitor constantFieldInlineClassVisitor =
                        new ConstantFieldInlineClassVisitor(Constants.ASM_VERSION, classWriter)
                new ClassReader(fieldInlineClassFile.bytes).accept(constantFieldInlineClassVisitor, ClassReader.EXPAND_FRAMES)
                // 如果内部数据改变了，则需要将修改后的类数据写回对应的文件中
                if (constantFieldInlineClassVisitor.dataIsChanged) {
                    Utils.write2File(classWriter.toByteArray(), fieldInlineClassFile)
                }
            }
        }
    }

    @Override
    void onOptimizeEnd() {
        super.onOptimizeEnd()
        // recycler memory
        deletedClassFilePathList.clear()
        Logger.d3(getLogTag(), "被删除的类文件路径：$deletedClassFilePathList, " +
                "删除的类文件数: ${deletedClassFilePathList.size()}")
    }

    /**
     * 判断参数给定的类名是否可以进行内部常量值移除逻辑
     *
     * @param className JVM 层的类名描述(例：java/lang/String)
     * @return true/false：能/不能进行内部常量值移除逻辑
     */
    protected abstract boolean canRemoveConstantFields(String className);

    /**
     * 获取当前类打印的关键 log 的 tag，子类可重写
     */
    protected String getLogTag() {
        return TAG + "_" + this
    }
}