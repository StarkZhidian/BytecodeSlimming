package com.hiro.bytecode_slimming.rm_not_runtime_annotation

import com.hiro.bytecode_slimming.BaseProcessor
import com.hiro.bytecode_slimming.ClassModel
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

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
    void onAccept(List<ClassModel> classModelList) {
        classModelList.each {classModel ->
            ClassReader cr = new ClassReader(classModel.fileBytes)
            ClassWriter classWriter = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
            cr.accept(new ClassAnnotationRmVisitor(Opcodes.ASM6, classWriter), ClassReader.EXPAND_FRAMES)
            Utils.write2File(classWriter.toByteArray(), classModel.classFile)
        }
    }

    private static class InstanceHolder {
        static final def INSTANCE = new AnnotationRemoveProcessor()
    }
}