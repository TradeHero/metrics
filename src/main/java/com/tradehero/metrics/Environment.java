package com.tradehero.metrics;
/**
 * Created by thonguyen on 7/11/14.
 */
public class Environment {
    public static boolean hasLocalyticsOnClasspath() {
        return hasClass("com.localytics.android.Localytics");
    }

    public static boolean hasTalkingDataOnClasspath() {
        return hasClass("com.tendcloud.tenddata.TCAgent");
    }

    private static boolean hasClass(String classPath) {
        boolean hasClass = false;
        try {
            Class.forName(classPath);
            hasClass = true;
        } catch (Exception ignored) {}

        return hasClass;
    }
}
