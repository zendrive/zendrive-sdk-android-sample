# Google play services rules - from https://developer.android.com/google/play-services/setup.html
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

#Zendrive rules.
# Keep Zendrive classes as-is
-keep class com.zendrive.** { *; }
-keep enum com.zendrive.** { *; }

# Keep Zendrive dependency libraries as-is. Don't warn for these libraries.
-keep class org.apache.commons.** { *; }
-keep class org.apache.thrift.** { *; }
-keep class org.slf4j.** { *; }
-keep class com.google.gson.** { *; }
-keep class org.apache.http.** { *; }
-keep enum org.apache.commons.** { *; }
-keep enum org.apache.thrift.** { *; }
-keep enum org.slf4j.** { *; }
-keep enum com.google.gson.** { *; }

-dontwarn org.apache.http.**
-dontwarn org.apache.thrift.**
-dontwarn org.apache.log4j.**
-dontwarn org.slf4j.LoggerFactory
-dontwarn org.slf4j.impl.**

# Zendrive depends on AWS SDK. Proguard rules for AWS SDK.
-keep class org.apache.commons.logging.** { *; }
-keep class com.amazonaws.org.apache.commons.logging.** { *; }
-keep class com.amazonaws.services.sqs.QueueUrlHandler { *; }
-keep class com.amazonaws.javax.xml.transform.sax.* { public *; }
-keep class com.amazonaws.javax.xml.stream.** { *; }
-keep class com.amazonaws.internal.** { *; }
-keep class com.amazonaws.services.** { *; }
-keep enum com.amazonaws.services.** { *; }
-keep class com.amazonaws.regions.** { *; }
-keep enum com.amazonaws.regions.** { *; }
-keep class org.codehaus.** { *; }
-keep class org.joda.convert.* { *; }
-keep class com.amazonaws.org.joda.convert.* { *; }
-keepattributes Signature,*Annotation*,EnclosingMethod
-keepnames class com.amazonaws.** { *; }

-dontwarn com.amazonaws.auth.**
-dontwarn com.amazonaws.auth.policy.conditions.S3ConditionFactory
-dontwarn com.amazonaws.metrics.MetricInputStreamEntity
-dontwarn com.amazonaws.http.**
-dontwarn org.joda.time.**
-dontwarn com.amazonaws.org.joda.time.**
-dontwarn com.fasterxml.jackson.**
-dontwarn javax.xml.stream.events.**
-dontwarn org.codehaus.jackson.**
-dontwarn org.apache.commons.logging.impl.**
-dontwarn org.apache.commons.logging.**
-dontwarn org.apache.http.conn.scheme.**
-dontwarn org.apache.http.annotation.**
-dontwarn org.ietf.jgss.**
-dontwarn org.w3c.dom.bootstrap.**

# Zendrive depends on ebson. ebson rules
-keep class com.javax.xml.bind.**
-keep class sun.misc.**
-dontwarn javax.xml.bind.**
-dontwarn sun.misc.**
-dontwarn com.google.common.**
-dontwarn com.github.kohanyirobert.ebson.**
