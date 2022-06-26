package com.example.adpolelib;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class ActivityLifecycleHandler {
    static boolean nextResumeIsFirstActivity;

    interface ActivityAvailableListener {
        void available(Activity activity);
    }

    static Activity curActivity;
    private static ActivityAvailableListener mActivityAvailableListener;
    static FocusHandlerThread focusHandlerThread = new FocusHandlerThread();

    static void setActivityAvailableListener(ActivityAvailableListener activityAvailableListener) {
        if (curActivity != null) {
            activityAvailableListener.available(curActivity);
            mActivityAvailableListener = activityAvailableListener;
        }
        else
            mActivityAvailableListener = activityAvailableListener;
    }

    public static void removeActivityAvailableListener(ActivityAvailableListener activityAvailableListener) {
        mActivityAvailableListener = null;
    }

    private static void setCurActivity(Activity activity) {
        curActivity = activity;
        if (mActivityAvailableListener != null)
            mActivityAvailableListener.available(curActivity);
    }

    static void onActivityCreated(Activity activity) {}
    static void onActivityStarted(Activity activity) {}

    static void onActivityResumed(Activity activity) {
        setCurActivity(activity);

        logCurActivity();
        handleFocus();
    }

    static void onActivityPaused(Activity activity) {
        if (activity == curActivity) {
            curActivity = null;
            handleLostFocus();
        }

        logCurActivity();
    }

    static void onActivityStopped(Activity activity) {

        if (activity == curActivity) {
            curActivity = null;
            handleLostFocus();
        }

        logCurActivity();
    }

    static void onActivityDestroyed(Activity activity) {

        if (activity == curActivity) {
            curActivity = null;
            handleLostFocus();
        }

        logCurActivity();
    }

    static private void logCurActivity() {
    }

    static private void handleLostFocus() {
        focusHandlerThread.runRunnable(new AppFocusRunnable());
    }

    static private void handleFocus() {
        if (focusHandlerThread.hasBackgrounded() || nextResumeIsFirstActivity) {
            nextResumeIsFirstActivity = false;
            focusHandlerThread.resetBackgroundState();
            //OneSignal.onAppFocus();
        }
        else
            focusHandlerThread.stopScheduledRunnable();
    }

    static class FocusHandlerThread extends HandlerThread {
        Handler mHandler = null;
        private AppFocusRunnable appFocusRunnable;

        FocusHandlerThread() {
            super("FocusHandlerThread");
            start();
            mHandler = new Handler(getLooper());
        }

        Looper getHandlerLooper() {
            return  mHandler.getLooper();
        }

        void resetBackgroundState() {
            if (appFocusRunnable != null)
                appFocusRunnable.backgrounded = false;
        }

        void stopScheduledRunnable() {
            mHandler.removeCallbacksAndMessages(null);
        }

        void runRunnable(AppFocusRunnable runnable) {
            if (appFocusRunnable != null && appFocusRunnable.backgrounded && !appFocusRunnable.completed)
                return;

            appFocusRunnable = runnable;
            mHandler.removeCallbacksAndMessages(null);
            mHandler.postDelayed(runnable, 2000);
        }

        boolean hasBackgrounded() {
            return appFocusRunnable != null && appFocusRunnable.backgrounded;
        }
    }

    static private class AppFocusRunnable implements Runnable {
        private boolean backgrounded, completed;

        public void run() {
            if (curActivity != null)
                return;

            backgrounded = true;
            //OneSignal.onAppLostFocus();
            completed = true;
        }
    }
}
