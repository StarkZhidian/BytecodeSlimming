package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.BaseMethodInlineProcessor
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/**
 * access$xxx 方法内联处理器
 * @author hongweiqiu
 */
class AccessMethodInlineProcessor extends BaseMethodInlineProcessor {

    private static final def TAG = "AccessMethodInlineProcessor"

    private Map<String, Boolean> needOptimizeClassNameRecorder = new HashMap<>()
    Map<String, String> className2ClassFilePathMap = new HashMap<>()
    Map<String, String> className2SuperClassNameMap = new HashMap<>()

    static def getInstance() {
        return InstanceHolder.INSTANCE
    }

    private AccessMethodInlineProcessor() {
    }

    void appendOptimizeClassFile(String className) {
        if (className == null
                || needOptimizeClassNameRecorder.containsKey(className)) {
            return
        }
        needOptimizeClassNameRecorder.put(className, true)
    }

    def getAccessMethodInfo(def className, def methodName) {
        if (Utils.isEmpty(className) || Utils.isEmpty(methodName)) {
            return null
        }
        return methodInlineInfoMap.get(makeInlineMethodInfoKey(className, methodName))
    }

    @Override
    void onAcceptClassFiles(List<File> classList) {
        // first traversal
        classList.each { classFile ->
            ClassReader cr = new ClassReader(classFile.bytes)
            cr.accept(new AccessMethodInlineFirstClassVisitor(
                    Opcodes.ASM6, null, classFile), ClassReader.EXPAND_FRAMES)
        }
        println "$TAG, methodInlineInfoMap: $methodInlineInfoMap"
        def traversalFilePathList = classList
        if (!needOptimizeClassNameRecorder.isEmpty()) {
            traversalFilePathList = new ArrayList<>()
            def classFilePath
            needOptimizeClassNameRecorder.keySet().each { String className ->
                // 循环添加当前类文件和父类文件
                // 因为存在内部类访问外部类的父类中的 protected 字段/方法的情况，
                // 所以第二步处理文件列表中需要带上 access$xxx 方法所在类的所有父类
                while ((!Utils.isEmpty(className))
                        && (!Utils.isEmpty(classFilePath = className2ClassFilePathMap.get(className)))
                        && (!traversalFilePathList.contains(classFilePath))) {
                    traversalFilePathList.add(classFilePath)
                    className = className2SuperClassNameMap.get(className)
                }
            }
        }
        println "$TAG, needOptimizeClassFileSize = [" + traversalFilePathList.size() + "], needOptimizeClassNameRecorder: " + traversalFilePathList
        // second traversal
        traversalFilePathList.each { classFile ->
            if (classFile instanceof String) {
                classFile = new File(classFile)
            }
            ClassReader cr = new ClassReader(classFile.bytes)
            ClassWriter classWriter = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
            cr.accept(new AccessMethodInlineSecondClassVisitor(Opcodes.ASM6, classWriter), ClassReader.EXPAND_FRAMES)
            // write final bytes data to origin file
            Utils.write2File(classWriter.toByteArray(), new File(classFile.parentFile, classFile.name))
        }
    }

    @Override
    void onOptimizeEnd() {
        super.onOptimizeEnd()
        needOptimizeClassNameRecorder.clear()
        System.gc()
    }

    static class InstanceHolder {
        static final def INSTANCE = new AccessMethodInlineProcessor();
    }
}