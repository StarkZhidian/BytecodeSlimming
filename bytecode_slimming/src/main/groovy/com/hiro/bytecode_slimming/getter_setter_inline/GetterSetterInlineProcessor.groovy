package com.hiro.bytecode_slimming.getter_setter_inline

import com.hiro.bytecode_slimming.BaseMethodInlineProcessor
import com.hiro.bytecode_slimming.ClassModel
import com.hiro.bytecode_slimming.Utils
import com.hiro.bytecode_slimming.rm_not_runtime_annotation.ClassAnnotationRmVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/**
 * getter/setter 方法内联处理器
 *
 * @author hongweiqiu
 */
class GetterSetterInlineProcessor extends BaseMethodInlineProcessor {

    static def getInstance() {
        return InstanceHolder.INSTANCE
    }

    private GetterSetterInlineProcessor() {
    }

    @Override
    void onAccept(List<ClassModel> classModelList) {
        classModelList.each {classModel ->
            ClassReader cr = new ClassReader(classModel.fileBytes)
            cr.accept(new GetterSetterInlineFirstClassVisitor(Opcodes.ASM6, null), ClassReader.EXPAND_FRAMES)
        }
        // third traversal
        classModelList.each { classModel ->
            ClassReader cr = new ClassReader(classModel.fileBytes)
            ClassWriter classWriter = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
            cr.accept(new GetterSetterInlineSecondClassVisitor(Opcodes.ASM6, classWriter), ClassReader.EXPAND_FRAMES)
            // write final bytes data to origin file
            Utils.write2File(classWriter.toByteArray(), classModel.classFile)
        }
    }

    private static class InstanceHolder {
        static final def INSTANCE = new GetterSetterInlineProcessor();
    }
}