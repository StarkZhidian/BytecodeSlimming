package com.hiro.bytecode_slimming

import org.objectweb.asm.Opcodes

import java.util.regex.Matcher
import java.util.regex.Pattern

class Utils {

    private static Pattern paramsPat = Pattern.compile("(\\[?[BCZSIJFD])|(L[^;]+;)");

    static def isEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0
    }

    static def textEquals(CharSequence text1, CharSequence text2) {
        return Objects.equals(text1, text2)
    }

    static void write2File(byte[] data, File outputFile) {
        if (outputFile == null) {
            return
        }
        FileOutputStream fos = new FileOutputStream(outputFile)
        fos.write(data)
        fos.close()
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
}