package com.hiro.bytecode_slimming

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.hiro.bytecode_slimming.accessinline.AccessMethodInlineProcessor
import com.hiro.bytecode_slimming.rm_not_runtime_annotation.AnnotationRemoveProcessor
import org.gradle.api.GradleException
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
class BytecodeSlimmingPlugin implements Plugin<Project> {
    private static final String TAG = "BytecodeSlimmingPlugin"

    @Override
    void apply(Project project) {
        if (project.plugins.hasPlugin(LibraryPlugin.class)) {
            throw new GradleException("$TAG can't be used to library module.")
        }
        if (project.plugins.hasPlugin(AppPlugin)) {
            Logger.d3(TAG, "version = [${Constants.VERSION}]")
            // 创建插件的扩展选项
            project.extensions.create(BytecodeSlimmingExtension.NAME, BytecodeSlimmingExtension)
            def apkSlimmingTransform = new ApkSlimmingTransform(project)
            // 注册 transform
            project.extensions.getByType(AppExtension).registerTransform(apkSlimmingTransform)
            // 注册应用该插件的 module 对应的 gradle 文件配置完成后调用监听
            project.afterEvaluate {
                BytecodeSlimmingExtension bytecodeSlimmingExtension = project[BytecodeSlimmingExtension.NAME]
                Logger.d3(TAG, "BytecodeSlimmingExtension = [$bytecodeSlimmingExtension]")
                // 未启用优化，直接返回
                if (!bytecodeSlimmingExtension.enable) {
                    return
                }
                if (bytecodeSlimmingExtension.slimmingAccessInline) {
                    // 添加 access$xxx 方法内联 processor
                    AccessMethodInlineProcessor accessMethodInlineProcessor = AccessMethodInlineProcessor.getInstance()
                    accessMethodInlineProcessor.addInKeepClassList(bytecodeSlimmingExtension.keepAccessClass)
                    apkSlimmingTransform.addProcessor(accessMethodInlineProcessor)
                }
                if (bytecodeSlimmingExtension.slimmingNonRuntimeAnnotation) {
                    // 添加运行时不可见 annotation 注解去除 processor
                    AnnotationRemoveProcessor annotationRemoveProcessor = AnnotationRemoveProcessor.getInstance()
                    annotationRemoveProcessor.addInKeepClassList(bytecodeSlimmingExtension.keepAnnotationClass)
                    apkSlimmingTransform.addProcessor(annotationRemoveProcessor)
                }
//                if (bytecodeSlimmingExtension.slimmingR) {
//                    // 添加 R 文件瘦身 processor
//                    RSlimmingProcessor rSlimmingProcessor = RSlimmingProcessor.getInstance()
//                    rSlimmingProcessor.addInKeepClassList(bytecodeSlimmingExtension.keepRClass)
//                    apkSlimmingTransform.addProcessor(rSlimmingProcessor)
//                }
                // 设置全局的 logLevel
                Logger.setLogLevel(bytecodeSlimmingExtension.logLevel)
            }
        }
    }
}