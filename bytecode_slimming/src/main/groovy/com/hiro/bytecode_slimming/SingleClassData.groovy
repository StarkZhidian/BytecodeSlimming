package com.hiro.bytecode_slimming

/**
 * 单个 class 相关数据封装类
 */
class SingleClassData {
    /* 记录当前 class 名 */
    private String className
    /* 记录当前 Class 信息来源的 .class 文件 */
    private File classFile
    /* 记录当前 class 的父类名 */
    private String superClassName
    /**
     * 记录当前 class 信息 Model 对象来自的 jar 文件路径.
     * 如果是项目本身的源码编译得到的 .class 文件，则为 null
     */
    String fromJarFilePath

    SingleClassData(String className, File classFile, String superClassName) {
        if (Utils.isEmpty(className) || !Utils.isValidFile(classFile) || Utils.isEmpty(superClassName)) {
            throw new IllegalArgumentException("Argument is illegal!")
        }
        this.className = className
        this.classFile = classFile
        this.superClassName = superClassName
    }

    SingleClassData(String className, File classFile, String superClassName, String fromJarFilePath) {
        this(className, classFile, superClassName)
        this.fromJarFilePath = fromJarFilePath
    }

    byte[] getFileBytes() {
        return classFile.bytes
    }

    String getClassName() {
        return className
    }

    String getSuperClassName() {
        return superClassName
    }

    File getClassFile() {
        return classFile
    }

    @Override
    String toString() {
        return "{className: [$className], classFile: [${classFile.absolutePath}], fromJarFilePath: [$fromJarFilePath]"
    }
}