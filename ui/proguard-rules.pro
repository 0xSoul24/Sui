-keepattributes SourceFile,LineNumberTable,*Annotation*

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepnames class * implements android.os.Parcelable
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keep class androidx.appcompat.** { *; }
-keep interface androidx.appcompat.** { *; }

-keep class androidx.vectordrawable.** { *; }
-keep interface androidx.vectordrawable.** { *; }

-keep class androidx.core.** { *; }
-keep interface androidx.core.** { *; }

-keep class androidx.appcompat.widget.ResourceManagerInternal { *; }
-keep class androidx.appcompat.widget.AppCompatDrawableManager { *; }

-keep class rikka.sui.** { *; }
-keep interface rikka.sui.** { *; }

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
-assumenosideeffects class rikka.sui.util.Logger {
    public static *** d(...);
    public static *** v(...);
}

-dontoptimize
-dontpreverify
-dontwarn androidx.**
-dontwarn android.support.**
-dontwarn org.jetbrains.annotations.**

-keepattributes SourceFile,LineNumberTable

-printmapping mapping.txt
-printseeds seeds.txt
-printusage usage.txt
-printconfiguration proguard-config.txt