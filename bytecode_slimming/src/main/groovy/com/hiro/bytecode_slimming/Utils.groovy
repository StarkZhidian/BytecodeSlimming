package com.hiro.bytecode_slimming

class Utils {


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
}