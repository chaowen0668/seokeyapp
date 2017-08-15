package com.yang.seokeyapp;

import android.app.Application;

import com.zhouyou.http.EasyHttp;


/**
 * Created by user on 2017/8/9.
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EasyHttp.init(this);//默认初始化
    }
}
