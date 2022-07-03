package com.example.adpolelib;

import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class InAppRestClient {

    private static final String CONVERSION_URL = "conversion";
    private static final String IMPRESSION_URL = "impression";
    private static final String REGISTER_URL = "applications/check-register/";
    private static final String ADUNIT_URL = "advertisements/ad-units/";
    private static final String RETRY_CONVERSION_URL = "conversion/bulk";
    private static final String TAG = InAppRestClient.class.getName();
    private static final String BASE_URL = "https://api.ad-pole.com/";
    private static final int GET_TIMEOUT = 60000;
    private static final int TIMEOUT = 120000;
    private static boolean isSendingFailedConversion = false;

    static void sendConversion(final JSONObject conversion, final InAppResponseHandler responseHandler) {
        if (InterstitialActivity.appContext == null) {
            Log.e(TAG, "FUNCTION : sendConversion => App context is null");
            Exception notInitialized = new Exception("App context is null, you may have not been initialize InAppConstants in your Application class");
            responseHandler.onFailure(0, "Error: App context is null", notInitialized, InAppConstants.RequestType.Conversion);
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                makeRequest(CONVERSION_URL, "POST", conversion, responseHandler, TIMEOUT, InAppConstants.RequestType.Conversion);
            }
        }).start();
    }

    static void getImpression(final String query, final InAppResponseHandler responseHandler) {
        if (InterstitialActivity.appContext == null) {
            Log.e(TAG, "FUNCTION : getImpression => App context is null");
            Exception notInitialized = new Exception("App context is null, you may have not been initialize InAppConstants in your Application class");
            responseHandler.onFailure(0, "Error: App context is null", notInitialized, InAppConstants.RequestType.Impression);
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                makeRequest(IMPRESSION_URL + (query == null ? "" : query), null, "", responseHandler, GET_TIMEOUT, InAppConstants.RequestType.Impression);
            }
        }).start();
        retryPastFailedConversions(responseHandler);
    }

    static void getApplicationRegister(final String query, final InAppResponseHandler responseHandler) {
        if (AdPole.appContext == null) {
            Log.e(TAG, "FUNCTION : getApplicationRegister => App context is null");
            Exception notInitialized = new Exception("App context is null, you may have not been initialize InAppConstants in your Application class");
            responseHandler.onFailure(0, "Error: App context is null", notInitialized, InAppConstants.RequestType.REGISTER);
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                makeRequest(REGISTER_URL + (query == null ? "" : query), null, "", responseHandler, GET_TIMEOUT, InAppConstants.RequestType.REGISTER);
            }
        }).start();
    }

    static void getAdUnit(final String query, final InAppResponseHandler responseHandler) {
        if (InterstitialActivity.appContext == null) {
            Log.e(TAG, "FUNCTION : getApplicationRegister => App context is null");
            Exception notInitialized = new Exception("App context is null, you may have not been initialize InAppConstants in your Application class");
            responseHandler.onFailure(0, "Error: App context is null", notInitialized, InAppConstants.RequestType.ADUNIT);
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                makeRequest(ADUNIT_URL + (query == null ? "" : query), null, "", responseHandler, GET_TIMEOUT, InAppConstants.RequestType.ADUNIT);
            }
        }).start();
    }

    static void get(final String url, final InAppResponseHandler responseHandler, final JSONObject body, final InAppConstants.RequestType requestType) {
        new Thread(new Runnable() {
            public void run() {
                makeRequest(url, "GET", body, responseHandler, GET_TIMEOUT, requestType);
            }
        }).start();
    }

    private static void makeRequest(final String url, final String method, final JSONObject jsonBody, final InAppResponseHandler responseHandler
            , final int timeout, final InAppConstants.RequestType requestType) {

        final Thread[] callbackThread = new Thread[1];
        Thread connectionThread = new Thread(new Runnable() {
            public void run() {
                callbackThread[0] = startHTTPConnection(url, method, jsonBody != null ? jsonBody.toString() : null, responseHandler, timeout, requestType);
            }
        }, "OS_HTTPConnection");

        connectionThread.start();

        // getResponseCode() can hang past it's timeout setting so join it's thread to ensure it is timing out.
        try {
            connectionThread.join(getThreadTimeout(timeout));
            if (connectionThread.getState() != Thread.State.TERMINATED)
                connectionThread.interrupt();
            if (callbackThread[0] != null)
                callbackThread[0].join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void makeRequest(final String url, final String method, final String jsonBody, final InAppResponseHandler responseHandler
            , final int timeout, final InAppConstants.RequestType requestType) {

        final Thread[] callbackThread = new Thread[1];
        Thread connectionThread = new Thread(new Runnable() {
            public void run() {
                callbackThread[0] = startHTTPConnection(url, method, !jsonBody.equals("") ? jsonBody : null, responseHandler, timeout, requestType);
            }
        }, "OS_HTTPConnection");

        connectionThread.start();

        // getResponseCode() can hang past it's timeout setting so join it's thread to ensure it is timing out.
        try {
            connectionThread.join(getThreadTimeout(timeout));
            if (connectionThread.getState() != Thread.State.TERMINATED)
                connectionThread.interrupt();
            if (callbackThread[0] != null)
                callbackThread[0].join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void get(final String url, final InAppResponseHandler responseHandler, final InAppConstants.RequestType requestType) {
        new Thread(new Runnable() {
            public void run() {
                makeRequest(url, null, "", responseHandler, GET_TIMEOUT, requestType);
            }
        }).start();
    }

    private static Thread startHTTPConnection(String url, String method, String jsonBody, InAppResponseHandler responseHandler, int timeout, InAppConstants.RequestType requestType) {
        HttpURLConnection con = null;
        int httpResponse = -1;
        String json = null;
        Thread callbackThread = null;

        try {
            AdPoleLog.log("AdPoleRestClient: Making request to: " + BASE_URL + url);
            con = (HttpURLConnection) new URL(BASE_URL + url).openConnection();

            con.setUseCaches(false);
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);

            if (jsonBody != null)
                con.setDoInput(true);

            if (method != null) {
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13)
                    con.setRequestProperty("Connection", "close"); // This property avoids End Of File exception (EOFException) during send request
                con.setRequestMethod(method);
                con.setDoOutput(true);
            }

            if (jsonBody != null) {
                String strJsonBody = jsonBody;
                AdPoleLog.log("AdPoleRestClient: " + method + " SEND JSON: " + strJsonBody);

                byte[] sendBytes = strJsonBody.getBytes("UTF-8");
                con.setFixedLengthStreamingMode(sendBytes.length);

                OutputStream outputStream = con.getOutputStream();
                outputStream.write(sendBytes);
            }

            httpResponse = con.getResponseCode();
            AdPoleLog.log("AdPoleRestClient: After con.getResponseCode  to: " + BASE_URL + url);

            InputStream inputStream;
            Scanner scanner;
            if (httpResponse == HttpURLConnection.HTTP_OK) {
                AdPoleLog.log("AdPoleRestClient: Successfully finished request to: " + BASE_URL + url);

                inputStream = con.getInputStream();
                scanner = new Scanner(inputStream, "UTF-8");
                json = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                scanner.close();
                AdPoleLog.log(method + " RECEIVED JSON: " + json);

                callbackThread = callResponseHandlerOnSuccess(responseHandler, json, requestType);
            } else {
                AdPoleLog.log("AdPoleRestClient: Failed request to: " + BASE_URL + url);
                inputStream = con.getErrorStream();
                if (inputStream == null)
                    inputStream = con.getInputStream();

                if (inputStream != null) {
                    scanner = new Scanner(inputStream, "UTF-8");
                    json = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                    scanner.close();
                    AdPoleLog.log("AdPoleRestClient: " + method + " RECEIVED JSON: " + json);
                } else
                    AdPoleLog.log("AdPoleRestClient: " + method + " HTTP Code: " + httpResponse + " No response body!");

                callbackThread = callResponseHandlerOnFailure(responseHandler, httpResponse, json, null, requestType, jsonBody);
            }
        } catch (Throwable t) {
            if (t instanceof java.net.ConnectException || t instanceof java.net.UnknownHostException)
                AdPoleLog.log("AdPoleRestClient: Could not send last request, device is offline. Throwable: " + t.getClass().getName());
            else
                AdPoleLog.log("AdPoleRestClient: " + method + " Error thrown from network stack. " + t.toString());

            callbackThread = callResponseHandlerOnFailure(responseHandler, httpResponse, null, t, requestType, jsonBody);
        } finally {
            if (con != null)
                con.disconnect();
        }

        return callbackThread;
    }

    static void post(final String url, final JSONObject jsonBody, final InAppResponseHandler responseHandler) {
        new Thread(new Runnable() {
            public void run() {
                makeRequest(url, "POST", jsonBody, responseHandler, TIMEOUT, null);
            }
        }).start();
    }

    static void post(final String url, final JSONObject jsonBody, final InAppResponseHandler responseHandler, final InAppConstants.RequestType requestType) {
        new Thread(new Runnable() {
            public void run() {
                makeRequest(url, "POST", jsonBody, responseHandler, TIMEOUT, requestType);
            }
        }).start();
    }


    static void post(final String url, final String jsonBody, final InAppResponseHandler responseHandler, final InAppConstants.RequestType requestType) {
        new Thread(new Runnable() {
            public void run() {
                makeRequest(url, "POST", jsonBody, responseHandler, TIMEOUT, requestType);
            }
        }).start();
    }

    private static Thread callResponseHandlerOnSuccess(final InAppResponseHandler handler, final String response,
                                                       final InAppConstants.RequestType requestType) {

        if (requestType == InAppConstants.RequestType.Conversion_Retry) {
            Log.i(TAG, "FUNCTION : callResponseHandlerOnSuccess => Conversion retry successful");
            SharedPreferencesHelper.put(InterstitialActivity.appContext, SharedPreferencesHelper.Property.COVERSION_LIST, "");
        }

        if (requestType == InAppConstants.RequestType.Conversion_Retry)
            isSendingFailedConversion = false;

        if (handler == null)
            return null;

        Thread thread = new Thread(new Runnable() {
            public void run() {
                handler.onSuccess(response, requestType);
            }
        });
        thread.start();

        return thread;
    }

    private static Thread callResponseHandlerOnFailure(final InAppResponseHandler handler, final int statusCode, final String response,
                                                       final Throwable throwable, final InAppConstants.RequestType requestType, String request) {

        if (requestType == InAppConstants.RequestType.Conversion) {
            Log.e(TAG, "FUNCTION : callResponseHandlerOnFailure => Conversion is failed");
            saveConversionToRetryLater(request);
        }

        if (requestType == InAppConstants.RequestType.Conversion_Retry)
            isSendingFailedConversion = false;

        if (handler == null)
            return null;

        Thread thread = new Thread(new Runnable() {
            public void run() {
                handler.onFailure(statusCode, response, throwable, requestType);
            }
        });
        thread.start();

        return thread;
    }

    private static int getThreadTimeout(int timeout) {
        return timeout + 5000;
    }

    interface InAppResponseHandler {
        void onSuccess(String response, InAppConstants.RequestType requestType);

        void onFailure(int statusCode, String response, Throwable throwable, InAppConstants.RequestType requestType);
    }


    private static void saveConversionToRetryLater(String request) {
        Log.i(TAG, "FUNCTION : saveConversionToRetryLater");
        if (request != null) {
            Log.i(TAG, "FUNCTION : saveConversionToRetryLater => Have conversion json, going to send it later");
            try {
                JSONArray conversionArrayJson = new JSONArray();
                String conversionArrayString = SharedPreferencesHelper.get(InterstitialActivity.appContext, SharedPreferencesHelper.Property.COVERSION_LIST, "");
                if (!conversionArrayString.equals("")) {
                    Log.i(TAG, "FUNCTION : saveConversionToRetryLater => There where some unsuccessful conversions in past, going to add it to current one");
                    conversionArrayJson = new JSONArray(conversionArrayString);
                }
                conversionArrayJson.put(new JSONObject(request));
                Log.i(TAG, "FUNCTION : saveConversionToRetryLater => String to save: " + conversionArrayJson.toString());
                SharedPreferencesHelper.put(InterstitialActivity.appContext, SharedPreferencesHelper.Property.COVERSION_LIST, conversionArrayJson.toString());
            } catch (JSONException e) {
                Log.i(TAG, "FUNCTION : saveConversionToRetryLater => Error parsing JSON: " + e.toString());
            }
        } else {
            Log.e(TAG, "FUNCTION : saveConversionToRetryLater => Conversion json is null");
        }
    }

    private static void retryPastFailedConversions(final InAppResponseHandler responseHandler){
        Log.i(TAG, "FUNCTION : retryPastFailedConversions");
        try {
            final String conversionsArrayString = SharedPreferencesHelper.get(InterstitialActivity.appContext, SharedPreferencesHelper.Property.COVERSION_LIST, "");
            if (conversionsArrayString.equals("")) {
                Log.i(TAG, "FUNCTION : retryPastFailedConversions => There were no past failed conversions");
            } else if(!isSendingFailedConversion){
                Log.i(TAG, "FUNCTION : retryPastFailedConversions => There are some conversions to send");
                isSendingFailedConversion = true;
                final JSONArray conversionsArrayJson = new JSONArray(conversionsArrayString);
                new Thread(new Runnable() {
                    public void run() {
                        makeRequest(RETRY_CONVERSION_URL, "POST", conversionsArrayJson.toString(), responseHandler, TIMEOUT, InAppConstants.RequestType.Conversion_Retry);
                    }
                }).start();
            } else if(isSendingFailedConversion){
                Log.i(TAG, "FUNCTION : retryPastFailedConversions => Currently some other thread is doing that");
            }
        } catch (Exception e){
            Log.e(TAG, "FUNCTION : retryPastFailedConversions => Error: " + e.toString());
            e.printStackTrace();
        }
    }
}
