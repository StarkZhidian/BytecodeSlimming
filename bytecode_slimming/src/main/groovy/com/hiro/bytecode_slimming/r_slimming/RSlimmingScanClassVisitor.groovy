package com.hiro.bytecode_slimming.r_slimming

import com.hiro.bytecode_slimming.BaseClassVisitor
import com.hiro.bytecode_slimming.Logger
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor

/**
 * 扫描并且删除 R 文件中无用的非数组型常量字段的 class visitor
 */
class RSlimmingScanClassVisitor extends BaseRSlimmingClassVisitor {
    private static final String TAG = "RSlimmingClassVisitor"

    /* 判断某个 R 类文件是否可以删除，如果一个 R 类中的所有字段都被删除了，那么这个类文件也可以被删除 */
    private boolean canDeleteClassFile = true

    RSlimmingScanClassVisitor(int api, ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        Logger.d1(TAG, "visit R class: access: $access, name: $name, signature: $signature, superName: $superClassName, interfaces: $interfaces")
        super.visit(version, access, name, signature, superName, interfaces)
    }

    @Override
    FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (canRemove(access, value)) {
            // 如果当前字段可以移除，则记录移除的字段值
            RFieldRecorder.getInstance().appendRField(className, name, value)
            // 处理器优化项自增 1，标记该类数据已经被更改过，同时 return null
            RSlimmingProcessor.getInstance().increaseOptimizeCount()
            dataIsChanged = true
            Logger.d1(TAG, "remove field: access: $access, name: $name, desc: $desc, signature: $signature, value: $value")
            return null
        } else {
            // 只要有一个字段不能删除，那么这个类文件就不能删除
            canDeleteClassFile = false
        }
        return super.visitField(access, name, desc, signature, value)
    }

    boolean getCanDeleteClassFile() {
        return canDeleteClassFile
    }

    private static boolean canRemove(int access, Object value) {
        // 如果字段为 public static final int 类型的，则该字段可以移除
        return (Utils.isPublic(access) && Utils.isStatic(access) && Utils.isFinal(access)
                && (value instanceof Integer))
    }
}