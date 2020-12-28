package com.hiro.bytecode_slimming.constants_field_rm

import com.hiro.bytecode_slimming.Logger
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor

/**
 * 扫描并且删除类文件中无用常量字段的 class visitor
 */
class ConstantFieldSlimmingScanClassVisitor extends BaseConstantFieldSlimmingClassVisitor {
    private static final String TAG = "RSlimmingClassVisitor"

    /* 判断某个类文件是否可以删除，如果一个类中的所有字段都被删除了，那么这个类文件也可以被删除 */
    private boolean canDeleteClassFile = true
    private BaseConstantFieldSlimmingProcessor processor

    ConstantFieldSlimmingScanClassVisitor(int api, ClassVisitor cv,
                                          BaseConstantFieldSlimmingProcessor processor) {
        super(api, cv)
        if (processor == null) {
            throw new IllegalArgumentException("processor can not be null!")
        }
        this.processor = processor
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        Logger.d1(TAG, "visit R class: access: $access, name: $name, signature: $signature, superName: $superClassName, interfaces: $interfaces")
        super.visit(version, access, name, signature, superName, interfaces)
    }

    @Override
    FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (className.endsWith("Constants")) {
            Logger.d3(TAG, "className = [$className], name = [$name], desc = [$desc], signature=[$signature], value=[$value]")
        }
        if (canRemove(access, value)) {
            // 如果当前字段可以移除，则记录移除的字段值
            ConstantFieldRecorder.getInstance().appendRField(className, name, value)
            // 处理器优化项自增 1，标记该类数据已经被更改过，同时 return null
            processor.increaseOptimizeCount()
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
        // 如果字段不为编译时常量(即为编译期间就能确定值的常量字段，例:
        // public static final long TIME = System.currentTimeMillis(); 需要在运行时才知道其值，不为编译时常量)，
        // 此时 value 值为 null
        return (Utils.isPublic(access) && Utils.isStatic(access) && Utils.isFinal(access)
                && isInlineField(value))
    }

    private static boolean isInlineField(Object value) {
        // 当某个类中访问的常量字段的类型为 boolean、byte、char、short、int、long、float、double、java/lang/String 时，
        // 会在编译时直接转换为字面量直接值储存在访问该字段类的常量池中，即这些类型的常量字段是可以移除的。
        // 其中，boolean、byte、char、short 类型的字段在编译后的 class 文件中都会被转换为 4 字节的 int 类型
        return (value instanceof Integer || value instanceof Long || value instanceof Float
                || value instanceof Double || value instanceof String)
    }
}