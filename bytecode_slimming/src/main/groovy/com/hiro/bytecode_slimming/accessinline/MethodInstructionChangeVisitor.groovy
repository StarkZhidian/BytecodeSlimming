package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.ProcessorManager
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode


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
        def visitResult = [false]
        def opcodes = [opcode]
        def owners = [owner]
        def names = [name]
        def descs = [desc]
        // 调用 access$xxx 方法的指令为 invokestatic
        // 调用 private 方法的指令为 invokespecial，改了 private 方法为 public，
        // 需要把该方法的其他调用点改成 invokevirtual
        if (opcode == Opcodes.INVOKESTATIC || opcode == Opcodes.INVOKESPECIAL) {
            def processor = ProcessorManager.getInstance().getProcessor(processorKey)
            processor.methodInlineInfoMap.values().each { AccessMethodInfo accessMethodInfo ->
                // 如果调用的是 access$xxx 方法的地方，则需要替换为 access$xxx 方法内部的指令
                if (!visitResult[0]
                        && opcode == Opcodes.INVOKESTATIC
                        && Utils.textEquals(owner, accessMethodInfo.className)
                        && Utils.textEquals(name, accessMethodInfo.methodName)
                        && Utils.textEquals(desc, accessMethodInfo.desc)
                        && accessMethodInfo.instructions != null) {
                    for (AbstractInsnNode insnNode : accessMethodInfo.instructions) {
                        insnNode.accept(this)
                    }
                    visitResult[0] = true
                } else if (opcode == Opcodes.INVOKESPECIAL
                        && AccessMethodInlineSecondClassVisitor.isInvokeMethodMember(accessMethodInfo.invokeMethodInfoList, owner, name, desc)) {
                    // 调用的是 access$xxx 方法内部中调用的方法，需要将调用字节码改为 invokevirtual
                    opcodes[0] = Opcodes.INVOKEVIRTUAL
                }
            }
        }
        if (!visitResult[0]) {
            super.visitMethodInsn(opcodes[0], owners[0], names[0], descs[0], itf)
        }
    }
}