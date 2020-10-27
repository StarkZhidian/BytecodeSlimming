package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.BaseClassVisitor
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes;

/**
 * 第一次扫描 class 文件的访问器，主要是记录每一个类中的 access$xxx 方法信息
 * @author hongweqiiu
 */
class AccessMethodInlineFirstClassVisitor extends BaseClassVisitor {

    static final def TAG = "GetterSetterInlineFirstClassVisitor"

    AccessMethodInlineFirstClassVisitor(int api) {
        super(api);
    }

    AccessMethodInlineFirstClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return super.visitAnnotation(desc, visible)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        println "$TAG, access: $access, name: $name, desc: $desc, signature: $signature, exception: $exceptions"
        def methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)
        if (isAccessMethod(access, name)) {
            // 如果是编译器自动生成的 access$xxx 方法，则需要用自定义的方法访问器读取内部访问的字段/调用的方法信息
            return new AccessMethodInfoVisitor(Opcodes.ASM6, methodVisitor, className, name, desc)
        }
        return methodVisitor
    }

    /**
     * 通过方法 flag 和方法名判断是否为编译器生成的 access$xxx 方法
     * @param access 方法 flag
     * @param name 方法名
     * @return
     */
    static boolean isAccessMethod(int access, String name) {
        return ((access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC
                && (access & Opcodes.ACC_SYNTHETIC) == Opcodes.ACC_SYNTHETIC
                && name.startsWith('access$'))
    }
}
