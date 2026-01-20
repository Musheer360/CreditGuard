# ProGuard rules for CreditGuard
-keepattributes *Annotation*
-keep class com.creditguard.data.model.** { *; }
-keep class * extends androidx.room.RoomDatabase
