package com.hiro.bytecode_slimming

import org.objectweb.asm.ClassVisitor

/**
 * 基础的类名访问器
 * @author hongweiqiu
 */
class BaseClassVisitor extends ClassVisitor {

    /* 记录当前访问的类名 */
    def className

    BaseClassVisitor(int api) {
        super(api)
    }

    BaseClassVisitor(int api, ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name
        super.visit(version, access, name, signature, superName, interfaces)
    }
}
