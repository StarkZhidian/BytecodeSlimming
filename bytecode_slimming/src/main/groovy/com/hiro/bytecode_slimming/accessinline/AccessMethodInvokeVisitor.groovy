package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.BaseMethodVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 过滤且记录在方法中调用了 access$xxx 方法的类文件，
 * 后续需要将这些类文件调用 access$xxx 方法的地方改为直接执行 access$xxx 方法中的相关指令
 *
 * @author hongweiqiu
 */
class AccessMethodInvokeVisitor extends BaseMethodVisitor {

    def classFile

    AccessMethodInvokeVisitor(int api, MethodVisitor mv,
                              String className, int access, String methodName, String desc,
                              String signature, String[] exceptions, File classFile) {
        super(api, mv, className, access, methodName, desc, signature, exceptions)
        this.classFile = classFile
    }

    @Override
    void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        // 如果方法内部调用了 access$xxx 方法，那么将这个文件也记录到 access$xxx 方法关联的类文件列表中
        if (opcode == Opcodes.INVOKESTATIC && name.startsWith('access$')) {
            AccessMethodInlineProcessor.getInstance().appendOptimizeClassFile(className)
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf)
    }
}