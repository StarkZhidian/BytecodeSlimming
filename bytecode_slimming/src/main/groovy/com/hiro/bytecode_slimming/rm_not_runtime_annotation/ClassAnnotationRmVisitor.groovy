package com.hiro.bytecode_slimming.rm_not_runtime_annotation

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 移除类里面的运行时不可见注解的 visitor
 * @author hongweiqiu
 */
class ClassAnnotationRmVisitor extends ClassVisitor {
    static final def TAG = "ClassAnnotationRmVisitor"

    ClassAnnotationRmVisitor(int api) {
        super(api)
    }

    ClassAnnotationRmVisitor(int api, ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        println "$TAG, visitAnnotation, desc: $desc, visible: $visible"
        // 如果该注解运行时不可见（即为非 runtime 作用域的注解，则删除）
        if (!visible) {
            AnnotationRemoveProcessor.getInstance().increaseOptimizeCount()
            return null
        }
        return super.visitAnnotation(desc, visible)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodAnnotationRmVisitor(
                Opcodes.ASM6, super.visitMethod(access, name, desc, signature, exceptions))
    }

    @Override
    FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return new FieldAnnotationRmVisitor(
                Opcodes.ASM6, super.visitField(access, name, desc, signature, value))
    }
}