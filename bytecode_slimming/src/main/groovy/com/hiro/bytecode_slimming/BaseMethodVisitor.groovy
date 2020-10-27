package com.hiro.bytecode_slimming

import org.objectweb.asm.MethodVisitor

/**
 * 基础的方法访问器
 * @author hongweiqiu
 */
class BaseMethodVisitor extends MethodVisitor {
    /* 方法所属的类名 */
    def className
    /* 方法的访问标识 */
    def access
    /* 方法名 */
    def methodName
    /* 方法描述（JVM 层的方法签名） */
    def desc
    /* 方法的泛型签名（只有方法中涉及到泛型参数时才存在） */
    def signature
    /* 方法抛出的异常列表（方法声明时通过 throws 关键字声明的） */
    def exceptions

    BaseMethodVisitor(int api) {
        super(api)
    }

    BaseMethodVisitor(int api, MethodVisitor mv) {
        super(api, mv)
    }

    BaseMethodVisitor(int api, MethodVisitor mv, int access, String name,
                      String desc, String signature, String[] exceptions) {
        this(api, mv, null, access, name ,desc, signature, exceptions)
    }

    BaseMethodVisitor(int api, MethodVisitor mv, String className, int access, String methodName,
                      String desc, String signature, String[] exceptions) {
        super(api, mv)
        this.className = className
        this.access = access
        this.methodName = methodName
        this.desc = desc
        this.signature = signature
        this.exceptions = exceptions
    }
}