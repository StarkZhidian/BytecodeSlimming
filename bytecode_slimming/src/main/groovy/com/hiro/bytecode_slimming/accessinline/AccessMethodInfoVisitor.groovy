package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.BaseMethodVisitor
import com.hiro.bytecode_slimming.Logger
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

/**
 * 编译器自动生成的 access$xxx 方法的访问器
 *
 * @author hongweiqiu
 */
class AccessMethodInfoVisitor extends BaseMethodVisitor {

    private static final def TAG = "AccessMethodInfoVisitor"

    /* 记录当前方法的相关信息 */
    AccessMethodInfo accessMethodInfo
    /* 当前访问的 access$xxx 方法内部的相关指令暂存器 */
    List<AbstractInsnNode> instructions = new ArrayList()

    AccessMethodInfoVisitor(int api, MethodVisitor mv, String className,
                            int access, String methodName, String desc, String signature, String[] exceptions) {
        super(api, mv, className, access, methodName, desc, signature, exceptions)
        accessMethodInfo = new AccessMethodInfo(className, methodName, desc)
    }

    @Override
    void visitInsn(final int opcode) {
        super.visitInsn(opcode)
        instructions.add(new InsnNode(opcode))
    }

    @Override
    void visitIntInsn(final int opcode, final int operand) {
        super.visitIntInsn(opcode, operand)
        instructions.add(new IntInsnNode(opcode, operand))
    }

    @Override
    void visitVarInsn(final int opcode, final int var) {
        super.visitVarInsn(opcode, var);
        instructions.add(new VarInsnNode(opcode, var));
    }

    @Override
    void visitTypeInsn(final int opcode, final String type) {
        super.visitTypeInsn(opcode, type)
        instructions.add(new TypeInsnNode(opcode, type))
    }

    @Override
    void visitFieldInsn(final int opcode, final String owner,
                        final String name, final String desc) {
        super.visitFieldInsn(opcode, owner, name, desc)
        instructions.add(new FieldInsnNode(opcode, owner, name, desc))
    }

    @Deprecated
    @Override
    void visitMethodInsn(int opcode, String owner, String name,
                         String desc) {
        if (api >= Opcodes.ASM5) {
            super.visitMethodInsn(opcode, owner, name, desc)
            return
        }
        super.visitMethodInsn(opcode, owner, name, desc)
        instructions.add(new MethodInsnNode(opcode, owner, name, desc))
    }

    @Override
    void visitMethodInsn(int opcode, String owner, String name,
                         String desc, boolean itf) {
        if (api < Opcodes.ASM5) {
            super.visitMethodInsn(opcode, owner, name, desc, itf)
            return
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf)
        instructions.add(new MethodInsnNode(opcode, owner, name, desc, itf))
    }

