package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.BaseClassVisitor
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor

/**
 * 第二次 class 文件扫描器，记录第一步中 access$xxx 方法访问的字段/方法，
 * 需要在下一次扫描中改为包默认的访问权限
 *
 * @author hongweiqiu
 */
class AccessMethodInlineSecondClassVisitor extends BaseClassVisitor {

    static final def String TAG = "AccessMethodInlineSecondClassVisitor"

    AccessMethodInlineSecondClassVisitor(int api) {
        super(api)
    }

    AccessMethodInlineSecondClassVisitor(int api, ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        println "$TAG, visitField, access: $access, name: $name, desc: $desc, signature: $signature, value: $value"
        def accessMethodProcessor = AccessMethodInlineProcessor.getInstance()
        accessMethodProcessor.methodInlineInfoMap.values().each { AccessMethodInfo accessMethodInfo ->
            // 字段匹配，则标记该字段需要将访问权限修改为默认访问权限，同时标记该 access$xxx 方法为可删除方法
            if (isSameField(accessMethodInfo.readFieldInfo, className, name, desc)) {
                accessMethodInfo.readFieldInfo.needChange2PackageAccess = true
                accessMethodInfo.canDelete = true
            }
        }
        return super.visitField(access, name, desc, signature, value)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        println "$TAG, visitMethod, access: $access, name: $name, desc: $desc, signature: $signature, exceptions: $exceptions"
        def accessMethodProcessor = AccessMethodInlineProcessor.getInstance()
        accessMethodProcessor.methodInlineInfoMap.values().each { AccessMethodInfo accessMethodInfo ->
            // 方法匹配，则标记该方法需要将访问权限修改为默认访问权限，同时标记该 access$xxx 方法为可删除方法
            if (isSameMethod(accessMethodInfo.invokeMethodInfo, className, name, desc)) {
                accessMethodInfo.invokeMethodInfo.needChange2PackageAccess = true
                accessMethodInfo.canDelete = true
            }
        }
        return super.visitMethod(access, name, desc, signature, exceptions)
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