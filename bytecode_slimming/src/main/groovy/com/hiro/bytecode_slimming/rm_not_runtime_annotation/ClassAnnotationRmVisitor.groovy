package com.hiro.bytecode_slimming.rm_not_runtime_annotation

import com.hiro.bytecode_slimming.Logger
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import com.hiro.bytecode_slimming.Constants


/**
 * 移除类里面的运行时不可见注解的 visitor
 *
 * @author hongweiqiu
 */
class ClassAnnotationRmVisitor extends ClassVisitor {
    static final def TAG = "ClassAnnotationRmVisitor"

    ClassAnnotationRmVisitor(int api, ClassVisitor cv) {
        super(api, cv)
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
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodAnnotationRmVisitor(
                Constants.ASM_VERSION, super.visitMethod(access, name, desc, signature, exceptions))
    }

    @Override
    FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return new FieldAnnotationRmVisitor(
                Constants.ASM_VERSION, super.visitField(access, name, desc, signature, value))
    }
}