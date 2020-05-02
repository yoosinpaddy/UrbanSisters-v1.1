package com.urban.sisters.lang;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import java.util.Locale;

import static com.urban.sisters.utils.Utils.APP_SHARED_PREF;
import static com.urban.sisters.utils.Utils.USER_LANGUAGE;
import static com.urban.sisters.utils.Utils.USER_LANGUAGE_EN;
import static com.urban.sisters.utils.Utils.USER_LANGUAGE_SW;
import static android.content.Context.MODE_PRIVATE;

public class LanguageHelper {

    public static Context onAttach(Context context) {
        String defaultDeviceLanguage = Locale.getDefault().getLanguage().toUpperCase();
        Log.e("TAG-DEVICE_LANG-A1", defaultDeviceLanguage);
        String userLanguage = (!defaultDeviceLanguage.equalsIgnoreCase(USER_LANGUAGE_EN) ||
                                !defaultDeviceLanguage.equalsIgnoreCase(USER_LANGUAGE_SW))? USER_LANGUAGE_EN : defaultDeviceLanguage;
        Log.e("TAG-USER_LANG-A2", userLanguage);
        String lang = getPersistedData(context, userLanguage);
        return setLocale(context, lang);
    }


    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        String defaultDeviceLanguage = Locale.getDefault().getLanguage().toUpperCase();
        Log.e("TAG-DEVICE_LANG-B1", defaultDeviceLanguage);
        String userLanguage = (!defaultDeviceLanguage.equalsIgnoreCase(USER_LANGUAGE_EN) ||
                !defaultDeviceLanguage.equalsIgnoreCase(USER_LANGUAGE_SW))? USER_LANGUAGE_EN : defaultDeviceLanguage;
        Log.e("TAG-USER_LANG-B2", userLanguage);
        return getPersistedData(context, userLanguage);
    }

    public static Context setLocale(Context context, String language) {
        persist(context, language);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }

        return updateResourcesLegacy(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = context.getSharedPreferences(APP_SHARED_PREF, MODE_PRIVATE);
        return preferences.getString(USER_LANGUAGE, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = context.getSharedPreferences(APP_SHARED_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(USER_LANGUAGE, language);
        editor.apply();
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale);
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }
}
