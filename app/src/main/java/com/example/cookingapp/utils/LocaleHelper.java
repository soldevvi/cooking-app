package com.example.cookingapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Locale;

public class LocaleHelper {
    private static final String PREF_KEY_LOCALE = "app_locale";

    public static Context onAttach(Context context) {
        String locale = getPersistedLocale(context);
        if (locale == null || locale.isEmpty()) {
            // Use device default language if supported, otherwise English
            String deviceLang = Locale.getDefault().getLanguage();
            if (deviceLang.equals("ru") || deviceLang.equals("de") || deviceLang.equals("en")) {
                locale = deviceLang;
            } else {
                locale = "en";
            }
            persistLocale(context, locale);
        }
        return setLocale(context, locale);
    }

    public static Context setLocale(Context context, String language) {
        persistLocale(context, language);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }

    public static String getPersistedLocale(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PREF_KEY_LOCALE, "");
    }

    public static void persistLocale(Context context, String language) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(PREF_KEY_LOCALE, language).apply();
    }

    public static String getCurrentLanguageCode(Context context) {
        String locale = getPersistedLocale(context);
        return (locale == null || locale.isEmpty()) ? "en" : locale;
    }
}
