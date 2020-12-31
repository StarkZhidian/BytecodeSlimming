package com.hiro.bytecode_slimming

/**
 * 当前插件的所有 class 相关信息数据管理类
 */
class ClassDataManager {

    /* 类数据列表: className 到 ClassModel 对象的映射 */
    private Map<String, SingleClassData> classModelMap = new HashMap<>()

    private ClassDataManager() {
    }

    static void init() {
        clear()
    }

    static void clear() {
        getInstance().classModelMap.clear()
    }

    static ClassDataManager getInstance() {
        return InstanceHolder.INSTANCE
    }

    static SingleClassData getClassModel(String className) {
        return getInstance().classModelMap.get(className)
    }

    static List<SingleClassData> getClassDataList() {
        List<SingleClassData> classModelList = new LinkedList<>()
        Collection<SingleClassData> classModels = getInstance().classModelMap.values()
        for (SingleClassData classModel : classModels) {
            classModelList.add(classModel)
        }
        return classModelList
    }

    /**
     * 获取当前的类信息总数
     */
    static int getClassDataSize() {
        return getInstance().classModelMap.size()
    }

    static void addClassModel(SingleClassData classModel) {
        ClassDataManager classDataManager = getInstance()
        if (classModel == null || Utils.isEmpty(classModel.className)
                || classDataManager.classModelMap.containsKey(classModel.className)) {
            return
        }
        classDataManager.classModelMap.put(classModel.className, classModel)
    }

    /**
     * 通过类名获取类文件，当碰见参数为 android framework(android.jar) 中类的情况(或者对应的类文件不参与编译)时，
     * 这里返回值为 null，因为 framework 中的类文件本身不参与编译，也就没有对应的文件路径
     */
    static File getClassFile(String className) {
        if (Utils.isEmpty(className)) {
            return null
        }
        SingleClassData classModel = getClassModel(className)
        return classModel == null ? null : classModel.classFile
    }

    static String getSuperClass(String className) {
        if (Utils.isEmpty(className)) {
            return null
        }
        SingleClassData classModel = getClassModel(className)
        return classModel == null ? null : classModel.superClassName
    }

    /**
     * 移除某个类的信息，同时删除对应的 class 文件
     *
     * @param className 移除的类名
     * @return 是否删除成功
     */
    static boolean removeClassAndDeleteFile(String className) {
        if(Utils.isEmpty(className)) {
            return false
        }
        SingleClassData removedClassData = getInstance().classModelMap.remove(className)
        return removedClassData != null && Utils.deleteFile(removedClassData.classFile)
    }

    private static class InstanceHolder {
        static final ClassDataManager INSTANCE = new ClassDataManager()
    }
}