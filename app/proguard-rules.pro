# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class org.breezyweather.common.activities.models.** { *; }
-keep class org.breezyweather.db.entities.** { *; }
-keep interface org.breezyweather.sources.**.* { *; }
-keep class org.breezyweather.sources.**.json.** { *; }

-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver

-keep class androidx.lifecycle.** {*;}
-keep class android.arch.lifecycle.** {*;}

-keep class **.R$* {*;}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclassmembers class * {
    void *(**On*Event);
    void *(**On*Listener);
}

-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# suncalc
-dontwarn edu.umd.cs.findbugs.annotations.Nullable

# RestrictionBypass
-keep class org.chickenhook.restrictionbypass.** { *; }
