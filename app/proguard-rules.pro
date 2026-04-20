# CreditGuard ProGuard Rules

# Keep Room entities (instantiated via reflection)
-keep class com.creditguard.data.model.** { *; }

# Keep BroadcastReceivers (instantiated by Android framework)
-keep class com.creditguard.receiver.** { <init>(); }

# Preserve line numbers for readable crash stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Remove verbose/debug/info logging in release (keep warn/error for diagnostics)
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
