package com.hiro.bytecode_slimming.rm_not_runtime_annotation

import com.hiro.bytecode_slimming.BaseProcessor
import com.hiro.bytecode_slimming.SingleClassData
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import com.hiro.bytecode_slimming.Constants

/**
 * Class 文件中运行时不可见的注解清除处理器
 * @author hongweiqiu
 */
class AnnotationRemoveProcessor extends BaseProcessor {

    private AnnotationRemoveProcessor() {
    }

    static def getInstance() {
        return InstanceHolder.INSTANCE
    }

    @Override
    void onAccept(List<SingleClassData> classModelList) {
        classModelList.each {classModel ->
            ClassReader cr = new ClassReader(classModel.fileBytes)
            ClassWriter classWriter = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
            cr.accept(new ClassAnnotationRmVisitor(Constants.ASM_VERSION, classWriter), ClassReader.EXPAND_FRAMES)
            Utils.write2File(classWriter.toByteArray(), classModel.classFile)
        }
    }

    private static class InstanceHolder {
        static final def INSTANCE = new AnnotationRemoveProcessor()
    }
}