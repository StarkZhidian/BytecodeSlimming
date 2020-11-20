package com.hiro.bytecode_slimming.rm_not_runtime_annotation

import com.hiro.bytecode_slimming.Logger
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.TypePath

/**
 * 字段注解处理器
 */
class FieldAnnotationRmVisitor extends FieldVisitor {
    private static final def TAG = "FieldAnnotationRmVisitor"

    FieldAnnotationRmVisitor(int api) {
        super(api)
    }

    FieldAnnotationRmVisitor(int api, FieldVisitor fv) {
        super(api, fv)
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
    AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        AnnotationRemoveProcessor processor = AnnotationRemoveProcessor.getInstance()
        // 如果该注解运行时不可见（即为非 runtime 作用域的注解，则删除）
        if (!visible && processor.annotationCanRemove(desc)) {
            Logger.d1(TAG, "visitTypeAnnotation, desc: $desc, visible: $visible")
            processor.addRemovedAnnotation(desc)
            processor.increaseOptimizeCount()
            return null
        }
        return super.visitTypeAnnotation(typeRef, typePath, desc, visible)
    }
}