-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.view.View

-keep class com.mybox.data.model.** { *; }

-keep class * extends java.lang.Enum
-keepclassmembers enum * { public static **[] values(); public static ** valueOf(java.lang.String); }

-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { *; }

-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

-keepnames class androidx.compose.runtime.Composer
-keepnames class androidx.compose.runtime.RecomposeScope
-keepnames class androidx.compose.runtime.internal.ComposableLambda

-keep class com.mybox.data.database.** { *; }
-keep class com.mybox.data.dao.** { *; }

-keepclassmembers class ** {
    @androidx.room.Entity *;
    @androidx.room.Dao *;
    @androidx.room.Query *;
}

-dontwarn androidx.security.crypto.**
-dontwarn androidx.datastore.**

# Keep ChineseCalendar for lunar date calculation
-keep class android.icu.util.ChineseCalendar { *; }
-keep class android.icu.util.Calendar { *; }

# Gson specific rules
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}