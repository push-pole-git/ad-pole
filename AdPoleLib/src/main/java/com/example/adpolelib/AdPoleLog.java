package com.example.adpolelib;

import static com.example.adpolelib.AdPolePrefs.PREFS_ADPOLE;
import static com.example.adpolelib.AdPolePrefs.PREF_APP_ID;
import static com.example.adpolelib.RestClientMethods.sendExceptionReportRequest;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AdPoleLog {

    public static final String TAG = "AdPole";


    public static void catchException(String source, String log, Throwable throwable) {
        sendExceptionLog(source, log, throwable);
    }

    public static void catchException(String source, String log, int statusCode, String response, Throwable throwable) {
        catchException(source, log + " StatusCode: " + statusCode + " Response: " + response, throwable);
    }

    public static void log(String log) {
        Log.i(TAG, log == null ? "" : log) ;
    }

    public static void log(String log, Throwable t) {
            Log.i(TAG, log == null ? "" : log, t);
    }


    private static void sendExceptionLog(final String source, final String log, final Throwable throwable){
        new Thread(new Runnable() {
            public void run() {
                try {
                    sendExceptionLogTask(source, log, throwable);
                } catch (JSONException e) {
                    log("Error occurred when sending exception logs to api: ", e);
                } catch (NullPointerException e){
                    log("Error occurred when sending exception logs to api: ", e);
                }
            }
        }, "OS_EXCEPTION_ASYNC").start();
    }

    private static void sendExceptionLogTask(String source, String description, Throwable exception) throws JSONException, NullPointerException {
        String exceptionMessage;
        if (exception != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            exceptionMessage = sw.toString();
        } else {
            exceptionMessage = null;
        }

        JSONObject body = new JSONObject();

        body.put("AppId", AdPolePrefs.getString(PREFS_ADPOLE, PREF_APP_ID, null));
        body.put("DeviceId", DeviceInfo.id(AdPole.appContext));
        //body.put("Token", PersistenceManager.getInstance(AdPole.appContext).getPushToken());
        body.put("Source", source);
        body.put("Description", description);
        body.put("ExceptionMessage", exceptionMessage);
        body.put("SdkVersion", BuildConfig.VERSION_NAME);

        sendExceptionReportRequest(body);
    }
}
