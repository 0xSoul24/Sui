-keep class rikka.sui.** { *; }
-keep interface rikka.sui.** { *; }
-keep class rikka.rish.** { *; }

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep interface **.I* { *; }
-keep class **.I*$* { *; }

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
    <fields>;
    <methods>;
}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
-assumenosideeffects class rikka.sui.util.Logger {
    public *** d(...);
}

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-dontwarn android.**
-dontwarn com.android.**
-dontwarn androidx.**
-dontwarn sun.misc.**
