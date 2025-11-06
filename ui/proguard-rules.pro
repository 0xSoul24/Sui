# Keep the main activities and dialogs, including all their members.
-keep class rikka.sui.SuiActivity { *; }
-keep class rikka.sui.SuiRequestPermissionDialog { *; }

# Broadly keep everything in the app's main package and sub-packages.
# This is crucial for code loaded dynamically or via instrumentation.
-keep class rikka.sui.** { *; }
-keep interface rikka.sui.** { *; }

# Keep Parcelable implementations, which are used for IPC.
-keepnames class * implements android.os.Parcelable
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

# Suppress log messages from release builds.
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
-assumenosideeffects class rikka.sui.util.Logger {
    public *** d(...);
    public *** v(...);
}

# Keep Kotlin-specific metadata and intrinsics, which are essential for Kotlin interoperability.
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
    public static void throw*(...);
}
-keep class kotlin.Metadata { *; }

# Keep attributes for debugging and for libraries that rely on annotations.
-keepattributes SourceFile,LineNumberTable,*Annotation*
-renamesourcefileattribute SourceFile

# Don't warn about Android internal or library classes.
-dontwarn android.**
-dontwarn com.android.**
