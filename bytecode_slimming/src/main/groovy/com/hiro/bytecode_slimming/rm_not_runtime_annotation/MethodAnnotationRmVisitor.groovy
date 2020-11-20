package com.hiro.bytecode_slimming.rm_not_runtime_annotation

import com.hiro.bytecode_slimming.Logger
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.TypePath

/**
 * 方法注解处理器
 *
 * @author hongweiqiu
 */
class MethodAnnotationRmVisitor extends MethodVisitor {
    private static final def TAG = "MethodAnnotationRmVisitor"

    MethodAnnotationRmVisitor(int api) {
        super(api)
    }

    MethodAnnotationRmVisitor(int api, MethodVisitor mv) {
        super(api, mv)
    }

    @Override
    AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationRemoveProcessor processor = AnnotationRemoveProcessor.getInstance()
        // 如果该注解运行时不可见（即为非 runtime 作用域的注解，则删除）
        if (!visible && processor.annotationCanRemove(desc)) {
            Logger.d1(TAG, "visitAnnotation, desc: $desc, visible: $visible")
            processor.addRemovedAnnotation(desc)
            processor.increaseOptimizeCount()
            return null
        }
        return super.visitAnnotation(desc, visible)
    }

    @Override
    AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        AnnotationRemoveProcessor processor = AnnotationRemoveProcessor.getInstance()
        Logger.d1(TAG, "visitParameterAnnotation, parameter: $parameter, desc: $desc, visible: $visible")
        // 如果该注解运行时不可见（即为非 runtime 作用域的注解，则删除）
        if (!visible && processor.annotationCanRemove(desc)) {
            processor.addRemovedAnnotation(desc)
            processor.increaseOptimizeCount()
            return null
        }
        return super.visitParameterAnnotation(parameter, desc, visible)
    }

    @Override
    AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
        AnnotationRemoveProcessor processor = AnnotationRemoveProcessor.getInstance()
        // 如果该注解运行时不可见（即为非 runtime 作用域的注解，则删除）
        if (!visible && processor.annotationCanRemove(desc)) {
            processor.addRemovedAnnotation(desc)
            processor.increaseOptimizeCount()
            return null
        }
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible)
    }

    @Override
    AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        AnnotationRemoveProcessor processor = AnnotationRemoveProcessor.getInstance()
        // 如果该注解运行时不可见（即为非 runtime 作用域的注解，则删除）
        if (!visible && processor.annotationCanRemove(desc)) {
            processor.addRemovedAnnotation(desc)
            processor.increaseOptimizeCount()
            return null
        }
        return super.visitTypeAnnotation(typeRef, typePath, desc, visible)
    }

    @Override
    AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        AnnotationRemoveProcessor processor = AnnotationRemoveProcessor.getInstance()
        // 如果该注解运行时不可见（即为非 runtime 作用域的注解，则删除）
        if (!visible && processor.annotationCanRemove(desc)) {
            processor.addRemovedAnnotation(desc)
            processor.increaseOptimizeCount()
            return null
        }
        return super.visitInsnAnnotation(typeRef, typePath, desc, visible)
    }

    @Override
    AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        AnnotationRemoveProcessor processor = AnnotationRemoveProcessor.getInstance()
        // 如果该注解运行时不可见（即为非 runtime 作用域的注解，则删除）
        if (!visible && processor.annotationCanRemove(desc)) {
            processor.addRemovedAnnotation(desc)
            processor.increaseOptimizeCount()
            return null
        }
        return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible)
    }
}