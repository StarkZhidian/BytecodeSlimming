package com.hiro.bytecode_slimming.rm_not_runtime_annotation

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor

/**
 * 方法注解处理器
 * @author hongweiqiu
 */
class MethodAnnotationRmVisitor extends MethodVisitor {
    static final def TAG = "MethodAnnotationRmVisitor"

    MethodAnnotationRmVisitor(int api) {
        super(api)
    }

    MethodAnnotationRmVisitor(int api, MethodVisitor mv) {
        super(api, mv)
    }

    @Override
    AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        // 如果该注解运行时不可见（即为非 runtime 作用域的注解，则删除）
        if (!visible) {
            println "$TAG, visitAnnotation, desc: $desc, visible: $visible"
            AnnotationRemoveProcessor.getInstance().increaseOptimizeCount()
            return null
        }
        return super.visitAnnotation(desc, visible)
    }

    @Override
    AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        println "$TAG, visitParameterAnnotation, parameter: $parameter, desc: $desc, visible: $visible"
        // 如果该注解运行时不可见（即为非 runtime 作用域的注解，则删除）
        if (!visible) {
            AnnotationRemoveProcessor.getInstance().increaseOptimizeCount()
            return null
        }
        return super.visitParameterAnnotation(parameter, desc, visible)
    }


}