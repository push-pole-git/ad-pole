package com.example.adpolelib;

import static com.example.adpolelib.AdPole.appContext;
import static com.example.adpolelib.AdPolePrefs.PREFS_ADPOLE;
import static com.example.adpolelib.AdPolePrefs.PREFS_SUBSCRIBE_TOKEN_REPORT;
import static com.example.adpolelib.AdPolePrefs.PREF_APP_ID;

import org.json.JSONException;
import org.json.JSONObject;

public class RestClientMethods {
    static void sendExceptionReportRequest(JSONObject requestModelJson) {
        AdPoleRestClient.ResponseHandler responseHandler = new AdPoleRestClient.ResponseHandler() {
            @Override
            void onFailure(final int statusCode, String response, Throwable throwable) {
                AdPoleLog.log(response);
            }

            @Override
            void onSuccess(String response) {
                AdPoleLog.log(response);
            }
        };

        AdPoleLog.log("Starting request to post data for subscription.");
        AdPoleRestClient.post("mobilepush/ClientException", requestModelJson, responseHandler);
    }

    static void sendSubscriptionRequest(JSONObject requestModelJson) {
        AdPoleRestClient.ResponseHandler responseHandler = new AdPoleRestClient.ResponseHandler() {
            @Override
            void onFailure(final int statusCode, String response, Throwable throwable) {
                AdPolePrefs.saveBool(PREFS_ADPOLE, PREFS_SUBSCRIBE_TOKEN_REPORT, false);
                AdPoleLog.log(response);
                AdPoleLog.catchException(this.getClass().getName(), "error occurred when registering to server.", statusCode, response, throwable);
                AdPoleLog.log("errors during have registrations via api");
            }

            @Override
            void onSuccess(String response) {
                AdPolePrefs.saveBool(PREFS_ADPOLE, PREFS_SUBSCRIBE_TOKEN_REPORT, true);
                AdPoleLog.log(response);
                try {
                    sendDeviceInfo();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        AdPoleLog.log("Starting request to post data for subscription.");
        AdPoleRestClient.post("mobilepush/Subscriber", requestModelJson, responseHandler);
    }

    static void sendDeviceInfo() throws JSONException {
        JSONObject deviceInfo = DeviceInfo.getDeviceInfo(appContext, AdPolePrefs.getString(PREFS_ADPOLE, PREF_APP_ID, null));
        AdPoleRestClient.ResponseHandler responseHandler = new AdPoleRestClient.ResponseHandler() {
            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                AdPoleLog.log(response);
                AdPoleLog.catchException(this.getClass().getName(), "Error when sending device info to server.", statusCode, response, throwable);
            }

            @Override
            void onSuccess(String response) {
                AdPoleLog.log(response);
            }
        };

        AdPoleLog.log("Starting request to post data for subscription.");
        AdPoleRestClient.post("mobilepush/Subscriber/DevcieSpecification", deviceInfo, responseHandler);
    }
}
