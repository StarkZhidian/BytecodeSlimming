package com.hiro.bytecode_slimming.getter_setter_inline

import com.hiro.bytecode_slimming.BaseClassVisitor
import com.hiro.bytecode_slimming.ProcessorManager
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Getter/Setter 方法内联第二次 class 文件访问器，主要是为了删除匹配到的 getter/setter 方法，
 * 同时修改对应 field 的访问标识为 public
 *
 * @author hongweiqiu
 */
class GetterSetterInlineSecondClassVisitor extends BaseClassVisitor {
    private static final def TAG = "GetterSetterInlineSecondClassVisitor"

    GetterSetterInlineSecondClassVisitor(int api) {
        super(api)
    }

    GetterSetterInlineSecondClassVisitor(int api, ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        def resultVisitor = [false]
        def getterSetterInlineProcessor = GetterSetterInlineProcessor.getInstance()
        // 遍历已经记录需要改为 ACC_PUBLIC 标志的字段，每个字段只改一次
        getterSetterInlineProcessor.methodInlineInfoMap.values().each { getterSetterMethodInfo ->
            if (!resultVisitor[0]
                    && isSameField(getterSetterMethodInfo.readFieldInfo, className, name, desc)) {
                println "$TAG, changeFieldAccess2Public: " + getterSetterMethodInfo.readFieldInfo
                // 匹配成功，设置字段 flag 中的 ACC_PUBLIC 标识
                super.visitField(makePublicFlag(access), name, desc, signature, value)
                resultVisitor[0] = true
            }
        }
        // 如果 resultVisitor[0] 为 false，证明没有匹配到需要改为 ACC_PUBLIC 标志的字段
        // 使用父类的默认处理方法
        if (!resultVisitor[0]) {
            return super.visitField(access, name, desc, signature, value)
        }
        return null;
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        def resultMethodVisitor = [false];
        def getterSetterMethodInlineProcessor = GetterSetterInlineProcessor.getInstance()
        // 遍历已经记录的所有方法
        getterSetterMethodInlineProcessor.methodInlineInfoMap.values().each { getterSetterMethodInfo ->
            // 匹配到要删除的 getter/setter 方法，设置 resultMethodVisitor[0] 为 true
            if (!resultMethodVisitor[0]
                    && (Utils.textEquals(getterSetterMethodInfo.className, className))
                    && (Utils.textEquals(getterSetterMethodInfo.methodName, name))
                    && (Utils.textEquals(getterSetterMethodInfo.desc, desc))) {
                println "$TAG, need2DeleteMethod: " + getterSetterMethodInfo
                resultMethodVisitor[0] = true
            }
        }
        // 如果 resultMethodVisitor[0] 为 false，
        // 证明没有匹配到要删除的 getter/setter 方法，返回自定义的方法访问器
        if (!resultMethodVisitor[0]) {
            return new GetterSetterInlineSecondMethodVisitor(Opcodes.ASM6,
                    super.visitMethod(access, name, desc, signature, exceptions),
                    ProcessorManager.KEY_GETTER_SETTER_METHOD_INLINE)
        }
        // 该方法为需要删除的 getter/setter 方法，计数，同时返回 null
        GetterSetterInlineProcessor.getInstance().increaseOptimizeCount()
        return null;
    }

    private static def makePublicFlag(int access) {
        if ((access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE) {
            access -= Opcodes.ACC_PRIVATE
        }
        if ((access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED) {
            access -= Opcodes.ACC_PROTECTED
        }
        return (access | Opcodes.ACC_PUBLIC)
    }

    static def isSameField(def fieldInfo, def className, def fieldName, def desc) {
        if (fieldInfo == null) {
            return false;
        }
        return Utils.textEquals(fieldInfo.fieldClassName, className) && Utils.textEquals(fieldInfo.fieldName, fieldName) && Utils.textEquals(fieldInfo.desc, desc)
    }
}