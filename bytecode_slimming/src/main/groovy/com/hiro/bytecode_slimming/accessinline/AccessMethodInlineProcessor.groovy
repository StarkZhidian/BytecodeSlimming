package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.BaseMethodInlineProcessor
import com.hiro.bytecode_slimming.ClassModel
import com.hiro.bytecode_slimming.Constants
import com.hiro.bytecode_slimming.Logger
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

/**
 * access$xxx 方法内联处理器
 * @author hongweiqiu
 */
class AccessMethodInlineProcessor extends BaseMethodInlineProcessor<AccessMethodInfo> {

    private static final def TAG = "AccessMethodInlineProcessor"

    /* 记录需要进行 access$ 方法内联优化的类名 */
    private Map<String, Boolean> needOptimizeClassNameRecorder = new HashMap<>()
    /* 类名到类文件路径的映射 */
    private Map<String, String> className2ClassFilePathMap = new HashMap<>()
    /* 类名到父类名的映射 */
    private Map<String, String> className2SuperClassNameMap = new HashMap<>()
    /* 类名到对应 ClassNode 的映射 */
    private Map<String, ClassNode> classNode2ClassNodeMap = new HashMap<>()

    static def getInstance() {
        return InstanceHolder.INSTANCE
    }

    private AccessMethodInlineProcessor() {
    }

