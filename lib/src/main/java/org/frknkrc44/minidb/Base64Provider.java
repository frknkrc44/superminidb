package org.frknkrc44.minidb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;

final class Base64Provider {
    private Base64Provider() {}

    public static byte[] decode(byte[] encoded) {
        try {
            return Base64.getDecoder().decode(encoded);
        } catch(Throwable ignored) {
            try {
                return useAndroidMethod("decode", encoded);
            } catch (Throwable t) {
                throw new RuntimeException("Android and Java methods failed to decode Base64");
            }
        }
    }

    public static byte[] encode(byte[] decoded) {
        try {
            return Base64.getEncoder().encode(decoded);
        } catch(Throwable ignored) {
            try {
                return useAndroidMethod("encode", decoded);
            } catch (Throwable t) {
                throw new RuntimeException("Android and Java methods failed to encode Base64");
            }
        }
    }

    private static byte[] useAndroidMethod(String methodName, byte[] data)
            throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Class<?> androidUtilBase64 = Class.forName("android.util.Base64");
        Method method = androidUtilBase64.getDeclaredMethod(methodName, byte[].class, int.class);
        method.setAccessible(true);
        return (byte[]) method.invoke(null, data, 0 /* DEFAULT */);
    }
}
