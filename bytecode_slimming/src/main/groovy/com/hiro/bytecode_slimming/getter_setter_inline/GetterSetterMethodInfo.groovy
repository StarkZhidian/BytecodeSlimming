package com.hiro.bytecode_slimming.getter_setter_inline


/**
 * 描述 GetterSetter 方法信息的类
 */
class GetterSetterMethodInfo {

    /* getter/setter 方法所在的类名 */
    def className
    /* getter/setter 的方法名 */
    def methodName
    /* 方法的描述（java 层的方法签名除去方法名） */
    def desc
    /* getter/setter 方法内部访问(通过 getfield 指令)的字段 */
    def readFieldInfo

    GetterSetterMethodInfo(def className, def methodName, def desc) {
        this.className = className
        this.methodName = methodName
        this.desc = desc
    }

    void setReadFieldInfo(OperateFieldInfo readFieldInfo) {
        this.readFieldInfo = readFieldInfo
    }

    @Override
    String toString() {
        return "{className: $className, methodName: $methodName, desc: $desc, readlFieldInfo: " + readFieldInfo + "}"
    }

    /**
     * 记录 getter/setter 方法内部访问/修改的字段信息
     */
    static class OperateFieldInfo {
        def opcode
        def fieldClassName
        def fieldName
        def desc

        OperateFieldInfo(def opcode, def fieldClassName, def fieldName, def desc) {
            this.opcode = opcode
            this.fieldClassName = fieldClassName
            this.fieldName = fieldName
            this.desc = desc
        }

        @Override
        String toString() {
            return "{opcode: $opcode, fieldClassName: $fieldClassName, fieldName: $fieldName, desc: $desc}"
        }
    }

}