    /**
     * 通过类名获取类文件绝对路径，当碰见参数为 android framework 中类的情况(或者对应的类文件不参与编译)时，
     * 这里返回值为 null，因为 framework 中的类文件本身不参与编译，也就没有对应的文件路径
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
     * 获取或者新塞入一个 access$ 内联方法描述对象
     *
     * @param className access$ 内联方法所属类名
     * @param methodName access$ 内联方法名
     * @param desc access$ 内联方法在 JVM 层的描述
     * @return 对应的内联方法描述对象
     */
    AccessMethodInfo getOrNewAccessMethodInfo(String className, String methodName, String desc) {
        if (Utils.isEmpty(className) || Utils.isEmpty(methodName) || Utils.isEmpty(desc)) {
            return null
        }
        AccessMethodInfo accessMethodInfo = getInlineMethod(className, methodName, desc)
        if (accessMethodInfo == null) {
            accessMethodInfo = new AccessMethodInfo(className, methodName, desc)
            appendInlineMethod(className, methodName, desc, accessMethodInfo)
        }
        return accessMethodInfo
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
     * 通过类名、方法名和方法描述获取对应的 access$xxx 方法信息
     */
    AccessMethodInfo getAccessMethodInfo(String className, String methodName, String desc) {
        if (Utils.isEmpty(className) || Utils.isEmpty(methodName) || Utils.isEmpty(desc)) {
            return null
        }
        return getInlineMethod(className, methodName, desc)
    }

    @Override
    void onAccept(List<ClassModel> classModelList) {
        // first traversal
        classModelList.each { classModel ->
            ClassReader cr = new ClassReader(classModel.fileBytes)
            cr.accept(new AccessMethodInlineFirstClassVisitor(
                    Opcodes.ASM6, classModel.classFile), ClassReader.EXPAND_FRAMES)
        }
        Logger.d2(TAG, "before filter, methodInlineInfoMap size = [${methodInlineInfoMap.size()}]")
        filterCanInlineAccessMethod()
        Logger.d2(TAG, "after filter, methodInlineInfoMap: $methodInlineInfoMap, \nsize = [${methodInlineInfoMap.size()}]")
        // 如果第一遍并且过滤扫描之后，没有发现需要进行 access$xxx 方法内联优化的类，直接返回
        if (methodInlineInfoMap.isEmpty()) {
            return
        }
        // second traversal, 只扫描 access$xxx 方法的相关类，
        // 对每一个 access$xxx 相关联的类进行处理并将结果输出到文件
        Map<String, byte[]> tempAccessClassOutputDataMap = new HashMap<>()
        methodInlineInfoMap.values().each { AccessMethodInfo accessMethodInfo ->
            try {
                tempAccessClassOutputDataMap.clear()
                String tempClassFilePath
                accessMethodInfo.relatedClassName.each { String relatedClassName ->
                    tempClassFilePath = getClassFilePath(relatedClassName)
                    if (Utils.isEmpty(tempClassFilePath)) {
                        return
                    }
                    File optimizeClassFile = new File(tempClassFilePath)
                    ClassReader cr = new ClassReader(optimizeClassFile.bytes)
                    ClassWriter classWriter = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
                    cr.accept(new AccessMethodInlineSecondClassVisitor(
                            Opcodes.ASM6, classWriter, accessMethodInfo), ClassReader.EXPAND_FRAMES)
                    // 这里先通过 map 暂存起来 class 文件路径和对应的输出数据是为了
                    // 在某个 access$xxx 方法内联过程中发生了异常时可以方便回滚
                    tempAccessClassOutputDataMap.put(tempClassFilePath, classWriter.toByteArray())
                }
                // 只有完整并且正常的执行完了整个 access$xxx 方法相关类文件的处理后才一并输出到对应文件
                tempAccessClassOutputDataMap.entrySet().each { Map.Entry<String, byte[]> entry ->
                    // write final bytes data to origin file
                    Utils.write2File(entry.value, entry.key)
                }
            } catch (Exception e) {
                Logger.e(TAG, "some was wrong when inline access method = [$accessMethodInfo]", e)
            }
        }
    }

    /**
     * 过滤出最终可以进行内联的 access 方法
     */
    private void filterCanInlineAccessMethod() {
        String className
        AccessMethodInfo accessMethodInfo
        Iterator<Map.Entry> iterator = methodInlineInfoMap.iterator()
        // 遍历第一次扫描过程中所有的 access$xxx 方法
        while (iterator.hasNext()) {
            accessMethodInfo = iterator.next().value
            // access$xxx 方法所在的类名和其父类都需要加入被关联的类名列表中
            className = accessMethodInfo.className
            while (!Utils.isEmpty(className)) {
                accessMethodInfo.appendRelatedClassName(className)
                className = getSuperClass(className)
            }
            // 如果当前 access$xxx 不能内联，则从 map 中移除
            if (!accessMethodCanInline(accessMethodInfo)) {
                Logger.d2(TAG, "filterCanInlineAccessMethod, can not inline accessMethod = [$accessMethodInfo]")
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
        try {
            // 1、access$ 方法中操作的字段/调用的方法列表这两者只有一个为空，一个不为空
            boolean operateSingleType = !(accessMethodInfo.operateFieldInfoList.isEmpty()
                    == accessMethodInfo.invokeMethodInfoList.isEmpty())
            // 2、access$ 方法中调用的方法为可以被我们修改访问权限的方法
            // （如果方法定义在 android framework 层，则不能被我们修改访问权限）
            boolean[] methodAccessCanBeChanged = [true]
            if (operateSingleType && accessMethodInfo.invokeMethodInfoList != null) {
                accessMethodInfo.invokeMethodInfoList.each { AccessMethodInfo.InvokeMethodInfo invokeMethodInfo ->
                    if (methodAccessCanBeChanged[0]) {
                        methodAccessCanBeChanged[0] = memberAccessCanBeChanged(
                                invokeMethodInfo.methodClassName,
                                invokeMethodInfo.methodName,
                                invokeMethodInfo.desc,
                                true)
                    }
                }
            }
            // 3、access$ 方法中操作的字段为可以被我们修改访问权限的方法
            // （如果字段定义在 android framework 层，则不能被我们修改访问权限）
            boolean[] fieldsAccessCanBeChanged = [true]
            if (operateSingleType && methodAccessCanBeChanged[0]
                    && accessMethodInfo.operateFieldInfoList != null) {
                accessMethodInfo.operateFieldInfoList.each { AccessMethodInfo.OperateFieldInfo operateFieldInfo ->
                    if (fieldsAccessCanBeChanged[0]) {
                        fieldsAccessCanBeChanged[0] = memberAccessCanBeChanged(
                                operateFieldInfo.fieldClassName,
                                operateFieldInfo.fieldName,
                                operateFieldInfo.desc,
                                false)
                    }
                }
            }
            return operateSingleType && (methodAccessCanBeChanged[0]) && (fieldsAccessCanBeChanged[0])
        } catch (Exception exception) {
            Logger.e(TAG, "accessMethodCanInline some thing was wrong!", exception)
        }
        return false
    }

    /**
     * 判断某个类成员是否可以被修改访问权限
     *
     * @param className 从成员实际访问者的口径中得到的成员所在的类名
     *                  （这里不一定是成员实际定义的地方，所以内部需要循环向父类查找）
     * @param memberName 成员名
     * @param desc 成员在 JVM 层的描述
     * @param isMethod 是否在方法中进行查找
     * @return
     * @throws IllegalArgumentException
     */
    private boolean memberAccessCanBeChanged(String className, String memberName, String desc,
                                             boolean isMethod) throws IllegalArgumentException {
        if (Utils.isEmpty(className) || Utils.isEmpty(memberName) || Utils.isEmpty(desc)) {
            throw new IllegalArgumentException(TAG + ": memberFromAndroidPackage, argument is illegal!")
        }
        String tempClassFilePath = null
        File tempClassFile = null
        boolean[] findResult = [false]
        // className 不为空，进行查找
        while (!Utils.isEmpty(className)) {
            // fetch from cache first
            ClassNode classNode = classNode2ClassNodeMap.get(className)
            // no classNode cache was found，read it from file
            if ((classNode == null) && (!Utils.isEmpty(tempClassFilePath = getClassFilePath(className)))
                    && (Utils.isValidFile(tempClassFile = new File(tempClassFilePath)))) {
                classNode = new ClassNode(Constants.ASM_VERSION)
                new ClassReader(tempClassFile.bytes).accept(classNode, ClassReader.EXPAND_FRAMES)
                // save to cache
                classNode2ClassNodeMap.put(className, classNode)
            }
            if (classNode == null) {
                // classNode is still null, skip this class and continue
                Logger.d1(TAG, "memberAccessCanBeChanged get classNode, hit don't join compile class = [$className]")
                className = getSuperClass(className)
                continue
            }
            if (isMethod) {
                // search method, traversal classNode.method list
                classNode.methods.each { MethodNode methodNode ->
                    if ((!findResult[0])
                            && (Utils.textEquals(memberName, methodNode.name))
                            && (Utils.textEquals(desc, methodNode.desc))
                            && (!Utils.isPublic(methodNode.access))) {
                        // found it!
                        findResult[0] = true
                    }
                }
            } else {
                // search field, traversal classNode.field list
                classNode.fields.each { FieldNode fieldNode ->
                    if ((!findResult[0])
                            && (Utils.textEquals(memberName, fieldNode.name))
                            && (Utils.textEquals(desc, fieldNode.desc))
                            && (!Utils.isPublic(fieldNode.access))) {
                        // found it!
                        findResult[0] = true
                    }
                }
            }
            if (findResult[0]) {
                break
            }
            // current class not found this member, recursion find in parent class
            className = getSuperClass(className)
        }
        Logger.d1(TAG, "memberAccessCanBeChanged, found result = [${findResult[0]}], className = [$className], memberName = [$memberName]")
        // 如果找到了对应方法/字段成员，并且对应的类文件是参与编译的，则代表该字段的访问权限可以被我们修改
        // 这里为什么要重新获取一次 classFilePath，因为查找过程中用到的 classNode 对象可能是从缓存中取到的。
        // 所以最后判断的时候需要重新获取
        return findResult[0] && Utils.isValidFile(new File(getClassFilePath(className)))
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