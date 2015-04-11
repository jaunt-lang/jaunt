# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

-injars target-skummet/
# -injars target/skummet/
-injars target/skummet/(!**.clj)
# clojure-android-1.7.0-alpha5-r1.jar
-libraryjars <java.home>/lib/rt.jar
-outjars packed.jar

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
# -optimizations code/removal/advanced
# !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
# -optimizationpasses 2
-dontoptimize
-dontpreverify
-dontobfuscate
# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.

# -keepattributes *Annotation*
# -keep public class com.google.vending.licensing.ILicensingService
# -keep public class com.android.vending.licensing.ILicensingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
# -keepclassmembers public class * extends android.view.View {
#    void set*(***);
#    *** get*();
# }

# We want to keep methods in Activity that could be used in the XML attribute onClick
# -keepclassmembers class * extends android.app.Activity {
#    public void *(android.view.View);
# }

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# -keep class * implements android.os.Parcelable {
#   public static final android.os.Parcelable$Creator *;
# }

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}
-keep public class testskummet.bar__init

-keep public class clojure.lang.Fn
-keep public class **__init

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
# -dontwarn android.support.**

# -whyareyoukeeping class clojure.core__init {
#     public static java.lang.Object promise;
# }
