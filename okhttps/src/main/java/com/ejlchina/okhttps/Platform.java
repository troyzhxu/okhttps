package com.ejlchina.okhttps;

import java.lang.reflect.Field;

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
        okhttp3.internal.platform.Platform.get().log(okhttp3.internal.platform.Platform.INFO, message, null);
    }

    public static void logError(String message) {
        logError(message, null);
    }

    public static void logError(String message, Throwable t) {
        okhttp3.internal.platform.Platform.get().log(okhttp3.internal.platform.Platform.WARN, message, t);
    }

}
