package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.BaseClassVisitor
import com.hiro.bytecode_slimming.Constants
import com.hiro.bytecode_slimming.Logger
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 第一次扫描 class 文件的访问器，主要是记录每一个类中的 access$xxx 方法信息
 * @author hongweqiiu
 */
class AccessMethodInlineFirstClassVisitor extends BaseClassVisitor {
    private static final def TAG = "AccessMethodInlineFirstClassVisitor"

    private File classFile

    AccessMethodInlineFirstClassVisitor(int api, File classFile) {
        super(api)
        this.classFile = classFile
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (isAccessMethod(access, name) && (!AccessMethodInlineProcessor.getInstance().inKeepClassList(className))) {
            Logger.d1(TAG, "visitMethod accessMthod: $access, name: $name, desc: $desc, signature: $signature, exception: $exceptions")
            // 如果是编译器自动生成的 access$xxx 方法，同时当前类不在白名单类列表中
            // 则需要用自定义的方法访问器读取内部访问的字段/调用的方法信息
            return new AccessMethodInfoVisitor(Constants.ASM_VERSION, null, className, access, name, desc, signature, exceptions)
        }
        return new AccessMethodInvokeVisitor(Constants.ASM_VERSION, null, className, access, name, desc, signature, exceptions)
    }

    /**
     * 通过方法 flag 和方法名判断是否为编译器生成的 access$xxx 方法
     * @param access 方法 flag
     * @param name 方法名
     */
    private static boolean isAccessMethod(int access, String name) {
        return ((access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC
                && (access & Opcodes.ACC_SYNTHETIC) == Opcodes.ACC_SYNTHETIC
                && name.startsWith('access$'))
    }
}
