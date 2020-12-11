## 背景
在进行应用宝终端安装包尺寸优化的需求中，发现目前还没有对编译过程中产生的 .class 文件进行优化的 gradle 插件。
而对于大型工程来说，引入对 .class 文件进行优化的 gradle 插件不仅可以对当前存量的代码编译产生的 .class 文件进行优化，
更重要的是在编译流程中插入了一个可以对编译产物进行持续优化的插件，对日后的新增代码具有同样的优化作用。

## 成本和收益
在启用该插件（access 内联、不可见注解移除、R 文件瘦身）的情况下，应用宝灰度/正式版安装包大小缩减 100kb。
同时在新增相同代码量的情况下，相比不使用该插件而言，使用该插件可以缩小安装包尺寸增长的绝对值。
而成本为增加 30s (此为蓝盾构建结果，真实结果随着构建机器性能和内部优化 task 的开启数量变化) 左右的安装包构建时间。

## 使用
在工程中的 app module 中的 buildscript 闭包中的依赖仓库闭包（repositories）中加入该 maven 库：
`https://mirrors.tencent.com/repository/maven/tassistant`

随后在构建依赖闭包（dependencies）中加入如下库的依赖：
```groovy
/* 插件最新版本为 1.2.7 */
classpath 'com.hiro.android:bytecode_slimming:1.2.7'
classpath 'org.ow2.asm:asm:6.0'
```
完成后即可通过 gradle `apply` 函数来使用这个插件：
```groovy
apply plugin: 'com.hiro.android.bytecode_slimming'
```

插件默认会开启所有的优化功能，可通过插件提供的 `bytecodeSlimming` 扩展来自定义行为，语法如下：
```groovy
bytecodeSlimming {
  /* 是否启用整个插件，默认启用 */
  enable = true
  /* 插件 log 过滤级别，级别越高插件打印的 log 越少，共 1,2,3 三个值可选，默认值为 2 */
  logLevel 2
  /* 是否开启 access 方法内联功能，默认开启 */
  slimmingAccessInline true
  /* 是否开启运行时不可见注解去除，默认开启 */
  slimmingNonRuntimeAnnotation true
  /* 是否开启 R 文件瘦身，默认开启 */
  slimmingR true
  /* 不进行 access 方法内联的类名（语法：java 中类全限定名：包名.纯类名）列表，默认为空 */
  keepAccessClass
  /* 要保留的非运行时注解类名（语法：java 中类全限定名：包名.纯类名）列表，默认为空 */
  keepAnnotationClass
  /* 不进行瘦身的 R 文件类名（语法：java 中类全限定名：包名.纯类名）列表，默认为空 */
  keepRClass
}
```

## 框架原理

### Android 安装包构建流程

我们先来看一下 Android 安装包的构建流程：
![](./1.png)

图中 `Compilers` 过程为将工程 module 中的源代码和外部依赖库编译为 `.class/jar` 文件集，同时包含了 `proguard` (代码混淆)、`dexbuild` (dex 文件生成)、`mergedex`（dex 文件合并）等过程，最后将得到的一个/多个 `dex` 文件输出到指定目录，用于 apk 文件的合成和签名等。这里的 `Compilers` 过程的内部流程可以用如下的图来描述：

![](./2.png)



可以看到，在 dex 文件的生成过程中，通过 `javac` 将 Java 源代码编译为 .class 文件后，将 .class 文件转换为 dex 文件都是通过一系列的 `transform` 对象完成的。`Transform` 是 Gradle Android 构建工具为开发者提供的一个可以修改编译得到的 .class/jar 文件的抽象类模型，开发者可通过自定义 `Transfrom` 类并通过对应 API 将该类的对象注册到当前 module 的 Transform 列表中（图中橙色部分为开发者自定义的 `Transfrom` 对象）来实现这个目的。

值得注意的是，所有开发者注册的自定义 `Tranform` 对象都会在官方的 `Transform` 之前执行。每一个 `Transform` 对象的数据输入都是一系列文件/文件夹的描述对象，每一个 `Tranform` 对象都将上一个 `Trasnform` 对象执行的输出结果文件作为输入，同时，每一个 `Transform` 在执行完成后也需要将处理完成后的 class/jar 文件写入下一个 `Transform` 对象的输入文件夹，作为下一个 `Transform` 对象的数据输入。最后，当所有的 `Transform` 对象都执行完成之后，目标的 dex 文件也被构建出来了。 

### 框架架构

本插件本质上也是注册了一个自定义的 `Transform` 对象，在内部将输入的 class/jar 文件进行优化，最后将优化完成的 class 文件写入到下一个 `Transfrom` 的输入文件夹。该 `Transform` 的内部原理如下图：

![](./3.png)



## 优化点
该插件目前集成了三个优化点：access 方法内联、R 文件瘦身和运行时不可见注解去除。
每一个优化点对应框架架构小节图中蓝色部分的 class 优化处理器，理论上来说，class 优化处理器可无限添加。
当前添加的每个优化点的内部原理如下：

### access 方法内联
### R 文件瘦身
### 运行时不可见注解去除

更多的优化点待加入...


