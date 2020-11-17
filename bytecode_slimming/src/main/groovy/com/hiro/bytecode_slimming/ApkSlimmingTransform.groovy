package com.hiro.bytecode_slimming

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode

/**
 * Transform 类
 */
class ApkSlimmingTransform extends Transform {
    private static final def TAG = "ApkSlimmingTransform"

    final Project project
    /* class/jar 处理器列表 */
    private List<BaseProcessor> processorList = new ArrayList<>()
    /* 类数据列表: className 到 ClassModel 对象的映射 */
    private Map<String, ClassModel> classModelMap = new HashMap<>()
    /* jar 文件中过滤 class 文件的处理器 */
    private Utils.UncompressFileFilter classFileUncompressFilter = new ClassFileUncompressFilter()
    /* Jar 文件解压监听处理器 */
    private Utils.JarUncompressListener jarUncompressListener = new JarFileUncompressListener()

    ApkSlimmingTransform(Project project) {
        this.project = project
    }

    ClassModel getClassModel(String className) {
        return classModelMap.get(className)
    }

    List<ClassModel> getClassModelList() {
        List<ClassModel> classModelList = new LinkedList<>()
        Collection<ClassModel> classModels = classModelMap.values()
        for (ClassModel classModel : classModels) {
            classModelList.add(classModel)
        }
        return classModelList
    }

    void addProcessor(BaseProcessor processor) {
        processorList.add(processor)
    }

    void addClassModel(ClassModel classModel) {
        if (classModel == null || Utils.isEmpty(classModel.className)
                || classModelMap.containsKey(classModel.className)) {
            return
        }
        classModelMap.put(classModel.className, classModel)
    }

    @Override
    String getName() {
        return TAG;
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    boolean isIncremental() {
        return false;
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider,
                   boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        handleJarFileList(inputs, outputProvider)
        handleClassFileList(inputs)
        processorList.each { processor ->
            processor.optimizeStart()
            processor.accept(getClassModelList())
            processor.optimizeEnd()
        }
        // 处理完成之后进行 class 文件的拷贝
        copyClassDir(inputs, outputProvider)
    }

    /**
     * 获取输入中的 jar 文件列表
     * @param inputs 输入信息
     * @return
     */
    private void handleJarFileList(Collection<TransformInput> inputs,
                                   TransformOutputProvider outputProvider) {
        final File compiledClassesDir = getFirstCompiledClassesDir(inputs)
        inputs.each { input ->
            // 对类型为jar 文件的 input 进行遍历
            input.jarInputs.each { jarInput ->
                //jar文件一般是第三方依赖库jar文件，添加到 list 中
                File file = jarInput.getFile()
                if (Utils.isValidFile(file) && Utils.isJarFile(file)) {
                    // 生成输出路径，将 jar 里面的 class 文件看成用户自己输入的 class 代码，
                    // 解压到对应目录下，并对解压后的 .class 文件生成对应的 ClassModel 对象
                    Utils.uncompressJarFile(file, compiledClassesDir,
                            classFileUncompressFilter, jarUncompressListener)
                }
            }
        }
    }

    /**
     * 获取 class 文件列表
     * @param inputs 输入信息
     * @return
     */
    private void handleClassFileList(Collection<TransformInput> inputs) {
        inputs.each { input ->
            // 这里一般是自己编写的 .class 文件
            input.directoryInputs.each { directoryInput ->
                directoryInput.file.eachFileRecurse { file ->
                    if (Utils.isClassFile(file)) {
                        // class 文件，直接解析生成对应的 ClassModel 对象
                        generateSingleClassModel(file, null)
                    }
                }
            }
        }
    }

    /**
     * 生成 .class 文件对应的 ClassModel 对象
     *
     * @param classFile .class 文件对象
     * @param jarFilePath 当前 .class 文件是从哪个 jar 文件中解压出来的，
     *                    如果是项目内创建的源码文件编译得到的 .class 文件，则传 null
     * @return 是否
     */
    private boolean generateSingleClassModel(File classFile, String jarFilePath) {
        Logger.d1(TAG, "generateSingleClassModel, classFile = [${classFile.getAbsolutePath()}," +
                " jarFilePath = [$jarFilePath]")
        if ((!Utils.isValidFile(classFile)) || (!Utils.isClassFile(classFile))) {
            return false
        }
        try {
            ClassNode classParser = new ClassNode()
            new ClassReader(classFile.bytes).accept(classParser, ClassReader.EXPAND_FRAMES)
            ClassModel classModel = new ClassModel(classParser.name, classFile)
            classModel.fromJarFilePath = jarFilePath
            addClassModel(classModel)
            return true
        } catch (Exception exception) {
            Logger.e(TAG, "generateSingleClassModel", exception)
        }
        return false
    }

    private static File getFirstCompiledClassesDir(Collection<TransformInput> inputs) {
        File resultDir = null
        inputs.each { input ->
            // 这里一般是自己编写的 .class 文件的目录
            input.directoryInputs.each { DirectoryInput directoryInput ->
                if (resultDir == null || (!resultDir.isDirectory())) {
                    Logger.d3(TAG, "getFirstCompiledClassesDir = " + directoryInput.file.getAbsolutePath())
                    resultDir = directoryInput.file
                }
            }
        }
        return resultDir
    }

    /**
     * 这里需要将编译后最终得到的 .class 文件输送到下一个 transform 的输入位置
     * @param inputs
     * @param outputProvider
     */
    private static void copyClassDir(Collection<TransformInput> inputs,
                                     TransformOutputProvider outputProvider) {
        inputs.each { input ->
            // 这里一般是自己编写的 .class 文件的目录
            input.directoryInputs.each { DirectoryInput directoryInput ->
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                Logger.d3(TAG, "copyClassDir, from dir = [" + directoryInput.file.getAbsolutePath() + "], to dir = [" + dest.getAbsolutePath() + "]")
                FileUtils.copyDirectory(directoryInput.file, dest);
            }
        }
    }

    /**
     * 项目中引入的 sdk 中 jar 内部文件的解压过滤器
     */
    private class ClassFileUncompressFilter implements Utils.UncompressFileFilter {

        @Override
        boolean canUncompress(String uncompressedFilePath, String fromJarFilePath) {
            // 只解压 .class 文件
            return ((!Utils.isEmpty(uncompressedFilePath))
                    && (uncompressedFilePath.endsWith(Constants.CLASS_FILE_SUFFIX)))
        }
    }

    /**
     * 项目中引入的 sdk 中 jar 文件的解压监听
     */
    private class JarFileUncompressListener implements Utils.JarUncompressListener {

        @Override
        void onUncompressSingleFile(File uncompressedFile, String fromJarFilePath) {
            if (Utils.isValidFile(uncompressedFile) && Utils.isClassFile(uncompressedFile)) {
                // 当前文件如果是合法的 .class 文件，则生成对应的 ClassModel 对象
                generateSingleClassModel(uncompressedFile, fromJarFilePath)
            }
        }
    }
}
