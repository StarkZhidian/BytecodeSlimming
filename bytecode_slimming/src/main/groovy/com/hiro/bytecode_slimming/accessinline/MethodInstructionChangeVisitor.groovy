package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.ProcessorManager
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode


/**
 * 用这个类来进行第三步中方法指令的改变，从 invokestatic -> getfield，或者 invokestatic -> invokevirtual
 * 要么将方法中调用 access$xxx 方法的地方改为直接执行 access$xxx 方法的指令
 * 要么将 access$xxx 内部调用的某个方法的其他调用点改为 invokevirtual 指令，例：
 * 如果 access$xxx 方法中调用的是某个名字为 yyy 的 private 方法，
 * 则需要把该类在其他地方调用 yyy 方法的指令从 invokespecial 改为 invokevirtual（因为 yyy 已经变为 public 修饰了，需要保持 java 多态的特性）
 *
 * @author hongweiqiu
 */
class MethodInstructionChangeVisitor extends MethodVisitor {

    private static final def TAG = 'MethodInstructionChangeVisitor'

    private AccessMethodInfo accessMethodInfo

    MethodInstructionChangeVisitor(int api, MethodVisitor mv, AccessMethodInfo accessMethodInfo) {
        super(api, mv)
        this.accessMethodInfo = accessMethodInfo
    }

    @Override
    void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        def instructionReplaceResult = false
        // 调用 access$xxx 方法的指令为 invokestatic
        // 调用 private 方法的指令为 invokespecial，将方法访问权限修饰从 private 改为 public，
        // 需要把该方法的其他调用点改成 invokevirtual
        if (opcode == Opcodes.INVOKESTATIC || opcode == Opcodes.INVOKESPECIAL) {
            // 如果调用的是 access$xxx 方法的地方，则需要替换为 access$xxx 方法内部的指令
            if (opcode == Opcodes.INVOKESTATIC
                    && Utils.textEquals(owner, accessMethodInfo.className)
                    && Utils.textEquals(name, accessMethodInfo.methodName)
                    && Utils.textEquals(desc, accessMethodInfo.desc)
                    && accessMethodInfo.instructions != null) {
                for (AbstractInsnNode insnNode : accessMethodInfo.instructions) {
                    insnNode.accept(this)
                }
                instructionReplaceResult = true
            } else if (opcode == Opcodes.INVOKESPECIAL
                    && AccessMethodInlineSecondClassVisitor.isInvokeMethodMember(accessMethodInfo.invokeMethodInfoList, owner, name, desc)) {
                // 调用的是 access$xxx 方法内部中调用的方法，需要将调用字节码改为 invokevirtual
                opcode = Opcodes.INVOKEVIRTUAL
            }
        }
        // 如果 access 方法调用没有被替换为 access 方法内部的指令，则需要调用 super 逻辑
        if (!instructionReplaceResult) {
            super.visitMethodInsn(opcode, owner, name, desc, itf)
        }
    }
}