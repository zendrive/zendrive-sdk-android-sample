# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/adt-bundle-mac-x86_64-20140702/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Keep Zendrive classes as-is
-keep class com.zendrive.** { *; }
-keep enum com.zendrive.** { *; }

# Keep Zendrive dependency libraries as-is. Don't warn for these libraries.
-keep class org.apache.commons.** { *; }
-keep class org.apache.thrift.** { *; }
-keep class org.slf4j.** { *; }
-keep class com.fasterxml.jackson.** { *; }
-keep class com.google.gson.** { *; }
-keep enum org.apache.commons.** { *; }
-keep enum org.apache.thrift.** { *; }
-keep enum org.slf4j.** { *; }
-keep enum com.fasterxml.jackson.** { *; }
-keep enum com.google.gson.** { *; }
-dontwarn org.apache.http.**
-dontwarn org.apache.thrift.**
-dontwarn org.apache.log4j.**
-dontwarn org.slf4j.LoggerFactory


# Google play services rules
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}


# Zendrive depends on AWS SDK. Proguard rules for AWS SDK.
-keep class org.apache.commons.logging.**               { *; }
-keep class com.amazonaws.services.sqs.QueueUrlHandler  { *; }
-keep class com.amazonaws.javax.xml.transform.sax.*     { public *; }
-keep class com.amazonaws.javax.xml.stream.**           { *; }
-keep class com.amazonaws.internal.**                   { *; }
-keep class com.amazonaws.services.**                   { *; }
-keep enum com.amazonaws.services.**                    { *; }
-keep class org.codehaus.**                             { *; }
-keep class org.joda.convert.*                          { *; }
-keepattributes Signature,*Annotation*,EnclosingMethod
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class com.amazonaws.** { *; }
-dontwarn com.amazonaws.auth.**
-dontwarn com.amazonaws.auth.policy.conditions.S3ConditionFactory
-dontwarn org.joda.time.**
-dontwarn com.fasterxml.jackson.databind.**
-dontwarn javax.xml.stream.events.**
-dontwarn org.codehaus.jackson.**
-dontwarn org.apache.commons.logging.impl.**
-dontwarn org.apache.http.conn.scheme.**
-dontwarn org.apache.http.annotation.**
-dontwarn org.ietf.jgss.**
-dontwarn org.w3c.dom.bootstrap.**

# ebson rules
-keep class com.javax.xml.bind.**
-keep class sun.misc.**
-dontwarn javax.xml.bind.**
-dontwarn sun.misc.**