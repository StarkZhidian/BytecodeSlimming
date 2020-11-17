package com.hiro.bytecode_slimming

/**
 * class 相关数据封装类
 */
class ClassModel {
    /* 记录当前 class 名 */
    private String className
    /* 记录当前 Class 信息来源的 .class 文件 */
    private File classFile
    /**
     * 记录当前 class 信息 Model 对象来自的 jar 文件路径.
     * 如果是项目本身的源码编译得到的 .class 文件，则为 null
     */
    String fromJarFilePath

    ClassModel(String className, File classFile) {
        if (!Utils.isValidFile(classFile)) {
            throw new IllegalArgumentException("Argument classFile is not valid file!")
        }
        this.className = className
        this.classFile = classFile
    }

    byte[] getFileBytes() {
        return classFile.bytes
    }

    String getClassName() {
        return className
    }

    File getClassFile() {
        return classFile
    }
}