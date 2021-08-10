package com.zendrive.zendrivesdkdemo;

/**
 * List of constants.
 */
public interface Constants {

    // TODO: Set your Zendrive SDK key and driver id here.
    // The Zendrive SDK key is available in your account at https://www.zendrive.com
    String zendriveSDKKey = "";

    // logging key.
    String LOG_TAG_DEBUG = "ZendriveSDKDemo";
    String ERROR = "Error";

    // keys used for local broadcast to update UI.
    String REFRESH_UI = "refresh_ui";
    String EVENT_LOCATION_PERMISSION_ERROR = "location_permission_denied";
    String EVENT_LOCATION_SETTING_ERROR = "location_setting_error";
    String EVENT_ACTIVITY_PERMISSION_ERROR = "activity_permission_error";
    String EVENT_MULTIPLE_PERMISSIONS_ERROR = "multiple_permissions_error";
    // data field names used in local broadcast messages.
    String DRIVE_DISTANCE = "drive_distance";

    String ACCIDENT_INFO = "accident_info";
    String TRIP_TRACKING_ID = "tracking_id";

    String EVENT_GOOGLE_PLAY_SETTING_ERROR = "google_play_settings_error";
    String SETTING_ERRORS = "settings_errors";
    String SETTING_WARNINGS = "settings_warnings";

    String MULTIPLE_PERMISSIONS_DENIED_LIST = "multiple_permissions_denied_list";
    String NONE_VEHICLE_TYPE_OPTION_VALUE = "None";
    String UUID = "uuid";
}
