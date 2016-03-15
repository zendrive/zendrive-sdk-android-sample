package com.zendrive.zendrivesdkdemo;

/**
 * List of constants.
 */
public class Constants {

    // TODO: Set your Zendrive SDK key and driver id here.
    // The Zendrive SDK key is available in your account at https://www.zendrive.com
    public static final String zendriveSDKKey = "";

    // logging key.
    public static final String LOG_TAG_DEBUG = "ZendriveSDKDemo";

    // keys used for local broadcast to update UI.
    public static String DRIVE_START = "drive_start";
    public static String ACCIDENT = "accident";
    public static String DRIVE_END = "drive_end";
    public static String EVENT_LOCATION_PERMISSION_CHANGE = "location_permission_change";
    public static String EVENT_LOCATION_SETTING_CHANGE = "location_setting_change";
    // data field names used in local broadcast messages.
    public static final String DRIVE_DISTANCE = "drive_distance";
    public static final String ACCIDENT_ID = "accident_id";
}
