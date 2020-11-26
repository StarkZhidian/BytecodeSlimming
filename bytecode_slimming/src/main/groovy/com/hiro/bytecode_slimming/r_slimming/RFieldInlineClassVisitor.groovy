package com.hiro.bytecode_slimming.r_slimming

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

/**
 * 第二次的类扫描器，主要用来将非 R 文件类中引用到的 R 文件类中已经移除的字段替换为实际对应的常量值
 */
class RFieldInlineClassVisitor extends BaseRSlimmingClassVisitor {

    RFieldInlineClassVisitor(int api, ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new RFieldInlineMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions),
                className, access, name, desc, signature, exceptions, this)
    }
}