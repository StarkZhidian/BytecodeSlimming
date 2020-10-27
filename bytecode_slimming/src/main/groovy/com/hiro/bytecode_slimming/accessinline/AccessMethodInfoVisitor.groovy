package com.hiro.bytecode_slimming.accessinline

import org.objectweb.asm.MethodVisitor

/**
 * 编译器自动生成的 access$xxx 方法的访问器
 *
 * @author hongweiqiu
 */
class AccessMethodInfoVisitor extends MethodVisitor {

    static final def TAG = "AccessMethodVisitor"

    /* 当前访问的 access$xxx 方法所属的类 */
    def className;
    /* 当前访问的 access$xxx 方法名 */
    def methodName;
    /* 记录当前方法的相关信息 */
    def accessMethodInfo

    AccessMethodInfoVisitor(int api, MethodVisitor mv, def className, def methodName, def desc) {
        super(api, mv)
        this.className = className
        this.methodName = methodName
        accessMethodInfo = new AccessMethodInfo(className, methodName, desc)
    }

    @Override
    void visitFieldInsn(int opcode, String owner, String name, String desc) {
        println "$TAG, methodName: $methodName, opcode: $opcode, owner: $owner, name: $name, desc: $desc"
        accessMethodInfo.setReadFieldInfo(new AccessMethodInfo.ReadFieldInfo(opcode, owner, name, desc))
        super.visitFieldInsn(opcode, owner, name, desc)
    }

    @Override
    void visitInsn(int opcode) {
        println "$TAG, opcode: $opcode"
        super.visitInsn(opcode)
    }

    @Override
    void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        println "$TAG, methodName: $methodName, opcode: $opcode, owner: $owner, name: $name, desc: $desc, itf: $itf"
        accessMethodInfo.setInvokeMethodInfo(new AccessMethodInfo.InvokeMethodInfo(opcode, owner, name, desc))
        super.visitMethodInsn(opcode, owner, name, desc, itf)
    }

    @Override
    void visitEnd() {
        println "visitEnd"
        super.visitEnd()
        AccessMethodInlineProcessor.getInstance().appendInlineMethod(className, methodName, accessMethodInfo)
    }
}