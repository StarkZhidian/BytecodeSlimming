package com.hiro.bytecode_slimming.getter_setter_inline

import com.hiro.bytecode_slimming.BaseMethodVisitor
import com.hiro.bytecode_slimming.ProcessorManager
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 用这个类来进行第三步中方法指令的改变，从 invokevirtual/invokespecial -> getfield/putfield
 * @author hongweiqiu
 */
class GetterSetterInlineSecondMethodVisitor extends BaseMethodVisitor {
    private static final def TAG = "GetterSetterInlineSecondMethodVisitor"

    private def processKey

    GetterSetterInlineSecondMethodVisitor(int api, MethodVisitor mv, String processorKey) {
        super(api, mv)
        this.processKey = processorKey
    }


    @Override
    void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        println "$TAG, visitMethodInsn, opcode: $opcode, owner: $owner, name: $name, desc: $desc, itf: $itf"
        boolean[] visitResult = new boolean[1]
        visitResult[0] = false;
        if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKESPECIAL) {
            def processor = ProcessorManager.getInstance().getProcessor(processKey)
            processor.methodInlineInfoMap.values().each { getterSetterMethodInfo ->
                // 如果调用的是 getter/setter 方法，则需要替换为 getfield/putfield 指令
                if (Utils.textEquals(owner, getterSetterMethodInfo.className)
                        && Utils.textEquals(name, getterSetterMethodInfo.methodName)
                        && Utils.textEquals(desc, getterSetterMethodInfo.desc)) {
                    // 处理 getter/setter 中获取的字段，变成直接调用
                    if (getterSetterMethodInfo.readFieldInfo != null) {
                        // 标识为 true，意为该方法可以删除
                        visitResult[0] = true
                        visitFieldInsn(getterSetterMethodInfo.readFieldInfo.opcode,
                                getterSetterMethodInfo.readFieldInfo.fieldClassName,
                                getterSetterMethodInfo.readFieldInfo.fieldName,
                                getterSetterMethodInfo.readFieldInfo.desc)
                        return
                    }
                }
            }
        }
        if (!visitResult[0]) {
            super.visitMethodInsn(opcode, owner, name, desc, itf)
        }
    }

}