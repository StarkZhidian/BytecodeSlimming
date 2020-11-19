package com.hiro.bytecode_slimming

import com.android.build.gradle.AppExtension
import com.hiro.bytecode_slimming.accessinline.AccessMethodInlineProcessor
import com.hiro.bytecode_slimming.getter_setter_inline.GetterSetterInlineProcessor
import com.hiro.bytecode_slimming.rm_not_runtime_annotation.AnnotationRemoveProcessor
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * bytecode_slimming 插件入口
 *
 * caution:
 * 1、使用该插件需禁用 R8 构建
 *
 * 2、在文件名不区分大小写的系统中(Windows)使用时可能出现问题，
 * 原因是在解压 jar 文件过程中可能出现同一个目录下有字母相同但是大小写不同的文件会存在相互替换的现象，
 * 导致类缺失问题，see {@link Utils#uncompressJarFile(File, File, UncompressFileFilter, JarUncompressListener)}
 * 因此该插件只适合开启了文件名大小写敏感的系统(Linux)上使用，
 * see: https://blog.csdn.net/weixin_42240407/article/details/96593863
 *
 * @author hongweiqiu
 */
class ApkSlimmingPlugin implements Plugin<Project> {
    private static final String TAG = "ApkSlimmingPlugin"

    @Override
    void apply(Project project) {
        Logger.setLogLevel(Logger.LOG_LEVEL_2)
        Logger.d3(TAG, "BytecodeSlimming, version: 1.2.3")
        def androidExtension = project.extensions.getByType(AppExtension)
        def apkSlimmingTransform = new ApkSlimmingTransform(project)
        // 添加 access$xxx 方法内联 processor
        apkSlimmingTransform.addProcessor(AccessMethodInlineProcessor.getInstance())
        // 添加 getter/setter 方法内联 processor
//        apkSlimmingTransform.addProcessor(processorManager.getProcessor(ProcessorManager.KEY_GETTER_SETTER_METHOD_INLINE))
        // 添加运行时不可见 annotation 注解去除 processor
        apkSlimmingTransform.addProcessor(AnnotationRemoveProcessor.getInstance())
        // 注册 transform
        androidExtension.registerTransform(apkSlimmingTransform)
    }
}