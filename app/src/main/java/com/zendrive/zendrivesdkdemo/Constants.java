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
    String ACCIDENT = "accident";
    String REFRESH_UI = "refresh_ui";
    String EVENT_LOCATION_PERMISSION_ERROR = "location_permission_denied";
    String EVENT_LOCATION_SETTING_ERROR = "location_setting_error";
    // data field names used in local broadcast messages.
    String DRIVE_DISTANCE = "drive_distance";
    String ACCIDENT_ID = "accident_id";
    String DRIVE_ID = "drive_id";
    String ACCIDENT_TIMESTAMP = "accident_timestamp";

    String TRIP_TRACKING_ID = "tracking_id";

    String EVENT_GOOGLE_PLAY_SETTING_ERROR = "google_play_settings_error";
    String SETTING_ERRORS = "settings_errors";
    String SETTING_WARNINGS = "settings_warnings";
}
