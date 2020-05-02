package com.urban.sisters;

import android.app.Application;
import android.content.Context;

import com.urban.sisters.lang.LanguageHelper;


public class App extends Application {
    @Override
    protected void attachBaseContext(Context context) {

        //will just set default locale of your device as the default locale of your application.
        super.attachBaseContext(LanguageHelper.onAttach(context));
    }
}
