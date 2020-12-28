package com.hiro.bytecode_slimming.constants_field_rm

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

/**
 * 第二次的类扫描器，主要用来将类中引用到的其他类中已经移除的字段替换为实际对应的常量值
 */
class ConstantFieldInlineClassVisitor extends BaseConstantFieldSlimmingClassVisitor {

    ConstantFieldInlineClassVisitor(int api, ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new ConstantFieldInlineMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions),
                className, access, name, desc, signature, exceptions, this)
    }
}