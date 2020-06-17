package com.zendrive.zendrivesdkdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.util.Locale;


public class OnBoardingHelper {
    /**
     * Xiaomi
     */
    public static final String BRAND_XIAOMI = "xiaomi";
    public static final String BRAND_XIAOMI_REDMI = "redmi";
    private static final String PACKAGE_XIAOMI_MAIN = "com.miui.securitycenter";
    private static final String PACKAGE_XIAOMI_COMPONENT = "com.miui.permcenter.autostart.AutoStartManagementActivity";

    /**
     * Huawei
     */
    public static final String BRAND_HUAWEI = "huawei";
    private static final String PACKAGE_HUAWEI_MAIN = "com.huawei.systemmanager";
    private static final String PACKAGE_HUAWEI_COMPONENT = "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity";
    private static final String PACKAGE_HUAWEI_COMPONENT_FALLBACK = "com.huawei.systemmanager.optimize.process.ProtectActivity";

    public static boolean isOnBoardingNeeded () {
        String phoneBrand = Build.BRAND.toLowerCase(Locale.ROOT);
        if (phoneBrand.equals(OnBoardingHelper.BRAND_HUAWEI) || phoneBrand.equals(OnBoardingHelper.
                BRAND_XIAOMI) || phoneBrand.equals(OnBoardingHelper.BRAND_XIAOMI_REDMI)) {
            return true;
        }
        return false;
    }

    public static void getAutoStartPermission(Context context) {
        switch (Build.BRAND.toLowerCase(Locale.ROOT)) {
            case BRAND_XIAOMI:
            case BRAND_XIAOMI_REDMI:
                autoStartXiaomi(context);
                return;
            case BRAND_HUAWEI:
                autoStartHuawei(context);
                return;
            default:
        }
    }

    private static void autoStartXiaomi(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + context.getPackageName()));

        boolean activityExists = isActivityIntentResolvable(context, intent);
        if (activityExists) {
            context.startActivity(intent);
        }
    }

    private static void autoStartHuawei(Context context) {
        boolean started = startIntent(context, PACKAGE_HUAWEI_MAIN, PACKAGE_HUAWEI_COMPONENT);
        if (!started) {
            startIntent(context, PACKAGE_HUAWEI_MAIN, PACKAGE_HUAWEI_COMPONENT_FALLBACK);
        }
    }

    private static boolean startIntent(Context context, String packageName, String componentName) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, componentName));
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        boolean activityExists = isActivityIntentResolvable(context, intent);
        if (activityExists) {
            context.startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    private static boolean isActivityIntentResolvable(Context context, Intent intent) {
        return context.getPackageManager().resolveActivity(intent, 0) != null;
    }

    private OnBoardingHelper() { }
}
