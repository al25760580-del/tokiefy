# Tokiefy ProGuard rules.
# Keep Moshi data classes and their JSON-annotated members so reflection
# adapters (and codegen) continue to find fields after minification.
-keep class com.milasoraki.tokiefy.extractor.model.** { <fields>; }
-keepclasseswithmembers class * { @com.squareup.moshi.JsonClass *; }
-dontwarn org.jetbrains.annotations.**
