package com.hiro.bytecode_slimming.method_log

import com.hiro.bytecode_slimming.BaseClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class MethodLogClassVisitor extends BaseClassVisitor {

    /* 该类中是否有方法被插入了 log，如果插入了，需要将 class 数据更新到文件中 */
    private def insertMethodEnterLog = false

    MethodLogClassVisitor(int api, ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodLogMethodVisitor(this, api,
                super.visitMethod(access, name, desc, signature, exceptions), className, access, name, desc, signature, exceptions)
    }

    boolean getInsertMethodEnterLog() {
        return insertMethodEnterLog
    }

    void setInsertMethodEnterLog(boolean value) {
        insertMethodEnterLog = value
    }
}