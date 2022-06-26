package com.example.adpolelib;

import android.content.Context;

import com.example.adpolelib.Interfaces.AdvertisingIdentifierProvider;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;


public class AdvertisingIdProviderGPS implements AdvertisingIdentifierProvider {

    private static String lastValue;

    static String getLastValue() {
        return lastValue;
    }

    @Override
    public String getIdentifier(Context appContext) {
        try {
            AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(appContext);
            if (adInfo.isLimitAdTrackingEnabled())
                lastValue = "OptedOut"; // Google restricts usage of the id to "build profiles" if the user checks opt out so we can't collect.
            else
                lastValue = adInfo.getId();

            return lastValue;
        } catch (Throwable t) {
            //OneSignal.Log(OneSignal.LOG_LEVEL.INFO, "Error getting Google Ad id: ", t);
        }

        return null;
    }
}
