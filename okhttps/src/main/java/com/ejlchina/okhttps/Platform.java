package com.ejlchina.okhttps;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

}
