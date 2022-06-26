package com.example.adpolelib;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;


public final class SharedPreferencesHelper {

    private static final String TAG = SharedPreferencesHelper.class.getName();

    public enum Property {
        COVERSION_LIST("conversion_list");

        private String value;

        Property(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    private static SharedPreferences getPreferences(final Context context) {

        return context.getSharedPreferences("", Context.MODE_PRIVATE);
    }

    public static String get(@NonNull final Context context,
                             @NonNull final Property property,
                             @NonNull final String defaultValue) {

        SharedPreferences sharedPreferences = getPreferences(context);
        String str = sharedPreferences.getString(property.value(), defaultValue);

        return str;
    }

    public static void put(@NonNull final Context context,
                           @NonNull final Property property,
                           @NonNull final String value) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(property.value(), value);
        editor.apply();
    }

    public static int get(@NonNull final Context context,
                          @NonNull final Property property,
                          final int defaultValue) {
        return getPreferences(context).getInt(property.value(), defaultValue);
    }

    public static void put(@NonNull final Context context,
                           @NonNull final Property property,
                           final int value) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(property.value(), value);
        editor.apply();
    }

    public static boolean get(@NonNull final Context context,
                              @NonNull final Property property,
                              final boolean defaultValue) {
        return getPreferences(context).getBoolean(property.value(), defaultValue);
    }

    public static void put(@NonNull final Context context,
                           @NonNull final Property property,
                           final boolean value) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(property.value(), value);
        editor.apply();
    }
}