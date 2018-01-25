package com.quhwa;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by Administrator on 2018-01-17.
 */

public class MyApplication extends Application {
    public static MyApplication instance;
    public static boolean debugMode = false;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        setDebug(true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // 将MultiDex注入到项目中
        MultiDex.install(this);
    }

    /**
     * 设置debug状态
     * @param debugMode
     */
    public void setDebug(boolean debugMode){
        this.debugMode = debugMode;
    }
    /**
     * 返回debug状态
     * @return true：处于调试状态，日志打开   false：处于未调试状态，日志未打开
     */
    public static boolean getDebug(){
        return debugMode;
    }
}
