package com.codetend.demo;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by Administrator on 2018/10/24.
 */

public class AppApplication extends Application {
    private String sign;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            sign = packageInfo.signatures[0].toCharsString();
            Log.i("cdemo", "sign:" + sign);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
