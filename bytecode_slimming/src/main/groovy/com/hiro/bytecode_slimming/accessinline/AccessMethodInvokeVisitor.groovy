package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.BaseMethodVisitor
import com.hiro.bytecode_slimming.Constants
import com.hiro.bytecode_slimming.Logger
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 过滤且记录在方法中调用了 access$xxx 方法的类文件，
 * 后续需要将这些类文件调用 access$xxx 方法的地方改为直接执行 access$xxx 方法中的相关指令
 *
 * @author hongweiqiu
 */
class AccessMethodInvokeVisitor extends BaseMethodVisitor {
    private static final String TAG = "AccessMethodInvokeVisitor"

    AccessMethodInvokeVisitor(int api, MethodVisitor mv,
                              String className, int access, String methodName, String desc,
                              String signature, String[] exceptions) {
        super(api, mv, className, access, methodName, desc, signature, exceptions)
    }

    @Override
    void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        // 如果方法内部调用了 access$xxx 方法，那么将这个文件也记录到 access$xxx 方法关联的类文件列表中
        if (opcode == Opcodes.INVOKESTATIC && name.startsWith(Constants.ACCESS_METHOD_NAME_PREFIX)) {
            AccessMethodInlineProcessor processor = AccessMethodInlineProcessor.getInstance();
            AccessMethodInfo accessMethodInfo = processor.getOrNewAccessMethodInfo(owner, name, desc)
            // 如果对应的内联方法为 null，则说明参数非法，此时调用父类方法逻辑并返回
            if (accessMethodInfo == null) {
                // something was wrong, show stacktrace and return
                Logger.e(TAG, "visitMethodInsn, got className, methodName or desc is empty!", new Throwable())
                super.visitMethodInsn(opcode, owner, name, desc, itf)
                return
            }
            // 添加 access$ 方法关联的类
            accessMethodInfo.appendRelatedClassName(className)
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf)
    }
}