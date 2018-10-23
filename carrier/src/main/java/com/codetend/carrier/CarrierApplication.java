package com.codetend.carrier;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import leo.android.cglib.proxy.Enhancer;

/**
 * Created by Administrator on 2018/10/24.
 */

public class CarrierApplication extends Application {
    private String mDexPath;
    private DexClassLoader mApkLoader;
    private Application mOriginalApplication;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        prepareDex();
        loadClass();
        createAppApplication();
    }

    private void prepareDex() {
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("classes.dex");
            File file = new File(getFilesDir(), "classes.dex");
            mDexPath = file.getCanonicalPath();
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] bytes = new byte[4096];
            int len;
            while ((len = inputStream.read(bytes)) > 0) {
                fileOutputStream.write(bytes, 0, len);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadClass() {
        File optDir = new File(getFilesDir(), "apk_path");
        if (optDir.exists()) {
            optDir.delete();
        }
        optDir.mkdirs();
        mApkLoader = new DexClassLoader(mDexPath, optDir.getAbsolutePath(), null, getClassLoader());
        try {
            Field parentField = ClassLoader.class.getDeclaredField("parent");
            parentField.setAccessible(true);
            ClassLoader topClassLoader = getClassLoader().getParent();
            parentField.set(getClassLoader(), mApkLoader);
            parentField.set(mApkLoader, topClassLoader);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void createAppApplication() {
        try {
            Class appClass = mApkLoader.loadClass("com.codetend.demo.AppApplication");
            mOriginalApplication = (Application) appClass.newInstance();
            Method attachBaseContextMethod = appClass.getDeclaredMethod("attachBaseContext", Context.class);
            attachBaseContextMethod.setAccessible(true);
            attachBaseContextMethod.invoke(mOriginalApplication, this);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
