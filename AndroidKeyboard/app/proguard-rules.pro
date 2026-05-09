# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep InputMethodService subclass
-keep class com.botnoitu.keyboard.BotKeyboardService { *; }
-keep class com.botnoitu.keyboard.SettingsActivity { *; }
