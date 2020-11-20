package com.hiro.bytecode_slimming.rm_not_runtime_annotation

import com.hiro.bytecode_slimming.BaseClassVisitor
import com.hiro.bytecode_slimming.Constants
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor

/**
 * 删除 R 文件中无用的非数组型常量字段的 class visitor
 */
class RSlimmingClassVisitor extends BaseClassVisitor {

    private boolean changedData

    RSlimmingClassVisitor(int api, ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (canRemove(access, desc)) {
            // 如果当前字段可以移除，则处理器优化项自增 1，标记该类数据已经被更改过，同时 return null
            RSlimmingProcessor.getInstance().increaseOptimizeCount()
            changedData = true
            return null
        }
        return super.visitField(access, name, desc, signature, value)
    }

    private static boolean canRemove(int access, String desc) {
        // 将 public static final 基本类型字段看成可以移除的常量字段
        return (Utils.isPublic(access) && Utils.isStatic(access) && Utils.isFinal(access)
                && !Utils.isEmpty(desc) && !desc.startsWith(Constants.CLASS_DESC_PREFIX))
    }

    boolean getChangedData() {
        return changedData
    }
}