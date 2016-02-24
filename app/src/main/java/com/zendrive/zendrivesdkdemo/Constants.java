package com.zendrive.zendrivesdkdemo;

/**
 * List of constants.
 */
public class Constants {

    // TODO: Set your Zendrive SDK key and driver id here.
    // The Zendrive SDK key is available in your account at https://www.zendrive.com
    public static final String zendriveSDKKey = "";

    // keys used for local broadcast to update UI.
    public static String DRIVE_START = "drive_start";
    public static String ACCIDENT = "accident";
    public static String DRIVE_END = "drive_end";
    public static String EVENT_LOCATION_PERMISSION_CHANGE = "location_permission_change";
    public static String EVENT_LOCATION_SETTING_CHANGE = "location_setting_change";

    // key to save driver id in shared preference.
    public static String DRIVER_ID_KEY = "driver_id";
    // key to save trips in shared preference.
    public static String TRIP_DETAILS_KEY = "trip_details";

    // logging key.
    public static final String LOG_TAG_DEBUG = "ZendriveSDKDemo";

    // Application local broadcasts data keys.
    public static final String kLocationPermissionDeniedBroadcast = "LOCATION_PERMISSION_DENIED_BROADCAST";

    // data field names used in local broadcast messages.
    public static final String DRIVE_DISTANCE = "drive_distance";
    public static final String ACCIDENT_CONFIDENCE = "accident_confidence";

    // accident id.
    public static final String ACCIDENT_ID = "accident_id";

    // user type.
    public static final String USER_TYPE = "user_type";

}
