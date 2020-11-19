package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.BaseClassVisitor
import com.hiro.bytecode_slimming.Logger
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import com.hiro.bytecode_slimming.Constants

/**
 * 第二次 class 文件扫描器，在这里读取第一次的扫描结果，进行真正的 class 文件内容修改
 * @author hongweiqiu
 */
class AccessMethodInlineSecondClassVisitor extends BaseClassVisitor {
    private static def TAG = 'AccessMethodInlineSecondClassVisitor'

    private AccessMethodInfo accessMethodInfo

    AccessMethodInlineSecondClassVisitor(int api, ClassVisitor cv, AccessMethodInfo accessMethodInfo) {
        super(api, cv)
        this.accessMethodInfo = accessMethodInfo
    }

    @Override
    FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        // 遍历已经记录需要删除 ACC_PRIVATE 标志的所有字段
        if (isOperateFieldMember(accessMethodInfo.operateFieldInfoList, className, name, desc)) {
            Logger.d1(TAG, "changeFieldAccess2Public: " + accessMethodInfo.operateFieldInfoList)
            // 匹配成功，将字段改成 public 的
            access = Utils.toPublic(access)
        }
        return super.visitField(access, name, desc, signature, value)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        def resultMethodVisitor = false
        // 遍历已经记录的所有方法
        // 匹配到要删除的 access$xxx 方法，则删除，通过 resultMethodVisitor 的值来标记
        if (((access & Opcodes.ACC_SYNTHETIC) == Opcodes.ACC_SYNTHETIC)
                && (Utils.textEquals(accessMethodInfo.className, className))
                && (Utils.textEquals(accessMethodInfo.methodName, name))
                && (Utils.textEquals(accessMethodInfo.desc, desc))) {
            Logger.d1(TAG, "need2DeleteMethod: " + accessMethodInfo)
            resultMethodVisitor = true
        } else if (isInvokeMethodMember(accessMethodInfo.invokeMethodInfoList, className, name, desc)) {
            // 匹配到 access$xxx 方法调用的 private 方法，改成 public 访问权限
            access = Utils.toPublic(access)
            Logger.d1(TAG, "changeMethodAccess2Public: " + accessMethodInfo.invokeMethodInfoList)
        }
        // 如果 resultMethodVisitor 为 false，证明没有匹配到要删除的方法，返回自定义的方法访问器
        if (!resultMethodVisitor) {
            return new MethodInstructionChangeVisitor(Constants.ASM_VERSION,
                    super.visitMethod(access, name, desc, signature, exceptions),
                    accessMethodInfo)
        }
        // 该方法为需要删除的 access$xxx 方法，计数，同时返回 null
        AccessMethodInlineProcessor.getInstance().increaseOptimizeCount()
        return null
    }

    static def isOperateFieldMember(List fieldInfoList, String className, String fieldName, String desc) {
        if (fieldInfoList == null || fieldInfoList.isEmpty()) {
            return false;
        }
        def result = [false]
        fieldInfoList.each { AccessMethodInfo.OperateFieldInfo fileInfo ->
            if (result[0]) {
                return true
            }
            if ((judgeIsSameOrSuperClass(fileInfo.fieldClassName, className))
                    && (Utils.textEquals(fileInfo.fieldName, fieldName))
                    && (Utils.textEquals(fileInfo.desc, desc))) {
                result[0] = true
            }
        }
        return result[0]
    }

    static def isInvokeMethodMember(List methodInfoList, String className, String methodName, String desc) {
        if (methodInfoList == null || methodInfoList.isEmpty()) {
            return false
        }
        def result = [false]
        methodInfoList.each { AccessMethodInfo.InvokeMethodInfo methodInfo ->
            if (result[0]) {
                return true
            }
            if ((judgeIsSameOrSuperClass(methodInfo.methodClassName, className))
                    && (Utils.textEquals(methodInfo.methodName, methodName))
                    && (Utils.textEquals(methodInfo.desc, desc))) {
                result[0] = true
                return
            }
        }
        return result[0]
    }

    /**
     * 判断参数 1 是否和参数 2 是同一个类/是参数 2 的子类
     */
    static def judgeIsSameOrSuperClass(String judgeSubClassName, String judgeSuperClassName) {
        def superClassName = judgeSubClassName
        while (!Utils.isEmpty(superClassName)) {
            if (Utils.textEquals(superClassName, judgeSuperClassName)) {
                return true
            }
            // 循环判断是否为父子类关系
            superClassName = AccessMethodInlineProcessor.getInstance().getSuperClass(superClassName)
        }
        return false
    }
}