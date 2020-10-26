package com.hiro.bytecode_slimming.rm_not_runtime_annotation

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.FieldVisitor

class FieldAnnotationRmVisitor extends FieldVisitor {
    static final def TAG = "FieldAnnotationRmVisitor"

    FieldAnnotationRmVisitor(int api) {
        super(api)
    }

    FieldAnnotationRmVisitor(int api, FieldVisitor fv) {
        super(api, fv)
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
}