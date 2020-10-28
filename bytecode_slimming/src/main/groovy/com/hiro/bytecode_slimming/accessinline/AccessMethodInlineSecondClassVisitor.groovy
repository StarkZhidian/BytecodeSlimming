package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.BaseClassVisitor
import com.hiro.bytecode_slimming.ProcessorManager
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 第二次 class 文件扫描器，在这里读取第一次的扫描结果，进行真正的 class 文件内容修改
 * @author hongweiqiu
 */
class AccessMethodInlineSecondClassVisitor extends BaseClassVisitor {

    def TAG = 'AccessMethodInlineSecondClassVisitor'

    AccessMethodInlineSecondClassVisitor(int api) {
        super(api)
    }

    AccessMethodInlineSecondClassVisitor(int api, ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        println "$TAG, visitField, access: $access, name: $name, desc: $desc, signature: $signature, value: $value"
        def resultVisitor = [false]
        def accessMethodProcessor = AccessMethodInlineProcessor.getInstance()
        // 遍历已经记录需要删除 ACC_PRIVATE 标志的所有字段
        accessMethodProcessor.methodInlineInfoMap.values().each { AccessMethodInfo accessMethodInfo ->
            // 每个字段只操作一次，需要通过 resultMethodVisitor[0] 的值来过滤
            if (!resultVisitor[0]
                    && isSameField(accessMethodInfo.readFieldInfo, className, name, desc)) {
                println "$TAG, changeFieldAccess2Package: " + accessMethodInfo.readFieldInfo
                // 匹配成功，删除字段 flag 中的 ACC_PRIVATE 标识
                super.visitField(access - Opcodes.ACC_PRIVATE, name, desc, signature, value)
                resultVisitor[0] = true
            }
        }
        // 如果 resultVisitor[0] 为 false，证明没有匹配到需要删除 ACC_PRIVATE 标志的字段，
        // 使用父类的默认处理方法
        if (!resultVisitor[0]) {
            return super.visitField(access, name, desc, signature, value)
        }
        return null;
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        println "$TAG, visitMethod, access: $access, name: $name, desc: $desc, signature: $signature, exceptions: $exceptions"
        def resultMethodVisitor = [false];
        def accesses = [access]
        def accessMethodProcessor = AccessMethodInlineProcessor.getInstance()
        // 遍历已经记录的所有方法
        accessMethodProcessor.methodInlineInfoMap.values().each { AccessMethodInfo accessMethodInfo ->
            println "$TAG, accessMethodInfo: $accessMethodInfo, className equals: " + Utils.textEquals(accessMethodInfo.className, className) + ", methodName equals: " + Utils.textEquals(accessMethodInfo.methodName, name) + ", desc equals: " + Utils.textEquals(accessMethodInfo.desc, desc)
            // 匹配到要删除的 access$xxx 方法，方法只删除一次，通过 resultMethodVisitor[0] 的值来过滤
            if (!resultMethodVisitor[0]
                    && ((accesses[0] & Opcodes.ACC_SYNTHETIC) == Opcodes.ACC_SYNTHETIC)
                    && (Utils.textEquals(accessMethodInfo.className, className))
                    && (Utils.textEquals(accessMethodInfo.methodName, name))
                    && (Utils.textEquals(accessMethodInfo.desc, desc))) {
                println "$TAG, need2DeleteMethod: " + accessMethodInfo
                resultMethodVisitor[0] = true
            } else if (isSameMethod(accessMethodInfo.invokeMethodInfo, className, name, desc)
                    && (accesses[0] & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE) {
                // 匹配到 access$xxx 方法调用的 private 方法，
                // 删除方法 flag 中的 ACC_PRIVATE 标识，方法访问权限上升到默认访问权限
                accesses[0] -= Opcodes.ACC_PRIVATE
                println "$TAG, changeMethodAccess2Package: " + accessMethodInfo.invokeMethodInfo
            }
        }
        // 如果 resultMethodVisitor[0] 为 false，证明没有匹配到要删除的方法，返回自定义的方法访问器
        if (!resultMethodVisitor[0]) {
            return new MethodInstructionChangeVisitor(Opcodes.ASM6,
                    super.visitMethod(accesses[0], name, desc, signature, exceptions),
                    ProcessorManager.KEY_ACCESS_METHOD_INLINE)
        }
        // 该方法为需要删除的 access$xxx 方法，计数，同时返回 null
        AccessMethodInlineProcessor.getInstance().increaseOptimizeCount()
        return null;
    }

    @Override
    void visitEnd() {
        println "$TAG, visitEnd"
        super.visitEnd()
    }

    static def isSameField(def fieldInfo, def className, def fieldName, def desc) {
        if (fieldInfo == null) {
            return false;
        }
        return Utils.textEquals(fieldInfo.fieldClassName, className) && Utils.textEquals(fieldInfo.fieldName, fieldName) && Utils.textEquals(fieldInfo.desc, desc)
    }

    static def isSameMethod(def methodInfo, def className, def methodName, def desc) {
        if (methodInfo == null) {
            return false
        }
        return Utils.textEquals(methodInfo.methodClassName, className) && Utils.textEquals(methodInfo.methodName, methodName) && Utils.textEquals(methodInfo.desc, desc)
    }
}