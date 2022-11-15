package com.zendrive.zendrivesdkdemo;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    public static final String EMUI_11 = "11.0.0";
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
        Intent intent;
        if (isEmui11()) {
            intent = context.getPackageManager()
                    .getLaunchIntentForPackage("com.huawei.systemmanager");
        } else {
            intent = new Intent();
            intent.setComponent(new ComponentName(packageName, componentName));
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }

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

    @SuppressLint("PrivateApi")
    public static String getEMUIVersion()  {
        try {
            Class propertyClass = Class.forName("android.os.SystemProperties");
            Method method = propertyClass.getMethod("get", String.class);
            String versionEmui = (String) method.invoke(propertyClass, "ro.build.version.emui");
            if (versionEmui != null && versionEmui.startsWith("EmotionUI_")) {
                versionEmui = versionEmui.substring(10);
            }
            return versionEmui;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isHarmony(Context context) {
        try {
            int id = Resources.getSystem().getIdentifier("config_os_brand",
                    "string", "android");
            return context.getString(id).equals("harmony");
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isEmui11() {
        return compareTo(getEMUIVersion(), EMUI_11) >= 0;
    }

    private static int compareTo(String v1, String v2) {
        String[] thisParts = v1.split("\\.");
        String[] thatParts = v2.split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for(int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ?
                    Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ?
                    Integer.parseInt(thatParts[i]) : 0;
            if(thisPart < thatPart)
                return -1;
            if(thisPart > thatPart)
                return 1;
        }
        return 0;
    }

    private OnBoardingHelper() { }
}
