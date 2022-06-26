package com.example.adpolelib;

import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AdPoleRestClient {
    static class ResponseHandler {
        void onSuccess(String response) {
        }

        void onFailure(int statusCode, String response, Throwable throwable) {
        }
    }

    private static final String BASE_URL = "https://mpapi.adwised.com/api/";
    private static final int TIMEOUT = 120000;
    private static final int GET_TIMEOUT = 60000;

    private static int getThreadTimeout(int timeout) {
        return timeout + 5000;
    }

    static void put(final String url, final JSONObject jsonBody, final ResponseHandler responseHandler) {

        new Thread(new Runnable() {
            public void run() {
                makeRequest(url, "PUT", jsonBody, responseHandler, TIMEOUT);
            }
        }).start();
    }

    static void post(final String url, final JSONObject jsonBody, final ResponseHandler responseHandler) {
        new Thread(new Runnable() {
            public void run() {
                makeRequest(url, "POST", jsonBody, responseHandler, TIMEOUT);
            }
        }).start();
    }

    static void post(final String url, final JSONArray jsonArray, final ResponseHandler responseHandler) {
        new Thread(new Runnable() {
            public void run() {
                makeRequestWithJsonArray(url, "POST", jsonArray, responseHandler, TIMEOUT);
            }
        }).start();
    }


    static void get(final String url, final ResponseHandler responseHandler) {
        new Thread(new Runnable() {
            public void run() {
                makeRequest(url, null, null, responseHandler, GET_TIMEOUT);
            }
        }).start();
    }

    static void getSync(final String url, final ResponseHandler responseHandler) {
        makeRequest(url, null, null, responseHandler, GET_TIMEOUT);
    }

    static void putSync(String url, JSONObject jsonBody, ResponseHandler responseHandler) {
        makeRequest(url, "PUT", jsonBody, responseHandler, TIMEOUT);
    }

    static void postSync(String url, JSONObject jsonBody, ResponseHandler responseHandler) {
        makeRequest(url, "POST", jsonBody, responseHandler, TIMEOUT);
    }

    private static void makeRequest(final String url, final String method, final JSONObject jsonBody, final ResponseHandler responseHandler, final int timeout) {

        final Thread[] callbackThread = new Thread[1];
        Thread connectionThread = new Thread(new Runnable() {
            public void run() {
                callbackThread[0] = startHTTPConnection(url, method, jsonBody != null ? jsonBody.toString() : null, responseHandler, timeout);
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

    private static void makeRequestWithJsonArray(final String url, final String method, final JSONArray jsonArray, final ResponseHandler responseHandler, final int timeout) {

        final Thread[] callbackThread = new Thread[1];
        Thread connectionThread = new Thread(new Runnable() {
            public void run() {
                callbackThread[0] = startHTTPConnection(url, method, jsonArray != null ? jsonArray.toString() : null, responseHandler, timeout);
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

    private static Thread startHTTPConnection(String url, String method, String jsonBody, ResponseHandler responseHandler, int timeout) {
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

                callbackThread = callResponseHandlerOnSuccess(responseHandler, json);
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

                callbackThread = callResponseHandlerOnFailure(responseHandler, httpResponse, json, null);
            }
        } catch (Throwable t) {
            if (t instanceof java.net.ConnectException || t instanceof java.net.UnknownHostException)
                AdPoleLog.log("AdPoleRestClient: Could not send last request, device is offline. Throwable: " + t.getClass().getName());
            else
                AdPoleLog.log("AdPoleRestClient: " + method + " Error thrown from network stack. " + t.toString());

            callbackThread = callResponseHandlerOnFailure(responseHandler, httpResponse, null, t);
        } finally {
            if (con != null)
                con.disconnect();
        }

        return callbackThread;
    }


    // These helper methods run the callback a new thread so they don't count towards the fallback thread join timer.

    private static Thread callResponseHandlerOnSuccess(final ResponseHandler handler, final String response) {
        if (handler == null)
            return null;

        Thread thread = new Thread(new Runnable() {
            public void run() {
                handler.onSuccess(response);
            }
        });
        thread.start();

        return thread;
    }

    private static Thread callResponseHandlerOnFailure(final ResponseHandler handler, final int statusCode, final String response, final Throwable throwable) {
        if (handler == null)
            return null;

        Thread thread = new Thread(new Runnable() {
            public void run() {
                handler.onFailure(statusCode, response, throwable);
            }
        });
        thread.start();

        return thread;
    }
}
