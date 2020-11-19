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
    private static final def TAG = "Utils"

    private static Pattern paramsPat = Pattern.compile("(\\[?[BCZSIJFD])|(L[^;]+;)")
    /* 默认的缓存 byte 数组长度 */
    private static final int DEFAULT_BUFFER_LENGTH = 1024
    /* 读取到文件末尾的标识 */
    private static final int FILE_EOF = -1

    static def isEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0
    }

    static def textEquals(CharSequence text1, CharSequence text2) {
        return Objects.equals(text1, text2)
    }

    static void write2File(byte[] data, File outputFile) {
        if (outputFile == null || !outputFile.exists()) {
            return
        }
        FileOutputStream fos = new FileOutputStream(outputFile)
        fos.write(data)
        fos.close()
    }

    static void write2File(byte[] data, String outputFilePath) {
        write2File(data, new File(outputFilePath))
    }

    static void write2File(InputStream inputStream, File outputFile) {
        if (outputFile == null || !outputFile.exists()) {
            return
        }
        FileOutputStream fos = new FileOutputStream(outputFile);
        byte[] bs = new byte[1024]
        int readLen
        while ((readLen = inputStream.read(bs)) >= 0) {
            fos.write(bs, 0, readLen);
        }
        inputStream.close();
        fos.close();
    }

    static boolean isValidFile(File file) {
        return file != null && file.isFile() && file.size() > 0;
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

    static def isPrivate(int access) {
        return (access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE
    }

    static def isPublic(int access) {
        return (access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC;
    }

    static def isProtected(int access) {
        return (access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED;
    }

    static def isPackage(int access) {
        return !isPrivate(access) && !isProtected(access) && !isPublic(access);
    }

    static def toPublic(int access) {
        if (isPrivate(access)) {
            access = access & (~Opcodes.ACC_PRIVATE)
        } else if (isProtected(access)) {
            access = access & (~Opcodes.ACC_PROTECTED)
        }
        return (access | Opcodes.ACC_PUBLIC)
    }

    static def toPackage(int access) {
        if (isPrivate(access)) {
            access = access & (~Opcodes.ACC_PRIVATE)
        } else if (isProtected(access)) {
            access = access & (~Opcodes.ACC_PROTECTED)
        } else if (isPublic(access)) {
            access = access & (!Opcodes.ACC_PUBLIC)
        }
        return access
    }

    static def getParameterCountFromMethodDesc(String desc) {
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
     * 解压参数指定的 jar 文件到当前文件夹
     *
     * TODO 非常坑的是: Windows 系统文件默认不区分大小写，
     * TODO 在解压过程中可能出现同一个目录下有字母相同但是大小写不同的文件会存在相互替换的现象，导致类缺失问题，
     * TODO 而 linux 系统默认区分文件名的大小写，因此该插件只适合开启了文件名大小写敏感的系统上使用
     * TODO see: https://blog.csdn.net/weixin_42240407/article/details/96593863
     */
    static void uncompressJarFile(File jarFile, File outputDir,
                                  UncompressFileFilter uncompressFileFilter,
                                  JarUncompressListener uncompressListener) {
        Logger.d2(TAG, "uncompressJarFile: jarFile = [${jarFile.getAbsolutePath()}]," +
                " outputDir = [${outputDir.getAbsolutePath()}]," +
                " uncompressListener = [$uncompressListener]")
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
                write2File(jarFileDes.getInputStream(jarEntry), currentFile)
                // 回调接口
                if (uncompressListener != null) {
                    uncompressListener.onUncompressSingleFile(currentFile, jarFile.getAbsolutePath())
                }
            }
        }
    }

    static def readDataFromInputStream(InputStream inputStream) {
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
    static def isClassData(byte[] data) {
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