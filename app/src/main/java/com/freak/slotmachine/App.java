package com.freak.slotmachine;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;


import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Freak
 * @date 2019/9/11.
 */


public class App extends MultiDexApplication {
    public static final int DESIGN_WIDTH = 375;
    private static App instance;
    /**
     * 存放activity的列表
     */
    public HashMap<Class<?>, Activity> allActivities;

    public static synchronized App getInstance() {
        return instance;
    }


    public static void setInstance(App instance) {
        App.instance = instance;
    }

    private boolean isSend = false;

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                //限制竖屏
                //8.0.0版本系统，同时设置竖屏和设置全屏透明冲突，
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                resetDensity(getApplicationContext(), DESIGN_WIDTH);
                resetDensity(activity, DESIGN_WIDTH);
                setImmersiveStatusBar(activity);

            }

            @Override
            public void onActivityStarted(Activity activity) {
                setToolBar(activity);
                resetDensity(getApplicationContext(), DESIGN_WIDTH);
                resetDensity(activity, DESIGN_WIDTH);
            }

            @Override
            public void onActivityResumed(Activity activity) {
                resetDensity(getApplicationContext(), DESIGN_WIDTH);
                resetDensity(activity, DESIGN_WIDTH);
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                resetDensity(getApplicationContext(), DESIGN_WIDTH);
                resetDensity(activity, DESIGN_WIDTH);
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });

    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);
    }

    /**
     * 设置ToolBar
     *
     * @param activity
     */
    private void setToolBar(final Activity activity) {
        if (activity.findViewById(R.id.tool_bar) != null && ((AppCompatActivity) activity).getSupportActionBar() == null) {
            Toolbar toolbar = activity.findViewById(R.id.tool_bar);
            if (!TextUtils.isEmpty(activity.getTitle())) {
                toolbar.setTitle(activity.getTitle());
            } else {
                toolbar.setTitle("");
            }

            if (((IActivityStatusBar) activity).getStatusBarColor() != 0) {
                toolbar.setBackgroundColor(((IActivityStatusBar) activity).getStatusBarColor());
            } else {
                toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.color_white));
            }

            ((AppCompatActivity) activity).setSupportActionBar(toolbar);
            ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onBackPressed();
                }
            });
        }
    }

    /**
     * 设置状态栏
     *
     * @param activity
     */
    private void setImmersiveStatusBar(Activity activity) {
        if (activity instanceof IActivityStatusBar) {
            if (((IActivityStatusBar) activity).getStatusBarColor() != 0) {
                setTranslucentStatus(activity);
                addImmersiveStatusBar(activity, ((IActivityStatusBar) activity).getStatusBarColor());
            } else {
                if (((IActivityStatusBar) activity).getDrawableStatusBar() != 0) {
                    setTranslucentStatus(activity);
                    addImmersiveShadeStatusBar(activity, ((IActivityStatusBar) activity).getDrawableStatusBar());
                }
            }
        }
    }

    /**
     * 添加自定义状态栏
     *
     * @param activity
     */
    private void addImmersiveStatusBar(Activity activity, int color) {
        ViewGroup contentFrameLayout = activity.findViewById(android.R.id.content);
        View contentView = contentFrameLayout.getChildAt(0);
        if (contentView != null) {
            contentView.setFitsSystemWindows(true);
        }

        View statusBar = new View(activity);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (hasNotchInScreen(activity)) {
            params.height = getNotchSize(activity)[1];
        } else {
            params.height = getStatusBarHeight();
        }
        params.height = getStatusBarHeight();
        statusBar.setLayoutParams(params);
        statusBar.setBackgroundColor(color);
        contentFrameLayout.addView(statusBar);
    }

    /**
     * 设置状态栏渐变色
     *
     * @param activity activity
     * @param drawable drawable资源文件
     */
    private void addImmersiveShadeStatusBar(Activity activity, @DrawableRes int drawable) {
        ViewGroup contentFrameLayout = activity.findViewById(android.R.id.content);
        View contentView = contentFrameLayout.getChildAt(0);
        if (contentView != null && Build.VERSION.SDK_INT >= 14) {
            contentView.setFitsSystemWindows(true);
        }

        View statusBar = new View(activity);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (hasNotchInScreen(activity)) {
            params.height = getNotchSize(activity)[1];
        } else {
            params.height = getStatusBarHeight();
        }
//        params.height = getStatusBarHeight();
        statusBar.setLayoutParams(params);
        statusBar.setBackgroundResource(drawable);
        contentFrameLayout.addView(statusBar);
    }

    /**
     * 获取状态栏高度
     *
     * @return
     */
    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    /**
     * 设置状态栏为透明
     *
     * @param activity
     */
    private void setTranslucentStatus(Activity activity) {
        //******** 5.0以上系统状态栏透明 ********

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 以pt为单位重新计算大小
     */
    public static void resetDensity(Context context, float designWidth) {
        if (context == null) {
            return;
        }
        Point size = new Point();
        ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getSize(size);
        Resources resources = context.getResources();
        resources.getDisplayMetrics().xdpi = size.x / designWidth * 72f;
        DisplayMetrics metrics = getMetricsOnMIUI(context.getResources());
        if (metrics != null) {
            metrics.xdpi = size.x / designWidth * 72f;
        }
    }

    /**
     * 解决MIUI屏幕适配问题
     *
     * @param resources
     * @return
     */
    private static DisplayMetrics getMetricsOnMIUI(Resources resources) {
        if ("MiuiResources".equals(resources.getClass().getSimpleName()) || "XResources".equals(resources.getClass().getSimpleName())) {
            try {
                Field field = Resources.class.getDeclaredField("mTmpMetrics");
                field.setAccessible(true);
                return (DisplayMetrics) field.get(resources);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 添加activity
     *
     * @param activity
     * @param tClass
     */
    public void addActivity(AppCompatActivity activity, Class<?> tClass) {
        if (allActivities == null) {
            allActivities = new LinkedHashMap<>();
        }

//        LogUtil.e("activity堆栈 addActivity " + activity.getClass().getSimpleName());
        allActivities.put(tClass, activity);
//        LogUtil.d("activity堆栈 addActivity  " + allActivities);
    }

    /**
     * 移除activity,代替finish
     *
     * @param activity
     */
    public void removeActivity(AppCompatActivity activity) {
        if (allActivities != null) {
//            LogUtil.d("activity堆栈 removeActivity  " + allActivities);
//            LogUtil.d("activity堆栈 removeActivity  " + activity.getClass().getSimpleName());
            if (allActivities.containsValue(activity)) {
                allActivities.remove(activity.getClass());
//                LogUtil.e("activity堆栈 removeActivity " + activity.getClass().getSimpleName());
            }
        }
    }

    /**
     * 移除所有activity并结束程序
     */
    public void finishActivity() {
        if (allActivities != null && allActivities.size() > 0) {
            Set<Map.Entry<Class<?>, Activity>> sets = allActivities.entrySet();
            for (Map.Entry<Class<?>, Activity> s : sets) {
                if (!s.getValue().isFinishing()) {
                    s.getValue().finish();
                }
            }
            allActivities.clear();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    /**
     * 移除所有的Activity
     */
    public void removeAllActivity() {
        if (allActivities != null && allActivities.size() > 0) {
            Set<Map.Entry<Class<?>, Activity>> sets = allActivities.entrySet();

            for (Map.Entry<Class<?>, Activity> s : sets) {
                if (!s.getValue().isFinishing()) {
                    s.getValue().finish();
                }
            }
            allActivities.clear();
        }
    }

    /**
     * 判断一个Activity 是否存在
     *
     * @param clz
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public <T extends AppCompatActivity> boolean isActivityExist(Class<T> clz) {
        boolean res;
        AppCompatActivity activity = getActivity(clz);
        if (activity == null) {
            res = false;
        } else {
            if (activity.isFinishing() || activity.isDestroyed()) {
                res = false;
            } else {
                res = true;
            }
        }

        return res;
    }

    /**
     * 获得指定activity实例
     *
     * @param clazz Activity 的类对象
     * @return
     */
    public <T extends AppCompatActivity> T getActivity(Class<T> clazz) {
        return (T) allActivities.get(clazz);
    }


    /**
     * 判断是否是刘海屏  华为手机
     *
     * @param context
     * @return
     */
    public static boolean hasNotchInScreen(Context context) {

        boolean ret = false;

        try {

            ClassLoader cl = context.getClassLoader();

            Class hwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");

            Method get = hwNotchSizeUtil.getMethod("hasNotchInScreen");

            ret = (boolean) get.invoke(hwNotchSizeUtil);

        } catch (ClassNotFoundException e) {

            Log.e("test", "hasNotchInScreen ClassNotFoundException");

        } catch (NoSuchMethodException e) {

            Log.e("test", "hasNotchInScreen NoSuchMethodException");

        } catch (Exception e) {

            Log.e("test", "hasNotchInScreen Exception");

        }

        return ret;

    }

    /**
     * 获取刘海屏尺寸 华为手机
     *
     * @param context
     * @return
     */
    public static int[] getNotchSize(Context context) {

        int[] ret = new int[]{0, 0};

        try {

            ClassLoader cl = context.getClassLoader();

            Class hwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");

            Method get = hwNotchSizeUtil.getMethod("getNotchSize");

            ret = (int[]) get.invoke(hwNotchSizeUtil);

        } catch (ClassNotFoundException e) {



        } catch (NoSuchMethodException e) {


        } catch (Exception e) {



        }
        return ret;

    }
}

