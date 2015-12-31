-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod


-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.actionbarsherlock.**{ *; }
-keep public class  com.kbeanie.imagechooser.api.**{ *; }
-keep public class * implements android.os.Parcelable
-keep class cn.trinea.android.** { *; }
-dontwarn cn.trinea.android.**
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.support.design.**
-keep public class * extends android.app.Fragment
-dontwarn com.j256.ormlite.**
-dontwarn com.j256.ormlite.android.**
-dontwarn com.j256.ormlite.field.**  
-dontwarn com.j256.ormlite.stmt.**  
-keep public class * extends com.j256.ormlite.**  
-keep public class * extends com.j256.ormlite.android.**  
-keep public class * extends com.j256.ormlite.field.**  
-keep public class * extends com.j256.ormlite.stmt.**  
-keep public class * extends com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
-keep public class * extends com.j256.ormlite.android.apptools.OpenHelperManager

-keep class javax.lang.model.**
-keep class javax.annotation.**

-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }
-keep class com.liba.carpool.db.**
-keepclassmembers class com.liba.carpool.db.** { *; } 
-keep class com.baidu.** { *; }
-keep class vi.com.gdi.bgl.android.**{*;}
-keep class com.google.** { *; }
-keep class com.j256.ormlite.** { *; }
-keep class com.j256.ormlite.android.** { *; }  
-keep class com.j256.ormlite.field.** { *; }  
-keep class com.j256.ormlite.stmt.** { *; } 
-keep class com.tencent.mm.sdk.openapi.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.openapi.** implements com.tencent.mm.sdk.openapi.WXMediaMessage$IMediaObject {*;}
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage { *;}
-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;} 
-keep class com.nostra13.universalimageloader.**{ *; }
-keep class com.actionbarsherlock.**{ *; }
-keep class com.viewpagerindicator.**{ *; }
-keep class com.tencent.* {*;}
-keep class com.nostra13.* {*;}
-keep public class com.jeremyfeinstein.slidingmenu.lib.**{ *; }
-keep public class com.tencent.**{ *; }
-keep public class com.sina.**{ *; }
-keep class com.google.** { *; }
-keep class cn.etsy.** { *; }
-keep class com.mining.app.zxing.** { *; }
-keep class com.umeng.analytics.** {*;}
-keep class com.umeng.u.aly.** {*;}
-keep class com.umeng.analytics.game.** {*;}
-keep class com.umeng.analytics.onlineconfig.** {*;}
-keep class com.umeng.analytics.social.** {*;}
-keep class com.umeng.** {*;}
-keep class com.umeng.update.** {*;}
-keep class com.umeng.update.net.** {*;}
-keep class com.umeng.update.util.** {*;}
-keep class com.umeng.fb.** {*;}
-keep class com.umeng.u.fb.** {*;}
-keep class com.umeng.a.a.a.* {*;}
-keep public class com.umeng.fb.ui.ThreadView {*;}

-dontwarn com.wecook.uikit.widget.pulltorefresh.**
-keep class com.wecook.uikit.widget.pulltorefresh.** { *; }

-dontwarn ly.count.android.api.*
-keep class ly.count.android.api.* { *; }

-dontwarn android.net.http**
-keep class android.net.http.** { *; }

-dontwarn android.os.**
-keep class android.os.** { *; }

-dontwarn android.webkit.**
-keep class android.webkit.** { *; }

-dontwarn com.sun.activation.**
-keep class com.sun.activation.** { *; }

-dontwarn javax.activation.**
-keep class javax.activation.** { *; }

-dontwarn com.xiaomi.channel.commonutils.android.**

-keep class android.support.design.widget.** { *; }
-keep interface android.support.design.widget.** { *; }
-dontwarn android.support.design.**

-keep public class cn.wecook.app.R$*{
public static final int *;
}
-keep class com.j256.** {*;}
-keep class com.nostra13.** {*;}
-keep class com.mining.** { *; }
-keep class cn.wecook.app.wxapi.** { *; }
-keep class org.** { *; }
-keep class com.loopj.** { *; }

-keep class android.support.v7.** { *; }

-keep public class * extends android.support.v8.**
-keep class android.support.v8.** { *; }
-dontwarn android.support.v8.**

-keep class com.wecook.common.modules.database.** { *; }
-keep class com.wecook.sdk.dbprovider.** { *; }
-keep class com.wecook.sdk.api.** { *; }
-keep class com.wecook.uikit.fragment.** { *; }
-keep class com.wecook.common.modules.messager.XMPushMessageReceiver {*;}
-keep class com.wecook.common.modules.jsbridge.WebViewJavascriptBridge {*;}

-keepclassmembers class cn.trinea.android.** { *; }

-keepclassmembers class * {
  public <init>(android.content.Context);
}
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

 -keepclasseswithmembernames class * {
     native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}


# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
-keep class * implements java.io.Serializable{*;}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
    public <fields>;
}

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}