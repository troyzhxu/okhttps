package com.ejlchina.okhttps;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class Platform {

    /**
     * Android 的 SDK 版本，若不是 Android 平台，则为 0
     */
    public static final int ANDROID_SDK_INT = getAndroidSdkInt();


    private static int getAndroidSdkInt() {
        try {
            Class<?> versionClass = Class.forName("android.os.Build$VERSION");
            Field field = versionClass.getDeclaredField("SDK_INT");
            return field.getInt(field);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            return 0;
        }
    }

    public static void logInfo(String message) {
        doLog(okhttp3.internal.platform.Platform.INFO, message, null);
    }

    public static void logError(String message) {
        logError(message, null);
    }

    public static void logError(String message, Throwable t) {
        doLog(okhttp3.internal.platform.Platform.WARN, message, t);
    }

    private static Method logMethod = null;

    private static void doLog(int level, String message, Throwable t) {
        okhttp3.internal.platform.Platform platform = okhttp3.internal.platform.Platform.get();
        if (isOkHttpVersionLessThan4()) {
            platform.log(level, message, t);
            return;
        }
        try {
            synchronized (Platform.class) {
                if (logMethod == null) {
                    logMethod = platform.getClass().getMethod("log", String.class, int.class, Throwable.class);
                }
            }
            logMethod.invoke(platform, message, level, t);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static String okHttpVersion;

    private static boolean isOkHttpVersionLessThan4() {
        if (okHttpVersion == null) {
            try {
                Class<?> versionClass = Class.forName("okhttp3.internal.Version");
                Method method = versionClass.getDeclaredMethod("userAgent");
                okHttpVersion = (String) method.invoke(null);
            } catch (Exception e) {
                okHttpVersion = "4.x";
            }
        }
        return !okHttpVersion.startsWith("4");
    }

    // 该方法是为兼容 Android 低版本
    public static <K, V> void forEach(Map<K, V> map, BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            K k;
            V v;
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch (IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
            action.accept(k, v);
        }
    }

    /**
     * 判断字符串是否为 null 或 空
     * @param str 待判断的字符串
     * @return str 是否是空白字符串
     * @since v3.5.0
     */
    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        int len = str.length();
        if (len == 0) {
            return true;
        }
        for (int i = 0; i < len; i++) {
            switch (str.charAt(i)) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    // case '\b':
                    // case '\f':
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否非空
     * @param str 待判断的字符串
     * @return str 是否是非空字符串
     * @since v3.5.0
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

}
