package com.example.adpolelib;

public class RootToolsInternalMethods {

    public static boolean isRooted() {
        String[] places = {"/sbin/", "/system/bin/", "/system/xbin/",
                "/data/local/xbin/", "/data/local/bin/",
                "/system/sd/xbin/", "/system/bin/failsafe/",
                "/data/local/"};

        for (String where : places) {
            if (new java.io.File(where + "su").exists())
                return true;
        }

        return false;
    }

}
