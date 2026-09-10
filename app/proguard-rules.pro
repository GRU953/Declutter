# SPDX-License-Identifier: Apache-2.0
# Copyright 2026 Aninda Sundar Howlader (GRU953)

# AGP 9 turns on R8's strict full mode for keep rules, so a bare "-keep class A" no longer
# keeps A's constructors. Every rule below names its members explicitly.

# Shizuku's provider is instantiated by the system from the manifest, and its API surface
# is reached over Binder.
-keep class rikka.shizuku.ShizukuProvider { <init>(...); *; }
-keep class rikka.shizuku.** { <init>(...); *; }
-keep interface rikka.shizuku.** { *; }
-keep class moe.shizuku.** { <init>(...); *; }

# We call hidden framework interfaces by name. R8 cannot see these uses, and a renamed
# class or a stripped constructor turns into a runtime failure on the user's phone.
-keep class dev.gru953.declutter.shizuku.** { <init>(...); *; }
-keep class org.lsposed.hiddenapibypass.** { <init>(...); *; }
-keepclassmembers class * extends android.app.Application { <init>(); }

# Framework classes we pass across the reflective boundary.
-keep class android.content.pm.VersionedPackage { <init>(...); *; }

# kotlinx.serialization writes serialisers R8 cannot see used.
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisibleAnnotations
-dontnote kotlinx.serialization.**
-keepclassmembers class dev.gru953.declutter.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class dev.gru953.declutter.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class dev.gru953.declutter.**$$serializer { <init>(...); *; }

# Our own serialisable data classes are constructed by the serialiser, not by us.
-keepclassmembers @kotlinx.serialization.Serializable class dev.gru953.declutter.** {
    <init>(...);
    <fields>;
}
