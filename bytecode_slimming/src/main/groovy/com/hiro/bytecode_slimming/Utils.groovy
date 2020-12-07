package com.hiro.bytecode_slimming

import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * 通用工具类
 */
class Utils {
    private static final String TAG = "Utils"

    private static Pattern paramsPat = Pattern.compile("(\\[?[BCZSIJFD])|(L[^;]+;)")
    /* 默认的缓存 byte 数组长度 */
    private static final int DEFAULT_BUFFER_LENGTH = 1024
    /* 读取到文件末尾的标识 */
    private static final int FILE_EOF = -1
    /* 秒到毫秒的倍率 */
    private static final int SECOND_2_MILLSECOND_RATE = 1000

    static boolean isEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0
    }

    static boolean textEquals(CharSequence text1, CharSequence text2) {
        return Objects.equals(text1, text2)
    }

    static void write2File(byte[] data, File outputFile) {
        if (outputFile == null || !outputFile.exists()) {
            return
        }
        FileOutputStream fos = null
        try {
            fos = new FileOutputStream(outputFile)
            fos.write(data)
        } catch (Throwable t) {
            Logger.e(TAG, "write2File method got exception: $t")
            throw t
        } finally {
            closeStream(fos)
        }
    }

    static void write2File(byte[] data, String outputFilePath) {
        write2File(data, new File(outputFilePath))
    }

    static void write2File(InputStream inputStream, File outputFile, boolean closeInputStream) {
        if (outputFile == null || !outputFile.exists()) {
            return
        }
        FileOutputStream fos = null
        try {
            fos = new FileOutputStream(outputFile);
            byte[] bs = new byte[1024]
            int readLen
            while ((readLen = inputStream.read(bs)) >= 0) {
                fos.write(bs, 0, readLen);
            }
        } catch (Throwable t) {
            Logger.e(TAG, "write2File method got exception: $t")
            throw t
        } finally {
            if (closeInputStream) {
                closeStream(inputStream)
            }
            closeStream(fos)
        }
    }

    static boolean closeStream(Closeable closeable) {
        if (closeable == null) {
            return false
        }
        try {
            closeable.close()
            return true
        } catch (Throwable t) {
            Logger.e(TAG, "closeStream: $closeable got exception: $t")
            return false
        }
    }

    static boolean isValidFile(File file) {
        return file != null && file.isFile() && file.exists() && file.size() > 0;
    }

    static boolean isValidDir(File file) {
        return file != null && file.isDirectory()
    }

    /**
     * 通过文件名后缀判断是否是 jar 文件
     */
    static boolean isJarFile(File file) {
        return file != null && file.isFile() && file.name.endsWith(".jar")
    }

    /**
     * 通过文件后缀判断是否是 .class 文件
     */
    static boolean isClassFile(File file) {
        return file != null && file.isFile() && file.name.endsWith(Constants.CLASS_FILE_SUFFIX)
    }

    static boolean isPrivate(int access) {
        return (access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE
    }

    static boolean isPublic(int access) {
        return (access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC;
    }

    static boolean isProtected(int access) {
        return (access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED;
    }

    static boolean isPackage(int access) {
        return !isPrivate(access) && !isProtected(access) && !isPublic(access);
    }

    static boolean isStatic(int access) {
        return (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC
    }

    static boolean isFinal(int access) {
        return (access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL
    }

    static int toPublic(int access) {
        if (isPrivate(access)) {
            access = access & (~Opcodes.ACC_PRIVATE)
        } else if (isProtected(access)) {
            access = access & (~Opcodes.ACC_PROTECTED)
        }
        return (access | Opcodes.ACC_PUBLIC)
    }

    static int toPackage(int access) {
        if (isPrivate(access)) {
            access = access & (~Opcodes.ACC_PRIVATE)
        } else if (isProtected(access)) {
            access = access & (~Opcodes.ACC_PROTECTED)
        } else if (isPublic(access)) {
            access = access & (!Opcodes.ACC_PUBLIC)
        }
        return access
    }

    static int getParameterCountFromMethodDesc(String desc) {
        int beginIndex = desc.indexOf('(') + 1;
        int endIndex = desc.lastIndexOf(')');
        String paramsDesc = desc.substring(beginIndex, endIndex);
        if (paramsDesc.isEmpty()) return 0;
        int count = 0;
        Matcher matcher = paramsPat.matcher(paramsDesc);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * 解压参数指定的 jar 文件到指定的文件夹下
     *
     * @param jarFile 要解压的 jar 文件
     * @param outputDir 解压文件输出目录
     * @param uncompressFileFilter 解压文件过滤器
     * @param uncompressListener 解压文件监听
     */
    static void uncompressJarFile(File jarFile, File outputDir,
                                  UncompressFileFilter uncompressFileFilter,
                                  JarUncompressListener uncompressListener) {
        Logger.d2(TAG, "uncompressJarFile: jarFile = [${jarFile.getAbsolutePath()}],"
                + " outputDir = [${outputDir.getAbsolutePath()}]")
        if ((!isValidFile(jarFile)) || (!isJarFile(jarFile))) {
            return
        }
        JarFile jarFileDes = new JarFile(jarFile)
        Enumeration<JarEntry> entries = jarFileDes.entries()
        JarEntry jarEntry
        File currentFile
        while (entries.hasMoreElements()) {
            jarEntry = entries.nextElement()
            currentFile = new File(outputDir, jarEntry.name)
            if ((uncompressFileFilter != null)
                    && (!uncompressFileFilter.canUncompress(currentFile.getAbsolutePath(), jarFile.getAbsolutePath()))) {
                // 如果设置了文件解压过滤器，并且得到的结果时 false，则 skip
                continue
            }
            Logger.d1(TAG, "uncompressJarFile: current file: ${currentFile.getAbsolutePath()}, isFile = ${currentFile.isFile()}")
            if (currentFile.exists()) {
                currentFile.delete()
            }
            if (jarEntry.isDirectory()) {
                // 处理文件夹
                currentFile.mkdirs()
            } else {
                // 处理文件
                currentFile.parentFile.mkdirs()
                currentFile.createNewFile()
                write2File(jarFileDes.getInputStream(jarEntry), currentFile, true)
                // 回调接口
                if (uncompressListener != null) {
                    uncompressListener.onUncompressSingleFile(currentFile, jarFile.getAbsolutePath())
                }
            }
        }
        jarFileDes.close()
    }

    static byte[] readDataFromInputStream(InputStream inputStream) {
        if (inputStream == null) {
            return null
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream()
        byte[] buffer = new byte[DEFAULT_BUFFER_LENGTH]
        int readLength
        while ((readLength = inputStream.read(buffer)) > 0) {
            bos.write(buffer, 0, readLength)
        }
        return bos.toByteArray()
    }

    /**
     * 通过判断给定数据流的 magic 魔数部分数据来判断是否是 .class 文件的数据
     *
     * @param data 给定的文件数据流
     */
    static boolean isClassData(byte[] data) {
        // .class 文件的前 4 个字节为魔数部分，值为 0xCAFEBABE
        if (data.length < 4) {
            return false
        }
        return ((((int) data[0]) == 0xCA)
                && (((int) data[1] == 0xFE))
                && (((int) data[2] == 0xBA))
                && (((int) data[3] == 0xBE)))
    }

    /**
     * 删除参数指定的文件，如果执行操作后，对应文件不存在了，返回 true，否则返回 false
     */
    static boolean deleteFile(File file) {
        if (file != null && file.exists()) {
            return file.delete()
        }
        return true
    }

    /**
     * 将 java 层的类名格式换成 JVM 层的类名，例：java.lang.String -> java/lang/String
     */
    static String getJVMClassName(String javaClassName) {
        if (isEmpty(javaClassName)) {
            return javaClassName
        }
        return javaClassName.replace('.', '/')
    }

    /**
     * 毫秒转换到秒
     */
    static long millSecond2Second(long millSecond) {
        return millSecond / SECOND_2_MILLSECOND_RATE
    }

    /**
     * 以 / 作为分隔符，分隔 JVM 层的类名中各个部分，例：java/lang/String -> [java, lang, String]
     */
    static String[] splitJVMClassName(String className) {
        if (isEmpty(className)) {
            return className
        }
        return className.split(Constants.JVM_CLASS_INTERVAL)
    }

    /**
     * 获取参数指定的 JVM 中类全限定名的纯类名部分字符串
     */
    static String getPureClassName(String jvmClassName) {
        if (isEmpty(jvmClassName)) {
            return jvmClassName
        }
        int classIntervalPos = jvmClassName.lastIndexOf(Constants.JVM_CLASS_INTERVAL)
        return classIntervalPos >= 0 ? jvmClassName.substring(classIntervalPos + 1) : jvmClassName
    }

    /**
     * 获取参数指定的 JVM 类型描述是否为基本类型
     */
    static boolean isBasicType(String jvmTypeDesc) {
        if (isEmpty(jvmTypeDesc)) {
            return false
        }
        return ((!jvmTypeDesc.startsWith(Constants.ARRAY_CLASS_DESC_PREFIX))
                && (!jvmTypeDesc.startsWith(Constants.CUSTOM_CLASS_DESC_PREFIX)))
    }

    /**
     * jar 文件中单个文件解压过滤器
     */
    interface UncompressFileFilter {
        /**
         * 如果返回 true，则当前文件路径对应的文件可以进行解压操作，回调该方法时，文件还未开始解压
         */
        boolean canUncompress(String uncompressedFile, String fromJarFilePath)
    }

    /**
     * jar 文件解压时的监听接口
     */
    interface JarUncompressListener {
        /**
         * jar 文件中单个文件完成解压时回调的方法
         *
         * @param uncompressedFile 解压完成的单个文件
         * @param fromJarFilePath 解压的文件所来自 jar 文件的绝对路径
         *
         */
        void onUncompressSingleFile(File uncompressedFile, String fromJarFilePath)
    }
}