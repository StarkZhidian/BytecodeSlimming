package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.BaseMethodInlineProcessor
import com.hiro.bytecode_slimming.ClassModel
import com.hiro.bytecode_slimming.Logger
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

    /* 记录需要进行 access$ 方法内联优化的类名 */
    private Map<String, Boolean> needOptimizeClassNameRecorder = new HashMap<>()
    /* 类名到类文件路径的映射 */
    private Map<String, String> className2ClassFilePathMap = new HashMap<>()
    /* 类名到父类名的映射 */
    private Map<String, String> className2SuperClassNameMap = new HashMap<>()

    static def getInstance() {
        return InstanceHolder.INSTANCE
    }

    private AccessMethodInlineProcessor() {
    }

    /**
     * 通过类名获取类文件绝对路径
     */
    String getClassFilePath(String className) {
        if (Utils.isEmpty(className)) {
            return null
        }
        return className2ClassFilePathMap.get(className)
    }

    /**
     * 通过类名获取对应的父类名
     */
    String getSuperClass(String className) {
        if (Utils.isEmpty(className)) {
            return null
        }
        return className2SuperClassNameMap.get(className)
    }

    /**
     * 添加需要进行 access$ 方法内联的类名
     */
    void appendOptimizeClassFile(String className) {
        if (Utils.isEmpty(className)) {
            return
        }
        needOptimizeClassNameRecorder.put(className, true)
    }

    /**
     * 添加类名到类文件路径的映射信息 item
     *
     * @param className 类名
     * @param filePath 对应的类文件路径
     */
    void putClassName2FilePathMapItem(String className, String filePath) {
        if (Utils.isEmpty(className) || Utils.isEmpty(filePath)) {
            return
        }
        className2ClassFilePathMap.put(className, filePath)
    }

    /**
     * 添加类名到父类映射信息 item
     *
     * @param className 类名
     * @param superClassName 父类名
     */
    void putClassName2SuperClassNameMapItem(String className, String superClassName) {
        if (Utils.isEmpty(className) || Utils.isEmpty(superClassName)) {
            return
        }
        className2SuperClassNameMap.put(className, superClassName)
    }

    /**
     * 通过类名和方法名获取对应的 access$xxx 方法信息
     */
    AccessMethodInfo getAccessMethodInfo(def className, def methodName) {
        if (Utils.isEmpty(className) || Utils.isEmpty(methodName)) {
            return null
        }
        return methodInlineInfoMap.get(makeInlineMethodInfoKey(className, methodName))
    }

    @Override
    void onAccept(List<ClassModel> classModelList) {
        // first traversal
        classModelList.each { classModel ->
            ClassReader cr = new ClassReader(classModel.fileBytes)
            cr.accept(new AccessMethodInlineFirstClassVisitor(
                    Opcodes.ASM6, classModel.classFile), ClassReader.EXPAND_FRAMES)
        }
        filterCanInlineAccessMethod()
        Logger.d2(TAG, "methodInlineInfoMap: $methodInlineInfoMap")
        // 如果第一遍并且过滤扫描之后，没有发现需要进行 access$xxx 方法内联优化的类，直接返回
        if (needOptimizeClassNameRecorder.isEmpty()) {
            return
        }
        List<String> traversalFilePathList = new ArrayList<>()
        def classFilePath
        needOptimizeClassNameRecorder.keySet().each { String className ->
            // 循环添加当前类文件和父类文件
            // 因为存在内部类访问外部类的父类中的 protected 字段/方法的情况，
            // 所以第二步处理文件列表中需要带上 access$xxx 方法所在类的所有父类
            while ((!Utils.isEmpty(className))
                    && (!Utils.isEmpty(classFilePath = getClassFilePath(className)))
                    && (!traversalFilePathList.contains(classFilePath))) {
                traversalFilePathList.add(classFilePath)
                className = getSuperClass(className)
            }
        }
        Logger.d2(TAG, "needOptimizeClassFileSize = [" + traversalFilePathList.size() + "], needOptimizeClassNameRecorder: " + traversalFilePathList)
        // second traversal
        traversalFilePathList.each { optimizeClassFilePath ->
            File optimizeClassFile = new File(optimizeClassFilePath)
            ClassReader cr = new ClassReader(optimizeClassFile.bytes)
            ClassWriter classWriter = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
            cr.accept(new AccessMethodInlineSecondClassVisitor(Opcodes.ASM6, classWriter), ClassReader.EXPAND_FRAMES)
            // write final bytes data to origin file
            Utils.write2File(classWriter.toByteArray(), optimizeClassFile)
        }
    }

    /**
     * 过滤出最终可以进行内联的 access 方法
     */
    private void filterCanInlineAccessMethod() {
        Iterator<Map.Entry> iterator = methodInlineInfoMap.iterator()
        // 遍历第一次扫描过程中所有的 access$xxx 方法，如果不能删除，则从 map 中移除
        while (iterator.hasNext()) {
            if (!accessMethodCanInline(iterator.next().value)) {
                iterator.remove()
            }
        }
    }

    /**
     * 判断参数指定的 access$ 方法是否可以被内联
     */
    private boolean accessMethodCanInline(AccessMethodInfo accessMethodInfo) {
        if (accessMethodInfo == null) {
            return false
        }
        // 1、access$ 方法中操作的字段/调用的方法列表这两者只有一个为空，一个不为空
        boolean operateSingleType = !(accessMethodInfo.operateFieldInfoList.isEmpty()
                == accessMethodInfo.invokeMethodInfoList.isEmpty())
        // 2、access$ 方法中调用的方法不为使用到的 android 源码中类的方法
        boolean[] methodsFromAndroid = [false]
        if (operateSingleType && accessMethodInfo.invokeMethodInfoList != null) {
            accessMethodInfo.invokeMethodInfoList.each { AccessMethodInfo.InvokeMethodInfo invokeMethodInfo ->
                if (!methodsFromAndroid[0]) {
                    methodsFromAndroid[0] = classFromAndroidPackage(invokeMethodInfo.methodClassName, true)
                }
            }
        }
        // 3、access$ 方法中操作的字段不为使用到的 android 源码中类的字段
        boolean[] fieldsFromAndroid = [false]
        if (operateSingleType && !methodsFromAndroid[0]
                && accessMethodInfo.operateFieldInfoList != null) {
            accessMethodInfo.operateFieldInfoList.each { AccessMethodInfo.OperateFieldInfo operateFieldInfo ->
                if (!fieldsFromAndroid[0]) {
                    fieldsFromAndroid[0] = classFromAndroidPackage(operateFieldInfo.fieldClassName, true)
                }
            }
        }
        return operateSingleType && (!methodsFromAndroid[0]) && (!fieldsFromAndroid[0])
    }

    /**
     * 判断某个类名是否是 Android 官方类，标准为判断参数类名中是否包含 android 包间隔常量
     *
     * @param recursion 是否要递归判断父类
     */
    private boolean classFromAndroidPackage(String className, boolean recursion) {
        if (Utils.isEmpty(className)) {
            return false
        }
        return (classFromAndroidPackageDirectly(className)
                || (recursion && classFromAndroidPackage(getSuperClass(className), recursion)))
    }

    private static boolean classFromAndroidPackageDirectly(String className) {
        return ((!Utils.isEmpty(className)) && (className.contains("/android") || className.contains("android/")))
    }

    @Override
    void onOptimizeEnd() {
        super.onOptimizeEnd()
        needOptimizeClassNameRecorder.clear()
        System.gc()
    }

    private static class InstanceHolder {
        static final def INSTANCE = new AccessMethodInlineProcessor();
    }
}