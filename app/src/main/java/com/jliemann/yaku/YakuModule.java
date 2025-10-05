package com.jliemann.yaku;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.view.Window;
import android.widget.Toast;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.lang.reflect.Field;
public final class YakuModule implements IXposedHookLoadPackage {

private static final String PKG = "com.cbt.exam.browser";
private static final String CLS = "com.cbt.exam.browser.activity.ExamQR";

@Override
public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
    if (!lpparam.packageName.equals(PKG)) return;
    android.content.Context ctx = (android.content.Context)
            XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", null),
                    "currentApplication");
    android.widget.Toast.makeText(ctx, "YAKU activated ⚡", android.widget.Toast.LENGTH_SHORT)
            .show();

    XposedHelpers.findAndHookMethod(CLS, lpparam.classLoader, "isAppInLockTaskMode",
            XC_MethodReplacement.returnConstant(true));

    XposedHelpers.findAndHookMethod(CLS, lpparam.classLoader, "onWindowFocusChanged",
            boolean.class, new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    param.args[0] = true;
                }
            });

    XposedHelpers.findAndHookMethod(CLS, lpparam.classLoader, "onTrimMemory",
            int.class, new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    int level = (int) param.args[0];
                    if (level == 20) {          // TRIM_MEMORY_UI_HIDDEN
                        try {
                            Field exitField = param.thisObject.getClass().getDeclaredField("isExitPressed");
                            exitField.setAccessible(true);
                            boolean isExit = exitField.getBoolean(param.thisObject);
                            if (!isExit) exitField.setBoolean(param.thisObject, true);
                        } catch (Throwable t) {
                            param.args[0] = 0;    // fallback
                        }
                        param.setResult(null);    // eat the call
                    }
                }
            });

    XposedHelpers.findAndHookMethod(CLS, lpparam.classLoader, "onCreate",
            Bundle.class, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    Activity act = (Activity) param.thisObject;
                    try {
                        WebView wv = (WebView) XposedHelpers.getObjectField(act, "webView");
                        wv.setLongClickable(true);
                        wv.setHapticFeedbackEnabled(true);
                        wv.setOnLongClickListener(null);
                    } catch (Throwable ignored) {}
                }
            });

    XposedHelpers.findAndHookMethod(
        "android.view.Window",
        lpparam.classLoader,
        "setHideOverlayWindows",
        boolean.class,
        new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.args[0] = false;
            }
        });

    XposedHelpers.findAndHookMethod(Activity.class, "isInMultiWindowMode",
            XC_MethodReplacement.returnConstant(false));

    XposedHelpers.findAndHookMethod(Toast.class, "show", new XC_MethodHook() {
        @Override protected void beforeHookedMethod(MethodHookParam param) {
            StackTraceElement[] st = Thread.currentThread().getStackTrace();
            for (StackTraceElement e : st) {
                if (CLS.equals(e.getClassName()) && "onResume".equals(e.getMethodName())) {
                    param.setResult(null);   // swallow toast
                    return;
                }
            }
        }
    });
}

}
