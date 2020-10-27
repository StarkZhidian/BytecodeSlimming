package com.hiro.bytecode_slimming.getter_setter_inline

import com.hiro.bytecode_slimming.BaseMethodVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Getter/Setter 方法第一遍扫描器，
 * 主要是通过方法内部的字节码来最终确定方法是否为最终要内联的 getter/setter 方法
 * 将确定要内联的方法封装成 GetterSetterMethodInfo 对象存入 GetterSetterInlineProcessor 中
 *
 * @author hongweiqiu
 */
class GetterSetterInlineFirstMethodVisitor extends BaseMethodVisitor {
    private static final def TAG = "GetterSetterInlineFirstMethodVisitor"
    private static final def INVALID_OPCODE = Integer.MIN_VALUE

    /* 记录方法代码中的指令 */
    def instructions = []
    /* 记录操作字段的字节码（getfield、putfield...) */
    def opcode = Opcodes.NOP
    /* 记录操作的字段所属的类 */
    def fieldOwner
    /* 记录操作的字段名 */
    def fieldName
    /* 记录操作的字段在 JVM 层的描述（类型签名） */
    def fieldDesc

    GetterSetterInlineFirstMethodVisitor(int api, MethodVisitor mv, String className, int access,
                                         String methodName, String desc, String signature, String[] exceptions) {
        super(api, mv, className, access, methodName, desc, signature, exceptions)
    }

    @Override
    void visitInsn(int opcode) {
        instructions.add(opcode)
        super.visitInsn(opcode)
    }

    @Override
    void visitFieldInsn(int opcode, String owner, String name, String desc) {
        println "$TAG, visitFieldInsn: opcode: $opcode, owner: $owner, name: $name, desc: $desc"
        if (this.opcode != Opcodes.NOP) {
            this.opcode = INVALID_OPCODE
        } else if ((opcode == Opcodes.GETFIELD || opcode == Opcodes.PUTFIELD)) {
            this.opcode = opcode
            this.fieldOwner = owner
            this.fieldName = name
            this.fieldDesc = desc
        }
        super.visitFieldInsn(opcode, owner, name, desc)
    }

    @Override
    void visitEnd() {
        super.visitEnd()
        // opcode 即不为 NOP 也不为无效字节码，证明是合法的 getter/setter 方法
        if (opcode != Opcodes.NOP && opcode != INVALID_OPCODE) {
            def getterSetterMethodInfo = new GetterSetterMethodInfo(className, methodName, desc)
            getterSetterMethodInfo.readFieldInfo =
                    new GetterSetterMethodInfo.OperateFieldInfo(opcode, fieldOwner, fieldName, fieldDesc)
            GetterSetterInlineProcessor
                    .getInstance()
                    .appendInlineMethod(className, methodName, getterSetterMethodInfo)
        }
    }
}