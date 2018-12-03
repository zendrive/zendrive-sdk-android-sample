# For android databinding
-dontwarn android.databinding.**
-keep class android.databinding.** { *; }

# Because Gson is used to serialize TripListDetails and DriveInfo which uses reflection
-keepclassmembernames,includedescriptorclasses class com.zendrive.sdk.* { *; }
-keepclassmembernames,includedescriptorclasses class <package>.TripListDetails { *; }
