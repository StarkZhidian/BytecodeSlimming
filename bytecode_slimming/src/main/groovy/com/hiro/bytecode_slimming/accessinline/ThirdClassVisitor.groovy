package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 第三次 class 文件扫描器，在这里读取前两次的扫描结果，进行真正的 class 文件内容修改
 * @author hongweiqiu
 */
class ThirdClassVisitor extends ClassVisitor {

    def TAG = 'ThirdClassVisitor'

    def className

    ThirdClassVisitor(int api) {
        super(api)
    }

    ThirdClassVisitor(int api, ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name
        TAG += ", className: " + className
        super.visit(version, access, name, signature, superName, interfaces)
    }

    @Override
    FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        println "$TAG, visitField, access: $access, name: $name, desc: $desc, signature: $signature, value: $value"
        def resultVisitor = [false]
        def accessMethodProcessor = AccessMethodInlineProcessor.getInstance()
        // 遍历已经记录需要删除 ACC_PRIVATE 标志的所有字段
        accessMethodProcessor.accessMethodInfoMap.values().each { AccessMethodInfo accessMethodInfo ->
            if (SecondClassVisitor.isSameField(accessMethodInfo.readFieldInfo, className, name, desc)) {
                println "$TAG, changeFieldAccess2Package: " + accessMethodInfo.readFieldInfo
                // 匹配成功，删除字段 flag 中的 ACC_PRIVATE 标识
                super.visitField(access - Opcodes.ACC_PRIVATE, name, desc, signature, value)
                resultVisitor[0] = true
                return
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
        accessMethodProcessor.accessMethodInfoMap.values().each { AccessMethodInfo accessMethodInfo ->
            println "$TAG, accessMethodInfo: $accessMethodInfo, className equals: " + Utils.textEquals(accessMethodInfo.className, className) + ", methodName equals: " + Utils.textEquals(accessMethodInfo.methodName, name) + ", desc equals: " + Utils.textEquals(accessMethodInfo.desc, desc) + ", canDelete: " + accessMethodInfo.canDelete
            // 匹配到要删除的 access$xxx 方法
            if (((accesses[0] & Opcodes.ACC_SYNTHETIC) == Opcodes.ACC_SYNTHETIC)
                    && (Utils.textEquals(accessMethodInfo.className, className))
                    && (Utils.textEquals(accessMethodInfo.methodName, name))
                    && (Utils.textEquals(accessMethodInfo.desc, desc))
                    && (accessMethodInfo.canDelete)) {
                println "$TAG, need2DeleteMethod: " + accessMethodInfo
                resultMethodVisitor[0] = true
                return
            } else if (SecondClassVisitor.isSameMethod(accessMethodInfo.invokeMethodInfo, className, name, desc)
                    && (accesses[0] & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE) {
                // 匹配到 access$xxx 方法调用的 private 方法，
                // 删除方法 flag 中的 ACC_PRIVATE 标识，方法访问权限上升到默认访问权限
                accesses[0] -= Opcodes.ACC_PRIVATE
                println "$TAG, changeMethodAccess2Package: " + accessMethodInfo.invokeMethodInfo
                return
            }
        }
        // 如果 resultMethodVisitor[0] 为 false，证明没有匹配到要删除的方法，返回自定义的方法访问器
        if (!resultMethodVisitor[0]) {
            return new MethodInstructionChangeVisitor(Opcodes.ASM6, super.visitMethod(accesses[0], name, desc, signature, exceptions))
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
}