    @Override
    void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
                                Object... bsmArgs) {
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs)
        instructions.add(new InvokeDynamicInsnNode(name, desc, bsm, bsmArgs))
    }

    @Override
    void visitJumpInsn(final int opcode, final Label label) {
        super.visitJumpInsn(opcode, label)
        instructions.add(new JumpInsnNode(opcode, getLabelNode(label)))
    }

    @Override
    void visitLabel(final Label label) {
        super.visitLabel(label)
        instructions.add(getLabelNode(label))
    }

    @Override
    void visitLdcInsn(final Object cst) {
        super.visitLdcInsn(cst)
        instructions.add(new LdcInsnNode(cst))
    }

    @Override
    void visitIincInsn(final int var, final int increment) {
        super.visitIincInsn(var, increment)
        instructions.add(new IincInsnNode(var, increment))
    }

    @Override
    void visitTableSwitchInsn(final int min, final int max,
                              final Label dflt, final Label... labels) {
        super.visitTableSwitchInsn(min, max, dflt, labels)
        instructions.add(new TableSwitchInsnNode(min, max, getLabelNode(dflt),
                getLabelNodes(labels)));
    }

    @Override
    void visitLookupSwitchInsn(final Label dflt, final int[] keys,
                               final Label[] labels) {
        super.visitLookupSwitchInsn(dflt, keys, labels)
        instructions.add(new LookupSwitchInsnNode(getLabelNode(dflt), keys, getLabelNodes(labels)))
    }

    @Override
    void visitMultiANewArrayInsn(final String desc, final int dims) {
        super.visitMultiANewArrayInsn(desc, dims)
        instructions.add(new MultiANewArrayInsnNode(desc, dims))
    }

    protected LabelNode getLabelNode(final Label l) {
        if (!(l.info instanceof LabelNode)) {
            l.info = new LabelNode()
        }
        return (LabelNode) l.info
    }

    private LabelNode[] getLabelNodes(final Label[] l) {
        LabelNode[] nodes = new LabelNode[l.length]
        for (int i = 0; i < l.length; ++i) {
            nodes[i] = getLabelNode(l[i])
        }
        return nodes;
    }

    private Object[] getLabelNodes(final Object[] objs) {
        Object[] nodes = new Object[objs.length]
        for (int i = 0; i < objs.length; ++i) {
            Object o = objs[i]
            if (o instanceof Label) {
                o = getLabelNode((Label) o)
            }
            nodes[i] = o
        }
        return nodes
    }

    private List<AbstractInsnNode> refineInstructions(List<AbstractInsnNode> instructions) {
        List<AbstractInsnNode> refinedInsns = new ArrayList<>()
        boolean shouldSkipVarInsn = true
        int varLoadInsnCount = 0
        try {
            for (AbstractInsnNode insnNode : instructions) {
                if (insnNode.getType() == AbstractInsnNode.LINE) continue;
                if (insnNode.getType() == AbstractInsnNode.LABEL) continue;
                if (insnNode.getOpcode() >= Opcodes.IRETURN && insnNode.getOpcode() <= Opcodes.RETURN)
                    break;
                if (insnNode.getType() == AbstractInsnNode.JUMP_INSN) {
                    throw new ShouldSkipInlineException("Unexpected JUMP_INSN instruction in access method body.");
                }
                if (insnNode.getOpcode() == Opcodes.ATHROW) {
                    throw new ShouldSkipInlineException("Unexpected ATHROW instruction in access method body.");
                }
                // no control instruction
                if (insnNode.getOpcode() >= Opcodes.GOTO && insnNode.getOpcode() <= Opcodes.LOOKUPSWITCH) {
                    throw new ShouldSkipInlineException("Unexpected control instruction in access method body.");
                }
                // If those instructions appear in access$ method body, skip inline it.
                // 如果在access$方法内部有这些指令，则不内联这个方法，因为这些指令都比较新，for safe.
                if (insnNode.getOpcode() > Opcodes.MONITOREXIT) {
                    throw new ShouldSkipInlineException("Unexpected new instruction in access method body.");
                }
                if (shouldSkipVarInsn && insnNode.getOpcode() >= Opcodes.ILOAD && insnNode.getOpcode() <= Opcodes.SALOAD) {
                    varLoadInsnCount++;
                    continue;
                }
                // no store instruction
                if (insnNode.getOpcode() >= Opcodes.ISTORE && insnNode.getOpcode() <= Opcodes.SASTORE) {
                    throw new ShouldSkipInlineException("Unexpected store instruction in access method body.");
                }
                if (insnNode.getType() == AbstractInsnNode.METHOD_INSN) {
                    if (shouldSkipVarInsn) {
                        shouldSkipVarInsn = false;
                        MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                        int parameterCountOfTargetMethod = Utils.getParameterCountFromMethodDesc(methodInsnNode.desc);
                        int parameterCountOfAccess$Method = Utils.getParameterCountFromMethodDesc(accessMethodInfo.desc);
                        switch (insnNode.getOpcode()) {
                            case Opcodes.INVOKEVIRTUAL:
                            case Opcodes.INVOKESPECIAL:
                            case Opcodes.INVOKEINTERFACE:
                                if (parameterCountOfTargetMethod != parameterCountOfAccess$Method - 1
                                        || varLoadInsnCount != parameterCountOfAccess$Method) {
                                    throw new ShouldSkipInlineException("The parameter count of access method is abnormal.");
                                }
                                break;
                            case Opcodes.INVOKESTATIC:
                                if (parameterCountOfTargetMethod != parameterCountOfAccess$Method ||
                                        varLoadInsnCount != parameterCountOfAccess$Method) {
                                    throw new ShouldSkipInlineException("The parameter count of access method is abnormal.");
                                }
                                break;
                            default:
                                throw new ShouldSkipInlineException("The instruction of access method is unknown.");
                        }
                        if (methodInsnNode.getOpcode() == Opcodes.INVOKESPECIAL) {
                            methodInsnNode.setOpcode(Opcodes.INVOKEVIRTUAL)
                        }
                        if (isInnerOuterClass(methodInsnNode.owner)) {
                            accessMethodInfo.appendInvokeMethodInfo(new AccessMethodInfo.InvokeMethodInfo(
                                    insnNode.getOpcode(), methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc))
                        }
                    }
                    refinedInsns.add(insnNode);
                } else if (insnNode.getType() == AbstractInsnNode.FIELD_INSN) {
                    if (shouldSkipVarInsn) {
                        shouldSkipVarInsn = false;
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNode;
                        int parameterCountOfAccess$Method = Utils.getParameterCountFromMethodDesc(accessMethodInfo.desc);
                        switch (insnNode.getOpcode()) {
                            case Opcodes.GETSTATIC:
                                if (parameterCountOfAccess$Method != 0
                                        || varLoadInsnCount != parameterCountOfAccess$Method) {
                                    throw new ShouldSkipInlineException("The parameter count of access method is abnormal.");
                                }
                                break;
                            case Opcodes.GETFIELD:
                                if (parameterCountOfAccess$Method != 1
                                        || varLoadInsnCount != parameterCountOfAccess$Method) {
                                    throw new ShouldSkipInlineException("The parameter count of access method is abnormal.");
                                }
                                break;
                            case Opcodes.PUTFIELD:
                                if (parameterCountOfAccess$Method != 2 ||
                                        varLoadInsnCount != parameterCountOfAccess$Method) {
                                    throw new ShouldSkipInlineException("The parameter count of access method is abnormal.");
                                }
                                break;
                            case Opcodes.PUTSTATIC:
                                if (parameterCountOfAccess$Method != 1 ||
                                        varLoadInsnCount != parameterCountOfAccess$Method) {
                                    throw new ShouldSkipInlineException("The parameter count of access method is abnormal.");
                                }
                                break;
                            default:
                                throw new ShouldSkipInlineException("The instruction of access method is unknown.");
                        }
                        if (isInnerOuterClass(fieldInsnNode.owner)) {
                            accessMethodInfo.appendOperateFiledInfo(new AccessMethodInfo.OperateFieldInfo(
                                    insnNode.getOpcode(), fieldInsnNode.owner, fieldInsnNode.name, fieldInsnNode.desc))
                        }
                    }
                    refinedInsns.add(insnNode);
                } else {
                    refinedInsns.add(insnNode);
                }
            }
        } catch (ShouldSkipInlineException e) {
            Logger.e(TAG, "refineInstructions", e)
            refinedInsns.clear()
        }
        return refinedInsns;
    }

    @Override
    void visitEnd() {
        super.visitEnd()

        def refinedList = refineInstructions(instructions)
        if (!refinedList.isEmpty()) {
            accessMethodInfo.instructions = refinedList
            AccessMethodInlineProcessor.getInstance()
                    .appendInlineMethod(className, methodName, accessMethodInfo)
        }
    }

    void printInstructions() {
        Logger.d1(TAG, "instructions size = " + instructions.size())
        for (int i = 0; i < instructions.size(); i++) {
            Logger.d1(TAG, "printInstructions i = [" + instructions.get(i).getOpcode() + "]")
        }
    }

    private def isInnerOuterClass(String anotherClassName) {
        if (Utils.isEmpty(anotherClassName)) {
            return false
        }
        return ((accessMethodInfo.className.startsWith(anotherClassName))
                || (anotherClassName.startsWith(accessMethodInfo.className)))
    }
}