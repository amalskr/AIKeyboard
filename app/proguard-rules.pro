# ── Kotlinx Serialization ─────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.ceylonapz.aikeyboard.**$$serializer { *; }
-keepclassmembers class com.ceylonapz.aikeyboard.** {
    *** Companion;
}
-keepclasseswithmembers class com.ceylonapz.aikeyboard.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Google Generative AI ──────────────────────────────────
-keep class com.google.ai.client.generativeai.** { *; }

# ── IME Service ──────────────────────────────────────────
-keep class com.ceylonapz.aikeyboard.AIKeyboardService { *; }

# ── Stack traces ──────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
