package com.example.adpolelib;

import static com.example.adpolelib.AdPolePrefs.CTA_URL;
import static com.example.adpolelib.AdPolePrefs.IS_LOADED;
import static com.example.adpolelib.AdPolePrefs.PREFS_ADPOLE;
import static com.example.adpolelib.AdPolePrefs.PREFS_SUBSCRIBE_TOKEN_REPORT;
import static com.example.adpolelib.AdPolePrefs.PREF_APP_ID;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.example.adpolelib.Interfaces.AdPoleLoadDataListener;
import com.example.adpolelib.Interfaces.AdUnitListener;
import com.example.adpolelib.Interfaces.SubscribeUserListener;

import org.json.JSONException;
import org.json.JSONObject;

public class InterstitialActivity extends AppCompatActivity implements InAppRestClient.InAppResponseHandler {

    private static final String TAG = InterstitialActivity.class.getName();
    private static InAppConstants.AdType adType = InAppConstants.AdType.INTERSTITIAL;
    private VideoView videoView;
    private static String adUnitId;
    private static boolean isSelfStarted = false;
    private String creativeId;
    private static boolean isForTest = false;
    private ImageView imageView;
    private static String displayUrl ;
    private static String ctaUrl ;
    private static String adUnitIdCache;
    private static AdPoleLoadDataListener listener;
    private static boolean isLoaded=false;
    private static boolean open = true;
    protected static Context IContext=null;
    protected static Context appContext;
    private static AdUnitListener adListener;

    private static InAppRestClient.InAppResponseHandler responseHandler = new InAppRestClient.InAppResponseHandler() {
        @Override
        public void onSuccess(String response, InAppConstants.RequestType requestType) {
            Log.i(TAG, "FUNCTION : onSuccess");
            try {
                JSONObject obj = new JSONObject(response);
                AdPolePrefs.saveString(PREFS_ADPOLE,CTA_URL,String.valueOf(obj.get("cta_url")));
                ctaUrl = String.valueOf(obj.get("cta_url"));
                displayUrl = String.valueOf(obj.get("display_content_url"));
                adUnitIdCache = String.valueOf(obj.get("ad_unit_id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (adListener!=null)
                adListener.success();
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
            if (adListener!=null)
                adListener.failure();
        }
    };
    public static void init(Context context) {
        AdPole.setSubscribeAbleStatusListener(new SubscribeUserListener() {
            @Override
            public void success() {
                if(AdPole.getAdPoleSubRepo()) {
                    isSelfStarted = true;
                    adType = InAppConstants.AdType.INTERSTITIAL;
                    IContext = context;
                    appContext=context;
                    AdPole.setContext(IContext);
                    String query = "?package_name=" + appContext.getPackageName() + "&app_id=" + AdPole.appId;
                    InAppRestClient.getAdUnit(query, responseHandler);
                }
            }

            @Override
            public void failure() {

            }
        });

    }
    public static void loadAd() {
        setAdUnitListener(new AdUnitListener() {
            @Override
            public void success() {
                if(AdPole.getAdPoleSubRepo() && IContext!=null) {
                    downloadVideo(IContext, displayUrl);
                }
            }

            @Override
            public void failure() {
            }
        });
    }
    public static void show() {
        if(IContext!=null) {
            if (isLoaded()) {
                IContext.startActivity(new Intent(IContext, InterstitialActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }

        }
    }

    public static boolean isLoaded() {
        if(IContext!=null) {
            isLoaded = Utils.isFilePresent(IContext);
        }
        return isLoaded;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        videoView = new VideoView(this);
        imageView = new ImageView(this);
        /*if (!isSelfStarted) {
            Log.e(TAG, "FUNCTION : onCreate => Start activity by calling InterstitialActivity.show()");
            finish();
        }*/

        createAndSetViews();
    }

    private void createAndSetViews() {
        Log.i(TAG, "FUNCTION : createAndSetViews");
        RelativeLayout relativeLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams rltvLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        relativeLayout.setLayoutParams(rltvLayout);
        relativeLayout.setBackgroundColor(Color.parseColor("#000000"));
        rltvLayout.setMargins(10, 10, 10, 10);
        relativeLayout.setGravity(Gravity.CENTER);

        Log.i(TAG, "FUNCTION : createAndSetViews => Relative layout initialized");
        Log.i(TAG, "createAndSetViews:=> RootDirPath: " + Utils.getRootDirPath(this) + "/ad.mp4");
        videoView.setVideoPath(Utils.getRootDirPath(this) + "/ad.mp4");
        LinearLayout.LayoutParams videoViewLp = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        videoView.setLayoutParams(videoViewLp);
        videoView.start();

        Log.i(TAG, "FUNCTION : createAndSetViews => VideoView initialized");
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.addView(videoView);
        Log.i(TAG, "FUNCTION : createAndSetViews => Linear layout initialized");
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdPolePrefs.saveBool(PREFS_ADPOLE, IS_LOADED, true);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AdPolePrefs.getString(PREFS_ADPOLE,CTA_URL,ctaUrl))));
                finish();
            }
        });

        imageView.setImageResource(R.drawable.image);
        RelativeLayout.LayoutParams close = new RelativeLayout.LayoutParams(100, 100);
        close.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, relativeLayout.getId());
        close.addRule(RelativeLayout.ALIGN_PARENT_TOP, relativeLayout.getId());
        close.setMargins(20, 20, 20, 20);
        imageView.setLayoutParams(close);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdPolePrefs.saveBool(PREFS_ADPOLE, IS_LOADED, true);
                finish();
            }
        });
        relativeLayout.addView(linearLayout);
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                finish();
                return false;
            }
        });
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                relativeLayout.addView(imageView);

            }
        };
        handler.postDelayed(runnable, 3000);
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

    @Override
    public void onFailure(int statusCode, String response, Throwable throwable, InAppConstants.RequestType requestType) {
        Log.e(TAG, "FUNCTION : onFailure");
        if (throwable != null) {
            Log.e(TAG, "FUNCTION : onFailure =>  Error: " + throwable);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isSelfStarted = false;
        AdPolePrefs.saveString(this, "no");
        Log.i(TAG, "INSIDE: onDestroy");
    }

    private void showImpression(final String response) {

        Log.i(TAG, "FUNCTION : showImpression");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);
                    //videoView.loadUrl(jsonResponse.getString("bannerUrl"));
                } catch (JSONException e) {
                    Log.e(TAG, "FUNCTION : showImpression => Error: " + e);
                    e.printStackTrace();
                }
            }
        });
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
            Log.e(TAG, "FUNCTION : onAdClick => Error: " + e);
            e.printStackTrace();
        }
    }

    private static void downloadVideo(Context context, String url) {
        // Enabling database for resume support even after the application is killed:
        PRDownloader.initialize(context);
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .build();
        PRDownloader.initialize(context, config);
        int downloadId = PRDownloader.download(url, Utils.getRootDirPath(context), "ad.mp4")
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                        Log.i(TAG, "Download progress ..." + progressPercent);
                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        AdPolePrefs.saveString(context, "yes");
                        AdPolePrefs.saveBool(PREFS_ADPOLE, IS_LOADED, true);
                        isLoaded=true;
                        if (listener!=null)
                            listener.onAdLoaded();

                    }

                    @Override
                    public void onError(Error error) {
                        isLoaded=false;
                        if (listener!=null)
                            listener.onAdFailedToLoad();
                    }

                });
    }

    public static void setAdPoleLoadDataListener(AdPoleLoadDataListener listener2) {
        listener = listener2;
    }
    public static void setAdUnitListener(AdUnitListener listener2) {
        adListener = listener2;
    }
}