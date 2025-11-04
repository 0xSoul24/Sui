# ------------------ 核心保护规则 ------------------
# 完整保留 sui 和 rish 包中的所有类、接口、字段和方法，不进行任何形式的重命名或移除。
# 这是解决当前崩溃问题的最关键步骤。
-keep class rikka.sui.** { *; }
-keep interface rikka.sui.** { *; }
-keep class rikka.rish.** { *; }

# ------------------ JNI (原生代码) 保护规则 ------------------
# 保留所有包含 native 方法的类，以及这些 native 方法本身。
-keepclasseswithmembernames class * {
    native <methods>;
}

# ------------------ Binder/AIDL 保护规则 ------------------
# 保留所有 AIDL 自动生成的接口及其内部 Stub 类。
-keep interface **.I* { *; }
-keep class **.I*$* { *; }

# 完整保留所有实现了 Parcelable 接口的类。
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
    <fields>;
    <methods>;
}

# ------------------ 其他优化与兼容性规则 ------------------
# 移除 Log 日志调用
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

# 保留源码文件名和行号信息
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 忽略警告
-dontwarn android.**
-dontwarn com.android.**
-dontwarn androidx.**
-dontwarn sun.misc.**
