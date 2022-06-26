package com.example.adpolelib;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.Locale;

public class OsUtils {

    static final int UNINITIALIZEABLE_STATUS = -999;
    int initializationChecker(Context context, String adWiseAppId) {
        int subscribableStatus = 1;

        return subscribableStatus;
    }
    static String getCorrectedLanguage() {
        String lang = Locale.getDefault().getLanguage();
        if (lang.equals("iw"))
            return "he";
        if (lang.equals("in"))
            return "id";
        if (lang.equals("ji"))
            return "yi";

        if (lang.equals("zh"))
            return lang + "-" + Locale.getDefault().getCountry();

        return lang;
    }
    String getCarrierName() {
        try {
            TelephonyManager manager = (TelephonyManager) AdPole.appContext.getSystemService(Context.TELEPHONY_SERVICE);
            String carrierName = manager.getNetworkOperatorName();
            return TextUtils.isEmpty(carrierName) ? null : carrierName;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
    Integer getNetType() {
        ConnectivityManager cm = (ConnectivityManager) AdPole.appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null) {
            int networkType = netInfo.getType();
            if (networkType == ConnectivityManager.TYPE_WIFI || networkType == ConnectivityManager.TYPE_ETHERNET)
                return 0;
            return 1;
        }

        return null;
    }
}
