package com.hiro.bytecode_slimming.getter_setter_inline

import com.hiro.bytecode_slimming.BaseClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * getter/setter 方法内联第一步的类访问器，用于访问类中的方法，初步判断 getter/setter 内联方法，
 * 并交给 GetterSetterInlineFirstMethodVisitor 处理
 *
 * @author hongweiqiu
 */
class GetterSetterInlineFirstClassVisitor extends BaseClassVisitor {

    private static final def TAG = "GetterSetterInlineFirstClassVisitor"

    GetterSetterInlineFirstClassVisitor(int api) {
        super(api)
    }

    GetterSetterInlineFirstClassVisitor(int api, ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        def methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)
        if (isGetterSetterMethod(access, name, desc)) {
            return new GetterSetterInlineFirstMethodVisitor(
                    Opcodes.ASM6, methodVisitor, className, access, name, desc, signature, exceptions)
        }
        return methodVisitor
    }

    /**
     * 判断某个方法是否为字段的 getter 方法，这里只是通过名字粗略的进行第一步的判断
     * 一个正常的 setter 方法一般是：
     * public XXX getXXX() {*}* @param access 访问标识
     * @param name 方法名
     * @param desc 方法 JVM 层描述
     */
    private def isGetterMethod(int access, String name, String desc) {
        return (((access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC)
                && (name != null && name.startsWith("get"))
                && (desc != null && desc.startsWith("()")))
    }

    /**
     * 判断某个方法是否为字段的 setter 方法，这里只是通过名字粗略的进行第一步的判断
     * 一个正常的 setter 方法一般是：
     * public void setXXX(xxx) {*}* @param access 访问标识
     * @param name 方法名
     * @param desc 方法 JVM 层签名
     */
    private def isSetterMethod(int access, String name, String desc) {
        return (((access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC)
                && (name != null && name.startsWith("set"))
                && desc != null && desc.endsWith("V"))
    }

    private def isGetterSetterMethod(int access, String name, String desc) {
        return isGetterMethod(access, name, desc) || isSetterMethod(access, name, desc)
    }
}