# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Programming\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# This is generated automatically by the Android Gradle plugin.

-dontwarn javax.script.**
-dontwarn org.apache.bcel.**

-dontobfuscate

# Do NOT optimize LuaJ or classes which serve as Lua API boundary
-keep class org.luaj.vm2.** {*; }
-keep class org.netdex.androidusbscript.lua.** {*; }