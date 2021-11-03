package com.hiro.bytecode_slimming.method_log

import com.hiro.bytecode_slimming.BaseMethodVisitor
import com.hiro.bytecode_slimming.Logger
import com.sun.org.apache.bcel.internal.generic.INVOKESTATIC
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class MethodLogMethodVisitor extends BaseMethodVisitor {

    private static final String TAG = "MethodLogMethodVisitor"

    private def needAddEnterLog = false

    private MethodLogClassVisitor methodLogClassVisitor

    MethodLogMethodVisitor(MethodLogClassVisitor methodLogClassVisitor,
                           int api, MethodVisitor mv, String className, int access, String methodName, String desc, String signature, String[] exceptions) {
        super(api, mv, className, access, methodName, desc, signature, exceptions)
        this.methodLogClassVisitor = methodLogClassVisitor
    }

    @Override
    AnnotationVisitor visitAnnotationDefault() {
        return super.visitAnnotationDefault()
    }

    @Override
    AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc == "Lcom/hiro/demo/annotation/MethodEnterLog;") {
            Logger.d3(TAG, "visitAnnotation, desc = $desc, className = $className, methodName = $methodName")
            needAddEnterLog = true
        }
        return super.visitAnnotation(desc, visible)
    }

    @Override
    void visitCode() {
        if (needAddEnterLog) {
            Logger.d3(TAG, "visitCode, insert method enter log, className = $className, methodName = $methodName")
            visitLdcInsn("BasicActivity")
            visitLdcInsn("$methodName() called".toString())
            visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I")
            visitInsn(Opcodes.POP)
            methodLogClassVisitor.setInsertMethodEnterLog(true)
            MethodLogProcessor.instance.increaseOptimizeCount()
        }
        super.visitCode()
    }
}