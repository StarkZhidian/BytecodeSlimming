package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.BaseClassVisitor
import com.hiro.bytecode_slimming.ProcessorManager
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 第一次扫描 class 文件的访问器，主要是记录每一个类中的 access$xxx 方法信息
 * @author hongweqiiu
 */
class AccessMethodInlineFirstClassVisitor extends BaseClassVisitor {

    private static final def TAG = "AccessMethodInlineFirstClassVisitor"

    def classFile

    AccessMethodInlineFirstClassVisitor(int api) {
        super(api)
    }

    AccessMethodInlineFirstClassVisitor(int api, ClassVisitor cv, File classFile) {
        super(api, cv)
        this.classFile = classFile
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        AccessMethodInlineProcessor.getInstance().className2ClassFilePathMap.put(name, classFile.absolutePath)
        if (!Utils.textEquals(superName, "java/lang/Object")) {
            AccessMethodInlineProcessor.getInstance().className2SuperClassNameMap.put(name, superName)
        }
    }

    @Override
    FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return super.visitField(access, name, desc, signature, value)

    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (isAccessMethod(access, name)) {
//            println "$TAG, access: $access, name: $name, desc: $desc, signature: $signature, exception: $exceptions"
            // 如果是编译器自动生成的 access$xxx 方法，
            // 则需要用自定义的方法访问器读取内部访问的字段/调用的方法信息
            // 同时记录类文件对象
            AccessMethodInlineProcessor processor =
                    ProcessorManager.getInstance().getProcessor(ProcessorManager.KEY_ACCESS_METHOD_INLINE)
            processor.appendOptimizeClassFile(className)
            return new AccessMethodInfoVisitor(Opcodes.ASM6, null, className, access, name, desc, signature, exceptions)
        }
        return new AccessMethodInvokeVisitor(Opcodes.ASM6, null, className, access, name, desc, signature, exceptions, classFile)
    }

    @Override
    void visitEnd() {
        super.visitEnd()
        AccessMethodInlineProcessor processor =
                ProcessorManager.getInstance().getProcessor(ProcessorManager.KEY_ACCESS_METHOD_INLINE)
        Iterator<Map.Entry> iterator = processor.methodInlineInfoMap.iterator()
        // 遍历第一次扫描过程中所有的 access$xxx 方法，如果不能删除，则从 map 中移除
        while (iterator.hasNext()) {
            if (!accessMethodCanDelete(iterator.next().value)) {
                iterator.remove()
            }
        }
    }

    static def accessMethodCanDelete(AccessMethodInfo accessMethodInfo) {
        if (accessMethodInfo == null) {
            return false
        }
        // 两者只有一个为空，一个不为空
        return (!(accessMethodInfo.operateFieldInfoList.isEmpty() == accessMethodInfo.invokeMethodInfoList.isEmpty()))
    }

    /**
     * 通过方法 flag 和方法名判断是否为编译器生成的 access$xxx 方法
     * @param access 方法 flag
     * @param name 方法名
     * @return
     */
    private static boolean isAccessMethod(int access, String name) {
        return ((access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC
                && (access & Opcodes.ACC_SYNTHETIC) == Opcodes.ACC_SYNTHETIC
                && name.startsWith('access$'))
    }
}
