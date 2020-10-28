package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.BaseProcessor
import com.hiro.bytecode_slimming.ProcessorManager
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 用这个类来进行第三步中方法指令的改变，从 invokestatic -> getfield，或者 invokestatic -> invokevirtual
 * @author hongweiqiu
 */
class MethodInstructionChangeVisitor extends MethodVisitor {

    static final def TAG = 'MethodInstructionChangeVisitor'

    private def processorKey

    MethodInstructionChangeVisitor(int api, MethodVisitor mv, String processorKey) {
        super(api, mv)
        this.processorKey = processorKey
    }

    @Override
    void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        println "$TAG, visitMethodInsn, opcode: $opcode, owner: $owner, name: $name, desc: $desc, itf: $itf"
        boolean[] visitResult = new boolean[1]
        visitResult[0] = false;
        if (opcode == Opcodes.INVOKESTATIC) {
            def processor = ProcessorManager.getInstance().getProcessor(processorKey)
            processor.methodInlineInfoMap.values().each { AccessMethodInfo accessMethodInfo ->
                // 如果调用的是 access$xxx 方法，则需要替换为 getfield/invokevirtual 指令
                if (Utils.textEquals(owner, accessMethodInfo.className)
                        && Utils.textEquals(name, accessMethodInfo.methodName)
                        && Utils.textEquals(desc, accessMethodInfo.desc)) {
                    // 优先处理 access$xxx 中获取的字段，变成直接调用
                    if (accessMethodInfo.readFieldInfo != null) {
                        visitResult[0] = true
                        visitFieldInsn(accessMethodInfo.readFieldInfo.opcode,
                                accessMethodInfo.readFieldInfo.fieldClassName,
                                accessMethodInfo.readFieldInfo.fieldName,
                                accessMethodInfo.readFieldInfo.desc)
                        return
                    } else if (accessMethodInfo.invokeMethodInfo != null) {
                        // 处理 access$xxx 中调用的方法
                        // 如果是 INVOKESPECIAL 字节码，在方法访问权限改成 package 后需要变成 invokevirtual
                        // 即满足 Java 多态的条件
                        opcode = accessMethodInfo.invokeMethodInfo.opcode ==
                                Opcodes.INVOKESPECIAL ? Opcodes.INVOKEVIRTUAL : opcode
                        owner = accessMethodInfo.invokeMethodInfo.methodClassName
                        name = accessMethodInfo.invokeMethodInfo.methodName
                        desc = accessMethodInfo.invokeMethodInfo.desc
                        return
                    }
                }
            }
        }
        if (!visitResult[0]) {
            super.visitMethodInsn(opcode, owner, name, desc, itf)
        }
    }

    @Override
    void visitFieldInsn(int opcode, String owner, String name, String desc) {
        println "$TAG, visitFieldInsn, opcode: $opcode, owner: $owner, name: $name, desc: $desc"
        super.visitFieldInsn(opcode, owner, name, desc)
    }
}