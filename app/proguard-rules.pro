# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Sentry ProGuard rules
-keep class io.sentry.** { *; }
-dontwarn io.sentry.**

