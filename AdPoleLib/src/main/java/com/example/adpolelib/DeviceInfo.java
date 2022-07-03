package com.example.adpolelib;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Base64;

import com.downloader.BuildConfig;
import com.example.adpolelib.Interfaces.AdvertisingIdentifierProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class DeviceInfo {

    public static JSONObject getDeviceInfo(Context context, String appId) throws JSONException {
        String packageName = context.getPackageName();
        PackageManager packageManager = context.getPackageManager();

        AdvertisingIdentifierProvider mainAdIdProvider = new AdvertisingIdProvider();
        OsUtils osUtils = new OsUtils();

        JSONObject deviceInfo = new JSONObject();

        deviceInfo.put("Id", id(context));

        deviceInfo.put("AppId", appId);

        String adId = mainAdIdProvider.getIdentifier(context);
        if (adId != null)
            deviceInfo.put("AdId", adId);
        deviceInfo.put("DeviceOs", Build.VERSION.RELEASE);
        deviceInfo.put("TimeZone", getTimeZoneOffset());
        deviceInfo.put("Language", OsUtils.getCorrectedLanguage());
        deviceInfo.put("SdkVersion", BuildConfig.VERSION_CODE);
        deviceInfo.put("AndroidPackage", packageName);
        deviceInfo.put("DeviceBrand", Build.BRAND);
        deviceInfo.put("DeviceModel", Build.MODEL);

        try {
            deviceInfo.put("HostApplicationVersion", packageManager.getPackageInfo(packageName, 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
        }

        try {
            List<PackageInfo> packList = packageManager.getInstalledPackages(0);
            JSONArray pkgs = new JSONArray();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (int i = 0; i < packList.size(); i++) {
                md.update(packList.get(i).packageName.getBytes());
                String pck = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                pkgs.put(pck);
            }
            deviceInfo.put("Packages", pkgs);
        } catch (Throwable t) {
        }

        deviceInfo.put("NetType", osUtils.getNetType());

        String carrierName = osUtils.getCarrierName();
        if (carrierName != null) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(carrierName);
            deviceInfo.put("Carrier", jsonArray.get(0));
        }

        deviceInfo.put("Rooted", RootToolsInternalMethods.isRooted());

        return deviceInfo;
    }

    private static int getTimeZoneOffset() {
        TimeZone timezone = Calendar.getInstance().getTimeZone();
        int offset = timezone.getRawOffset();

        if (timezone.inDaylightTime(new Date()))
            offset = offset + timezone.getDSTSavings();

        return offset / 1000;
    }

    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    public synchronized static String id(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();
            }
        }
        return uniqueID;
    }
    public synchronized static String packageName(Context context) {

        return uniqueID;
    }
}
