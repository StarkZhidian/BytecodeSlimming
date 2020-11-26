package com.hiro.bytecode_slimming.r_slimming

import com.hiro.bytecode_slimming.BaseMethodVisitor
import com.hiro.bytecode_slimming.Constants
import com.hiro.bytecode_slimming.Logger
import org.objectweb.asm.MethodVisitor

/**
 * 内联方法中使用到的 R.xxx.xxx 的 MethodVisitor
 */
class RFieldInlineMethodVisitor extends BaseMethodVisitor {
    private static final String TAG = "RFieldInlineMethodVisitor"

    private BaseRSlimmingClassVisitor baseRSlimmingClassVisitor

    RFieldInlineMethodVisitor(MethodVisitor mv, String className,
                              int access, String methodName, String desc,
                              String signature, String[] exceptions,
                              BaseRSlimmingClassVisitor baseRSlimmingClassVisitor) {
        super(Constants.ASM_VERSION, mv, className, access, methodName, desc, signature, exceptions)
        this.baseRSlimmingClassVisitor = baseRSlimmingClassVisitor
    }

    @Override
    void visitFieldInsn(int opcode, String owner, String name, String desc) {
        Integer rFieldValue = RFieldRecorder.getInstance().getRFieldValue(owner, name)
        if (rFieldValue != null) {
            super.visitLdcInsn(rFieldValue)
            // 标记改类的数据已经被修改
            if (baseRSlimmingClassVisitor != null) {
                baseRSlimmingClassVisitor.dataIsChanged = true
            }
            Logger.d1(TAG, "inline r field, class: $owner, name: $name, to value: $rFieldValue")
        } else {
            super.visitFieldInsn(opcode, owner, name, desc)
        }
    }
}