package com.example.adpolelib;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class InterstitialActivity extends AppCompatActivity implements InAppRestClient.InAppResponseHandler {

    private static final String TAG = InterstitialActivity.class.getName();
    private static InAppConstants.AdType adType = InAppConstants.AdType.INTERSTITIAL;
    private WebView webView;
    private ProgressBar loadingPb;
    private static String adUnitId;
    private static boolean isSelfStarted = false;
    private String creativeId;
    private static boolean isForTest = false;

    public static void show(Context context, String adUnitId, boolean isForTest) {
        context.startActivity(new Intent(context, InterstitialActivity.class));
        isSelfStarted = true;
        InterstitialActivity.adUnitId = adUnitId;
        adType = InAppConstants.AdType.INTERSTITIAL;
        InterstitialActivity.isForTest = isForTest;
    }

    public static void show(Context context, String adUnitId) {
        show(context, adUnitId, false);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isSelfStarted) {
            Log.e(TAG, "FUNCTION : onCreate => Start activity by calling InterstitialActivity.show()");
            finish();
        }

        createAndSetViews();

        String query = "?AdUnitId=" + adUnitId + "&DeviceId=" + DeviceInfo.id(this) + "&AdType=" + adType.getTypeCode() + (isForTest ? "&testMode=true" : "");
        InAppRestClient.getImpression(query, this);
    }

    private void createAndSetViews() {
        Log.i(TAG, "FUNCTION : createAndSetViews");
        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        relativeLayout.setBackgroundColor(Color.parseColor("#363636"));
        relativeLayout.setGravity(Gravity.CENTER);
        Log.i(TAG, "FUNCTION : createAndSetViews => Relative layout initialized");

        loadingPb = new ProgressBar(this);
        loadingPb.setIndeterminate(true);
        RelativeLayout.LayoutParams loadingPbLp = new RelativeLayout.LayoutParams(200, 200);
        loadingPbLp.addRule(RelativeLayout.CENTER_IN_PARENT, relativeLayout.getId());
        loadingPb.setLayoutParams(loadingPbLp);
        webView = new WebView(this);
        LinearLayout.LayoutParams webViewLp = new LinearLayout.LayoutParams(-1, -1);
        webViewLp.setMargins(40, 40, 40, 40);
        webView.setLayoutParams(webViewLp);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setVisibility(GONE);
        webView.getSettings().setDomStorageEnabled(true);
//        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JSInterface(), "SDK");
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "FUNCTION : createAndSetViews => onPageFinished");
                loadingPb.setVisibility(GONE);
                webView.setVisibility(VISIBLE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, "FUNCTION : createAndSetViews => shouldOverrideUrlLoading");
                webView.loadUrl(url);
                return true;
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e(TAG, "FUNCTION : createAndSetViews => Error: " + description);
                finish();
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        Log.i(TAG, "FUNCTION : createAndSetViews => Web view initialized");

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.addView(webView);
        Log.i(TAG, "FUNCTION : createAndSetViews => Linear layout initialized");

        relativeLayout.addView(loadingPb);
        relativeLayout.addView(linearLayout);
        setContentView(relativeLayout);
    }


    @Override
    public void onSuccess(final String response, InAppConstants.RequestType requestType) {
        Log.i(TAG, "FUNCTION : onSuccess => URL: " + requestType.toString() + " Response: " + response);
        switch (requestType) {
            case Impression:
                Log.i(TAG, "FUNCTION : onSuccess => Impression");
                showImpression(response);
                break;
            case Conversion:
                Log.i(TAG, "FUNCTION : onSuccess => Conversion");
                break;
        }
    }

    private void showImpression(final String response) {
        Log.i(TAG, "FUNCTION : showImpression");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);
                    webView.loadUrl(jsonResponse.getString("bannerUrl"));
                } catch (JSONException e) {
                    Log.e(TAG, "FUNCTION : showImpression => Error: " + e.toString());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onFailure(int statusCode, String response, Throwable throwable, InAppConstants.RequestType requestType) {
        Log.e(TAG, "FUNCTION : onFailure");
        if (throwable != null) {
            Log.e(TAG, "FUNCTION : onFailure =>  Error: " + throwable.toString());
            throwable.printStackTrace();
        } else {
            Log.e(TAG, "FUNCTION : onFailure => null throwable");
        }

        switch (requestType) {
            case Impression:
                Log.e(TAG, "FUNCTION : onFailure => Impression, Going to hide this");
                finish();
                break;
            case Conversion:
                Log.e(TAG, "FUNCTION : onFailure => Conversion");
                break;
        }
    }

    private void sendConversion(String impressionId) {
        Log.i(TAG, "FUNCTION : sendConversion");
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("adUnitId", adUnitId)
                    .put("deviceId", DeviceInfo.id(this))
                    .put("creativeId", creativeId)
                    .put("impressionId", impressionId)
                    .put("timestamp", System.currentTimeMillis());
            InAppRestClient.sendConversion(jsonObject, this);
        } catch (Exception e) {
            Log.e(TAG, "FUNCTION : onAdClick => Error: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isSelfStarted = false;
    }

    private class JSInterface {
        private final String TAG = JSInterface.class.getName();

        @JavascriptInterface
        public void conversion(String url, String impressionId) {
            Log.i(TAG, "FUNCTION : conversion");
            sendConversion(impressionId);
            if(URLUtil.isValidUrl(url)){
                Log.i(TAG, "FUNCTION : conversion => is valid URL going to open it");
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } else {
                Log.e(TAG, "FUNCTION : conversion => is NOT valid URL");
            }
        }

        @JavascriptInterface
        public void close() {
            Log.i(TAG, "FUNCTION : close");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }
    }
}