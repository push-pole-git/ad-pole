package com.example.adpolelib;

import static com.example.adpolelib.AdPolePrefs.PREFS_ADPOLE;
import static com.example.adpolelib.AdPolePrefs.PREF_APP_ID;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class InAppAdvertise extends AdPoleRestClient.ResponseHandler {

    private static final String TAG = InAppAdvertise.class.getName();
    protected static Context appContext;

    private static InAppRestClient.InAppResponseHandler responseHandler = new InAppRestClient.InAppResponseHandler() {
        @Override
        public void onSuccess(String response, InAppConstants.RequestType requestType) {
            Log.i(TAG, "FUNCTION : onSuccess");
        }

        @Override
        public void onFailure(int statusCode, String response, Throwable throwable, InAppConstants.RequestType requestType) {
            Log.e(TAG, "FUNCTION : onFailure");
            if(throwable != null){
                Log.e(TAG, "FUNCTION : onFailure => Error: " + throwable.toString());
                throwable.printStackTrace();
            } else {
                Log.e(TAG, "FUNCTION : onFailure => Throwable is null");
            }
        }
    };

    public static void init(Context context) {
        try {
            Log.i(TAG, "FUNCTION : init");
            appContext = context;
            AdPole.setContext(context);
            JSONObject deviceInfo = DeviceInfo.getDeviceInfo(appContext, AdPolePrefs.getString(PREFS_ADPOLE, PREF_APP_ID, null));
            InAppRestClient.post("deviceInfo", deviceInfo, responseHandler);
        } catch (JSONException e) {
            Log.i(TAG, "FUNCTION : init => Error: " + e.toString());
            e.printStackTrace();
        }
    }
}